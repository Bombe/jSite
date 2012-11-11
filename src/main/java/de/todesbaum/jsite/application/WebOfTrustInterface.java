/*
 * jSite - WebOfTrustInterface.java - Copyright © 2012 David Roden
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package de.todesbaum.jsite.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;
import de.todesbaum.util.freenet.fcp2.Client;
import de.todesbaum.util.freenet.fcp2.Connection;
import de.todesbaum.util.freenet.fcp2.FcpPluginMessage;
import de.todesbaum.util.freenet.fcp2.Message;
import de.todesbaum.util.freenet.fcp2.wot.DefaultOwnIdentity;
import de.todesbaum.util.freenet.fcp2.wot.OwnIdentity;

/**
 * FCP interface to the node’s web of trust.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class WebOfTrustInterface implements Runnable {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(WebOfTrustInterface.class);

	/** Unique ID for the command identifier. */
	private static final AtomicLong commandCounter = new AtomicLong(System.nanoTime());

	/** Object used for synchronization. */
	private final Object syncObject = new Object();

	/** The freenet interface. */
	private final Freenet7Interface freenetInterface;

	/** Whether the interface should stop. */
	private boolean shouldStop;

	/** The own identities. */
	private final List<OwnIdentity> ownIdentities = new ArrayList<OwnIdentity>();

	/**
	 * Creates a new web of trust interface.
	 *
	 * @param freenetInterface
	 *            The freenet interface
	 */
	public WebOfTrustInterface(Freenet7Interface freenetInterface) {
		this.freenetInterface = freenetInterface;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns a list of own identities. If the identities have not yet been
	 * retrieved, an empty list is returned.
	 *
	 * @return The list of own identities
	 */
	public List<OwnIdentity> getOwnIdentities() {
		synchronized (ownIdentities) {
			return new ArrayList<OwnIdentity>(ownIdentities);
		}
	}

	//
	// ACTIONS
	//

	/**
	 * Starts the web of trust interface.
	 */
	public void start() {
		Thread webOfTrustThread = new Thread(this, "WebOfTrust Interface");
		webOfTrustThread.start();
	}

	/**
	 * Stops the web of trust interface
	 */
	public void stop() {
		synchronized (syncObject) {
			shouldStop = true;
			syncObject.notifyAll();
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Returns whether the web of trust interface should stop.
	 *
	 * @return {@code true} if the web of trust interface should stop,
	 *         {@code false} otherwise
	 */
	private boolean shouldStop() {
		synchronized (syncObject) {
			return shouldStop;
		}
	}

	/**
	 * Returns the essential parts of an URI, consisting of only the
	 * private/public key, decryption key, and the flags.
	 *
	 * @param uri
	 *            The URI to shorten
	 * @return The shortened URI
	 */
	private static String shortenUri(String uri) {
		String shortenedUri = uri;
		if (shortenedUri.charAt(3) == '@') {
			shortenedUri = shortenedUri.substring(4);
		}
		if (shortenedUri.indexOf('/') > -1) {
			shortenedUri = shortenedUri.substring(0, shortenedUri.indexOf('/'));
		}
		return shortenedUri;
	}

	//
	// RUNNABLE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		boolean waitBeforeReconnect = false;
		while (!shouldStop()) {

			/* wait a minute before reconnecting for another try. */
			if (waitBeforeReconnect) {
				logger.log(Level.FINE, "Waiting 60 seconds before reconnecting.");
				synchronized (syncObject) {
					try {
						syncObject.wait(60 * 1000);
					} catch (InterruptedException ie1) {
						/* ignore. */
					}
				}
				if (shouldStop()) {
					continue;
				}
			} else {
				waitBeforeReconnect = true;
			}

			try {

				/* connect. */
				Connection connection = freenetInterface.getConnection("jSite-WoT-Connector");
				logger.log(Level.INFO, String.format("Trying to connect to node at %s...", freenetInterface.getNode()));
				if (!connection.connect()) {
					logger.log(Level.WARNING, "Connection failed.");
					continue;
				}
				Client client = new Client(connection);

				/* send FCP command to WebOfTrust plugin. */
				String messageIdentifier = "jSite-WoT-Command-" + commandCounter.getAndIncrement();
				FcpPluginMessage pluginMessage = new FcpPluginMessage(messageIdentifier);
				pluginMessage.setPluginName("plugins.WebOfTrust.WebOfTrust");
				pluginMessage.setParameter("Message", "GetOwnIdentities");
				client.execute(pluginMessage);

				/* read a message. */
				Message message = null;
				while (!client.isDisconnected() && !shouldStop() && (message == null)) {
					message = client.readMessage(1000);
				}
				if (message == null) {
					continue;
				}

				/* evaluate message. */
				if (message.getName().equals("FCPPluginReply")) {
					logger.log(Level.FINE, "Got matching Reply from WebOfTrust.");
					/* parse identities. */
					List<OwnIdentity> ownIdentities = new ArrayList<OwnIdentity>();
					int identityCounter = -1;
					while (message.get("Replies.Identity" + ++identityCounter) != null) {
						String id = message.get("Replies.Identity" + identityCounter);
						String nickname = message.get("Replies.Nickname" + identityCounter);
						String requestUri = shortenUri(message.get("Replies.RequestURI" + identityCounter));
						String insertUri = shortenUri(message.get("Replies.InsertURI" + identityCounter));
						DefaultOwnIdentity ownIdentity = new DefaultOwnIdentity(id, nickname, requestUri, insertUri);
						logger.log(Level.FINE, String.format("Parsed Own Identity %s.", ownIdentity));
						ownIdentities.add(ownIdentity);
					}
					logger.log(Level.INFO, String.format("Parsed %d Own Identities.", ownIdentities.size()));

					synchronized (this.ownIdentities) {
						this.ownIdentities.clear();
						this.ownIdentities.addAll(ownIdentities);
					}
				} else if ("ProtocolError".equals(message.getName())) {
					logger.log(Level.WARNING, "WebOfTrust Plugin not found!");
				} else if ("Error".equals(message.getName())) {
					logger.log(Level.WARNING, "WebOfTrust Plugin returned an error!");
				}

				/* disconnect. */
				logger.log(Level.INFO, "Disconnecting from Node.");
				connection.disconnect();

			} catch (IOException ioe1) {
				logger.log(Level.WARNING, String.format("Communication with node at %s failed.", freenetInterface.getNode()), ioe1);
			}

		}
	}

}

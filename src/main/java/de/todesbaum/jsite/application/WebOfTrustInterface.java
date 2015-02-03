/*
 * jSite - WebOfTrustInterface.java - Copyright © 2012–2014 David Roden
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

import static java.util.Collections.emptyList;

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
public class WebOfTrustInterface {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(WebOfTrustInterface.class);

	/** Unique ID for the command identifier. */
	private static final AtomicLong commandCounter = new AtomicLong(System.nanoTime());

	/** The freenet interface. */
	private final Freenet7Interface freenetInterface;

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
		try {

			/* connect. */
			Connection connection = freenetInterface.getConnection("jSite-WoT-Connector");
			logger.log(Level.INFO, String.format("Trying to connect to node at %s...", freenetInterface.getNode()));
			if (!connection.connect()) {
				logger.log(Level.WARNING, "Connection failed.");
				return emptyList();
			}
			Client client = new Client(connection);

			/* send FCP command to WebOfTrust plugin. */
			sendFcpCommandToWotPlugin(client);

			/* read a message. */
			Message message = null;
			while (!client.isDisconnected() && (message == null)) {
				message = client.readMessage(1000);
			}
			if (message == null) {
				return emptyList();
			}

			/* evaluate message. */
			List<OwnIdentity> ownIdentities = parseOwnIdentitiesFromMessage(message);

            /* disconnect. */
			logger.log(Level.INFO, "Disconnecting from Node.");
			connection.disconnect();
			return ownIdentities;
		} catch (IOException ioe1) {
			logger.log(Level.WARNING, String.format("Communication with node at %s failed.", freenetInterface.getNode()), ioe1);
			return emptyList();
		}
	}

	private void sendFcpCommandToWotPlugin(Client client) throws IOException {
		String messageIdentifier = "jSite-WoT-Command-" + commandCounter.getAndIncrement();
		FcpPluginMessage pluginMessage = new FcpPluginMessage(messageIdentifier);
		pluginMessage.setPluginName("plugins.WebOfTrust.WebOfTrust");
		pluginMessage.setParameter("Message", "GetOwnIdentities");
		client.execute(pluginMessage);
	}

	private List<OwnIdentity> parseOwnIdentitiesFromMessage(Message message) {
		List<OwnIdentity> ownIdentities = new ArrayList<OwnIdentity>();
		if (message.getName().equals("FCPPluginReply")) {
			logger.log(Level.FINE, "Got matching Reply from WebOfTrust.");
				/* parse identities. */
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
		} else if ("ProtocolError".equals(message.getName())) {
			logger.log(Level.WARNING, "WebOfTrust Plugin not found!");
		} else if ("Error".equals(message.getName())) {
			logger.log(Level.WARNING, "WebOfTrust Plugin returned an error!");
		}
		return ownIdentities;
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

}

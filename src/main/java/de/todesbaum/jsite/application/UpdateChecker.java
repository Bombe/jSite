/*
 * jSite - UpdateChecker.java - Copyright © 2008–2014 David Roden
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.io.Closer;
import de.todesbaum.jsite.main.Main;
import de.todesbaum.jsite.main.Version;
import de.todesbaum.util.freenet.fcp2.Client;
import de.todesbaum.util.freenet.fcp2.ClientGet;
import de.todesbaum.util.freenet.fcp2.Connection;
import de.todesbaum.util.freenet.fcp2.Message;
import de.todesbaum.util.freenet.fcp2.Persistence;
import de.todesbaum.util.freenet.fcp2.ReturnType;
import de.todesbaum.util.freenet.fcp2.Verbosity;

/**
 * Checks for newer versions of jSite.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class UpdateChecker implements Runnable {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(UpdateChecker.class.getName());

	/** Counter for connection names. */
	private static int counter = 0;

	/** The edition for the update check URL. */
	private static final int UPDATE_EDITION = 7;

	/** The URL for update checks. */
	private static final String UPDATE_KEY = "USK@1waTsw46L9-JEQ8yX1khjkfHcn--g0MlMsTlYHax9zQ,oYyxr5jyFnaTsVGDQWk9e3ddOWGKnqEASxAk08MHT2Y,AQACAAE";

	/** Object used for synchronization. */
	private final Object syncObject = new Object();

	/** Update listeners. */
	private final List<UpdateListener> updateListeners = new ArrayList<UpdateListener>();

	/** Whether the main thread should stop. */
	private boolean shouldStop = false;

	/** Current last found edition of update key. */
	private int lastUpdateEdition = UPDATE_EDITION;

	/** Last found version. */
	private Version lastVersion = Main.getVersion();

	/** The freenet interface. */
	private final Freenet7Interface freenetInterface;

	/**
	 * Creates a new update checker that uses the given frame as its parent and
	 * communications via the given freenet interface.
	 *
	 * @param freenetInterface
	 *            The freenet interface
	 */
	public UpdateChecker(Freenet7Interface freenetInterface) {
		this.freenetInterface = freenetInterface;
	}

	//
	// EVENT LISTENER MANAGEMENT
	//

	/**
	 * Adds an update listener to the list of registered listeners.
	 *
	 * @param updateListener
	 *            The update listener to add
	 */
	public void addUpdateListener(UpdateListener updateListener) {
		updateListeners.add(updateListener);
	}

	/**
	 * Removes the given listener from the list of registered listeners.
	 *
	 * @param updateListener
	 *            The update listener to remove
	 */
	public void removeUpdateListener(UpdateListener updateListener) {
		updateListeners.remove(updateListener);
	}

	/**
	 * Notifies all listeners that a version was found.
	 *
	 * @param foundVersion
	 *            The version that was found
	 * @param versionTimestamp
	 *            The timestamp of the version
	 */
	protected void fireUpdateFound(Version foundVersion, long versionTimestamp) {
		for (UpdateListener updateListener : updateListeners) {
			updateListener.foundUpdateData(foundVersion, versionTimestamp);
		}
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the latest version that was found.
	 *
	 * @return The latest found version
	 */
	public Version getLatestVersion() {
		return lastVersion;
	}

	//
	// ACTIONS
	//

	/**
	 * Starts the update checker.
	 */
	public void start() {
		new Thread(this).start();
	}

	/**
	 * Stops the update checker.
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
	 * Returns whether the update checker should stop.
	 *
	 * @return <code>true</code> if the update checker should stop,
	 *         <code>false</code> otherwise
	 */
	private boolean shouldStop() {
		synchronized (syncObject) {
			return shouldStop;
		}
	}

	/**
	 * Creates the URI of the update file for the given edition.
	 *
	 * @param edition
	 *            The edition number
	 * @return The URI for the update file for the given edition
	 */
	private static String constructUpdateKey(int edition) {
		return UPDATE_KEY + "/jSite/" + edition + "/jSite.properties";
	}

	//
	// INTERFACE Runnable
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		int currentEdition = lastUpdateEdition;
		while (!shouldStop()) {

			/* try to connect. */
			Client client;
			while (true) {
				Connection connection = freenetInterface.getConnection("jSite-" + ++counter + "-UpdateChecker");
				try {
					connection.connect();
					logger.log(Level.INFO, "Connected to " + freenetInterface.getNode() + ".");
					client = new Client(connection);
					break;
				} catch (IOException ioe1) {
					logger.log(Level.INFO, "Could not connect to " + freenetInterface.getNode() + ".", ioe1);
				}
				if (!connection.isConnected()) {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException ie1) {
						/* ignore, we’re looping. */
					}
				}
			}

			boolean checkNow = false;
			logger.log(Level.FINE, "Trying " + constructUpdateKey(currentEdition));
			ClientGet clientGet = new ClientGet("get-update-key");
			clientGet.setUri(constructUpdateKey(currentEdition));
			clientGet.setPersistence(Persistence.CONNECTION);
			clientGet.setReturnType(ReturnType.direct);
			clientGet.setVerbosity(Verbosity.ALL);
			try {
				client.execute(clientGet);
				boolean stop = false;
				while (!stop) {
					Message message = client.readMessage();
					logger.log(Level.FINEST, "Received message: " + message);
					if (message == null) {
						break;
					}
					if ("GetFailed".equals(message.getName())) {
						if ("27".equals(message.get("code"))) {
							String editionString = message.get("redirecturi").split("/")[2];
							int editionNumber = -1;
							try {
								editionNumber = Integer.parseInt(editionString);
							} catch (NumberFormatException nfe1) {
								/* ignore. */
							}
							if (editionNumber != -1) {
								logger.log(Level.INFO, "Found new edition " + editionNumber);
								currentEdition = editionNumber;
								lastUpdateEdition = editionNumber;
								checkNow = true;
								break;
							}
						}
					}
					if ("AllData".equals(message.getName())) {
						logger.log(Level.FINE, "Update data found.");
						InputStream dataInputStream = null;
						Properties properties = new Properties();
						try {
							dataInputStream = message.getPayloadInputStream();
							properties.load(dataInputStream);
						} finally {
							Closer.close(dataInputStream);
						}

						String foundVersionString = properties.getProperty("jSite.Version");
						if (foundVersionString != null) {
							Version foundVersion = Version.parse(foundVersionString);
							if (foundVersion != null) {
								lastVersion = foundVersion;
								String versionTimestampString = properties.getProperty("jSite.Date");
								logger.log(Level.FINEST, "Version timestamp: " + versionTimestampString);
								long versionTimestamp = -1;
								try {
									versionTimestamp = Long.parseLong(versionTimestampString);
								} catch (NumberFormatException nfe1) {
									/* ignore. */
								}
								fireUpdateFound(foundVersion, versionTimestamp);
								stop = true;
								checkNow = true;
								++currentEdition;
							}
						}
					}
				}
			} catch (IOException e) {
				logger.log(Level.INFO, "Got IOException: " + e.getMessage());
				e.printStackTrace();
			}
			if (!checkNow && !shouldStop()) {
				synchronized (syncObject) {
					try {
						syncObject.wait(15 * 60 * 1000);
					} catch (InterruptedException ie1) {
						/* ignore. */
					}
				}
			}
		}
	}

}

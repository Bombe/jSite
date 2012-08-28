/*
 * jSite - Client.java - Copyright © 2006–2012 David Roden
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

package de.todesbaum.util.freenet.fcp2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.pterodactylus.util.io.StreamCopier.ProgressListener;

/**
 * A Client executes {@link Command}s over a {@link Connection} to a
 * {@link Node} and delivers resulting {@link Message}s.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class Client implements ConnectionListener {

	/** The connection this client operates on. */
	private final Connection connection;

	/** The identifiers the client filters messages for. */
	private List<String> identifiers = new ArrayList<String>();

	/** The queued messages. */
	private final List<Message> messageQueue = new ArrayList<Message>();

	/** Whether the client was disconnected. */
	private boolean disconnected = false;

	/** Whether to catch all messages from the connection. */
	private boolean catchAll = false;

	/**
	 * Creates a new client that operates on the specified connection.
	 *
	 * @param connection
	 *            The connection to operate on
	 */
	public Client(Connection connection) {
		this.connection = connection;
		connection.addConnectionListener(this);
	}

	/**
	 * Creates a new client that operates on the specified connection and
	 * immediately executes the specified command.
	 *
	 * @param connection
	 *            The connection to operate on
	 * @param command
	 *            The command to execute
	 * @throws IOException
	 *             if an I/O error occurs
	 * @see #execute(Command)
	 */
	public Client(Connection connection, Command command) throws IOException {
		this(connection);
		execute(command);
	}

	/**
	 * Returns whether this client catches all messages going over the
	 * connection.
	 *
	 * @return <code>true</code> if the client catches all messages,
	 *         <code>false</code> otherwise
	 */
	public boolean isCatchAll() {
		return catchAll;
	}

	/**
	 * Sets whether this client catches all messages going over the connection.
	 *
	 * @param catchAll
	 *            <code>true</code> if the client should catch all messages,
	 *            <code>false</code> otherwise
	 */
	public void setCatchAll(boolean catchAll) {
		this.catchAll = catchAll;
	}

	/**
	 * Executes the specified command. This will also clear the queue of
	 * messages, discarding all messages that resulted from the previous command
	 * and have not yet been read.
	 *
	 * @param command
	 *            The command to execute
	 * @throws IOException
	 *             if an I/O error occurs
	 * @see #execute(Command, boolean)
	 */
	public void execute(Command command) throws IOException {
		execute(command, true);
	}

	/**
	 * Executes the specified command. This will also clear the queue of
	 * messages, discarding all messages that resulted from the previous
	 * command and have not yet been read.
	 *
	 * @param command
	 *            The command to execute
	 * @param progressListener
	 *            The progress listener for payload transfers
	 * @throws IOException
	 *             if an I/O error occurs
	 * @see #execute(Command, boolean)
	 */
	public void execute(Command command, ProgressListener progressListener) throws IOException {
		execute(command, true, progressListener);
	}

	/**
	 * Executes the specified command and optionally clears the list of
	 * identifiers this clients listens to before starting the command.
	 *
	 * @param command
	 *            The command to execute
	 * @param removeExistingIdentifiers
	 *            If <code>true</code>, the list of identifiers that this
	 *            clients listens to is cleared
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void execute(Command command, boolean removeExistingIdentifiers) throws IOException {
		execute(command, removeExistingIdentifiers, null);
	}

	/**
	 * Executes the specified command and optionally clears the list of
	 * identifiers this clients listens to before starting the command.
	 *
	 * @param command
	 *            The command to execute
	 * @param removeExistingIdentifiers
	 *            If <code>true</code>, the list of identifiers that this
	 *            clients listens to is cleared
	 * @param progressListener
	 *            The progress listener for payload transfers
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void execute(Command command, boolean removeExistingIdentifiers, ProgressListener progressListener) throws IOException {
		synchronized (messageQueue) {
			messageQueue.clear();
			if (removeExistingIdentifiers) {
				identifiers.clear();
			}
			identifiers.add(command.getIdentifier());
		}
		connection.execute(command, progressListener);
	}

	/**
	 * Returns the next message, waiting endlessly for it, if need be. If you
	 * are not sure whether a message will arrive, better use
	 * {@link #readMessage(long)} to only wait for a specific time.
	 *
	 * @return The next message that resulted from the execution of the last
	 *         command
	 * @see #readMessage(long)
	 * @see #execute(Command)
	 */
	public Message readMessage() {
		return readMessage(0);
	}

	/**
	 * Returns the next message. If the message queue is currently empty, at
	 * least <code>maxWaitTime</code> milliseconds will be waited for a
	 * message to arrive.
	 *
	 * @param maxWaitTime
	 *            The minimum time to wait for a message, in milliseconds
	 * @return The message, or <code>null</code> if no message arrived in time
	 *         or the client is currently disconnected
	 * @see #isDisconnected()
	 * @see Object#wait(long)
	 */
	public Message readMessage(long maxWaitTime) {
		synchronized (messageQueue) {
			if (disconnected) {
				return null;
			}
			if (messageQueue.size() == 0) {
				try {
					messageQueue.wait(maxWaitTime);
				} catch (InterruptedException ie1) {
				}
			}
			if (messageQueue.size() > 0) {
				return messageQueue.remove(0);
			}
		}
		return null;
	}

	/**
	 * Returns whether the client is currently disconnected.
	 *
	 * @return <code>true</code> if the client is disconnected,
	 *         <code>false</code> otherwise
	 */
	public boolean isDisconnected() {
		synchronized (messageQueue) {
			return disconnected;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void messageReceived(Connection connection, Message message) {
		synchronized (messageQueue) {
			if (catchAll || (message.getIdentifier().length() == 0) || identifiers.contains(message.getIdentifier())) {
				messageQueue.add(message);
				messageQueue.notify();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void connectionTerminated(Connection connection) {
		synchronized (messageQueue) {
			disconnected = true;
			messageQueue.notify();
		}
	}

}

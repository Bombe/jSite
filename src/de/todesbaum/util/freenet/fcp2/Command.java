/*
 * jSite - Command.java - Copyright © 2006–2012 David Roden
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
import java.io.InputStream;
import java.io.Writer;

/**
 * Abstract base class for all commands.
 * <p>
 * In addition to the replies listed at the type comment of each specific
 * command the node can <strong>always</strong> send the following messages:
 * <code>ProtocolError</code> (if this library screws up),
 * <code>CloseConnectionDuplicateClientName</code> (if a client with the same
 * name of the {@link de.todesbaum.util.freenet.fcp2.Connection} connects). So
 * when receiving messages from the node you should always be prepared for
 * something you did not expect.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public abstract class Command {

	/** The line feed sequence used by the library. */
	protected static final String LINEFEED = "\r\n";

	/**
	 * The name of the command. The name is sent to the node so it can not be
	 * chosen arbitrarily!
	 */
	private final String commandName;

	/**
	 * The identifier of the command. This identifier is used to identify
	 * replies that are caused by a command.
	 */
	private final String identifier;

	/**
	 * Creates a new command with the specified name and identifier.
	 *
	 * @param name
	 *            The name of the command
	 * @param identifier
	 *            The identifier of the command
	 */
	public Command(String name, String identifier) {
		this.commandName = name;
		this.identifier = identifier;
	}

	/**
	 * Returns the name of this command.
	 *
	 * @return The name of this command
	 */
	public String getCommandName() {
		return commandName;
	}

	/**
	 * Return the identifier of this command.
	 *
	 * @return The identifier of this command
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Writes all parameters to the specified writer.
	 * <p>
	 * <strong>NOTE:</strong> Subclasses of Command <strong>must</strong> call
	 * <code>super.write(writer)</code> before or after writing their own
	 * parameters!
	 *
	 * @param writer
	 *            The stream to write the parameters to
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	protected void write(Writer writer) throws IOException {
		if (identifier != null)
			writer.write("Identifier=" + identifier + LINEFEED);
	}

	/**
	 * Returns whether this command has payload to send after the message.
	 * Subclasses need to return <code>true</code> here if they need to send
	 * payload after the message.
	 *
	 * @return <code>true</code> if this command has payload to send,
	 *         <code>false</code> otherwise
	 */
	protected boolean hasPayload() {
		return false;
	}

	/**
	 * Returns the payload of this command as an {@link InputStream}. This
	 * method is never called if {@link #hasPayload()} returns
	 * <code>false</code>.
	 *
	 * @return The payload of this command
	 */
	protected InputStream getPayload() {
		return null;
	}

	/**
	 * Returns the length of the payload. This method is never called if
	 * {@link #hasPayload()} returns <code>false</code>.
	 *
	 * @return The length of the payload
	 */
	protected long getPayloadLength() {
		return -1;
	}

}

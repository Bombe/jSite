/*
 * jSite - ClientHello.java - Copyright © 2006–2012 David Roden
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
import java.io.Writer;

/**
 * Implementation of the <code>ClientHello</code> command. This command must
 * be sent as the first command on a connection ({@link de.todesbaum.util.freenet.fcp2.Connection#connect()}
 * takes care of that) and must not be sent afterwards.
 * <p>
 * The node can answer with the following messages: <code>NodeHello</code>.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class ClientHello extends Command {

	/** The name of the client. */
	protected String name;

	/** The version of the FCP protocol the client expects. */
	protected String expectedVersion = "2.0";

	/**
	 * Creates a new <code>ClientHello</code> command.
	 */
	public ClientHello() {
		super("ClientHello", "ClientHello-" + System.currentTimeMillis());
	}

	/**
	 * Returns the value of the <code>ExpectedVersion</code> parameter of this
	 * command. At the moment this value is not used by the node but in the
	 * future this may be used to enforce certain node versions.
	 *
	 * @return The expected version
	 */
	public String getExpectedVersion() {
		return expectedVersion;
	}

	/**
	 * Sets the value of the <code>ExpectedVersion</code> parameter of this
	 * command. At the moment this value is not used by the node but in the
	 * future this may be used to enforce certain node versions.
	 *
	 * @param expectedVersion
	 *            The expected version
	 */
	public void setExpectedVersion(String expectedVersion) {
		this.expectedVersion = expectedVersion;
	}

	/**
	 * Returns the name of the client that is connecting.
	 *
	 * @return The name of the client
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the client that is connecting.
	 *
	 * @param name
	 *            The name of the client
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void write(Writer writer) throws IOException {
		writer.write("Name=" + name + LINEFEED);
		writer.write("ExpectedVersion=" + expectedVersion + LINEFEED);
	}

}

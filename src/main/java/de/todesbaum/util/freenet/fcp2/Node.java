/*
 * jSite - Node.java - Copyright © 2006–2012 David Roden
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

/**
 * Contains the hostname and port number of the Freenet node.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class Node {

	/** The default port of FCPv2. */
	public static final int DEFAULT_PORT = 9481;

	/** The hostname of the node. */
	protected String hostname;

	/** The port number of the node. */
	protected int port;

	/**
	 * Creates a new node with the specified hostname and the default port
	 * number.
	 *
	 * @param hostname
	 *            The hostname of the node
	 * @see #DEFAULT_PORT
	 */
	public Node(String hostname) {
		this(hostname, DEFAULT_PORT);
	}

	/**
	 * Creates a new node with the specified hostname and port number.
	 *
	 * @param hostname
	 *            The hostname of the node
	 * @param port
	 *            The port number of the node
	 */
	public Node(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	/**
	 * Returns the hostname of the node.
	 *
	 * @return The hostname of the node
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Returns the port number of the node.
	 *
	 * @return The port number of the node
	 */
	public int getPort() {
		return port;
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("%s:%d", getHostname(), getPort());
	}

}

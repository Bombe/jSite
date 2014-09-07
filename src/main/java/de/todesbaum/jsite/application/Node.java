/*
 * jSite - Node.java - Copyright © 2006–2014 David Roden
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

/**
 * Container for node information.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Node extends de.todesbaum.util.freenet.fcp2.Node {

	/** The name of the node. */
	protected String name;

	/**
	 * Creates a new node with the given hostname and the default port.
	 *
	 * @see de.todesbaum.util.freenet.fcp2.Node#DEFAULT_PORT
	 * @param hostname
	 *            The hostname of the new node
	 */
	public Node(String hostname) {
		this(hostname, DEFAULT_PORT);
	}

	/**
	 * Creates a new node with the given hostname and port.
	 *
	 * @param hostname
	 *            The hostname of the new node
	 * @param port
	 *            The port of the new node
	 */
	public Node(String hostname, int port) {
		this(hostname, port, "");
	}

	/**
	 * Creates a new node with the given hostname, port, and name.
	 *
	 * @param hostname
	 *            The hostname of the new node
	 * @param port
	 *            The port of the new node
	 * @param name
	 *            The name of the node
	 */
	public Node(String hostname, int port, String name) {
		super(hostname, port);
		this.name = name;
	}

	/**
	 * Creates a new node that gets its settings from the given node.
	 *
	 * @param node
	 *            The node to copy
	 */
	public Node(Node node) {
		this(node.getHostname(), node.getPort());
	}

	/**
	 * Creates a new node from the given node, overwriting the name.
	 *
	 * @param node
	 *            The node to copy from
	 * @param name
	 *            The new name of the node
	 */
	public Node(Node node, String name) {
		this(node.getHostname(), node.getPort(), name);
	}

	/**
	 * Sets the name of the node.
	 *
	 * @param name
	 *            The name of the node
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the node.
	 *
	 * @return The name of the node
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the hostname of the node.
	 *
	 * @param hostname
	 *            The hostname of the node
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Sets the port of the node.
	 *
	 * @param port
	 *            The port of the node
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * A node is considered as being equal to this node its name, hostname, and
	 * port equal their counterparts in this node.
	 */
	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof Node)) {
			return false;
		}
		Node node = (Node) o;
		return name.equals(node.name) && hostname.equals(node.hostname) && (port == node.port);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The hashcode for a node is created from its name, its hostname, and its
	 * port.
	 */
	@Override
	public int hashCode() {
		return name.hashCode() ^ hostname.hashCode() ^ port;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a textual representation of this node.
	 */
	@Override
	public String toString() {
		return name + " (" + hostname + ":" + port + ")";
	}

}

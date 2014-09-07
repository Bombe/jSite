/*
 * jSite - Freenet7Interface.java - Copyright © 2006–2014 David Roden
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

import de.todesbaum.util.freenet.fcp2.Client;
import de.todesbaum.util.freenet.fcp2.Connection;
import de.todesbaum.util.freenet.fcp2.GenerateSSK;
import de.todesbaum.util.freenet.fcp2.Message;
import de.todesbaum.util.freenet.fcp2.Node;

/**
 * Interface for freenet-related operations.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Freenet7Interface {

	/** Random number to differentiate several jSites. */
	private static final int number = (int) (Math.random() * Integer.MAX_VALUE);

	/** Counter. */
	private static int counter = 0;

	/** The node to connect to. */
	private Node node;

	/** The connection to the node. */
	private Connection connection;

	/**
	 * Sets the hostname of the node. The default port for FCP2 connections ({@link Node#DEFAULT_PORT})
	 * is used.
	 *
	 * @param hostname
	 *            The hostname of the node
	 */
	public void setNodeAddress(String hostname) {
		node = new Node(hostname);
		connection = new Connection(node, "jSite-" + number + "-connection-" + counter++);
	}

	/**
	 * Sets the hostname and the port of the node.
	 *
	 * @param hostname
	 *            The hostname of the node
	 * @param port
	 *            The port number of the node
	 */
	public void setNodeAddress(String hostname, int port) {
		node = new Node(hostname, port);
		connection = new Connection(node, "jSite-" + number + "-connection-" + counter++);
	}

	/**
	 * Sets hostname and port from the given node.
	 *
	 * @param node
	 *            The node to get the hostname and port from
	 */
	public void setNode(de.todesbaum.jsite.application.Node node) {
		if (node != null) {
			this.node = new Node(node.getHostname(), node.getPort());
			connection = new Connection(node, "jSite-" + number + "-connection-" + counter++);
		} else {
			this.node = null;
			connection = null;
		}
	}

	/**
	 * Removes the current node from the interface.
	 */
	public void removeNode() {
		node = null;
		connection = null;
	}

	/**
	 * Returns the node this interface is connecting to.
	 *
	 * @return The node
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * Creates a new connection to the current node with the given identifier.
	 *
	 * @param identifier
	 *            The identifier of the connection
	 * @return The connection to the node
	 */
	public Connection getConnection(String identifier) {
		return new Connection(node, identifier);
	}

	/**
	 * Checks whether the current node is connected. If the node is not
	 * connected, a connection will be tried.
	 *
	 * @return <code>true</code> if the node is connected, <code>false</code>
	 *         otherwise
	 * @throws IOException
	 *             if an I/O error occurs communicating with the node
	 */
	public boolean isNodePresent() throws IOException {
		if (!connection.isConnected()) {
			return connection.connect();
		}
		return true;
	}

	/**
	 * Generates an SSK key pair.
	 *
	 * @return An array of strings, the first one being the generated private
	 *         (insert) URI and the second one being the generated public
	 *         (request) URI
	 * @throws IOException
	 *             if an I/O error occurs communicating with the node
	 */
	public String[] generateKeyPair() throws IOException {
		if (!isNodePresent()) {
			throw new IOException("Node is offline.");
		}
		GenerateSSK generateSSK = new GenerateSSK();
		Client client = new Client(connection, generateSSK);
		Message keypairMessage = client.readMessage();
		return new String[] { keypairMessage.get("InsertURI"), keypairMessage.get("RequestURI") };
	}

	/**
	 * Checks whether the interface has already been configured with a node.
	 *
	 * @return <code>true</code> if this interface already has a node set,
	 *         <code>false</code> otherwise
	 */
	public boolean hasNode() {
		return (node != null) && (connection != null);
	}

}

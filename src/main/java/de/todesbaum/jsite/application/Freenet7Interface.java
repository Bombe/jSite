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
import de.todesbaum.util.freenet.fcp2.Command;
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

	private final NodeSupplier nodeSupplier;
	private final ConnectionSupplier connectionSupplier;
	private final ClientSupplier clientSupplier;

	/** The node to connect to. */
	private Node node;

	/** The connection to the node. */
	private Connection connection;

	public Freenet7Interface() {
		this(new DefaultNodeSupplier(), new DefaultConnectionSupplier(), new DefaultClientSupplier());
	}

	Freenet7Interface(NodeSupplier nodeSupplier, ConnectionSupplier connectionSupplier, ClientSupplier clientSupplier) {
		this.nodeSupplier = nodeSupplier;
		this.connectionSupplier = connectionSupplier;
		this.clientSupplier = clientSupplier;
	}

	/**
	 * Sets hostname and port from the given node.
	 *
	 * @param node
	 *            The node to get the hostname and port from
	 */
	public void setNode(de.todesbaum.jsite.application.Node node) {
		if (node != null) {
			this.node = nodeSupplier.supply(node.getHostname(), node.getPort());
			connection = connectionSupplier.supply(node, "jSite-" + number + "-connection-" + counter++);
		} else {
			this.node = null;
			connection = null;
		}
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
		return connectionSupplier.supply(node, identifier);
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
		Client client = clientSupplier.supply(connection, generateSSK);
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

	public interface NodeSupplier {

		Node supply(String hostname, int port);

	}

	public static class DefaultNodeSupplier implements NodeSupplier {

		@Override
		public Node supply(String hostname, int port) {
			return new Node(hostname, port);
		}

	}

	public interface ConnectionSupplier {

		Connection supply(Node node, String identifier);

	}

	public static class DefaultConnectionSupplier implements ConnectionSupplier {

		@Override
		public Connection supply(Node node, String identifier) {
			return new Connection(node, identifier);
		}

	}

	public interface ClientSupplier {

		Client supply(Connection connection, Command command) throws IOException;

	}

	public static class DefaultClientSupplier implements ClientSupplier {

		@Override
		public Client supply(Connection connection, Command command) throws IOException {
			return new Client(connection, command);
		}

	}

}

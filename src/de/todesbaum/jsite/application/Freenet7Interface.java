/*
 * jSite - 
 * Copyright (C) 2006 David Roden
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
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Freenet7Interface {

	private static int counter = 0;

	private Node node;
	private Connection connection;

	public void setNodeAddress(String hostname) {
		node = new Node(hostname);
		connection = new Connection(node, "connection-" + counter++);
	}

	public void setNodeAddress(String hostname, int port) {
		node = new Node(hostname, port);
		connection = new Connection(node, "connection-" + counter++);
	}
	
	public void setNode(de.todesbaum.jsite.application.Node node) {
		if (node != null) {
			this.node = new Node(node.getHostname(), node.getPort());
			connection = new Connection(node, "connection-" + counter++);
		} else {
			this.node = null;
			connection = null;
		}
	}
	
	public void removeNode() {
		node = null;
		connection = null;
	}

	/**
	 * @return Returns the node.
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * @return Returns the connection.
	 */
	public Connection getConnection(String identifier) {
		return new Connection(node, identifier);
	}

	public boolean isNodePresent() throws IOException {
		if (!connection.isConnected()) {
			return connection.connect();
		}
		return true;
	}

	public String[] generateKeyPair() throws IOException {
		if (!isNodePresent()) {
			return null;
		}
		GenerateSSK generateSSK = new GenerateSSK();
		Client client = new Client(connection, generateSSK);
		Message keypairMessage = client.readMessage();
		return new String[] { keypairMessage.get("InsertURI"), keypairMessage.get("RequestURI") };
	}

	/**
	 * @return <code>true</code> if this interface already has a node set,
	 *         <code>false</code> otherwise
	 */
	public boolean hasNode() {
		return (node != null) && (connection != null);
	}

}

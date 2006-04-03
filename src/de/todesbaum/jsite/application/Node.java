/*
 * jSite-0.7 - 
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

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id: Node.java 419 2006-03-29 17:49:46Z bombe $
 */
public class Node extends de.todesbaum.util.freenet.fcp2.Node {

	protected String name;

	/**
	 * @param hostname
	 */
	public Node(String hostname) {
		this(hostname, DEFAULT_PORT);
	}

	/**
	 * @param hostname
	 * @param port
	 */
	public Node(String hostname, int port) {
		this(hostname, port, "");
	}

	public Node(String hostname, int port, String name) {
		super(hostname, port);
		this.name = name;
	}

	public Node(Node node) {
		this(node.getHostname(), node.getPort());
	}

	public Node(Node node, String name) {
		this(node.getHostname(), node.getPort(), name);
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof Node)) {
			return false;
		}
		Node node = (Node) o;
		return name.equals(node.name) && hostname.equals(node.hostname) && (port == node.port);
	}
	
	public int hashCode() {
		return name.hashCode() ^ hostname.hashCode() ^ port;
	}

	public String toString() {
		return name + " (" + hostname + ":" + port + ")";
	}

}

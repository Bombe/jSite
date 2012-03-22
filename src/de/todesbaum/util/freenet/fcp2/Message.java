/*
 * jSite - Message.java - Copyright © 2006–2012 David Roden
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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Contains replies sent by the Freenet node. A message always has a name, and
 * most of the messages also have an identifier which binds it to a specific
 * command. Exceptions are among others <code>NodeHello</code>,
 * <code>SSKKeypair</code>, and <code>EndListPersistentRequests</code>.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 * @see de.todesbaum.util.freenet.fcp2.Client
 */
public class Message {

	/** The name of this message. */
	private final String name;

	/** The identifier of this message. */
	private String identifier = "";

	/** The parameters of this message. */
	private Map<String, String> parameters = new HashMap<String, String>();

	/** The payload. */
	private InputStream payloadInputStream;

	/**
	 * Creates a new message with the specified name.
	 *
	 * @param name
	 *            The name of this message
	 */
	public Message(String name) {
		this.name = name;
	}

	/**
	 * Returns the identifier of this message.
	 *
	 * @return The identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Sets the identifier of this message.
	 *
	 * @param identifier
	 *            The identifier of this message
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the name of this message.
	 *
	 * @return The name of this message
	 */
	public String getName() {
		return name;
	}

	/**
	 * Tests whether this message contains the parameter with the specified key.
	 * Key names are compared ignoring case.
	 *
	 * @param key
	 *            The name of the parameter
	 * @return <code>true</code> if this parameter exists in this message,
	 *         <code>false</code> otherwise
	 */
	public boolean containsKey(String key) {
		return parameters.containsKey(key.toLowerCase());
	}

	/**
	 * Returns all parameters of this message. The keys of the entries are all
	 * lower case so if you want to match the parameter names you have to watch
	 * out.
	 *
	 * @return All parameters of this message
	 */
	public Set<Entry<String, String>> entrySet() {
		return parameters.entrySet();
	}

	/**
	 * Returns the value of the parameter with the name specified by
	 * <code>key</code>.
	 *
	 * @param key
	 *            The name of the parameter
	 * @return The value of the parameter
	 */
	public String get(String key) {
		return parameters.get(key.toLowerCase());
	}

	/**
	 * Stores the specified value as parameter with the name specified by
	 * <code>key</code>.
	 *
	 * @param key
	 *            The name of the parameter
	 * @param value
	 *            The value of the parameter
	 * @return The previous value, or <code>null</code> if there was no
	 *         previous value
	 */
	public String put(String key, String value) {
		return parameters.put(key.toLowerCase(), value);
	}

	/**
	 * Returns the number of parameters in this message.
	 *
	 * @return The number of parameters
	 */
	public int size() {
		return parameters.size();
	}

	/**
	 * @return Returns the payloadInputStream.
	 */
	public InputStream getPayloadInputStream() {
		return payloadInputStream;
	}

	/**
	 * @param payloadInputStream
	 *            The payloadInputStream to set.
	 */
	public void setPayloadInputStream(InputStream payloadInputStream) {
		this.payloadInputStream = payloadInputStream;
	}

	/**
	 * Returns a textual representation of this message, containing its name,
	 * the identifier, and the parameters.
	 *
	 * @return A textual representation of this message
	 */
	@Override
	public String toString() {
		return name + "[identifier=" + identifier + ",parameters=" + parameters.toString() + "]";
	}

}

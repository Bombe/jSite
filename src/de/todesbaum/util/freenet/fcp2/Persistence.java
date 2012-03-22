/*
 * jSite - Persistence.java - Copyright © 2006–2012 David Roden
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
 * The possible persistence options. This specify whether (and for how long) the
 * node remembers to execute a request and the results. Possible values are
 * <code>connection</code>, <code>reboot</code>, and <code>forever</code>.
 * <code>connection</code> means that a request is aborted as soon as the
 * connection to the node is severed. <code>reboot</code> means that a request
 * is remembered as long as the node is running but not after restarts.
 * <code>forever</code> finally means that a request persists until it is
 * explicitely deleted.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 * @see de.todesbaum.util.freenet.fcp2.ModifyPersistentRequest
 * @see de.todesbaum.util.freenet.fcp2.RemovePersistentRequest
 */
public final class Persistence {

	/**
	 * Denotes that a request should be terminated if the connection to the node
	 * is severed.
	 */
	public static final Persistence CONNECTION = new Persistence("connection");

	/** Denotes that a request should be remembered until the node is restarted. */
	public static final Persistence REBOOT = new Persistence("reboot");

	/**
	 * Denotes that a request should be remembered until it is explicitely
	 * deleted.
	 */
	public static final Persistence FOREVER = new Persistence("forever");

	/** The name of this persistence option. */
	private String name;

	/**
	 * Private constructor that creates a persistence option with the specified
	 * name.
	 *
	 * @param name
	 *            The name of the persistence option.
	 */
	private Persistence(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of this persistence option.
	 *
	 * @return The name of this persistence option
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a textual representation of this persistence option. The result
	 * is identical to calling {@link #getName()}.
	 *
	 * @return The name of this persistence option
	 */
	@Override
	public String toString() {
		return name;
	}

}

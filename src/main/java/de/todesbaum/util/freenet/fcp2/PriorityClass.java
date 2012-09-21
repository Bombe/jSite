/*
 * jSite - PriorityClass.java - Copyright © 2006–2012 David Roden
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
 * The possible priority classes. Possible values are, in order of descending
 * priority: <code>maximum</code> (anything more important than fproxy),
 * <code>interactive</code> (fproxy), <code>semi-interactive</code> (fproxy
 * immediate mode large file downloads, not to disk), <code>updatable</code>
 * (updatable site checks), <code>bulk</code> (large file downloads to disk),
 * <code>prefetch</code>, <code>minimum</code>.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public final class PriorityClass {

	/** Denotes <code>maximum</code> priority class. */
	public static final PriorityClass MAXIMUM = new PriorityClass("maximum", 0);

	/** Denotes <code>interactive</code> priority class. */
	public static final PriorityClass INTERACTIVE = new PriorityClass("interactive", 1);

	/** Denotes <code>semi-interactive</code> priority class. */
	public static final PriorityClass SEMI_INTERACTIVE = new PriorityClass("semiInteractive", 2);

	/** Denotes <code>updatable</code> priority class. */
	public static final PriorityClass UPDATABLE = new PriorityClass("updatable", 3);

	/** Denotes <code>bulk</code> priority class. */
	public static final PriorityClass BULK = new PriorityClass("bulk", 4);

	/** Denotes <code>prefetch</code> priority class. */
	public static final PriorityClass PREFETCH = new PriorityClass("prefetch", 5);

	/** Denotes <code>minimum</code> priority class. */
	public static final PriorityClass MINIMUM = new PriorityClass("minimum", 6);

	/** The name of the priority class. */
	private String name;

	/** The value of the priority class. */
	private int value;

	/**
	 * Creates a new priority class with the specified name and value.
	 *
	 * @param name
	 *            The name of the priority class
	 * @param value
	 *            The value of the priority class
	 */
	private PriorityClass(String name, int value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the name of this priority class.
	 *
	 * @return The name of this priority class
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value of this priority class.
	 *
	 * @return The value of this priority class
	 */
	public int getValue() {
		return value;
	}

	//
	// STATIC METHODS
	//

	/**
	 * Returns the priority class with the given name, matched case-insensitive.
	 *
	 * @param value
	 *            The name of the priority
	 * @return The priority with the given name, or {@code null} if no priority
	 *         matches the given name
	 */
	public static PriorityClass valueOf(String value) {
		for (PriorityClass priorityClass : new PriorityClass[] { MINIMUM, PREFETCH, BULK, UPDATABLE, SEMI_INTERACTIVE, INTERACTIVE, MAXIMUM }) {
			if (priorityClass.getName().equalsIgnoreCase(value)) {
				return priorityClass;
			}
		}
		return null;
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return name;
	}

}

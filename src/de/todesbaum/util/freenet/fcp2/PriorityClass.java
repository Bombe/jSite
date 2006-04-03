/*
 * todesbaum-lib - 
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

package de.todesbaum.util.freenet.fcp2;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id: PriorityClass.java 356 2006-03-24 15:13:38Z bombe $
 */
public final class PriorityClass {

	public static final PriorityClass MAXIMUM = new PriorityClass("maximum", 0);
	public static final PriorityClass INTERACTIVE = new PriorityClass("interactive", 1);
	public static final PriorityClass SEMI_INTERACTIVE = new PriorityClass("semiInteractive", 2);
	public static final PriorityClass UPDATABLE = new PriorityClass("updatable", 3);
	public static final PriorityClass BULK = new PriorityClass("bulk", 4);
	public static final PriorityClass PREFETCH = new PriorityClass("prefetch", 5);
	public static final PriorityClass MINIMUM = new PriorityClass("minimum", 6);

	private String name;
	private int value;

	private PriorityClass(String name, int value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the value.
	 */
	public int getValue() {
		return value;
	}

}

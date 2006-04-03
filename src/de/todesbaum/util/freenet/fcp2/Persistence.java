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
 * @version $Id: Persistence.java 373 2006-03-25 10:42:52Z bombe $
 */
public final class Persistence {

	public static final Persistence CONNECTION = new Persistence("connection");
	public static final Persistence REBOOT = new Persistence("reboot");
	public static final Persistence FOREVER = new Persistence("forever");

	private String name;

	private Persistence(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

}

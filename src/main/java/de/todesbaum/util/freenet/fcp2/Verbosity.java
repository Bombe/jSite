/*
 * jSite - Verbosity.java - Copyright © 2006–2012 David Roden
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
 * @version $Id$
 */
public final class Verbosity {

	public static final Verbosity PROGRESS = new Verbosity(1);
	public static final Verbosity COMPRESSION = new Verbosity(512);

	public static final Verbosity NONE = new Verbosity(0);
	public static final Verbosity ALL = new Verbosity(PROGRESS, COMPRESSION);

	private final int value;

	private Verbosity(int value) {
		this.value = value;
	}

	private Verbosity(Verbosity verbosity1, Verbosity verbosity2) {
		this(verbosity1.value | verbosity2.value);
	}

	/**
	 * @return Returns the value.
	 */
	public int getValue() {
		return value;
	}

}

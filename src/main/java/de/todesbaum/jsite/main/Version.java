/*
 * jSite - Version.java - Copyright © 2006–2014 David Roden
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

package de.todesbaum.jsite.main;

/**
 * Container for version information.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Version implements Comparable<Version> {

	/** The components of the version information. */
	private final int[] components;

	/**
	 * Creates a new version container with the given components.
	 *
	 * @param components
	 *            The version components
	 */
	public Version(int... components) {
		this.components = new int[components.length];
		System.arraycopy(components, 0, this.components, 0, components.length);
	}

	/**
	 * Returns the number of version components.
	 *
	 * @return The number of version components
	 */
	public int size() {
		return components.length;
	}

	/**
	 * Returns the version component with the given index.
	 *
	 * @param index
	 *            The index of the version component
	 * @return The version component
	 */
	public int getComponent(int index) {
		return components[index];
	}

	/**
	 * Parses a version from the given string.
	 *
	 * @param versionString
	 *            The version string to parse
	 * @return The parsed version, or <code>null</code> if the string could not
	 *         be parsed
	 */
	public static Version parse(String versionString) {
		String[] componentStrings = versionString.split("\\.");
		int[] components = new int[componentStrings.length];
		int index = -1;
		for (String componentString : componentStrings) {
			try {
				components[++index] = Integer.parseInt(componentString);
			} catch (NumberFormatException nfe1) {
				return null;
			}
		}
		return new Version(components);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder versionString = new StringBuilder();
		for (int component : components) {
			if (versionString.length() != 0) {
				versionString.append('.');
			}
			versionString.append(component);
		}
		return versionString.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Version version) {
		int lessComponents = Math.min(components.length, version.components.length);
		for (int index = 0; index < lessComponents; index++) {
			if (version.components[index] == components[index]) {
				continue;
			}
			return components[index] - version.components[index];
		}
		return components.length - version.components.length;
	}

}

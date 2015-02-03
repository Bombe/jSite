/*
 * jSite - UpdateListener.java - Copyright © 2008–2014 David Roden
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

import java.util.EventListener;

import de.todesbaum.jsite.main.Version;

/**
 * Listener interface for objects that want to be notified when update data was
 * found.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public interface UpdateListener extends EventListener {

	/**
	 * Notifies a listener that data for the given version was found.
	 *
	 * @param foundVersion
	 *            The version that was found
	 * @param versionTimestamp
	 *            The timestamp of the version, or <code>-1</code> if the
	 *            timestamp is unknown
	 */
	public void foundUpdateData(Version foundVersion, long versionTimestamp);

}

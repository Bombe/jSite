/*
 * jSite - ProjectInsertListeners.java - Copyright © 2013–2014 David Roden
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

import java.util.ArrayList;
import java.util.List;

/**
 * Manages {@link InsertListener}s for the {@link ProjectInserter}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
class ProjectInsertListeners {

	/** The list of insert listeners. */
	private final List<InsertListener> insertListeners = new ArrayList<InsertListener>();

	/**
	 * Adds a listener to the list of registered listeners.
	 *
	 * @param insertListener
	 * 		The listener to add
	 */
	void addInsertListener(InsertListener insertListener) {
		insertListeners.add(insertListener);
	}

	/**
	 * Removes a listener from the list of registered listeners.
	 *
	 * @param insertListener
	 * 		The listener to remove
	 */
	void removeInsertListener(InsertListener insertListener) {
		insertListeners.remove(insertListener);
	}

	/**
	 * Notifies all listeners that the project insert has started.
	 *
	 * @param project
	 * @see InsertListener#projectInsertStarted(Project)
	 */
	void fireProjectInsertStarted(Project project) {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectInsertStarted(project);
		}
	}

	/**
	 * Notifies all listeners that the insert has generated a URI.
	 *
	 * @param project
	 * @param uri
	 * 		The generated URI
	 * @see InsertListener#projectURIGenerated(Project, String)
	 */
	void fireProjectURIGenerated(Project project, String uri) {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectURIGenerated(project, uri);
		}
	}

	/**
	 * Notifies all listeners that the insert has made some progress.
	 *
	 * @param project
	 * @see InsertListener#projectUploadFinished(Project)
	 */
	void fireProjectUploadFinished(Project project) {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectUploadFinished(project);
		}
	}

	/**
	 * Notifies all listeners that the insert has made some progress.
	 *
	 * @param project
	 * @param succeeded
	 * 		The number of succeeded blocks
	 * @param failed
	 * 		The number of failed blocks
	 * @param fatal
	 * 		The number of fatally failed blocks
	 * @param total
	 * 		The total number of blocks
	 * @param finalized
	 * 		<code>true</code> if the total number of blocks has already been
	 * 		finalized, <code>false</code> otherwise
	 * @see InsertListener#projectInsertProgress(Project, int, int, int, int,
	 *      boolean)
	 */
	void fireProjectInsertProgress(Project project, int succeeded, int failed, int fatal, int total, boolean finalized) {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectInsertProgress(project, succeeded, failed, fatal, total, finalized);
		}
	}

	/**
	 * Notifies all listeners the project insert has finished.
	 *
	 * @param project
	 * @param success
	 * 		<code>true</code> if the project was inserted successfully,
	 * 		<code>false</code> if it failed
	 * @param cause
	 * 		The cause of the failure, if any
	 * @see InsertListener#projectInsertFinished(Project, boolean, Throwable)
	 */
	void fireProjectInsertFinished(Project project, boolean success, Throwable cause) {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectInsertFinished(project, success, cause);
		}
	}

}

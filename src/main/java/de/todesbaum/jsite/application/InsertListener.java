/*
 * jSite - InsertListener.java - Copyright © 2006–2014 David Roden
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

/**
 * Interface for objects that want to be notified abount insert events.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public interface InsertListener extends EventListener {

	/**
	 * Enumeration for the different error situations.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	public static enum ErrorType {

		/** The key does already exist. */
		KEY_COLLISION,

		/** The route to the key was not found. */
		ROUTE_NOT_FOUND,

		/** The data was not found. */
		DATA_NOT_FOUND,

		/** Error in the FCP communication. */
		FCP_ERROR,

		/** General error in the communication. */
		IO_ERROR
	}

	/**
	 * Notifies a listener that an insert has started.
	 *
	 * @param project
	 *            The project that is now being inserted
	 */
	public void projectInsertStarted(Project project);

	/**
	 * Notifies a listener that the upload of a project has finished and the
	 * inserting will start now.
	 *
	 * @param project
	 *            The project that has been uploaded
	 */
	public void projectUploadFinished(Project project);

	/**
	 * Notifies a listener that a project insert has generated a URI.
	 *
	 * @param project
	 *            The project being inserted
	 * @param uri
	 *            The generated URI
	 */
	public void projectURIGenerated(Project project, String uri);

	/**
	 * Notifies a listener that an insert has made some progress.
	 *
	 * @param project
	 *            The project being inserted
	 * @param succeeded
	 *            The number of succeeded blocks
	 * @param failed
	 *            The number of failed blocks
	 * @param fatal
	 *            The number of fatally failed blocks
	 * @param total
	 *            The total number of blocks
	 * @param finalized
	 *            <code>true</code> if the total number of blocks has been
	 *            finalized, <code>false</code> otherwise
	 */
	public void projectInsertProgress(Project project, int succeeded, int failed, int fatal, int total, boolean finalized);

	/**
	 * Notifies a listener that a project insert has finished.
	 *
	 * @param project
	 *            The project being inserted
	 * @param success
	 *            <code>true</code> if the insert succeeded, <code>false</code>
	 *            otherwise
	 * @param cause
	 *            The cause of a failure, if any (may be <code>null</code>)
	 */
	public void projectInsertFinished(Project project, boolean success, Throwable cause);

}

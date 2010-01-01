/*
 * jSite - a tool for uploading websites into Freenet
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

package de.todesbaum.jsite.gui;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.i18n.I18n;

/**
 * Scans the local path of a project anychronously and returns the list of found
 * files as an event.
 *
 * @see Project#getLocalPath()
 * @see FileScannerListener#fileScannerFinished(FileScanner)
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FileScanner implements Runnable {

	/** The list of listeners. */
	private final List<FileScannerListener> fileScannerListeners = new ArrayList<FileScannerListener>();

	/** The project to scan. */
	private final Project project;

	/** The list of found files. */
	private List<String> files;

	/** Wether there was an error. */
	private boolean error = false;

	/**
	 * Creates a new file scanner for the given project.
	 *
	 * @param project
	 *            The project whose files to scan
	 */
	public FileScanner(Project project) {
		this.project = project;
	}

	/**
	 * Adds the given listener to the list of listeners.
	 *
	 * @param fileScannerListener
	 *            The listener to add
	 */
	public void addFileScannerListener(FileScannerListener fileScannerListener) {
		fileScannerListeners.add(fileScannerListener);
	}

	/**
	 * Removes the given listener from the list of listeners.
	 *
	 * @param fileScannerListener
	 *            The listener to remove
	 */
	public void removeFileScannerListener(FileScannerListener fileScannerListener) {
		fileScannerListeners.remove(fileScannerListener);
	}

	/**
	 * Notifies all listeners that the file scan finished.
	 */
	protected void fireFileScannerFinished() {
		for (FileScannerListener fileScannerListener : new ArrayList<FileScannerListener>(fileScannerListeners)) {
			fileScannerListener.fileScannerFinished(this);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Scans all available files in the project’s local path and emits an event
	 * when finished.
	 *
	 * @see FileScannerListener#fileScannerFinished(FileScanner)
	 */
	public void run() {
		files = new ArrayList<String>();
		error = false;
		try {
			scanFiles(new File(project.getLocalPath()), files);
			Collections.sort(files);
		} catch (IOException ioe1) {
			error = true;
		}
		fireFileScannerFinished();
	}

	/**
	 * Returns whether there was an error scanning for files.
	 *
	 * @return <code>true</code> if there was an error, <code>false</code>
	 *         otherwise
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * Returns the list of found files.
	 *
	 * @return The list of found files
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * Recursively scans a directory and adds all found files to the given list.
	 *
	 * @param rootDir
	 *            The directory to scan
	 * @param fileList
	 *            The list to which to add the found files
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private void scanFiles(File rootDir, List<String> fileList) throws IOException {
		File[] files = rootDir.listFiles(new FileFilter() {

			@SuppressWarnings("synthetic-access")
			public boolean accept(File file) {
				return !project.isIgnoreHiddenFiles() || !file.isHidden();
			}
		});
		if (files == null) {
			throw new IOException(I18n.getMessage("jsite.file-scanner.can-not-read-directory"));
		}
		for (File file : files) {
			if (file.isDirectory()) {
				scanFiles(file, fileList);
				continue;
			}
			String filename = project.shortenFilename(file);
			filename = filename.replace('\\', '/');
			fileList.add(filename);
		}
	}

}
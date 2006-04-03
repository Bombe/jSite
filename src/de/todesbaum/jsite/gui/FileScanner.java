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

public class FileScanner implements Runnable {

	private final List<FileScannerListener> fileScannerListeners = new ArrayList<FileScannerListener>();
	private final Project project;
	private List<String> files;
	private boolean error = false;

	public FileScanner(Project project) {
		this.project = project;
	}

	public void addFileScannerListener(FileScannerListener fileScannerListener) {
		fileScannerListeners.add(fileScannerListener);
	}

	public void removeFileScannerListener(FileScannerListener fileScannerListener) {
		fileScannerListeners.remove(fileScannerListener);
	}

	protected void fireFileScannerFinished() {
		for (FileScannerListener fileScannerListener: new ArrayList<FileScannerListener>(fileScannerListeners)) {
			fileScannerListener.fileScannerFinished(this);
		}
	}

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

	public boolean isError() {
		return error;
	}

	public List<String> getFiles() {
		return files;
	}

	private void scanFiles(File rootDir, List<String> fileList) throws IOException {
		File[] files = rootDir.listFiles(new FileFilter() {

			public boolean accept(File file) {
				return !file.isHidden();
			}
		});
		if (files == null) {
			throw new IOException(I18n.getMessage("jsite.file-scanner.can-not-read-directory"));
		}
		for (File file: files) {
			if (file.isDirectory()) {
				scanFiles(file, fileList);
				continue;
			}
			String filename = project.shortenFilename(file);
			fileList.add(filename);
		}
	}

}
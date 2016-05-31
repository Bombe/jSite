/*
 * jSite - FileScanner.java - Copyright © 2006–2014 David Roden
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.io.NullOutputStream;
import net.pterodactylus.util.io.StreamCopier;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.i18n.I18n;

/**
 * Scans the local path of a project anychronously and returns the list of found
 * files as an event.
 *
 * @see Project#getLocalPath()
 * @see FileScannerListener#fileScannerFinished(boolean, java.util.Collection)
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FileScanner implements Runnable {

	/** The logger. */
	private final static Logger logger = Logger.getLogger(FileScanner.class.getName());

	/** The list of listeners. */
	private final FileScannerListener fileScannerListener;

	/** The project to scan. */
	private final Project project;

	/** The list of found files. */
	private List<ScannedFile> files;

	/** Wether there was an error. */
	private boolean error = false;

	/** The name of the last file scanned. */
	private String lastFilename;

	/**
	 * Creates a new file scanner for the given project.
	 *
	 * @param project
	 *            The project whose files to scan
	 */
	public FileScanner(Project project, FileScannerListener fileScannerListener) {
		this.project = project;
		this.fileScannerListener = Objects.requireNonNull(fileScannerListener);
	}

	/**
	 * Returns the name of the last file scanned.
	 *
	 * @return The name of the last file scanned, or {@code null} if there was
	 *         no file scanned yet
	 */
	public String getLastFilename() {
		return lastFilename;
	}

	public void startInBackground() {
		new Thread(this).start();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Scans all available files in the project’s local path and emits an event
	 * when finished.
	 *
	 * @see FileScannerListener#fileScannerFinished(boolean, java.util.Collection)
	 */
	@Override
	public void run() {
		files = new ArrayList<ScannedFile>();
		error = false;
		lastFilename = null;
		try {
			scanFiles(new File(project.getLocalPath()), files);
			Collections.sort(files);
		} catch (IOException ioe1) {
			error = true;
		}
		fileScannerListener.fileScannerFinished(error, files);
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
	public List<ScannedFile> getFiles() {
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
	private void scanFiles(File rootDir, List<ScannedFile> fileList) throws IOException {
		File[] files = rootDir.listFiles(new FileFilter() {

			@Override
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
			String filename = project.shortenFilename(file).replace('\\', '/');
			String hash = hashFile(project.getLocalPath(), filename);
			fileList.add(new ScannedFile(filename, hash));
			lastFilename = filename;
		}
	}

	/**
	 * Hashes the given file.
	 *
	 * @param path
	 *            The path of the project
	 * @param filename
	 *            The name of the file, relative to the project path
	 * @return The hash of the file
	 */
	private static String hashFile(String path, String filename) {
		InputStream fileInputStream = null;
		DigestOutputStream digestOutputStream = null;
		File file = new File(path, filename);
		try {
			fileInputStream = new FileInputStream(file);
			digestOutputStream = new DigestOutputStream(new NullOutputStream(), MessageDigest.getInstance("SHA-256"));
			StreamCopier.copy(fileInputStream, digestOutputStream, file.length());
			return toHex(digestOutputStream.getMessageDigest().digest());
		} catch (NoSuchAlgorithmException nsae1) {
			logger.log(Level.WARNING, "Could not get SHA-256 digest!", nsae1);
		} catch (IOException ioe1) {
			logger.log(Level.WARNING, "Could not read file!", ioe1);
		} finally {
			Closer.close(digestOutputStream);
			Closer.close(fileInputStream);
		}
		return toHex(new byte[32]);
	}

	/**
	 * Converts the given byte array into a hexadecimal string.
	 *
	 * @param array
	 *            The array to convert
	 * @return The hexadecimal string
	 */
	private static String toHex(byte[] array) {
		StringBuilder hexString = new StringBuilder(array.length * 2);
		for (byte b : array) {
			hexString.append("0123456789abcdef".charAt((b >>> 4) & 0x0f)).append("0123456789abcdef".charAt(b & 0xf));
		}
		return hexString.toString();
	}

}

/*
 * jSite - Project.java - Copyright © 2006–2014 David Roden
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package de.todesbaum.jsite.application;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.pterodactylus.util.io.MimeTypes;

/**
 * Container for project information.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Project implements Comparable<Project> {

	/** The name of the project. */
	protected String name;

	/** The description of the project. */
	protected String description;

	/** The insert URI of the project. */
	protected String insertURI;

	/** The request URI of the project. */
	protected String requestURI;

	/** The index file of the project. */
	protected String indexFile;

	/** The local path of the project. */
	protected String localPath;

	/** The remote path of the URI. */
	protected String path;

	/** The time of the last insertion. */
	protected long lastInsertionTime;

	/** The edition to insert to. */
	protected int edition;

	/** Whether to always force inserts. */
	private boolean alwaysForceInserts;

	/** Whether to ignore hidden directory. */
	private boolean ignoreHiddenFiles;

	/** Options for files. */
	protected Map<String, FileOption> fileOptions = new HashMap<String, FileOption>();

	/**
	 * Empty constructor.
	 */
	public Project() {
		/* do nothing. */
	}

	/**
	 * Creates a new project from an existing one.
	 *
	 * @param project
	 *            The project to clone
	 */
	public Project(Project project) {
		name = project.name;
		description = project.description;
		insertURI = project.insertURI;
		requestURI = project.requestURI;
		path = project.path;
		edition = project.edition;
		localPath = project.localPath;
		indexFile = project.indexFile;
		lastInsertionTime = project.lastInsertionTime;
		alwaysForceInserts = project.alwaysForceInserts;
		ignoreHiddenFiles = project.ignoreHiddenFiles;
		for (Entry<String, FileOption> fileOption : fileOptions.entrySet()) {
			fileOptions.put(fileOption.getKey(), new FileOption(fileOption.getValue()));
		}
	}

	/**
	 * Returns the name of the project.
	 *
	 * @return The name of the project
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the project.
	 *
	 * @param name
	 *            The name of the project
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the description of the project.
	 *
	 * @return The description of the project
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the project.
	 *
	 * @param description
	 *            The description of the project
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the local path of the project.
	 *
	 * @return The local path of the project
	 */
	public String getLocalPath() {
		return localPath;
	}

	/**
	 * Sets the local path of the project.
	 *
	 * @param localPath
	 *            The local path of the project
	 */
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	/**
	 * Returns the name of the index file of the project, relative to the
	 * project’s local path.
	 *
	 * @return The name of the index file of the project
	 */
	public String getIndexFile() {
		return indexFile;
	}

	/**
	 * Sets the name of the index file of the project, relative to the project’s
	 * local path.
	 *
	 * @param indexFile
	 *            The name of the index file of the project
	 */
	public void setIndexFile(String indexFile) {
		this.indexFile = indexFile;
	}

	/**
	 * Returns the time the project was last inserted, in milliseconds since the
	 * epoch.
	 *
	 * @return The time of the last insertion
	 */
	public long getLastInsertionTime() {
		return lastInsertionTime;
	}

	/**
	 * Sets the time the project was last inserted, in milliseconds since the
	 * last epoch.
	 *
	 * @param lastInserted
	 *            The time of the last insertion
	 */
	public void setLastInsertionTime(long lastInserted) {
		lastInsertionTime = lastInserted;
	}

	/**
	 * Returns the remote path of the project. The remote path is the path that
	 * directly follows the request URI of the project.
	 *
	 * @return The remote path of the project
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the remote path of the project. The remote path is the path that
	 * directly follows the request URI of the project.
	 *
	 * @param path
	 *            The remote path of the project
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns the insert URI of the project.
	 *
	 * @return The insert URI of the project
	 */
	public String getInsertURI() {
		return insertURI;
	}

	/**
	 * Sets the insert URI of the project.
	 *
	 * @param insertURI
	 *            The insert URI of the project
	 */
	public void setInsertURI(String insertURI) {
		this.insertURI = shortenURI(insertURI);
	}

	/**
	 * Returns the request URI of the project.
	 *
	 * @return The request URI of the project
	 */
	public String getRequestURI() {
		return requestURI;
	}

	/**
	 * Sets the request URI of the project.
	 *
	 * @param requestURI
	 *            The request URI of the project
	 */
	public void setRequestURI(String requestURI) {
		this.requestURI = shortenURI(requestURI);
	}

	/**
	 * Returns whether files for this project should always be inserted, even
	 * when unchanged.
	 *
	 * @return {@code true} to always force inserts on this project,
	 *         {@code false} otherwise
	 */
	public boolean isAlwaysForceInsert() {
		return alwaysForceInserts;
	}

	/**
	 * Sets whether files for this project should always be inserted, even when
	 * unchanged.
	 *
	 * @param alwaysForceInsert
	 *            {@code true} to always force inserts on this project,
	 *            {@code false} otherwise
	 */
	public void setAlwaysForceInsert(boolean alwaysForceInsert) {
		this.alwaysForceInserts = alwaysForceInsert;
	}

	/**
	 * Returns whether hidden files are ignored, i.e. not inserted.
	 *
	 * @return {@code true} if hidden files are not inserted, {@code false}
	 *         otherwise
	 */
	public boolean isIgnoreHiddenFiles() {
		return ignoreHiddenFiles;
	}

	/**
	 * Sets whether hidden files are ignored, i.e. not inserted.
	 *
	 * @param ignoreHiddenFiles
	 *            {@code true} if hidden files are not inserted, {@code false}
	 *            otherwise
	 */
	public void setIgnoreHiddenFiles(boolean ignoreHiddenFiles) {
		this.ignoreHiddenFiles = ignoreHiddenFiles;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method returns the name of the project.
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Shortens the given URI by removing scheme and key-type prefixes.
	 *
	 * @param uri
	 *            The URI to shorten
	 * @return The shortened URI
	 */
	private static String shortenURI(String uri) {
		String shortUri = uri;
		if (shortUri.startsWith("freenet:")) {
			shortUri = shortUri.substring("freenet:".length());
		}
		if (shortUri.startsWith("SSK@")) {
			shortUri = shortUri.substring("SSK@".length());
		}
		if (shortUri.startsWith("USK@")) {
			shortUri = shortUri.substring("USK@".length());
		}
		if (shortUri.endsWith("/")) {
			shortUri = shortUri.substring(0, shortUri.length() - 1);
		}
		return shortUri;
	}

	/**
	 * Shortens the name of the given file by removing the local path of the
	 * project and leading file separators.
	 *
	 * @param file
	 *            The file whose name should be shortened
	 * @return The shortened name of the file
	 */
	public String shortenFilename(File file) {
		String filename = file.getPath();
		if (filename.startsWith(localPath)) {
			filename = filename.substring(localPath.length());
			if (filename.startsWith(File.separator)) {
				filename = filename.substring(1);
			}
		}
		return filename;
	}

	/**
	 * Returns the options for the file with the given name. If the file does
	 * not yet have any options, a new set of default options is created and
	 * returned.
	 *
	 * @param filename
	 *            The name of the file, relative to the project root
	 * @return The options for the file
	 */
	public FileOption getFileOption(String filename) {
		FileOption fileOption = fileOptions.get(filename);
		if (fileOption == null) {
			fileOption = new FileOption(MimeTypes.getMimeType(filename.substring(filename.lastIndexOf('.') + 1)));
			fileOptions.put(filename, fileOption);
		}
		return fileOption;
	}

	/**
	 * Sets options for a file.
	 *
	 * @param filename
	 *            The filename to set the options for, relative to the project
	 *            root
	 * @param fileOption
	 *            The options to set for the file, or <code>null</code> to
	 *            remove the options for the file
	 */
	public void setFileOption(String filename, FileOption fileOption) {
		if (fileOption != null) {
			fileOptions.put(filename, fileOption);
		} else {
			fileOptions.remove(filename);
		}
	}

	/**
	 * Returns all file options.
	 *
	 * @return All file options
	 */
	public Map<String, FileOption> getFileOptions() {
		return Collections.unmodifiableMap(fileOptions);
	}

	/**
	 * Sets all file options.
	 *
	 * @param fileOptions
	 *            The file options
	 */
	public void setFileOptions(Map<String, FileOption> fileOptions) {
		this.fileOptions.clear();
		this.fileOptions.putAll(fileOptions);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Projects are compared by their name only.
	 */
	@Override
	public int compareTo(Project project) {
		return name.compareToIgnoreCase(project.name);
	}

	/**
	 * Returns the edition of the project.
	 *
	 * @return The edition of the project
	 */
	public int getEdition() {
		return edition;
	}

	/**
	 * Sets the edition of the project.
	 *
	 * @param edition
	 *            The edition to set
	 */
	public void setEdition(int edition) {
		this.edition = edition;
	}

	/**
	 * Constructs the final request URI including the edition number.
	 *
	 * @param offset
	 *            The offset for the edition number
	 * @return The final request URI
	 */
	public String getFinalRequestURI(int offset) {
		return "USK@" + requestURI + "/" + path + "/" + (edition + offset) + "/";
	}

	/**
	 * Performs some post-processing on the project after it was inserted
	 * successfully. At the moment it copies the current hashes of all file
	 * options to the last insert hashes, updating the hashes for the next
	 * insert.
	 */
	public void onSuccessfulInsert() {
		for (Entry<String, FileOption> fileOptionEntry : fileOptions.entrySet()) {
			FileOption fileOption = fileOptionEntry.getValue();
			if ((fileOption.getCurrentHash() != null) && (fileOption.getCurrentHash().length() > 0) && (!fileOption.getCurrentHash().equals(fileOption.getLastInsertHash()) || fileOption.isForceInsert())) {
				fileOption.setLastInsertEdition(edition);
				fileOption.setLastInsertHash(fileOption.getCurrentHash());
				fileOption.setLastInsertFilename(fileOption.getChangedName().or(fileOptionEntry.getKey()));
			}
			fileOption.setForceInsert(false);
		}
	}

}

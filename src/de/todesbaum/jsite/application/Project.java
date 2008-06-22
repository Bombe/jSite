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

package de.todesbaum.jsite.application;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.todesbaum.util.mime.DefaultMIMETypes;

/**
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Project implements Comparable<Project> {

	protected String name;
	protected String description;

	protected String insertURI;
	protected String requestURI;

	protected String indexFile;
	protected String localPath;
	protected String path;
	protected long lastInsertionTime;
	/** The edition to insert to. */
	protected int edition;

	protected Map<String, FileOption> fileOptions = new HashMap<String, FileOption>();

	public Project() {
	}

	/**
	 * Clone-constructor.
	 * 
	 * @param project
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
		fileOptions = new HashMap<String, FileOption>(project.fileOptions);
	}

	/**
	 * @return Returns the title.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param title
	 *            The title to set.
	 */
	public void setName(String title) {
		name = title;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the localPath.
	 */
	public String getLocalPath() {
		return localPath;
	}

	/**
	 * @param localPath
	 *            The localPath to set.
	 */
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	/**
	 * @return Returns the indexFile.
	 */
	public String getIndexFile() {
		return indexFile;
	}

	/**
	 * @param indexFile
	 *            The indexFile to set.
	 */
	public void setIndexFile(String indexFile) {
		this.indexFile = indexFile;
	}

	/**
	 * @return Returns the lastInserted.
	 */
	public long getLastInsertionTime() {
		return lastInsertionTime;
	}

	/**
	 * @param lastInserted
	 *            The lastInserted to set.
	 */
	public void setLastInsertionTime(long lastInserted) {
		lastInsertionTime = lastInserted;
	}

	/**
	 * @return Returns the name.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setPath(String name) {
		path = name;
	}

	/**
	 * @return Returns the insertURI.
	 */
	public String getInsertURI() {
		return insertURI;
	}

	/**
	 * @param insertURI
	 *            The insertURI to set.
	 */
	public void setInsertURI(String insertURI) {
		this.insertURI = shortenURI(insertURI);
	}

	/**
	 * @return Returns the requestURI.
	 */
	public String getRequestURI() {
		return requestURI;
	}

	/**
	 * @param requestURI
	 *            The requestURI to set.
	 */
	public void setRequestURI(String requestURI) {
		this.requestURI = shortenURI(requestURI);
	}

	@Override
	public String toString() {
		return name;
	}

	private String shortenURI(String uri) {
		if (uri.startsWith("freenet:")) {
			uri = uri.substring("freenet:".length());
		}
		if (uri.startsWith("SSK@")) {
			uri = uri.substring("SSK@".length());
		}
		if (uri.startsWith("USK@")) {
			uri = uri.substring("USK@".length());
		}
		if (uri.endsWith("/")) {
			uri = uri.substring(0, uri.length() - 1);
		}
		return uri;
	}

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

	public FileOption getFileOption(String filename) {
		FileOption fileOption = fileOptions.get(filename);
		if (fileOption == null) {
			fileOption = new FileOption(DefaultMIMETypes.guessMIMEType(filename));
			fileOptions.put(filename, fileOption);
		}
		return fileOption;
	}

	public void setFileOption(String filename, FileOption fileOption) {
		if (fileOption != null) {
			fileOptions.put(filename, fileOption);
		} else {
			fileOptions.remove(filename);
		}
	}

	/**
	 * @return Returns the fileOptions.
	 */
	public Map<String, FileOption> getFileOptions() {
		return Collections.unmodifiableMap(fileOptions);
	}

	/**
	 * @param fileOptions
	 *            The fileOptions to set.
	 */
	public void setFileOptions(Map<String, FileOption> fileOptions) {
		this.fileOptions.clear();
		this.fileOptions.putAll(fileOptions);
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * @return The final request URI
	 */
	public String getFinalRequestURI(int offset) {
		return "USK@" + requestURI + "/" + path + "/" + (edition + offset) + "/";
	}

}

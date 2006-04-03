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
 * @author David Roden <dr@todesbaum.dyndns.org>
 * @version $Id: Project.java 357 2006-03-24 15:46:03Z bombe $
 */
public abstract class Project implements Comparable {

	protected String name;
	protected String description;

	protected String insertURI;
	protected String requestURI;

	protected String indexFile;
	protected String localPath;
	protected String path;
	protected long lastInsertionTime;

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
		this.name = title;
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
		this.lastInsertionTime = lastInserted;
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
		this.path = name;
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
		this.insertURI = insertURI;
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
		this.requestURI = requestURI;
	}

	public String toString() {
		return name;
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
		fileOptions.put(filename, fileOption);
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

	public String getFinalURI(int editionOffset) {
		return requestURI + path + "/";
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o) {
		return name.compareToIgnoreCase(((Project) o).name);
	}

}

/*
 * jSite - DiskFileEntry.java - Copyright © 2006–2012 David Roden
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

package de.todesbaum.util.freenet.fcp2;

/**
 * A {@link FileEntry} that reads the content from a file on the disk.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class DiskFileEntry extends FileEntry {

	/** The local file name. */
	private final String localFilename;

	/**
	 * Creates a new {@link FileEntry} with the specified name and content type
	 * that is read from the file specified by <code>localFilename</code>.
	 *
	 * @param filename
	 *            The name of the file
	 * @param contentType
	 *            The content type of the file
	 * @param localFilename
	 *            The name of the local file that holds the content of the file
	 *            to insert
	 */
	public DiskFileEntry(String filename, String contentType, String localFilename) {
		super(filename, contentType);
		this.localFilename = localFilename;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return "disk";
	}

	/**
	 * Returns the name of the local file that holds the content for this file.
	 *
	 * @return The name of the local file
	 */
	public String getLocalFilename() {
		return localFilename;
	}

}
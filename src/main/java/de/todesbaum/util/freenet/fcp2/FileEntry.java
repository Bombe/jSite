/*
 * jSite - FileEntry.java - Copyright © 2006–2012 David Roden
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
 * Abstract base class of file entries that are used in the
 * {@link de.todesbaum.util.freenet.fcp2.ClientPutComplexDir} command to define
 * the files of an insert.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public abstract class FileEntry {

	/** The name of the file. */
	private final String filename;

	/** The content type of the file. */
	private final String contentType;

	/**
	 * Creates a new file entry with the specified name and content type. The
	 * content type should be a standard MIME type with an additional charset
	 * specification for text-based types.
	 *
	 * @param filename
	 *            The name of the file
	 * @param contentType
	 *            The content type of the file, e.g.
	 *            <code>"application/x-tar"</code> or
	 *            <code>"text/html; charset=iso8859-15"</code>
	 */
	protected FileEntry(String filename, String contentType) {
		this.filename = filename;
		this.contentType = contentType;
	}

	/**
	 * Returns the name of this entry's type. Can be one of <code>direct</code>,
	 * <code>disk</code>, or <code>redirect</code>. This method is
	 * implemented by the subclasses {@link DirectFileEntry},
	 * {@link DiskFileEntry}, and {@link RedirectFileEntry}, respectively.
	 *
	 * @return The name of this entry's type
	 */
	public abstract String getName();

	/**
	 * Returns the content type of this file.
	 *
	 * @return The content type of this file
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Returns the name of this file.
	 *
	 * @return The name of this file
	 */
	public String getFilename() {
		return filename;
	}

}
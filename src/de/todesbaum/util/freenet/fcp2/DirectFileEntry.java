/*
 * jSite - DirectFileEntry.java - Copyright © 2006–2012 David Roden
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A {@link FileEntry} that sends its payload directly to the node, using the
 * existing FCP connection.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class DirectFileEntry extends FileEntry {

	/** The input stream to read the data for this file from. */
	private final InputStream dataInputStream;

	/** The length of the data. */
	private final long dataLength;

	/**
	 * Creates a new FileEntry with the specified name and content type that
	 * gets its data from the specified byte array.
	 *
	 * @param filename
	 *            The name of the file
	 * @param contentType
	 *            The content type of the file
	 * @param dataBytes
	 *            The content of the file
	 */
	public DirectFileEntry(String filename, String contentType, byte[] dataBytes) {
		this(filename, contentType, new ByteArrayInputStream(dataBytes), dataBytes.length);
	}

	/**
	 * Creates a new FileEntry with the specified name and content type that
	 * gets its data from the specified input stream.
	 *
	 * @param filename
	 *            The name of the file
	 * @param contentType
	 *            The content type of the file
	 * @param dataInputStream
	 *            The input stream to read the content from
	 * @param dataLength
	 *            The length of the data input stream
	 */
	public DirectFileEntry(String filename, String contentType, InputStream dataInputStream, long dataLength) {
		super(filename, contentType);
		this.dataInputStream = dataInputStream;
		this.dataLength = dataLength;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return "direct";
	}

	/**
	 * Returns the input stream for the file's content.
	 *
	 * @return The input stream for the file's content
	 */
	public InputStream getDataInputStream() {
		return dataInputStream;
	}

	/**
	 * Returns the length of this file's content.
	 *
	 * @return The length of this file's content
	 */
	public long getDataLength() {
		return dataLength;
	}

}
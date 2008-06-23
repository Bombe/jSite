/*
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

package de.todesbaum.util.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copies input from an {@link InputStream} to an {@link OutputStream}.
 *
 * @author <a href="mailto:droden@gmail.com">David Roden</a>
 * @version $Id$
 */
public class StreamCopier {

	/**
	 * The default size of the buffer.
	 */
	private static final int BUFFER_SIZE = 64 * 1024;

	/**
	 * The {@link InputStream} to read from.
	 */
	private InputStream inputStream;

	/**
	 * The {@link OutputStream} to write to.
	 */
	private OutputStream outputStream;

	/**
	 * The number of bytes to copy.
	 */
	private long length;

	/**
	 * The size of the buffer.
	 */
	private int bufferSize;

	/**
	 * Creates a new StreamCopier with the specified parameters and the default
	 * buffer size.
	 *
	 * @param inputStream
	 *            The {@link InputStream} to read from
	 * @param outputStream
	 *            The {@link OutputStream} to write to
	 * @param length
	 *            The number of bytes to copy
	 */
	public StreamCopier(InputStream inputStream, OutputStream outputStream, long length) {
		this(inputStream, outputStream, length, BUFFER_SIZE);
	}

	/**
	 * Creates a new StreamCopier with the specified parameters and the default
	 * buffer size.
	 *
	 * @param inputStream
	 *            The {@link InputStream} to read from
	 * @param outputStream
	 *            The {@link OutputStream} to write to
	 * @param length
	 *            The number of bytes to copy
	 * @param bufferSize
	 *            The number of bytes to copy at a time
	 */
	public StreamCopier(InputStream inputStream, OutputStream outputStream, long length, int bufferSize) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.length = length;
		this.bufferSize = bufferSize;
	}

	/**
	 * Copies the stream data. If the input stream is depleted before the
	 * requested number of bytes have been read an {@link EOFException} is
	 * thrown.
	 *
	 * @throws EOFException
	 *             if the input stream is depleted before the requested number
	 *             of bytes has been read
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void copy() throws EOFException, IOException {
		copy(inputStream, outputStream, length, bufferSize);
	}

	/**
	 * Copies <code>length</code> bytes from the <code>inputStream</code> to
	 * the <code>outputStream</code>.
	 *
	 * @param inputStream
	 *            The input stream to read from
	 * @param outputStream
	 *            The output stream to write to
	 * @param length
	 *            The number of bytes to copy
	 * @throws IOException
	 *             if an I/O exception occurs
	 */
	public static void copy(InputStream inputStream, OutputStream outputStream, long length) throws IOException {
		copy(inputStream, outputStream, length, BUFFER_SIZE);
	}

	/**
	 * Copies <code>length</code> bytes from the <code>inputStream</code> to
	 * the <code>outputStream</code> using a buffer with the specified size
	 *
	 * @param inputStream
	 *            The input stream to read from
	 * @param outputStream
	 *            The output stream to write to
	 * @param length
	 *            The number of bytes to copy
	 * @param bufferSize
	 *            The size of the copy buffer
	 * @throws IOException
	 *             if an I/O exception occurs
	 */
	public static void copy(InputStream inputStream, OutputStream outputStream, long length, int bufferSize) throws IOException {
		long remaining = length;
		byte[] buffer = new byte[bufferSize];
		while (remaining > 0) {
			int read = inputStream.read(buffer, 0, (int) Math.min(Integer.MAX_VALUE, Math.min(bufferSize, remaining)));
			if (read == -1) {
				throw new EOFException();
			}
			outputStream.write(buffer, 0, read);
			remaining -= read;
		}
	}

}

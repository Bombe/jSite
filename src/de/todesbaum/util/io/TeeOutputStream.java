/*
 * jSite - TeeOutputStream.java - Copyright © 2010 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.todesbaum.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} that sends all data it receives to multiple other output
 * streams. If an error occurs during a {@link #write(int)} to one of the
 * underlying output streams no guarantees are made about how much data is sent
 * to each stream, i.e. there is no good way to recover from such an error.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TeeOutputStream extends OutputStream {

	/** The output streams. */
	private final OutputStream[] outputStreams;

	/**
	 * Creates a new tee output stream that sends all to all given output
	 * streams.
	 *
	 * @param outputStreams
	 *            The output streams to send all data to
	 */
	public TeeOutputStream(OutputStream... outputStreams) {
		this.outputStreams = outputStreams;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * An effort is made to close all output streams. If multiple exceptions
	 * occur, only the first exception is thrown after all output streams have
	 * been tried to close.
	 */
	@Override
	public void close() throws IOException {
		IOException occuredException = null;
		for (OutputStream outputStream : outputStreams) {
			try {
				outputStream.flush();
			} catch (IOException ioe1) {
				if (occuredException == null) {
					occuredException = ioe1;
				}
			}
		}
		if (occuredException != null) {
			throw occuredException;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * An effort is made to flush all output streams. If multiple exceptions
	 * occur, only the first exception is thrown after all output streams have
	 * been tried to flush.
	 */
	@Override
	public void flush() throws IOException {
		IOException occuredException = null;
		for (OutputStream outputStream : outputStreams) {
			try {
				outputStream.flush();
			} catch (IOException ioe1) {
				if (occuredException == null) {
					occuredException = ioe1;
				}
			}
		}
		if (occuredException != null) {
			throw occuredException;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte[] buffer) throws IOException {
		for (OutputStream outputStream : outputStreams) {
			outputStream.write(buffer);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException {
		for (OutputStream outputStream : outputStreams) {
			outputStream.write(buffer, offset, length);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(int data) throws IOException {
		for (OutputStream outputStream : outputStreams) {
			outputStream.write(data);
		}
	}

}

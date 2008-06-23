/*
 * todesbaum-lib - Copyright (C) 2006 David Roden
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

package de.todesbaum.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper class that can close all kinds of resources without throwing exception
 * so that clean-up code can be written with less code. All methods check that
 * the given resource is not <code>null</code> before invoking the close()
 * method of the respective type.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David &lsquo;Bombe&squo;
 *         Roden</a>
 * @version $Id$
 */
public class Closer {

	/**
	 * Closes the given result set.
	 *
	 * @param resultSet
	 *            The result set to close
	 * @see ResultSet#close()
	 */
	public static void close(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException ioe1) {
			}
		}
	}

	/**
	 * Closes the given statement.
	 *
	 * @param statement
	 *            The statement to close
	 * @see Statement#close()
	 */
	public static void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException ioe1) {
			}
		}
	}

	/**
	 * Closes the given connection.
	 *
	 * @param connection
	 *            The connection to close
	 * @see Connection#close()
	 */
	public static void close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException ioe1) {
			}
		}
	}

	/**
	 * Closes the given server socket.
	 *
	 * @param serverSocket
	 *            The server socket to close
	 * @see ServerSocket#close()
	 */
	public static void close(ServerSocket serverSocket) {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException ioe1) {
			}
		}
	}

	/**
	 * Closes the given socket.
	 *
	 * @param socket
	 *            The socket to close
	 * @see Socket#close()
	 */
	public static void close(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ioe1) {
			}
		}
	}

	/**
	 * Closes the given input stream.
	 *
	 * @param inputStream
	 *            The input stream to close
	 * @see InputStream#close()
	 */
	public static void close(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException ioe1) {
			}
		}
	}

	/**
	 * Closes the given output stream.
	 *
	 * @param outputStream
	 *            The output stream to close
	 * @see OutputStream#close()
	 */
	public static void close(OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException ioe1) {
			}
		}
	}

	/**
	 * Closes the given reader.
	 *
	 * @param reader
	 *            The reader to close
	 * @see Reader#close()
	 */
	public static void close(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException ioe1) {
			}
		}
	}

	/**
	 * Closes the given writer.
	 *
	 * @param writer
	 *            The write to close
	 * @see Writer#close()
	 */
	public static void close(Writer writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException ioe1) {
			}
		}
	}

}

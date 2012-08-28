/*
 * jSite - TempFileInputStream.java - Copyright © 2006–2012 David Roden
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

package de.todesbaum.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class TempFileInputStream extends FileInputStream {

	private File tempFile;

	/**
	 * @param name
	 * @throws FileNotFoundException
	 */
	public TempFileInputStream(String name) throws FileNotFoundException {
		this(new File(name));
	}

	/**
	 * @param file
	 * @throws FileNotFoundException
	 */
	public TempFileInputStream(File file) throws FileNotFoundException {
		super(file);
		tempFile = file;
	}

	@Override
	public void close() throws IOException {
		super.close();
		tempFile.delete();
	}

}

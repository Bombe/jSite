/*
 * jSite - LineInputStream.java - Copyright © 2006–2012 David Roden
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class LineInputStream extends FilterInputStream {

	private boolean skipLinefeed = false;
	private StringBuffer lineBuffer = new StringBuffer();

	/**
	 * @param in
	 */
	public LineInputStream(InputStream in) {
		super(in);
	}

	public synchronized String readLine() throws IOException {
		lineBuffer.setLength(0);
		int c = 0;
		while (c != -1) {
			c = read();
			if ((c == -1) && lineBuffer.length() == 0)
				return null;
			if (skipLinefeed && (c == '\n')) {
				skipLinefeed = false;
				continue;
			}
			skipLinefeed = (c == '\r');
			if ((c == '\r') || (c == '\n')) {
				c = -1;
			} else {
				lineBuffer.append((char) c);
			}
		}
		return lineBuffer.toString();
	}

}

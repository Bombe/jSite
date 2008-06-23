/*
 * todesbaum-lib -
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

package de.todesbaum.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class ReplacingOutputStream extends FilterOutputStream {

	private Map<String, String> replacements = new HashMap<String, String>();
	private StringBuffer ringBuffer = new StringBuffer();

	/**
	 * @param out
	 */
	public ReplacingOutputStream(OutputStream out) {
		super(out);
	}

	public void addReplacement(String token, String value) {
		replacements.put(token, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(int b) throws IOException {
		ringBuffer.append((char) b);
		Iterator<Entry<String, String>> entries = replacements.entrySet().iterator();
		boolean found = false;
		Entry<String, String> entry = null;
		while (!found && entries.hasNext()) {
			entry = entries.next();
			if (entry.getKey().startsWith(ringBuffer.toString())) {
				found = true;
			}
		}
		if (!found) {
			String buffer = ringBuffer.toString();
			for (int index = 0, size = buffer.length(); index < size; index++) {
				super.write(buffer.charAt(index));
			}
			ringBuffer.setLength(0);
		} else {
			if (entry.getKey().equals(ringBuffer.toString())) {
				String buffer = entry.getValue();
				for (int index = 0, size = buffer.length(); index < size; index++) {
					super.write(buffer.charAt(index));
				}
				ringBuffer.setLength(0);
			}
		}
	}

}

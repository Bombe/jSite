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

package de.todesbaum.util.freenet.fcp2;

import java.io.IOException;
import java.io.Writer;

/**
 * Abstract base class for all put requests that insert a directory.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class ClientPutDir extends ClientPut {

	/** The default file of the directory. */
	protected String defaultName;

	/**
	 * Creates a new request with the specified name, identifier, and URI.
	 *
	 * @param name
	 *            The name of the request
	 * @param identifier
	 *            The identifier of the request
	 * @param uri
	 *            The URI of the request
	 */
	public ClientPutDir(String name, String identifier, String uri) {
		super(name, identifier, uri);
	}

	/**
	 * Returns the default name of the directory.
	 *
	 * @return The default name of the directory
	 */
	public String getDefaultName() {
		return defaultName;
	}

	/**
	 * Sets the default name of the directory. The default name of a directory
	 * is the name of the file that will be delivered if the directory was
	 * requested without a filename. It's about the same as the
	 * <code>index.html</code> file that gets delivered if you only request a
	 * directory from a webserver.
	 *
	 * @param defaultName
	 *            The default name of the directory
	 */
	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void write(Writer writer) throws IOException {
		super.write(writer);
		if (defaultName != null)
			writer.write("DefaultName=" + defaultName + LINEFEED);
	}

}

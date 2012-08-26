/*
 * jSite - ClientPutDir.java - Copyright © 2006–2012 David Roden
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
 * @param <C>
 *            The type of the “ClientPutDir” command
 * @author David Roden &lt;droden@gmail.com&gt;
 */
public class ClientPutDir<C extends ClientPutDir<?>> extends ClientPut {

	/**
	 * All possible manifest putters. Manifest putters are used to distribute
	 * files of a directory insert to different containers, depending on size,
	 * type, and other factors.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	public enum ManifestPutter {

		/**
		 * Use the “simple” manifest putter. Despite its name this is currently
		 * the default manifest putter.
		 */
		SIMPLE("simple"),

		/** Use the “default” manifest putter. */
		DEFAULT("default");

		/** The name of the manifest putter. */
		private final String name;

		/**
		 * Creates a new manifest putter.
		 *
		 * @param name
		 *            The name of the manifest putter
		 */
		private ManifestPutter(String name) {
			this.name = name;
		}

		/**
		 * Returns the name of the manifest putter.
		 *
		 * @return The name of the manifest putter
		 */
		public String getName() {
			return name;
		}

		//
		// OBJECT METHODS
		//

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}

	}

	/** The default file of the directory. */
	protected String defaultName;

	/** The manifest putter to use. */
	private ManifestPutter manifestPutter;

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
	 * Returns the current manifest putter.
	 *
	 * @return The current manifest putter (may be {@code null})
	 */
	public ManifestPutter getManifestPutter() {
		return manifestPutter;
	}

	/**
	 * Sets the manifest putter for the “ClientPutDir” command. If {@code null}
	 * is given the node will choose a manifest putter.
	 *
	 * @param manifestPutter
	 *            The manifest putter to use for the command (may be
	 *            {@code null})
	 * @return This ClientPutDir command
	 */
	@SuppressWarnings("unchecked")
	public C setManifestPutter(ManifestPutter manifestPutter) {
		this.manifestPutter = manifestPutter;
		return (C) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void write(Writer writer) throws IOException {
		super.write(writer);
		if (defaultName != null)
			writer.write("DefaultName=" + defaultName + LINEFEED);
		if (manifestPutter != null) {
			writer.write("ManifestPutter=" + manifestPutter.getName() + LINEFEED);
		}
	}

}

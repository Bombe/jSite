/*
 * jSite - FcpPluginMessage.java - Copyright © 2012 David Roden
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
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of the <code>FCPPluginMessage</code> command.
 * <p>
 * TODO: Implement passing of data as an {@link InputStream}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpPluginMessage extends Command {

	/** The name of the plugin to talk to. */
	private String pluginName;

	/** The parameters to send to the plugin. */
	private final Map<String, String> parameters = new HashMap<String, String>();

	/**
	 * Creates a new FCPPluginMessage command.
	 *
	 * @param identifier
	 *            The identifier of the command
	 */
	public FcpPluginMessage(String identifier) {
		super("FCPPluginMessage", identifier);
	}

	//
	// ACCESSORS
	//

	/**
	 * Sets the name of the plugin to talk to.
	 *
	 * @param pluginName
	 *            The name of the plugin to talk to
	 * @return This command
	 */
	public FcpPluginMessage setPluginName(String pluginName) {
		this.pluginName = pluginName;
		return this;
	}

	/**
	 * Sets a parameter to send to the plugin.
	 *
	 * @param name
	 *            The name of the parameter
	 * @param value
	 *            The value of the parameter
	 * @return This command
	 */
	public FcpPluginMessage setParameter(String name, String value) {
		parameters.put(name, value);
		return this;
	}

	//
	// COMMAND METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void write(Writer writer) throws IOException {
		super.write(writer);
		writer.write("PluginName=" + pluginName + LINEFEED);
		for (Entry<String, String> parameter : parameters.entrySet()) {
			writer.write(String.format("Param.%s=%s%s", parameter.getKey(), parameter.getValue(), LINEFEED));
		}
	}

}

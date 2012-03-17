/*
 * jSite - ClientPut.java - Copyright © 2006–2012 David Roden
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
 * Abstract base class for all put requests. It contains all parameters that put
 * requests have in common.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public abstract class ClientPut extends Command {

	/** The URI of this request. */
	protected final String uri;

	/** The client token of this request. */
	protected String clientToken = null;

	/** Whether this request should only create a CHK. */
	protected boolean getCHKOnly = false;

	/** Whether this request is a global request. */
	protected boolean global = false;

	/** Whether the node should not try to compress the file. */
	protected boolean dontCompress = false;

	/** The maximum number of retries of this command. */
	protected int maxRetries = 0;

	/** Whether to generate the keys early. */
	protected boolean earlyEncode = false;

	/** The persistence of this request. */
	protected Persistence persistence = Persistence.CONNECTION;

	/** The priority class of this request. */
	protected PriorityClass priorityClass = PriorityClass.INTERACTIVE;

	/** The verbosiry of this request. */
	protected Verbosity verbosity = Verbosity.NONE;

	/**
	 * Creates a new put request with the specified name, identifier and URI.
	 *
	 * @param name
	 *            The name of this request
	 * @param identifier
	 *            The identifier of this request
	 * @param uri
	 *            The URI of this request
	 */
	protected ClientPut(String name, String identifier, String uri) {
		super(name, identifier);
		this.uri = uri;
	}

	/**
	 * Returns whether the node should not try to compress the data.
	 *
	 * @return <code>true</code> if the node should <strong>not</strong> try
	 *         to compress the data
	 */
	public boolean isDontCompress() {
		return dontCompress;
	}

	/**
	 * Sets whether the node should not try to compress the data. A client might
	 * set this hint on data that is clearly not compressible, like MPEG audio
	 * files, JPEG or PNG images, highly compressed movies, or compressed
	 * archives like ZIP files. Otherwise the node will try to compress the file
	 * which -- depending on the size of the data -- might take a lot of time
	 * and memory.
	 *
	 * @param dontCompress
	 *            <code>true</code> if the node should <strong>not</strong>
	 *            try to compress the data
	 */
	public void setDontCompress(boolean dontCompress) {
		this.dontCompress = dontCompress;
	}

	/**
	 * Returns whether this request should only return the CHK of the data.
	 * @return Whether this request should only return the CHK of the data
	 */
	public boolean isGetCHKOnly() {
		return getCHKOnly;
	}

	/**
	 * Sets whether this request should only return the CHK of the data.
	 * @param getCHKOnly
	 *            <code>true</code> if this request should only return the CHK of the data
	 */
	public void setGetCHKOnly(boolean getCHKOnly) {
		this.getCHKOnly = getCHKOnly;
	}

	/**
	 * Returns whether this request is a global request.
	 * @return <code>true</code> if this request is a global request, <code>false</code> otherwise
	 */
	public boolean isGlobal() {
		return global;
	}

	/**
	 * Sets whether this request is a global request.
	 * @param global
	 *            <code>true</code> if this request is a global request, <code>false</code> otherwise
	 */
	public void setGlobal(boolean global) {
		this.global = global;
	}

	/**
	 * Returns the maximum number of retries of this request.
	 * @return The maximum number of retries of this request
	 */
	public int getMaxRetries() {
		return maxRetries;
	}

	/**
	 * Sets the maximum number of retries of this request
	 * @param maxRetries
	 *            The maximum number of retries of this request
	 */
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	/**
	 * Returns whether the data should be encoded early to generate the final
	 * key as fast as possible.
	 *
	 * @return {@code true} if the key should be generated early, {@code false}
	 *         otherwise
	 */
	public boolean isEarlyEncode() {
		return earlyEncode;
	}

	/**
	 * Sets whether the data should be encoded early to generate the final key
	 * as fast as possible.
	 *
	 * @param earlyEncode
	 *            {@code true} if the key should be generated early, {@code
	 *            false} otherwise
	 */
	public void setEarlyEncode(boolean earlyEncode) {
		this.earlyEncode = earlyEncode;
	}

	/**
	 * Returns the priority class of this request.
	 * @return The priority class of this request
	 */
	public PriorityClass getPriorityClass() {
		return priorityClass;
	}

	/**
	 * Sets the priority class of this request.
	 * @param priorityClass
	 *            The priority class of this request
	 */
	public void setPriorityClass(PriorityClass priorityClass) {
		this.priorityClass = priorityClass;
	}

	/**
	 * Returns the verbosity of this request.
	 * @return The verbosity of this request
	 */
	public Verbosity getVerbosity() {
		return verbosity;
	}

	/**
	 * Sets the verbosity of this request.
	 * @param verbosity
	 *            The verbosity of this request
	 */
	public void setVerbosity(Verbosity verbosity) {
		this.verbosity = verbosity;
	}

	/**
	 * Returns the URI of this request
	 * @return The URI of this request.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void write(Writer writer) throws IOException {
		super.write(writer);
		writer.write("URI=" + uri + LINEFEED);
		if (verbosity != null)
			writer.write("Verbosity=" + verbosity.getValue() + LINEFEED);
		if (maxRetries != 0)
			writer.write("MaxRetries=" + maxRetries + LINEFEED);
		writer.write("EarlyEncode=" + earlyEncode);
		if (priorityClass != null)
			writer.write("PriorityClass=" + priorityClass.getValue() + LINEFEED);
		writer.write("GetCHKOnly=" + getCHKOnly + LINEFEED);
		writer.write("Global=" + global + LINEFEED);
		writer.write("DontCompress=" + dontCompress + LINEFEED);
		if (clientToken != null)
			writer.write("ClientToken=" + clientToken + LINEFEED);
		if (persistence != null)
			writer.write("Persistence=" + persistence.getName() + LINEFEED);
	}

}

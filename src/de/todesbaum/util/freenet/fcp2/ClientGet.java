/*
 * jSite - ClientGet.java - Copyright © 2008–2012 David Roden
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
 * Implementation of the “ClientGet” command.
 *
 * @author David ‘BombeB Roden &lt;bombe@freenetproject.org&gt;
 */
public class ClientGet extends Command {

	private boolean ignoreDataStore;
	private boolean dataStoreOnly;
	private String uri;
	private Verbosity verbosity = Verbosity.NONE;
	private long maxSize = -1;
	private long maxTempSize = -1;
	private int maxRetries = -1;
	private PriorityClass priorityClass = PriorityClass.INTERACTIVE;
	private Persistence persistence = Persistence.CONNECTION;
	private String clientToken;
	private boolean global = false;
	private ReturnType returnType = ReturnType.direct;
	private boolean binaryBlob = false;
	private String allowedMimeTypes = null;
	private String filename = null;
	private String tempFilename = null;

	/**
	 *Creates a new ClientGet command with the given request identifier.
	 *
	 * @param identifier
	 *            The request identifier
	 */
	public ClientGet(String identifier) {
		super("ClientGet", identifier);
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public boolean isIgnoreDataStore() {
		return ignoreDataStore;
	}

	/**
	 * TODO
	 *
	 * @param ignoreDataStore
	 */
	public void setIgnoreDataStore(boolean ignoreDataStore) {
		this.ignoreDataStore = ignoreDataStore;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public boolean isDataStoreOnly() {
		return dataStoreOnly;
	}

	/**
	 * TODO
	 *
	 * @param dataStoreOnly
	 */
	public void setDataStoreOnly(boolean dataStoreOnly) {
		this.dataStoreOnly = dataStoreOnly;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * TODO
	 *
	 * @param uri
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Verbosity getVerbosity() {
		return verbosity;
	}

	/**
	 * TODO
	 *
	 * @param verbosity
	 */
	public void setVerbosity(Verbosity verbosity) {
		this.verbosity = verbosity;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public long getMaxSize() {
		return maxSize;
	}

	/**
	 * TODO
	 *
	 * @param maxSize
	 */
	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public long getMaxTempSize() {
		return maxTempSize;
	}

	/**
	 * TODO
	 *
	 * @param maxTempSize
	 */
	public void setMaxTempSize(long maxTempSize) {
		this.maxTempSize = maxTempSize;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public int getMaxRetries() {
		return maxRetries;
	}

	/**
	 * TODO
	 *
	 * @param maxRetries
	 */
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public PriorityClass getPriorityClass() {
		return priorityClass;
	}

	/**
	 * TODO
	 *
	 * @param priorityClass
	 */
	public void setPriorityClass(PriorityClass priorityClass) {
		this.priorityClass = priorityClass;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Persistence getPersistence() {
		return persistence;
	}

	/**
	 * TODO
	 *
	 * @param persistence
	 */
	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public String getClientToken() {
		return clientToken;
	}

	/**
	 * TODO
	 *
	 * @param clientToken
	 */
	public void setClientToken(String clientToken) {
		this.clientToken = clientToken;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public boolean isGlobal() {
		return global;
	}

	/**
	 * TODO
	 *
	 * @param global
	 */
	public void setGlobal(boolean global) {
		this.global = global;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public ReturnType getReturnType() {
		return returnType;
	}

	/**
	 * TODO
	 *
	 * @param returnType
	 */
	public void setReturnType(ReturnType returnType) {
		this.returnType = returnType;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public boolean isBinaryBlob() {
		return binaryBlob;
	}

	/**
	 * TODO
	 *
	 * @param binaryBlob
	 */
	public void setBinaryBlob(boolean binaryBlob) {
		this.binaryBlob = binaryBlob;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public String getAllowedMimeTypes() {
		return allowedMimeTypes;
	}

	/**
	 * TODO
	 *
	 * @param allowedMimeTypes
	 */
	public void setAllowedMimeTypes(String allowedMimeTypes) {
		this.allowedMimeTypes = allowedMimeTypes;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * TODO
	 *
	 * @param filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public String getTempFilename() {
		return tempFilename;
	}

	/**
	 * TODO
	 *
	 * @param tempFilename
	 */
	public void setTempFilename(String tempFilename) {
		this.tempFilename = tempFilename;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void write(Writer writer) throws IOException {
		super.write(writer);
		writer.write("IgnoreDS=" + ignoreDataStore + LINEFEED);
		writer.write("DSonly=" + dataStoreOnly + LINEFEED);
		writer.write("URI=" + uri + LINEFEED);
		writer.write("Verbosity=" + verbosity.getValue() + LINEFEED);
		if (maxSize > -1) {
			writer.write("MaxSize=" + maxSize + LINEFEED);
		}
		if (maxTempSize > -1) {
			writer.write("MaxTempSize=" + maxTempSize + LINEFEED);
		}
		if (maxRetries >= -1) {
			writer.write("MaxRetries=" + maxRetries + LINEFEED);
		}
		writer.write("PriorityClass=" + priorityClass.getValue() + LINEFEED);
		writer.write("Persistence=" + persistence.getName() + LINEFEED);
		if (clientToken != null) {
			writer.write("ClientToken=" + clientToken + LINEFEED);
		}
		writer.write("Global=" + global + LINEFEED);
		writer.write("BinaryBlob=" + binaryBlob + LINEFEED);
		if (allowedMimeTypes != null) {
			writer.write("AllowedMIMETypes=" + allowedMimeTypes + LINEFEED);
		}
		if (returnType == ReturnType.disk) {
			writer.write("Filename=" + filename + LINEFEED);
			if (tempFilename != null) {
				writer.write("TempFilename=" + tempFilename + LINEFEED);
			}
		}
	}

}

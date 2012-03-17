/*
 * jSite - FileOption.java - Copyright © 2006–2011 David Roden
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

package de.todesbaum.jsite.application;

/**
 * Container for various file options.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FileOption {

	/** The default for the insert state. */
	private static final boolean DEFAULT_INSERT = true;

	/** The default for the insert redirect state. */
	private static final boolean DEFAULT_INSERT_REDIRECT = true;

	/** The default for the custom key. */
	private static final String DEFAULT_CUSTOM_KEY = "CHK@";

	/** The default changed name. */
	private static final String DEFAULT_CHANGED_NAME = null;

	/** The insert state. */
	private boolean insert;

	/** Whether to insert a redirect. */
	private boolean insertRedirect;

	/** The hash of the last insert. */
	private String lastInsertHash;

	/** The current hash of the file. */
	private String currentHash;

	/** The custom key. */
	private String customKey;

	/** The changed name. */
	private String changedName;

	/** The default MIME type. */
	private final String defaultMimeType;

	/** The current MIME type. */
	private String mimeType;

	/**
	 * Creates new file options.
	 *
	 * @param defaultMimeType
	 *            The default MIME type of the file
	 */
	public FileOption(String defaultMimeType) {
		insert = DEFAULT_INSERT;
		insertRedirect = DEFAULT_INSERT_REDIRECT;
		customKey = DEFAULT_CUSTOM_KEY;
		changedName = DEFAULT_CHANGED_NAME;
		this.defaultMimeType = defaultMimeType;
		mimeType = defaultMimeType;
	}

	/**
	 * Returns the custom key. The custom key is only used when
	 * {@link #isInsert()} and {@link #isInsertRedirect()} both return {@code
	 * true}.
	 *
	 * @return The custom key
	 */
	public String getCustomKey() {
		return customKey;
	}

	/**
	 * Sets the custom key. The custom key is only used when {@link #isInsert()}
	 * and {@link #isInsertRedirect()} both return {@code true}.
	 *
	 * @param customKey
	 *            The custom key
	 */
	public void setCustomKey(String customKey) {
		if (customKey == null) {
			this.customKey = "";
		} else {
			this.customKey = customKey;
		}
	}

	/**
	 * Returns whether the file should be inserted. If a file is not inserted
	 * and {@link #isInsertRedirect()} is also {@code false}, the file will not
	 * be inserted at all.
	 *
	 * @see #setCustomKey(String)
	 * @return <code>true</code> if the file should be inserted,
	 *         <code>false</code> otherwise
	 */
	public boolean isInsert() {
		return insert;
	}

	/**
	 * Sets whether the file should be inserted. If a file is not inserted and
	 * {@link #isInsertRedirect()} is also {@code false}, the file will not be
	 * inserted at all.
	 *
	 * @param insert
	 *            <code>true</code> if the file should be inserted,
	 *            <code>false</code> otherwise
	 */
	public void setInsert(boolean insert) {
		this.insert = insert;
	}

	/**
	 * Returns whether a redirect to a different key should be inserted. This
	 * will only matter if {@link #isInsert()} returns {@code false}. The key
	 * that should be redirected to still needs to be specified via
	 * {@link #setCustomKey(String)}.
	 *
	 * @return {@code true} if a redirect should be inserted, {@code false}
	 *         otherwise
	 */
	public boolean isInsertRedirect() {
		return insertRedirect;
	}

	/**
	 * Sets whether a redirect should be inserted. This will only matter if
	 * {@link #isInsert()} returns {@code false}, i.e. it has been
	 * {@link #setInsert(boolean)} to {@code false}. The key that should be
	 * redirected to still needs to be specified via
	 * {@link #setCustomKey(String)}.
	 *
	 * @param insertRedirect
	 *            {@code true} if a redirect should be inserted, {@code false}
	 *            otherwise
	 */
	public void setInsertRedirect(boolean insertRedirect) {
		this.insertRedirect = insertRedirect;
	}

	/**
	 * Returns the hash of the file when it was last inserted
	 *
	 * @return The last hash of the file
	 */
	public String getLastInsertHash() {
		return lastInsertHash;
	}

	/**
	 * Sets the hash of the file when it was last inserted.
	 *
	 * @param lastInsertHash
	 *            The last hash of the file
	 * @return These file options
	 */
	public FileOption setLastInsertHash(String lastInsertHash) {
		this.lastInsertHash = lastInsertHash;
		return this;
	}

	/**
	 * Returns the current hash of the file. This value is ony a temporary value
	 * that is copied to {@link #getLastInsertHash()} when a project has
	 * finished inserting.
	 *
	 * @see Project#copyHashes()
	 * @return The current hash of the file
	 */
	public String getCurrentHash() {
		return currentHash;
	}

	/**
	 * Sets the current hash of the file.
	 *
	 * @param currentHash
	 *            The current hash of the file
	 * @return These file options
	 */
	public FileOption setCurrentHash(String currentHash) {
		this.currentHash = currentHash;
		return this;
	}

	/**
	 * Returns whether this file has a changed name. Use
	 * {@link #getChangedName()} is this method returns {@code true}.
	 *
	 * @return {@code true} if this file has a changed name, {@code false}
	 *         otherwise
	 */
	public boolean hasChangedName() {
		return (changedName != null) && (changedName.length() > 0);
	}

	/**
	 * Returns the changed name for this file. This method will return {@code
	 * null} or an empty {@link String} if this file should not be renamed.
	 *
	 * @return The changed name, or {@code null} if this file should not be
	 *         renamed
	 */
	public String getChangedName() {
		return changedName;
	}

	/**
	 * Sets the changed name for this file. Setting the changed file to {@code
	 * null} or an empty {@link String} will disable renaming.
	 *
	 * @param changedName
	 *            The new changed name for this file
	 */
	public void setChangedName(String changedName) {
		this.changedName = changedName;
	}

	/**
	 * Sets the MIME type of the file. Setting the MIME type to
	 * <code>null</code> will set the MIME type to the default MIME type.
	 *
	 * @param mimeType
	 *            The MIME type of the file
	 */
	public void setMimeType(String mimeType) {
		if (mimeType == null) {
			this.mimeType = defaultMimeType;
		} else {
			this.mimeType = mimeType;
		}
	}

	/**
	 * Returns the MIME type of the file. If no custom MIME type has been set,
	 * the default MIME type is returned.
	 *
	 * @return The MIME type of the file
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Returns whether the options for this file have been modified, i.e. are
	 * not at their default values.
	 *
	 * @return <code>true</code> if the options have been modified,
	 *         <code>false</code> if they are at default values
	 */
	public boolean isCustom() {
		if (insert != DEFAULT_INSERT) {
			return true;
		}
		if (!customKey.equals(DEFAULT_CUSTOM_KEY)) {
			return true;
		}
		if (((changedName != null) && !changedName.equals(DEFAULT_CHANGED_NAME)) || ((DEFAULT_CHANGED_NAME != null) && !DEFAULT_CHANGED_NAME.equals(changedName))) {
			return true;
		}
		if (!defaultMimeType.equals(mimeType)) {
			return true;
		}
		if (insertRedirect != DEFAULT_INSERT_REDIRECT) {
			return true;
		}
		return false;
	}

}

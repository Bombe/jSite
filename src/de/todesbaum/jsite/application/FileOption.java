/*
 * jSite - a tool for uploading websites into Freenet
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

package de.todesbaum.jsite.application;

public class FileOption {

	private static final boolean DEFAULT_INSERT = true;
	private static final String DEFAULT_CUSTOM_KEY = "CHK@";
	private static final String DEFAULT_CONTAINER = "";
	private static final int DEFAULT_EDITION_RANGE = 3;
	private static final boolean DEFAULT_REPLACE_EDITION = false;

	private boolean insert;
	private String customKey;
	private final String defaultMimeType;
	private String mimeType;
	private String container;
	private int editionRange;
	private boolean replaceEdition;

	public FileOption(String defaultMimeType) {
		insert = DEFAULT_INSERT;
		customKey = DEFAULT_CUSTOM_KEY;
		this.defaultMimeType = defaultMimeType;
		mimeType = defaultMimeType;
		container = DEFAULT_CONTAINER;
		editionRange = DEFAULT_EDITION_RANGE;
		replaceEdition = DEFAULT_REPLACE_EDITION;
	}

	/**
	 * @return Returns the customKey.
	 */
	public String getCustomKey() {
		return customKey;
	}

	/**
	 * @param customKey
	 *            The customKey to set.
	 */
	public void setCustomKey(String customKey) {
		if (customKey == null) {
			customKey = "";
		}
		this.customKey = customKey;
	}

	/**
	 * @return Returns the insert.
	 */
	public boolean isInsert() {
		return insert;
	}

	/**
	 * @param insert
	 *            The insert to set.
	 */
	public void setInsert(boolean insert) {
		this.insert = insert;
	}

	public void setMimeType(String mimeType) {
		if (mimeType == null) {
			mimeType = defaultMimeType;
		}
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @return Returns the container.
	 */
	public String getContainer() {
		return container;
	}

	/**
	 * @param container
	 *            The container to set.
	 */
	public void setContainer(String container) {
		if (container == null) {
			container = DEFAULT_CONTAINER;
		}
		this.container = container;
	}

	public void setReplaceEdition(boolean replaceEdition) {
		this.replaceEdition = replaceEdition;
	}

	public boolean getReplaceEdition() {
		return replaceEdition;
	}

	public void setEditionRange(int editionRange) {
		this.editionRange = editionRange;
	}

	public int getEditionRange() {
		return editionRange;
	}

	public boolean isCustom() {
		if (insert != DEFAULT_INSERT) {
			return true;
		}
		if (!customKey.equals(DEFAULT_CUSTOM_KEY)) {
			return true;
		}
		if (!defaultMimeType.equals(mimeType)) {
			return true;
		}
		if (!DEFAULT_CONTAINER.equals(container)) {
			return true;
		}
		if (replaceEdition != DEFAULT_REPLACE_EDITION) {
			return true;
		}
		if (editionRange != DEFAULT_EDITION_RANGE) {
			return true;
		}
		return false;
	}

}
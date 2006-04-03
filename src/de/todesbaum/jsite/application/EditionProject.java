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

/**
 * Project extension for edition-based projects. In Freenet 0.7 this is
 * currently the only project type.
 * 
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id: EditionProject.java 417 2006-03-29 12:36:54Z bombe $
 */
public class EditionProject extends Project {

	/** The edition to insert to. */
	private int edition;

	/**
	 * Creates a new edition-based project.
	 */
	public EditionProject() {
	}

	/**
	 * Clones the specified project as an edition-based project.
	 * 
	 * @param project
	 *            The project to clone
	 */
	public EditionProject(Project project) {
		super(project);
		if (project instanceof EditionProject) {
			edition = ((EditionProject) project).edition;
		}
	}

	/**
	 * Returns the edition of the project.
	 * 
	 * @return The edition of the project
	 */
	public int getEdition() {
		return edition;
	}

	/**
	 * Sets the edition of the project.
	 * 
	 * @param edition
	 *            The edition to set
	 */
	public void setEdition(int edition) {
		this.edition = edition;
	}

	/**
	 * Constructs the final request URI including the edition number.
	 * 
	 * @return The final request URI
	 */
	@Override
	public String getFinalURI(int editionOffset) {
		return requestURI + path + "-" + (edition + editionOffset) + "/";
	}

}

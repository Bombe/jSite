/*
 * jSite - TWizardPage.java - Copyright © 2006–2012 David Roden
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

package de.todesbaum.util.swing;

import javax.swing.JPanel;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class TWizardPage extends JPanel {

	protected final TWizard wizard;
	protected String heading;
	protected String description;

	public TWizardPage(final TWizard wizard) {
		this.wizard = wizard;
	}

	public TWizardPage(final TWizard wizard, String heading) {
		this.wizard = wizard;
		this.heading = heading;
	}

	public TWizardPage(final TWizard wizard, String heading, String description) {
		this(wizard, heading);
		this.description = description;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the heading.
	 */
	public String getHeading() {
		return heading;
	}

	/**
	 * @param heading
	 *            The heading to set.
	 */
	public void setHeading(String heading) {
		this.heading = heading;
	}

	public void pageAdded(TWizard wizard) {
	}

	public void pageDeleted(TWizard wizard) {
	}

}

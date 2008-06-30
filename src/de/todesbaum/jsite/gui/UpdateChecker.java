/*
 * jSite-remote - UpdateChecker.java -
 * Copyright © 2008 David Roden
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

package de.todesbaum.jsite.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.todesbaum.jsite.application.Freenet7Interface;
import de.todesbaum.jsite.i18n.I18n;

/**
 * Checks for newer versions of jSite.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class UpdateChecker extends JDialog {

	/** The URL for update checks. */
	@SuppressWarnings("unused")
	private static final String UPDATE_KEY = "USK@e3myoFyp5avg6WYN16ImHri6J7Nj8980Fm~aQe4EX1U,QvbWT0ImE0TwLODTl7EoJx2NBnwDxTbLTE6zkB-eGPs,AQACAAE/jSite/0/currentVersion.txt";

	/** The freenet interface. */
	@SuppressWarnings("unused")
	private final Freenet7Interface freenetInterface;

	/** The cancel action. */
	@SuppressWarnings("unused")
	private Action cancelAction;

	/**
	 * Creates a new update checker that uses the given frame as its parent and
	 * communications via the given freenet interface.
	 *
	 * @param parent
	 *            The parent of the dialog
	 * @param freenetInterface
	 *            The freenet interface
	 */
	public UpdateChecker(JFrame parent, Freenet7Interface freenetInterface) {
		super(parent);
		this.freenetInterface = freenetInterface;
		initActions();
	}

	//
	// ACTIONS
	//

	/**
	 * Checks for updates, showing a dialog with an indeterminate progress bar.
	 */
	public void checkForUpdates() {
		/* TODO */
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Initializes all actions.
	 */
	private void initActions() {
		cancelAction = new AbstractAction(I18n.getMessage("")) {

			/**
			 * {@inheritDoc}
			 */
			public void actionPerformed(ActionEvent actionEvent) {
				/* TODO */
			}
		};
	}

	/**
	 * A panel that shows a busy progress bar and a “please wait” message.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	@SuppressWarnings("unused")
	private class BusyPanel extends JPanel {

		/* TODO */

	}

}

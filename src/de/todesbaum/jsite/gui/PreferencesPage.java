/*
 * jSite - PreferencesPage.java -
 * Copyright © 2009 David Roden
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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.jsite.i18n.I18nContainer;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;

/**
 * Page that shows some preferences that are valid for the complete application.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PreferencesPage extends TWizardPage {

	/** Select default temp directory action. */
	private Action selectDefaultTempDirectoryAction;

	/** Select custom temp directory action. */
	private Action selectCustomTempDirectoryAction;

	/** Action that chooses a new temp directory. */
	private Action chooseTempDirectoryAction;

	/** The text field containing the directory. */
	private JTextField tempDirectoryTextField;

	/** The temp directory. */
	private String tempDirectory;

	/** The “default” button. */
	private JRadioButton defaultTempDirectory;

	/** The “custom” button. */
	private JRadioButton customTempDirectory;

	/**
	 * Creates a new “preferences” page.
	 *
	 * @param wizard
	 *            The wizard this page belongs to
	 */
	public PreferencesPage(TWizard wizard) {
		super(wizard);
		pageInit();
		setHeading(I18n.getMessage("jsite.preferences.heading"));
		setDescription(I18n.getMessage("jsite.preferences.description"));
		I18nContainer.getInstance().registerRunnable(new Runnable() {

			/**
			 * {@inheritDoc}
			 */
			public void run() {
				setHeading(I18n.getMessage("jsite.preferences.heading"));
				setDescription(I18n.getMessage("jsite.preferences.description"));
			}
		});
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the temp directory.
	 *
	 * @return The temp directory, or {@code null} to use the default temp
	 *         directory
	 */
	public String getTempDirectory() {
		return tempDirectory;
	}

	/**
	 * Sets the temp directory.
	 *
	 * @param tempDirectory
	 *            The temp directory, or {@code null} to use the default temp
	 *            directory
	 */
	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
		tempDirectoryTextField.setText((tempDirectory != null) ? tempDirectory : "");
		if (tempDirectory != null) {
			customTempDirectory.setSelected(true);
			chooseTempDirectoryAction.setEnabled(true);
		} else {
			defaultTempDirectory.setSelected(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pageAdded(TWizard wizard) {
		super.pageAdded(wizard);
		this.wizard.setPreviousName(I18n.getMessage("jsite.menu.nodes.manage-nodes"));
		this.wizard.setNextName(I18n.getMessage("jsite.wizard.next"));
		this.wizard.setQuitName(I18n.getMessage("jsite.wizard.quit"));
		this.wizard.setNextEnabled(false);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Initializes this page.
	 */
	private void pageInit() {
		createActions();
		setLayout(new BorderLayout(12, 12));
		add(createPreferencesPanel(), BorderLayout.CENTER);
	}

	/**
	 * Creates all actions.
	 */
	private void createActions() {
		selectDefaultTempDirectoryAction = new AbstractAction(I18n.getMessage("jsite.preferences.temp-directory.default")) {

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				selectDefaultTempDirectory();
			}
		};
		selectCustomTempDirectoryAction = new AbstractAction(I18n.getMessage("jsite.preferences.temp-directory.custom")) {

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				selectCustomTempDirectory();
			}
		};
		chooseTempDirectoryAction = new AbstractAction(I18n.getMessage("jsite.preferences.temp-directory.choose")) {

			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent e) {
				chooseTempDirectory();
			}
		};

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@SuppressWarnings("synthetic-access")
			public void run() {
				selectDefaultTempDirectoryAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.temp-directory.default"));
				selectCustomTempDirectoryAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.temp-directory.custom"));
				chooseTempDirectoryAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.temp-directory.choose"));
			}
		});
	}

	/**
	 * Creates the panel containing all preferences.
	 *
	 * @return The preferences panel
	 */
	private JPanel createPreferencesPanel() {
		JPanel preferencesPanel = new JPanel(new BorderLayout(12, 12));

		JPanel tempDirectoryPanel = new JPanel(new GridBagLayout());
		preferencesPanel.add(tempDirectoryPanel, BorderLayout.CENTER);

		final JLabel tempDirectoryLabel = new JLabel("<html><b>" + I18n.getMessage("jsite.preferences.temp-directory") + "</b></html>");
		tempDirectoryPanel.add(tempDirectoryLabel, new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		defaultTempDirectory = new JRadioButton(selectDefaultTempDirectoryAction);
		tempDirectoryPanel.add(defaultTempDirectory, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(6, 18, 0, 0), 0, 0));

		customTempDirectory = new JRadioButton(selectCustomTempDirectoryAction);
		tempDirectoryPanel.add(customTempDirectory, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 18, 0, 0), 0, 0));

		ButtonGroup tempDirectoryButtonGroup = new ButtonGroup();
		defaultTempDirectory.getModel().setGroup(tempDirectoryButtonGroup);
		customTempDirectory.getModel().setGroup(tempDirectoryButtonGroup);

		tempDirectoryTextField = new JTextField();
		tempDirectoryTextField.setEditable(false);
		if (tempDirectory != null) {
			tempDirectoryTextField.setText(tempDirectory);
			customTempDirectory.setSelected(true);
		} else {
			defaultTempDirectory.setSelected(true);
		}
		chooseTempDirectoryAction.setEnabled(tempDirectory != null);
		tempDirectoryPanel.add(tempDirectoryTextField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 6, 0, 0), 0, 0));

		JButton chooseButton = new JButton(chooseTempDirectoryAction);
		tempDirectoryPanel.add(chooseButton, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(0, 6, 0, 0), 0, 0));

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			/**
			 * {@inheritDoc}
			 */
			public void run() {
				tempDirectoryLabel.setText("<html><b>" + I18n.getMessage("jsite.preferences.temp-directory") + "</b></html>");
			}
		});

		return preferencesPanel;
	}

	/**
	 * Activates the default temp directory radio button.
	 */
	private void selectDefaultTempDirectory() {
		tempDirectoryTextField.setEnabled(false);
		chooseTempDirectoryAction.setEnabled(false);
		tempDirectory = null;
	}

	/**
	 * Activates the custom temp directory radio button.
	 */
	private void selectCustomTempDirectory() {
		tempDirectoryTextField.setEnabled(true);
		chooseTempDirectoryAction.setEnabled(true);
		if (tempDirectoryTextField.getText().length() == 0) {
			chooseTempDirectory();
			if (tempDirectoryTextField.getText().length() == 0) {
				defaultTempDirectory.setSelected(true);
			}
		}
	}

	/**
	 * Lets the user choose a new temp directory.
	 */
	private void chooseTempDirectory() {
		JFileChooser fileChooser = new JFileChooser(tempDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnValue = fileChooser.showDialog(wizard, I18n.getMessage("jsite.preferences.temp-directory.choose.approve"));
		if (returnValue == JFileChooser.CANCEL_OPTION) {
			return;
		}
		tempDirectory = fileChooser.getSelectedFile().getPath();
		tempDirectoryTextField.setText(tempDirectory);
	}

}

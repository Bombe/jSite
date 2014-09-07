/*
 * jSite - PreferencesPage.java - Copyright © 2009–2014 David Roden
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
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.jsite.i18n.I18nContainer;
import de.todesbaum.jsite.main.ConfigurationLocator.ConfigurationLocation;
import de.todesbaum.util.freenet.fcp2.ClientPutDir.ManifestPutter;
import de.todesbaum.util.freenet.fcp2.PriorityClass;
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

	/** Action when selecting “next to JAR file.” */
	private Action nextToJarFileAction;

	/** Action when selecting “home directory.” */
	private Action homeDirectoryAction;

	/** Action when selecting “custom directory.” */
	private Action customDirectoryAction;

	/** Action when selecting “use early encode.” */
	private Action useEarlyEncodeAction;

	/** Action when a priority was selected. */
	private Action priorityAction;

	/** The text field containing the directory. */
	private JTextField tempDirectoryTextField;

	/** The temp directory. */
	private String tempDirectory;

	/** The configuration location. */
	private ConfigurationLocation configurationLocation;

	/** Whether to use “early encode.” */
	private boolean useEarlyEncode;

	/** The prioriy for inserts. */
	private PriorityClass priority;

	/** The “default” button. */
	private JRadioButton defaultTempDirectory;

	/** The “custom” button. */
	private JRadioButton customTempDirectory;

	/** The “next to JAR file” checkbox. */
	private JRadioButton nextToJarFile;

	/** The “home directory” checkbox. */
	private JRadioButton homeDirectory;

	/** The “custom directory” checkbox. */
	private JRadioButton customDirectory;

	/** The “use early encode” checkbox. */
	private JCheckBox useEarlyEncodeCheckBox;

	/** The insert priority select box. */
	private JComboBox insertPriorityComboBox;

	/** The manifest putter select box. */
	private JComboBox manifestPutterComboBox;

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
			@Override
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
	 * Returns the configuration location.
	 *
	 * @return The configuration location
	 */
	public ConfigurationLocation getConfigurationLocation() {
		return configurationLocation;
	}

	/**
	 * Sets the configuration location.
	 *
	 * @param configurationLocation
	 *            The configuration location
	 */
	public void setConfigurationLocation(ConfigurationLocation configurationLocation) {
		this.configurationLocation = configurationLocation;
		switch (configurationLocation) {
		case NEXT_TO_JAR_FILE:
			nextToJarFile.setSelected(true);
			break;
		case HOME_DIRECTORY:
			homeDirectory.setSelected(true);
			break;
		case CUSTOM:
			customDirectory.setSelected(true);
			break;
		}
	}

	/**
	 * Sets whether it is possible to select the “next to JAR file” option for
	 * the configuration location.
	 *
	 * @param nextToJarFile
	 *            {@code true} if the configuration file can be saved next to
	 *            the JAR file, {@code false} otherwise
	 */
	public void setHasNextToJarConfiguration(boolean nextToJarFile) {
		this.nextToJarFile.setEnabled(nextToJarFile);
	}

	/**
	 * Sets whether it is possible to select the “custom location” option for
	 * the configuration location.
	 *
	 * @param customDirectory
	 *            {@code true} if the configuration file can be saved to a
	 *            custom location, {@code false} otherwise
	 */
	public void setHasCustomConfiguration(boolean customDirectory) {
		this.customDirectory.setEnabled(customDirectory);
	}

	/**
	 * Returns whether to use the “early encode“ flag for the insert.
	 *
	 * @return {@code true} to set the “early encode” flag for the insert,
	 *         {@code false} otherwise
	 */
	public boolean useEarlyEncode() {
		return useEarlyEncode;
	}

	/**
	 * Sets whether to use the “early encode“ flag for the insert.
	 *
	 * @param useEarlyEncode
	 *            {@code true} to set the “early encode” flag for the insert,
	 *            {@code false} otherwise
	 */
	public void setUseEarlyEncode(boolean useEarlyEncode) {
		useEarlyEncodeCheckBox.setSelected(useEarlyEncode);
	}

	/**
	 * Returns the configured insert priority.
	 *
	 * @return The insert priority
	 */
	public PriorityClass getPriority() {
		return priority;
	}

	/**
	 * Sets the insert priority.
	 *
	 * @param priority
	 *            The insert priority
	 */
	public void setPriority(PriorityClass priority) {
		insertPriorityComboBox.setSelectedItem(priority);
	}

	/**
	 * Returns the selected manifest putter.
	 *
	 * @return The selected manifest putter
	 */
	public ManifestPutter getManifestPutter() {
		return (ManifestPutter) manifestPutterComboBox.getSelectedItem();
	}

	/**
	 * Sets the manifest putter.
	 *
	 * @param manifestPutter
	 *            The manifest putter
	 */
	public void setManifestPutter(ManifestPutter manifestPutter) {
		manifestPutterComboBox.setSelectedItem(manifestPutter);
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
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				selectDefaultTempDirectory();
			}
		};
		selectCustomTempDirectoryAction = new AbstractAction(I18n.getMessage("jsite.preferences.temp-directory.custom")) {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				selectCustomTempDirectory();
			}
		};
		chooseTempDirectoryAction = new AbstractAction(I18n.getMessage("jsite.preferences.temp-directory.choose")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent e) {
				chooseTempDirectory();
			}
		};
		nextToJarFileAction = new AbstractAction(I18n.getMessage("jsite.preferences.config-directory.jar")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionevent) {
				configurationLocation = ConfigurationLocation.NEXT_TO_JAR_FILE;
			}
		};
		homeDirectoryAction = new AbstractAction(I18n.getMessage("jsite.preferences.config-directory.home")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionevent) {
				configurationLocation = ConfigurationLocation.HOME_DIRECTORY;
			}
		};
		customDirectoryAction = new AbstractAction(I18n.getMessage("jsite.preferences.config-directory.custom")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				configurationLocation = ConfigurationLocation.CUSTOM;
			}
		};
		useEarlyEncodeAction = new AbstractAction(I18n.getMessage("jsite.preferences.insert-options.use-early-encode")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				useEarlyEncode = useEarlyEncodeCheckBox.isSelected();
			}
		};
		priorityAction = new AbstractAction(I18n.getMessage("jsite.preferences.insert-options.priority")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				priority = (PriorityClass) insertPriorityComboBox.getSelectedItem();
			}
		};

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				selectDefaultTempDirectoryAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.temp-directory.default"));
				selectCustomTempDirectoryAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.temp-directory.custom"));
				chooseTempDirectoryAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.temp-directory.choose"));
				nextToJarFileAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.config-directory.jar"));
				homeDirectoryAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.config-directory.home"));
				customDirectoryAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.config-directory.custom"));
				useEarlyEncodeAction.putValue(Action.NAME, I18n.getMessage("jsite.preferences.insert-options.use-early-encode"));
			}
		});
	}

	/**
	 * Creates the panel containing all preferences.
	 *
	 * @return The preferences panel
	 */
	private JPanel createPreferencesPanel() {
		JPanel preferencesPanel = new JPanel(new GridBagLayout());
		preferencesPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		final JLabel tempDirectoryLabel = new JLabel("<html><b>" + I18n.getMessage("jsite.preferences.temp-directory") + "</b></html>");
		preferencesPanel.add(tempDirectoryLabel, new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		defaultTempDirectory = new JRadioButton(selectDefaultTempDirectoryAction);
		preferencesPanel.add(defaultTempDirectory, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(6, 18, 0, 0), 0, 0));

		customTempDirectory = new JRadioButton(selectCustomTempDirectoryAction);
		preferencesPanel.add(customTempDirectory, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 18, 0, 0), 0, 0));

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
		preferencesPanel.add(tempDirectoryTextField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 6, 0, 0), 0, 0));

		JButton chooseButton = new JButton(chooseTempDirectoryAction);
		preferencesPanel.add(chooseButton, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(0, 6, 0, 0), 0, 0));

		final JLabel configurationDirectoryLabel = new JLabel("<html><b>" + I18n.getMessage("jsite.preferences.config-directory") + "</b></html>");
		preferencesPanel.add(configurationDirectoryLabel, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(12, 0, 0, 0), 0, 0));

		nextToJarFile = new JRadioButton(nextToJarFileAction);
		preferencesPanel.add(nextToJarFile, new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(6, 18, 0, 0), 0, 0));

		homeDirectory = new JRadioButton(homeDirectoryAction);
		preferencesPanel.add(homeDirectory, new GridBagConstraints(0, 5, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 18, 0, 0), 0, 0));

		customDirectory = new JRadioButton(customDirectoryAction);
		preferencesPanel.add(customDirectory, new GridBagConstraints(0, 6, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 18, 0, 0), 0, 0));

		ButtonGroup configurationDirectoryButtonGroup = new ButtonGroup();
		configurationDirectoryButtonGroup.add(nextToJarFile);
		configurationDirectoryButtonGroup.add(homeDirectory);
		configurationDirectoryButtonGroup.add(customDirectory);

		final JLabel insertOptionsLabel = new JLabel("<html><b>" + I18n.getMessage("jsite.preferences.insert-options") + "</b></html>");
		preferencesPanel.add(insertOptionsLabel, new GridBagConstraints(0, 7, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(12, 0, 0, 0), 0, 0));

		useEarlyEncodeCheckBox = new JCheckBox(useEarlyEncodeAction);
		preferencesPanel.add(useEarlyEncodeCheckBox, new GridBagConstraints(0, 8, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));

		final JLabel insertPriorityLabel = new JLabel(I18n.getMessage("jsite.preferences.insert-options.priority"));
		preferencesPanel.add(insertPriorityLabel, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));

		insertPriorityComboBox = new JComboBox(new PriorityClass[] { PriorityClass.MINIMUM, PriorityClass.PREFETCH, PriorityClass.BULK, PriorityClass.UPDATABLE, PriorityClass.SEMI_INTERACTIVE, PriorityClass.INTERACTIVE, PriorityClass.MAXIMUM });
		insertPriorityComboBox.setAction(priorityAction);
		preferencesPanel.add(insertPriorityComboBox, new GridBagConstraints(1, 9, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 18, 0, 0), 0, 0));

		final JLabel manifestPutterLabel = new JLabel(I18n.getMessage("jsite.preferences.insert-options.manifest-putter"));
		preferencesPanel.add(manifestPutterLabel, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));

		manifestPutterComboBox = new JComboBox(ManifestPutter.values());
		preferencesPanel.add(manifestPutterComboBox, new GridBagConstraints(1, 10, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 18, 0, 0), 0, 0));

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				tempDirectoryLabel.setText("<html><b>" + I18n.getMessage("jsite.preferences.temp-directory") + "</b></html>");
				configurationDirectoryLabel.setText("<html><b>" + I18n.getMessage("jsite.preferences.config-directory") + "</b></html>");
				insertOptionsLabel.setText("<html><b>" + I18n.getMessage("jsite.preferences.insert-options") + "</b></html>");
				insertPriorityLabel.setText(I18n.getMessage("jsite.preferences.insert-options.priority"));
				manifestPutterLabel.setText(I18n.getMessage("jsite.preferences.insert-options.manifest-putter"));
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

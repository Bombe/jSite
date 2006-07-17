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

package de.todesbaum.jsite.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import de.todesbaum.jsite.application.EditionProject;
import de.todesbaum.jsite.application.FileOption;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.util.mime.DefaultMIMETypes;
import de.todesbaum.util.swing.TLabel;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class ProjectFilesPage extends TWizardPage implements ActionListener, ListSelectionListener, DocumentListener, FileScannerListener, ChangeListener {

	protected TWizard wizard;

	protected Project project;

	private Action scanAction;
	private Action editContainerAction;
	private Action addContainerAction;
	private Action deleteContainerAction;

	protected JList projectFileList;
	private JCheckBox defaultFileCheckBox;
	private JCheckBox fileOptionsInsertCheckBox;
	private JTextField fileOptionsCustomKeyTextField;
	private JComboBox fileOptionsMIMETypeComboBox;
	protected DefaultComboBoxModel containerComboBoxModel;
	private JComboBox fileOptionsContainerComboBox;
	private JSpinner replaceEditionRangeSpinner;
	private JCheckBox replacementCheckBox;

	public ProjectFilesPage() {
		super();
		pageInit();
	}

	private void pageInit() {
		createActions();
		setLayout(new BorderLayout(12, 12));
		add(createProjectFilesPanel(), BorderLayout.CENTER);
	}

	private void createActions() {
		scanAction = new AbstractAction(I18n.getMessage("jsite.project-files.action.rescan")) {

			public void actionPerformed(ActionEvent actionEvent) {
				actionScan();
			}
		};
		scanAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		scanAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.rescan.tooltip"));

		addContainerAction = new AbstractAction(I18n.getMessage("jsite.project-files.action.add-container")) {

			public void actionPerformed(ActionEvent actionEvent) {
				actionAddContainer();
			}
		};
		addContainerAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.add-container.tooltip"));
		addContainerAction.setEnabled(false);

		editContainerAction = new AbstractAction(I18n.getMessage("jsite.project-files.action.edit-container")) {

			public void actionPerformed(ActionEvent actionEvent) {
				actionEditContainer();
			}
		};
		editContainerAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.edit-container.tooltip"));
		editContainerAction.setEnabled(false);

		deleteContainerAction = new AbstractAction(I18n.getMessage("jsite.project-files.action.delete-container")) {

			public void actionPerformed(ActionEvent actionEvent) {
				actionDeleteContainer();
			}
		};
		deleteContainerAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.delete-container.tooltip"));
		deleteContainerAction.setEnabled(false);
	}

	@Override
	public void pageAdded(TWizard wizard) {
		this.wizard = wizard;
		actionScan();
	}

	private JComponent createProjectFilesPanel() {
		JPanel projectFilesPanel = new JPanel(new BorderLayout(12, 12));

		projectFileList = new JList();
		projectFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		projectFileList.setMinimumSize(new Dimension(250, projectFileList.getPreferredSize().height));
		projectFileList.addListSelectionListener(this);

		projectFilesPanel.add(new JScrollPane(projectFileList), BorderLayout.CENTER);

		JPanel fileOptionsAlignmentPanel = new JPanel(new BorderLayout(12, 12));
		projectFilesPanel.add(fileOptionsAlignmentPanel, BorderLayout.PAGE_END);
		JPanel fileOptionsPanel = new JPanel(new GridBagLayout());
		fileOptionsAlignmentPanel.add(fileOptionsPanel, BorderLayout.PAGE_START);

		fileOptionsPanel.add(new JButton(scanAction), new GridBagConstraints(0, 0, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		fileOptionsPanel.add(new JLabel("<html><b>" + I18n.getMessage("jsite.project-files.file-options") + "</b></html>"), new GridBagConstraints(0, 1, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));

		defaultFileCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.default"));
		defaultFileCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.default.tooltip"));
		defaultFileCheckBox.setName("default-file");
		defaultFileCheckBox.addActionListener(this);
		defaultFileCheckBox.setEnabled(false);

		fileOptionsPanel.add(defaultFileCheckBox, new GridBagConstraints(0, 2, 5, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));

		fileOptionsInsertCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.insert"), true);
		fileOptionsInsertCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.insert.tooltip"));
		fileOptionsInsertCheckBox.setName("insert");
		fileOptionsInsertCheckBox.setMnemonic(KeyEvent.VK_I);
		fileOptionsInsertCheckBox.addActionListener(this);
		fileOptionsInsertCheckBox.setEnabled(false);

		fileOptionsPanel.add(fileOptionsInsertCheckBox, new GridBagConstraints(0, 3, 5, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));

		fileOptionsCustomKeyTextField = new JTextField(45);
		fileOptionsCustomKeyTextField.setToolTipText(I18n.getMessage("jsite.project-files.custom-key.tooltip"));
		fileOptionsCustomKeyTextField.setEnabled(false);
		fileOptionsCustomKeyTextField.getDocument().addDocumentListener(this);

		fileOptionsPanel.add(new TLabel(I18n.getMessage("jsite.project-files.custom-key"), KeyEvent.VK_K, fileOptionsCustomKeyTextField), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		fileOptionsPanel.add(fileOptionsCustomKeyTextField, new GridBagConstraints(1, 4, 4, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		fileOptionsMIMETypeComboBox = new JComboBox(DefaultMIMETypes.getAllMIMETypes());
		fileOptionsMIMETypeComboBox.setToolTipText(I18n.getMessage("jsite.project-files.mime-type.tooltip"));
		fileOptionsMIMETypeComboBox.setName("project-files.mime-type");
		fileOptionsMIMETypeComboBox.addActionListener(this);
		fileOptionsMIMETypeComboBox.setEnabled(false);

		fileOptionsPanel.add(new TLabel(I18n.getMessage("jsite.project-files.mime-type"), KeyEvent.VK_M, fileOptionsMIMETypeComboBox), new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		fileOptionsPanel.add(fileOptionsMIMETypeComboBox, new GridBagConstraints(1, 5, 4, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		containerComboBoxModel = new DefaultComboBoxModel();
		fileOptionsContainerComboBox = new JComboBox(containerComboBoxModel);
		fileOptionsContainerComboBox.setToolTipText(I18n.getMessage("jsite.project-files.container.tooltip"));
		fileOptionsContainerComboBox.setName("project-files.container");
		fileOptionsContainerComboBox.addActionListener(this);
		fileOptionsContainerComboBox.setEnabled(false);

		fileOptionsPanel.add(new TLabel(I18n.getMessage("jsite.project-files.container"), KeyEvent.VK_C, fileOptionsContainerComboBox), new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		fileOptionsPanel.add(fileOptionsContainerComboBox, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		fileOptionsPanel.add(new JButton(addContainerAction), new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		fileOptionsPanel.add(new JButton(editContainerAction), new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		fileOptionsPanel.add(new JButton(deleteContainerAction), new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		JPanel fileOptionsReplacementPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
		fileOptionsReplacementPanel.setBorder(new EmptyBorder(-6, -6, -6, -6));

		replacementCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.replacement"));
		replacementCheckBox.setName("project-files.replace-edition");
		replacementCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.replacement.tooltip"));
		replacementCheckBox.addActionListener(this);
		replacementCheckBox.setEnabled(false);
		fileOptionsReplacementPanel.add(replacementCheckBox);

		replaceEditionRangeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
		replaceEditionRangeSpinner.setName("project-files.replace-edition-range");
		replaceEditionRangeSpinner.setToolTipText(I18n.getMessage("jsite.project-files.replacement.edition-range.tooltip"));
		replaceEditionRangeSpinner.addChangeListener(this);
		replaceEditionRangeSpinner.setEnabled(false);
		fileOptionsReplacementPanel.add(new JLabel(I18n.getMessage("jsite.project-files.replacement.edition-range")));
		fileOptionsReplacementPanel.add(replaceEditionRangeSpinner);

		fileOptionsPanel.add(fileOptionsReplacementPanel, new GridBagConstraints(0, 7, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));

		return projectFilesPanel;
	}

	public void setProject(Project project) {
		this.project = project;
		setHeading(MessageFormat.format(I18n.getMessage("jsite.project-files.heading"), project.getName()));
		setDescription(I18n.getMessage("jsite.project-files.description"));
	}

	private List<String> getProjectFiles() {
		List<String> files = new ArrayList<String>();
		for (int index = 0, size = projectFileList.getModel().getSize(); index < size; index++) {
			files.add((String) projectFileList.getModel().getElementAt(index));
		}
		return files;
	}

	protected void rebuildContainerComboBox() {
		/* scan files for containers */
		List<String> files = getProjectFiles();
		List<String> containers = new ArrayList<String>(); // ComboBoxModel
		// sucks. No
		// contains()!
		containers.add("");
		for (String filename: files) {
			String container = project.getFileOption(filename).getContainer();
			if (!containers.contains(container)) {
				containers.add(container);
			}
		}
		Collections.sort(containers);
		containerComboBoxModel.removeAllElements();
		for (String container: containers) {
			containerComboBoxModel.addElement(container);
		}
	}

	//
	// ACTIONS
	//

	protected void actionScan() {
		projectFileList.clearSelection();
		projectFileList.setListData(new Object[0]);

		wizard.setNextEnabled(false);
		wizard.setPreviousEnabled(false);
		wizard.setQuitEnabled(false);

		FileScanner fileScanner = new FileScanner(project);
		fileScanner.addFileScannerListener(this);
		new Thread(fileScanner).start();
	}

	protected void actionAddContainer() {
		String containerName = JOptionPane.showInputDialog(wizard, I18n.getMessage("jsite.project-files.action.add-container.message") + ":", null, JOptionPane.INFORMATION_MESSAGE);
		if (containerName == null) {
			return;
		}
		containerName = containerName.trim();
		String filename = (String) projectFileList.getSelectedValue();
		FileOption fileOption = project.getFileOption(filename);
		fileOption.setContainer(containerName);
		rebuildContainerComboBox();
		fileOptionsContainerComboBox.setSelectedItem(containerName);
	}

	protected void actionEditContainer() {
		String selectedFilename = (String) projectFileList.getSelectedValue();
		FileOption fileOption = project.getFileOption(selectedFilename);
		String oldContainerName = fileOption.getContainer();
		String containerName = JOptionPane.showInputDialog(wizard, I18n.getMessage("jsite.project-files.action.edit-container.message") + ":", oldContainerName);
		if (containerName == null) {
			return;
		}
		if (containerName.equals("")) {
			fileOption.setContainer("");
			fileOptionsContainerComboBox.setSelectedItem("");
			return;
		}
		List<String> files = getProjectFiles();
		for (String filename: files) {
			fileOption = project.getFileOption(filename);
			if (fileOption.getContainer().equals(oldContainerName)) {
				fileOption.setContainer(containerName);
			}
		}
		rebuildContainerComboBox();
		fileOptionsContainerComboBox.setSelectedItem(containerName);
	}

	protected void actionDeleteContainer() {
		if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.project-files.action.delete-container.message"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
			String containerName = (String) fileOptionsContainerComboBox.getSelectedItem();
			List<String> files = getProjectFiles();
			for (String filename: files) {
				FileOption fileOption = project.getFileOption(filename);
				if (fileOption.getContainer().equals(containerName)) {
					fileOption.setContainer("");
				}
			}
			fileOptionsContainerComboBox.setSelectedItem("");
		}
	}

	public void fileScannerFinished(FileScanner fileScanner) {
		final boolean error = fileScanner.isError();
		if (!error) {
			final List<String> files = fileScanner.getFiles();
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					projectFileList.setListData(files.toArray(new String[files.size()]));
					projectFileList.clearSelection();
					rebuildContainerComboBox();
				}
			});
			Iterator<String> filenames = project.getFileOptions().keySet().iterator();
			while (filenames.hasNext()) {
				String filename = filenames.next();
				if (!files.contains(filename)) {
					project.setFileOption(filename, null);
				}
			}
		} else {
			JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project-files.scan-error"), null, JOptionPane.ERROR_MESSAGE);
		}
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				wizard.setPreviousEnabled(true);
				wizard.setNextEnabled(!error);
				wizard.setQuitEnabled(true);
			}
		});
	}

	//
	// INTERFACE ActionListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent actionEvent) {
		String filename = (String) projectFileList.getSelectedValue();
		if (filename == null) {
			return;
		}
		FileOption fileOption = project.getFileOption(filename);
		Object source = actionEvent.getSource();
		if (source instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox) source;
			if ("default-file".equals(checkBox.getName())) {
				if (checkBox.isSelected()) {
					project.setIndexFile(filename);
				} else {
					project.setIndexFile(null);
				}
			} else if ("insert".equals(checkBox.getName())) {
				boolean isInsert = checkBox.isSelected();
				fileOptionsCustomKeyTextField.setEnabled(!isInsert);
				fileOption.setInsert(isInsert);
				if (!isInsert) {
					fileOptionsContainerComboBox.setSelectedItem("");
				}
			} else if ("project-files.replace-edition".equals(checkBox.getName())) {
				boolean replaceEdition = checkBox.isSelected();
				fileOption.setReplaceEdition(replaceEdition);
				replaceEditionRangeSpinner.setEnabled(replaceEdition);
			}
		} else if (source instanceof JComboBox) {
			JComboBox comboBox = (JComboBox) source;
			if ("project-files.mime-type".equals(comboBox.getName())) {
				fileOption.setMimeType((String) comboBox.getSelectedItem());
			} else if ("project-files.container".equals(comboBox.getName())) {
				String containerName = (String) comboBox.getSelectedItem();
				fileOption.setContainer(containerName);
				boolean enabled = !"".equals(containerName);
				editContainerAction.setEnabled(enabled);
				deleteContainerAction.setEnabled(enabled);
				if (enabled) {
					fileOptionsInsertCheckBox.setSelected(true);
				}
			}
		}
	}

	//
	// INTERFACE ListSelectionListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void valueChanged(ListSelectionEvent e) {
		String filename = (String) projectFileList.getSelectedValue();
		boolean enabled = filename != null;
		boolean insert = fileOptionsInsertCheckBox.isSelected();
		defaultFileCheckBox.setEnabled(enabled);
		fileOptionsInsertCheckBox.setEnabled(enabled);
		fileOptionsCustomKeyTextField.setEnabled(enabled && !insert);
		fileOptionsMIMETypeComboBox.setEnabled(enabled);
		fileOptionsContainerComboBox.setEnabled(enabled);
		addContainerAction.setEnabled(enabled);
		editContainerAction.setEnabled(enabled);
		deleteContainerAction.setEnabled(enabled);
		replacementCheckBox.setEnabled(enabled && insert && (project instanceof EditionProject));
		if (filename != null) {
			FileOption fileOption = project.getFileOption(filename);
			defaultFileCheckBox.setSelected(filename.equals(project.getIndexFile()));
			fileOptionsInsertCheckBox.setSelected(fileOption.isInsert());
			fileOptionsCustomKeyTextField.setText(fileOption.getCustomKey());
			fileOptionsMIMETypeComboBox.getModel().setSelectedItem(fileOption.getMimeType());
			fileOptionsContainerComboBox.setSelectedItem(fileOption.getContainer());
			replacementCheckBox.setSelected(fileOption.getReplaceEdition());
			replaceEditionRangeSpinner.setValue(fileOption.getEditionRange());
			replaceEditionRangeSpinner.setEnabled(fileOption.getReplaceEdition());
		} else {
			defaultFileCheckBox.setSelected(false);
			fileOptionsInsertCheckBox.setSelected(true);
			fileOptionsCustomKeyTextField.setText("CHK@");
			fileOptionsMIMETypeComboBox.getModel().setSelectedItem(DefaultMIMETypes.DEFAULT_MIME_TYPE);
			fileOptionsContainerComboBox.setSelectedItem("");
			replacementCheckBox.setSelected(false);
			replaceEditionRangeSpinner.setValue(0);
		}
	}

	//
	// INTERFACE DocumentListener
	//

	private void processDocumentUpdate(DocumentEvent documentEvent) {
		String filename = (String) projectFileList.getSelectedValue();
		if (filename == null) {
			return;
		}
		FileOption fileOption = project.getFileOption(filename);
		Document document = documentEvent.getDocument();
		try {
			String text = document.getText(0, document.getLength());
			fileOption.setCustomKey(text);
		} catch (BadLocationException ble1) {
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void changedUpdate(DocumentEvent documentEvent) {
		processDocumentUpdate(documentEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void insertUpdate(DocumentEvent documentEvent) {
		processDocumentUpdate(documentEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeUpdate(DocumentEvent documentEvent) {
		processDocumentUpdate(documentEvent);
	}

	//
	// INTERFACE ChangeListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void stateChanged(ChangeEvent changeEvent) {
		String filename = (String) projectFileList.getSelectedValue();
		if (filename == null) {
			return;
		}
		FileOption fileOption = project.getFileOption(filename);
		Object source = changeEvent.getSource();
		if (source instanceof JSpinner) {
			JSpinner spinner = (JSpinner) source;
			fileOption.setEditionRange((Integer) spinner.getValue());
		}
	}

}

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
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.NumberEditor;
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
import de.todesbaum.jsite.application.Freenet7Interface;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.util.swing.SortedListModel;
import de.todesbaum.util.swing.TLabel;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class ProjectPage extends TWizardPage implements ListSelectionListener, ChangeListener, DocumentListener {

	private Freenet7Interface freenetInterface;

	private Action projectLocalPathBrowseAction;
	private Action projectAddAction;
	private Action projectDeleteAction;
	private Action projectCloneAction;

	private JFileChooser pathChooser;
	private SortedListModel projectListModel;
	private JList projectList;
	private JTextField projectNameTextField;
	private JTextField projectDescriptionTextField;
	private JTextField projectLocalPathTextField;
	private JTextField projectPublicKeyTextField;
	private JTextField projectPrivateKeyTextField;
	private JTextField projectPathTextField;

	public ProjectPage() {
		super();
		setLayout(new BorderLayout(12, 12));
		dialogInit();
		setHeading(I18n.getMessage("jsite.project.heading"));
		setDescription(I18n.getMessage("jsite.project.description"));
	}

	protected void dialogInit() {
		createActions();

		pathChooser = new JFileChooser();
		projectListModel = new SortedListModel();
		projectList = new JList(projectListModel);
		projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		projectList.addListSelectionListener(this);
		projectList.setPreferredSize(new Dimension(150, projectList.getPreferredSize().height));

		add(new JScrollPane(projectList), BorderLayout.LINE_START);
		add(createInformationPanel(), BorderLayout.CENTER);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pageAdded(TWizard wizard) {
		super.pageAdded(wizard);
		projectList.clearSelection();
		wizard.setNextEnabled(false);
	}

	/**
	 */
	public void addListSelectionListener(ListSelectionListener listener) {
		projectList.addListSelectionListener(listener);
	}

	/**
	 */
	public void removeListSelectionListener(ListSelectionListener listener) {
		projectList.removeListSelectionListener(listener);
	}

	private void createActions() {
		projectLocalPathBrowseAction = new AbstractAction(I18n.getMessage("jsite.project.action.browse")) {

			public void actionPerformed(ActionEvent actionEvent) {
				actionLocalPathBrowse();
			}
		};
		projectLocalPathBrowseAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project.action.browse.tooltip"));
		projectLocalPathBrowseAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);
		projectLocalPathBrowseAction.setEnabled(false);

		projectAddAction = new AbstractAction(I18n.getMessage("jsite.project.action.add-project")) {

			public void actionPerformed(ActionEvent actionEvent) {
				actionAdd();
			}
		};
		projectAddAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project.action.add-project.tooltip"));
		projectAddAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);

		projectDeleteAction = new AbstractAction(I18n.getMessage("jsite.project.action.delete-project")) {

			public void actionPerformed(ActionEvent actionEvent) {
				actionDelete();
			}
		};
		projectDeleteAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project.action.delete-project.tooltip"));
		projectDeleteAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
		projectDeleteAction.setEnabled(false);

		projectCloneAction = new AbstractAction(I18n.getMessage("jsite.project.action.clone-project")) {

			public void actionPerformed(ActionEvent actionEvent) {
				actionClone();
			}
		};
		projectCloneAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project.action.clone-project.tooltip"));
		projectCloneAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		projectCloneAction.setEnabled(false);
	}

	private JComponent createInformationPanel() {
		JPanel informationPanel = new JPanel(new BorderLayout(12, 12));

		JPanel informationTable = new JPanel(new GridBagLayout());

		JPanel functionButtons = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 12));
		functionButtons.setBorder(new EmptyBorder(-12, -12, -12, -12));
		functionButtons.add(new JButton(projectAddAction));
		functionButtons.add(new JButton(projectDeleteAction));
		functionButtons.add(new JButton(projectCloneAction));

		informationPanel.add(functionButtons, BorderLayout.PAGE_START);
		informationPanel.add(informationTable, BorderLayout.CENTER);

		informationTable.add(new JLabel("<html><b>" + I18n.getMessage("jsite.project.project.information") + "</b></html>"), new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		projectNameTextField = new JTextField();
		projectNameTextField.getDocument().putProperty("name", "project.name");
		projectNameTextField.getDocument().addDocumentListener(this);
		projectNameTextField.setEnabled(false);

		informationTable.add(new TLabel(I18n.getMessage("jsite.project.project.name") + ":", KeyEvent.VK_N, projectNameTextField), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		informationTable.add(projectNameTextField, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		projectDescriptionTextField = new JTextField();
		projectDescriptionTextField.getDocument().putProperty("name", "project.description");
		projectDescriptionTextField.getDocument().addDocumentListener(this);
		projectDescriptionTextField.setEnabled(false);

		informationTable.add(new TLabel(I18n.getMessage("jsite.project.project.description") + ":", KeyEvent.VK_D, projectDescriptionTextField), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		informationTable.add(projectDescriptionTextField, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		projectLocalPathTextField = new JTextField();
		projectLocalPathTextField.getDocument().putProperty("name", "project.localpath");
		projectLocalPathTextField.getDocument().addDocumentListener(this);
		projectLocalPathTextField.setEnabled(false);

		informationTable.add(new TLabel(I18n.getMessage("jsite.project.project.local-path") + ":", KeyEvent.VK_L, projectLocalPathTextField), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		informationTable.add(projectLocalPathTextField, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		informationTable.add(new JButton(projectLocalPathBrowseAction), new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		informationTable.add(new JLabel("<html><b>" + I18n.getMessage("jsite.project.project.address") + "</b></html>"), new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(12, 0, 0, 0), 0, 0));

		projectPublicKeyTextField = new JTextField(27);
		projectPublicKeyTextField.getDocument().putProperty("name", "project.publickey");
		projectPublicKeyTextField.getDocument().addDocumentListener(this);
		projectPublicKeyTextField.setEnabled(false);

		informationTable.add(new TLabel(I18n.getMessage("jsite.project.project.public-key") + ":", KeyEvent.VK_U, projectPublicKeyTextField), new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		informationTable.add(projectPublicKeyTextField, new GridBagConstraints(1, 5, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		projectPrivateKeyTextField = new JTextField(27);
		projectPrivateKeyTextField.getDocument().putProperty("name", "project.privatekey");
		projectPrivateKeyTextField.getDocument().addDocumentListener(this);
		projectPrivateKeyTextField.setEnabled(false);

		informationTable.add(new TLabel(I18n.getMessage("jsite.project.project.private-key") + ":", KeyEvent.VK_R, projectPrivateKeyTextField), new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		informationTable.add(projectPrivateKeyTextField, new GridBagConstraints(1, 6, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		projectPathTextField = new JTextField();
		projectPathTextField.getDocument().putProperty("name", "project.path");
		projectPathTextField.getDocument().addDocumentListener(this);
		projectPathTextField.setEnabled(false);

		informationTable.add(new TLabel(I18n.getMessage("jsite.project.project.path") + ":", KeyEvent.VK_P, projectPathTextField), new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		informationTable.add(projectPathTextField, new GridBagConstraints(1, 7, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		return informationPanel;
	}

	public void setProjects(Project[] projects) {
		projectListModel.clear();
		for (Project project: projects) {
			projectListModel.add(project);
		}
	}

	public Project[] getProjects() {
		return (Project[]) projectListModel.toArray(new Project[projectListModel.size()]);
	}

	/**
	 * @param freenetInterface
	 *            The freenetInterface to set.
	 */
	public void setFreenetInterface(Freenet7Interface freenetInterface) {
		this.freenetInterface = freenetInterface;
	}

	public Project getSelectedProject() {
		return (Project) projectList.getSelectedValue();
	}

	private void setTextField(DocumentEvent documentEvent) {
		Document document = documentEvent.getDocument();
		String propertyName = (String) document.getProperty("name");
		Project project = (Project) projectList.getSelectedValue();
		if (project == null) {
			return;
		}
		try {
			String text = document.getText(0, document.getLength()).trim();
			if ("project.name".equals(propertyName)) {
				project.setName(text);
				projectList.repaint();
			} else if ("project.description".equals(propertyName)) {
				project.setDescription(text);
			} else if ("project.localpath".equals(propertyName)) {
				project.setLocalPath(text);
			} else if ("project.privatekey".equals(propertyName)) {
				project.setInsertURI(text);
			} else if ("project.publickey".equals(propertyName)) {
				project.setRequestURI(text);
			} else if ("project.path".equals(propertyName)) {
				project.setPath(text);
			}
		} catch (BadLocationException e) {
		}
	}

	//
	// ACTIONS
	//

	protected void actionLocalPathBrowse() {
		Project project = (Project) projectList.getSelectedValue();
		if (project == null) {
			return;
		}
		pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (pathChooser.showDialog(this, I18n.getMessage("jsite.project.action.browse.choose")) == JFileChooser.APPROVE_OPTION) {
			projectLocalPathTextField.setText(pathChooser.getSelectedFile().getPath());
		}
	}

	protected void actionAdd() {
		String[] keyPair = null;
		if (!freenetInterface.hasNode()) {
			JOptionPane.showMessageDialog(this, I18n.getMessage("jsite.project-files.no-node-selected"), null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			keyPair = freenetInterface.generateKeyPair();
		} catch (IOException ioe1) {
			JOptionPane.showMessageDialog(this, MessageFormat.format(I18n.getMessage("jsite.project.keygen.io-error"), ioe1.getMessage()), null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		EditionProject newProject = new EditionProject();
		newProject.setName(I18n.getMessage("jsite.project.new-project.name"));
		newProject.setInsertURI(keyPair[0]);
		newProject.setRequestURI(keyPair[1]);
		newProject.setEdition(1);
		projectListModel.add(newProject);
		projectList.setSelectedIndex(projectListModel.size() - 1);
	}

	protected void actionDelete() {
		int selectedIndex = projectList.getSelectedIndex();
		if (selectedIndex > -1) {
			if (JOptionPane.showConfirmDialog(this, MessageFormat.format(I18n.getMessage("jsite.project.action.delete-project.confirm"), ((Project) projectList.getSelectedValue()).getName()), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
				projectListModel.remove(selectedIndex);
				projectList.clearSelection();
				if (projectListModel.getSize() != 0) {
					projectList.setSelectedIndex(Math.min(selectedIndex, projectListModel.getSize() - 1));
				}
			}
		}
	}

	protected void actionClone() {
		int selectedIndex = projectList.getSelectedIndex();
		if (selectedIndex > -1) {
			Project newProject = null;
			Project selectedProject = (Project) projectList.getSelectedValue();
			if (selectedProject instanceof EditionProject) {
				newProject = new EditionProject(selectedProject);
			} // else { /* BUG! */ }
			newProject.setName(MessageFormat.format(I18n.getMessage("jsite.project.action.clone-project.copy"), newProject.getName()));
			projectListModel.add(newProject);
			projectList.setSelectedIndex(projectListModel.indexOf(newProject));
		}
	}

	//
	// INTERFACE ListSelectionListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void valueChanged(ListSelectionEvent listSelectionEvent) {
		int selectedRow = projectList.getSelectedIndex();
		Project selectedProject = (Project) projectList.getSelectedValue();
		projectNameTextField.setEnabled(selectedRow > -1);
		projectDescriptionTextField.setEnabled(selectedRow > -1);
		projectLocalPathTextField.setEnabled(selectedRow > -1);
		projectPublicKeyTextField.setEnabled(selectedRow > -1);
		projectPrivateKeyTextField.setEnabled(selectedRow > -1);
		projectPathTextField.setEnabled(selectedRow > -1);
		projectLocalPathBrowseAction.setEnabled(selectedRow > -1);
		projectDeleteAction.setEnabled(selectedRow > -1);
		projectCloneAction.setEnabled(selectedRow > -1);
		if (selectedRow > -1) {
			projectNameTextField.setText(selectedProject.getName());
			projectDescriptionTextField.setText(selectedProject.getDescription());
			projectLocalPathTextField.setText(selectedProject.getLocalPath());
			projectPublicKeyTextField.setText(selectedProject.getRequestURI());
			projectPrivateKeyTextField.setText(selectedProject.getInsertURI());
			projectPathTextField.setText(selectedProject.getPath());
			if (selectedProject instanceof EditionProject) {
				EditionProject editionProject = (EditionProject) selectedProject;
			}
		} else {
			projectNameTextField.setText("");
			projectDescriptionTextField.setText("");
			projectLocalPathTextField.setText("");
			projectPublicKeyTextField.setText("");
			projectPrivateKeyTextField.setText("");
			projectPathTextField.setText("");
		}
	}

	//
	// INTERFACE ChangeListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void stateChanged(ChangeEvent changeEvent) {
		Object source = changeEvent.getSource();
		if (source instanceof JSpinner) {
			JSpinner spinner = (JSpinner) source;
			Project currentProject = (Project) projectList.getSelectedValue();
			if (currentProject == null) {
				return;
			}
			if ("project.edition".equals(spinner.getName())) {
				((EditionProject) currentProject).setEdition((Integer) spinner.getValue());
			}
		}
	}

	//
	// INTERFACE DocumentListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void insertUpdate(DocumentEvent documentEvent) {
		setTextField(documentEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeUpdate(DocumentEvent documentEvent) {
		setTextField(documentEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void changedUpdate(DocumentEvent documentEvent) {
		setTextField(documentEvent);
	}

}

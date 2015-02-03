/*
 * jSite - ProjectFilesPage.java - Copyright © 2006–2014 David Roden
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import net.pterodactylus.util.io.MimeTypes;
import net.pterodactylus.util.swing.SwingUtils;
import net.pterodactylus.util.thread.StoppableDelay;
import de.todesbaum.jsite.application.FileOption;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.gui.FileScanner.ScannedFile;
import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.jsite.i18n.I18nContainer;
import de.todesbaum.util.swing.TLabel;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;

/**
 * Wizard page that lets the user manage the files of a project.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ProjectFilesPage extends TWizardPage implements ActionListener, ListSelectionListener, DocumentListener, FileScannerListener {

	/** The project. */
	private Project project;

	/** The “scan files” action. */
	private Action scanAction;

	/** The “always force insert” checkbox. */
	private JCheckBox alwaysForceInsertCheckBox;

	/** The “ignore hidden files” checkbox. */
	private JCheckBox ignoreHiddenFilesCheckBox;

	/** The list of project files. */
	private JList projectFileList;

	/** The “default file” checkbox. */
	private JCheckBox defaultFileCheckBox;

	/** The “insert” checkbox. */
	private JCheckBox fileOptionsInsertCheckBox;

	/** The “force insert” checkbox. */
	private JCheckBox fileOptionsForceInsertCheckBox;

	/** The “insert redirect” checkbox. */
	private JCheckBox fileOptionsInsertRedirectCheckBox;

	/** The “custom key” textfield. */
	private JTextField fileOptionsCustomKeyTextField;

	/** The “rename” check box. */
	private JCheckBox fileOptionsRenameCheckBox;

	/** The “new name” text field. */
	private JTextField fileOptionsRenameTextField;

	/** The “mime type” combo box. */
	private JComboBox fileOptionsMIMETypeComboBox;

	/** Delayed notification for file scanning. */
	private StoppableDelay delayedNotification;

	/** Dialog to display while scanning. */
	private JDialog scanningFilesDialog;

	/** The file scanner. */
	private FileScanner fileScanner;

	/** The progress bar. */
	private JProgressBar progressBar;

	/**
	 * Creates a new project file page.
	 *
	 * @param wizard
	 *            The wizard the page belongs to
	 */
	public ProjectFilesPage(final TWizard wizard) {
		super(wizard);
		pageInit();
	}

	/**
	 * Initializes the page and all its actions and components.
	 */
	private void pageInit() {
		createActions();
		setLayout(new BorderLayout(12, 12));
		add(createProjectFilesPanel(), BorderLayout.CENTER);
	}

	/**
	 * Creates all actions.
	 */
	private void createActions() {
		scanAction = new AbstractAction(I18n.getMessage("jsite.project-files.action.rescan")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				actionScan();
			}
		};
		scanAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		scanAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.rescan.tooltip"));

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				scanAction.putValue(Action.NAME, I18n.getMessage("jsite.project-files.action.rescan"));
				scanAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project-files.action.rescan.tooltip"));
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pageAdded(TWizard wizard) {
		/* create file scanner. */
		fileScanner = new FileScanner(project);
		fileScanner.addFileScannerListener(this);

		actionScan();
		this.wizard.setPreviousName(I18n.getMessage("jsite.wizard.previous"));
		this.wizard.setNextName(I18n.getMessage("jsite.project-files.insert-now"));
		this.wizard.setQuitName(I18n.getMessage("jsite.wizard.quit"));
	}

	/**
	 * Creates the panel contains the project file list and options.
	 *
	 * @return The created panel
	 */
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

		alwaysForceInsertCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.always-force-insert"));
		alwaysForceInsertCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.always-force-insert.tooltip"));
		alwaysForceInsertCheckBox.setName("always-force-insert");
		alwaysForceInsertCheckBox.addActionListener(this);
		fileOptionsPanel.add(alwaysForceInsertCheckBox, new GridBagConstraints(0, 0, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		ignoreHiddenFilesCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.ignore-hidden-files"));
		ignoreHiddenFilesCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.ignore-hidden-files.tooltip"));
		ignoreHiddenFilesCheckBox.setName("ignore-hidden-files");
		ignoreHiddenFilesCheckBox.addActionListener(this);
		fileOptionsPanel.add(ignoreHiddenFilesCheckBox, new GridBagConstraints(0, 1, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		fileOptionsPanel.add(new JButton(scanAction), new GridBagConstraints(0, 2, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));

		final JLabel fileOptionsLabel = new JLabel("<html><b>" + I18n.getMessage("jsite.project-files.file-options") + "</b></html>");
		fileOptionsPanel.add(fileOptionsLabel, new GridBagConstraints(0, 3, 5, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));

		defaultFileCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.default"));
		defaultFileCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.default.tooltip"));
		defaultFileCheckBox.setName("default-file");
		defaultFileCheckBox.addActionListener(this);
		defaultFileCheckBox.setEnabled(false);

		fileOptionsPanel.add(defaultFileCheckBox, new GridBagConstraints(0, 4, 5, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));

		fileOptionsInsertCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.insert"), true);
		fileOptionsInsertCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.insert.tooltip"));
		fileOptionsInsertCheckBox.setName("insert");
		fileOptionsInsertCheckBox.setMnemonic(KeyEvent.VK_I);
		fileOptionsInsertCheckBox.addActionListener(this);
		fileOptionsInsertCheckBox.setEnabled(false);

		fileOptionsPanel.add(fileOptionsInsertCheckBox, new GridBagConstraints(0, 5, 5, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));

		fileOptionsForceInsertCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.force-insert"));
		fileOptionsForceInsertCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.force-insert.tooltip"));
		fileOptionsForceInsertCheckBox.setName("force-insert");
		fileOptionsForceInsertCheckBox.setMnemonic(KeyEvent.VK_F);
		fileOptionsForceInsertCheckBox.addActionListener(this);
		fileOptionsForceInsertCheckBox.setEnabled(false);

		fileOptionsPanel.add(fileOptionsForceInsertCheckBox, new GridBagConstraints(0, 6, 5, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));

		fileOptionsCustomKeyTextField = new JTextField(45);
		fileOptionsCustomKeyTextField.setToolTipText(I18n.getMessage("jsite.project-files.custom-key.tooltip"));
		fileOptionsCustomKeyTextField.setEnabled(false);
		fileOptionsCustomKeyTextField.getDocument().addDocumentListener(this);

		fileOptionsInsertRedirectCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.insert-redirect"), false);
		fileOptionsInsertRedirectCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.insert-redirect.tooltip"));
		fileOptionsInsertRedirectCheckBox.setName("insert-redirect");
		fileOptionsInsertRedirectCheckBox.setMnemonic(KeyEvent.VK_R);
		fileOptionsInsertRedirectCheckBox.addActionListener(this);
		fileOptionsInsertRedirectCheckBox.setEnabled(false);

		final TLabel customKeyLabel = new TLabel(I18n.getMessage("jsite.project-files.custom-key") + ":", KeyEvent.VK_K, fileOptionsCustomKeyTextField);
		fileOptionsPanel.add(fileOptionsInsertRedirectCheckBox, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		fileOptionsPanel.add(customKeyLabel, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
		fileOptionsPanel.add(fileOptionsCustomKeyTextField, new GridBagConstraints(2, 7, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		fileOptionsRenameCheckBox = new JCheckBox(I18n.getMessage("jsite.project-files.rename"), false);
		fileOptionsRenameCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.rename.tooltip"));
		fileOptionsRenameCheckBox.setName("rename");
		fileOptionsRenameCheckBox.setMnemonic(KeyEvent.VK_N);
		fileOptionsRenameCheckBox.addActionListener(this);
		fileOptionsRenameCheckBox.setEnabled(false);

		fileOptionsRenameTextField = new JTextField();
		fileOptionsRenameTextField.setEnabled(false);
		fileOptionsRenameTextField.getDocument().addDocumentListener(new DocumentListener() {

			@SuppressWarnings("synthetic-access")
			private void storeText(DocumentEvent documentEvent) {
				FileOption fileOption = getSelectedFile();
				if (fileOption == null) {
					/* no file selected. */
					return;
				}
				Document document = documentEvent.getDocument();
				int documentLength = document.getLength();
				try {
					fileOption.setChangedName(document.getText(0, documentLength).trim());
				} catch (BadLocationException ble1) {
					/* ignore, it should never happen. */
				}
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent) {
				storeText(documentEvent);
			}

			@Override
			public void insertUpdate(DocumentEvent documentEvent) {
				storeText(documentEvent);
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent) {
				storeText(documentEvent);
			}

		});

		fileOptionsPanel.add(fileOptionsRenameCheckBox, new GridBagConstraints(0, 8, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		fileOptionsPanel.add(fileOptionsRenameTextField, new GridBagConstraints(2, 8, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		fileOptionsMIMETypeComboBox = new JComboBox(MimeTypes.getAllMimeTypes().toArray());
		fileOptionsMIMETypeComboBox.setToolTipText(I18n.getMessage("jsite.project-files.mime-type.tooltip"));
		fileOptionsMIMETypeComboBox.setName("project-files.mime-type");
		fileOptionsMIMETypeComboBox.addActionListener(this);
		fileOptionsMIMETypeComboBox.setEditable(true);
		fileOptionsMIMETypeComboBox.setEnabled(false);

		final TLabel mimeTypeLabel = new TLabel(I18n.getMessage("jsite.project-files.mime-type") + ":", KeyEvent.VK_M, fileOptionsMIMETypeComboBox);
		fileOptionsPanel.add(mimeTypeLabel, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		fileOptionsPanel.add(fileOptionsMIMETypeComboBox, new GridBagConstraints(1, 9, 4, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		/* create dialog to show while scanning. */
		scanningFilesDialog = new JDialog(wizard);
		scanningFilesDialog.setModal(true);
		scanningFilesDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JPanel progressPanel = new JPanel(new BorderLayout(12, 12));
		scanningFilesDialog.getContentPane().add(progressPanel, BorderLayout.CENTER);
		progressPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		final TLabel scanningLabel = new TLabel(I18n.getMessage("jsite.project-files.scanning"), SwingConstants.CENTER);
		progressPanel.add(scanningLabel, BorderLayout.NORTH);
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
		progressPanel.add(progressBar, BorderLayout.SOUTH);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width * 2, progressBar.getPreferredSize().height));

		scanningFilesDialog.pack();
		scanningFilesDialog.addWindowListener(new WindowAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void windowOpened(WindowEvent e) {
				SwingUtils.center(scanningFilesDialog, wizard);
			}
		});

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				alwaysForceInsertCheckBox.setText(I18n.getMessage("jsite.project-files.always-force-insert"));
				alwaysForceInsertCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.always-force-insert.tooltip"));
				ignoreHiddenFilesCheckBox.setText(I18n.getMessage("jsite.project-files.ignore-hidden-files"));
				ignoreHiddenFilesCheckBox.setToolTipText(I18n.getMessage("jsite.projet-files.ignore-hidden-files.tooltip"));
				fileOptionsLabel.setText("<html><b>" + I18n.getMessage("jsite.project-files.file-options") + "</b></html>");
				defaultFileCheckBox.setText(I18n.getMessage("jsite.project-files.default"));
				defaultFileCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.default.tooltip"));
				fileOptionsInsertCheckBox.setText(I18n.getMessage("jsite.project-files.insert"));
				fileOptionsInsertCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.insert.tooltip"));
				fileOptionsForceInsertCheckBox.setText(I18n.getMessage("jsite.project-files.force-insert"));
				fileOptionsForceInsertCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.force-insert.tooltip"));
				fileOptionsInsertRedirectCheckBox.setText(I18n.getMessage("jsite.project-files.insert-redirect"));
				fileOptionsInsertRedirectCheckBox.setToolTipText(I18n.getMessage("jsite.project-files.insert-redirect.tooltip"));
				fileOptionsCustomKeyTextField.setToolTipText(I18n.getMessage("jsite.project-files.custom-key.tooltip"));
				customKeyLabel.setText(I18n.getMessage("jsite.project-files.custom-key") + ":");
				fileOptionsRenameCheckBox.setText("jsite.project-files.rename");
				fileOptionsRenameCheckBox.setToolTipText("jsite.project-files.rename.tooltip");
				fileOptionsMIMETypeComboBox.setToolTipText(I18n.getMessage("jsite.project-files.mime-type.tooltip"));
				mimeTypeLabel.setText(I18n.getMessage("jsite.project-files.mime-type") + ":");
				scanningLabel.setText(I18n.getMessage("jsite.project-files.scanning"));
			}
		});

		return projectFilesPanel;
	}

	/**
	 * Sets the project whose files to manage.
	 *
	 * @param project
	 *            The project whose files to manage
	 */
	public void setProject(final Project project) {
		this.project = project;
		setHeading(MessageFormat.format(I18n.getMessage("jsite.project-files.heading"), project.getName()));
		setDescription(I18n.getMessage("jsite.project-files.description"));
		ignoreHiddenFilesCheckBox.setSelected(project.isIgnoreHiddenFiles());
		alwaysForceInsertCheckBox.setSelected(project.isAlwaysForceInsert());
		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			public void run() {
				setHeading(MessageFormat.format(I18n.getMessage("jsite.project-files.heading"), project.getName()));
				setDescription(I18n.getMessage("jsite.project-files.description"));
			}
		});
	}

	//
	// ACTIONS
	//

	/**
	 * Rescans the project’s files.
	 */
	private void actionScan() {
		projectFileList.clearSelection();
		projectFileList.setListData(new Object[0]);

		wizard.setNextEnabled(false);
		wizard.setPreviousEnabled(false);
		wizard.setQuitEnabled(false);

		ignoreHiddenFilesCheckBox.setEnabled(false);
		scanAction.setEnabled(false);

		delayedNotification = new StoppableDelay(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				scanningFilesDialog.setVisible(true);
			}
		}, new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				scanningFilesDialog.setVisible(false);
			}
		}, 2000);
		new Thread(fileScanner).start();
		new Thread(delayedNotification).start();
		new Thread(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				while (!delayedNotification.isFinished()) {
					try {
						Thread.sleep(250);
					} catch (InterruptedException ie1) {
						/* ignore. */
					}
					progressBar.setString(fileScanner.getLastFilename());
				}
			}
		}).start();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Updates the file list.
	 */
	@Override
	public void fileScannerFinished(FileScanner fileScanner) {
		delayedNotification.finish();
		final boolean error = fileScanner.isError();
		if (!error) {
			final List<ScannedFile> files = fileScanner.getFiles();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void run() {
					projectFileList.setListData(files.toArray());
					projectFileList.clearSelection();
				}
			});
			Set<String> entriesToRemove = new HashSet<String>();
			Iterator<String> filenames = new HashSet<String>(project.getFileOptions().keySet()).iterator();
			while (filenames.hasNext()) {
				String filename = filenames.next();
				boolean found = false;
				for (ScannedFile scannedFile : files) {
					if (scannedFile.getFilename().equals(filename)) {
						found = true;
						break;
					}
				}
				if (!found) {
					entriesToRemove.add(filename);
				}
			}
			for (String filename : entriesToRemove) {
				project.setFileOption(filename, null);
			}
		} else {
			JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project-files.scan-error"), null, JOptionPane.ERROR_MESSAGE);
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				wizard.setPreviousEnabled(true);
				wizard.setNextEnabled(!error);
				wizard.setQuitEnabled(true);
				ignoreHiddenFilesCheckBox.setEnabled(true);
				scanAction.setEnabled(true);
			}
		});
	}

	/**
	 * Returns the {@link FileOption file options} for the currently selected
	 * file.
	 *
	 * @return The {@link FileOption}s for the selected file, or {@code null} if
	 *         no file is selected
	 */
	private FileOption getSelectedFile() {
		ScannedFile scannedFile = (ScannedFile) projectFileList.getSelectedValue();
		if (scannedFile == null) {
			return null;
		}
		return project.getFileOption(scannedFile.getFilename());
	}

	//
	// INTERFACE ActionListener
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object source = actionEvent.getSource();
		if (source instanceof JCheckBox) {
			String checkboxName = ((JCheckBox) source).getName();
			if ("ignore-hidden-files".equals(checkboxName)) {
				project.setIgnoreHiddenFiles(((JCheckBox) source).isSelected());
				actionScan();
				return;
			} else if ("always-force-insert".equals(checkboxName)) {
				project.setAlwaysForceInsert(((JCheckBox) source).isSelected());
				valueChanged(null);
				return;
			}
		}
		ScannedFile scannedFile = (ScannedFile) projectFileList.getSelectedValue();
		if (scannedFile == null) {
			return;
		}
		String filename = scannedFile.getFilename();
		FileOption fileOption = project.getFileOption(filename);
		if (source instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox) source;
			if ("default-file".equals(checkBox.getName())) {
				if (checkBox.isSelected()) {
					if (filename.indexOf('/') > -1) {
						JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project-files.invalid-default-file"), null, JOptionPane.ERROR_MESSAGE);
						checkBox.setSelected(false);
					} else {
						project.setIndexFile(filename);
					}
				} else {
					if (filename.equals(project.getIndexFile())) {
						project.setIndexFile(null);
					}
				}
			} else if ("insert".equals(checkBox.getName())) {
				boolean isInsert = checkBox.isSelected();
				fileOption.setInsert(isInsert);
				fileOptionsInsertRedirectCheckBox.setEnabled(!isInsert);
			} else if ("force-insert".equals(checkBox.getName())) {
				boolean isForceInsert = checkBox.isSelected();
				fileOption.setForceInsert(isForceInsert);
			} else if ("insert-redirect".equals(checkBox.getName())) {
				boolean isInsertRedirect = checkBox.isSelected();
				fileOption.setInsertRedirect(isInsertRedirect);
				fileOptionsCustomKeyTextField.setEnabled(isInsertRedirect);
			} else if ("rename".equals(checkBox.getName())) {
				boolean isRenamed = checkBox.isSelected();
				fileOptionsRenameTextField.setEnabled(isRenamed);
				fileOption.setChangedName(isRenamed ? fileOptionsRenameTextField.getText() : "");
			}
		} else if (source instanceof JComboBox) {
			JComboBox comboBox = (JComboBox) source;
			if ("project-files.mime-type".equals(comboBox.getName())) {
				fileOption.setMimeType((String) comboBox.getSelectedItem());
			}
		}
	}

	//
	// INTERFACE ListSelectionListener
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("null")
	public void valueChanged(ListSelectionEvent e) {
		ScannedFile scannedFile = (ScannedFile) projectFileList.getSelectedValue();
		boolean enabled = scannedFile != null;
		String filename = (scannedFile == null) ? null : scannedFile.getFilename();
		defaultFileCheckBox.setEnabled(enabled);
		fileOptionsInsertCheckBox.setEnabled(enabled);
		fileOptionsRenameCheckBox.setEnabled(enabled);
		fileOptionsMIMETypeComboBox.setEnabled(enabled);
		if (filename != null) {
			FileOption fileOption = project.getFileOption(filename);
			defaultFileCheckBox.setSelected(filename.equals(project.getIndexFile()));
			fileOptionsInsertCheckBox.setSelected(fileOption.isInsert());
			fileOptionsForceInsertCheckBox.setEnabled(!project.isAlwaysForceInsert() && scannedFile.getHash().equals(fileOption.getLastInsertHash()));
			fileOptionsForceInsertCheckBox.setSelected(fileOption.isForceInsert());
			fileOptionsInsertRedirectCheckBox.setEnabled(!fileOption.isInsert());
			fileOptionsInsertRedirectCheckBox.setSelected(fileOption.isInsertRedirect());
			fileOptionsCustomKeyTextField.setEnabled(fileOption.isInsertRedirect());
			fileOptionsCustomKeyTextField.setText(fileOption.getCustomKey());
			fileOptionsRenameCheckBox.setSelected(fileOption.getChangedName().isPresent());
			fileOptionsRenameTextField.setEnabled(fileOption.getChangedName().isPresent());
			fileOptionsRenameTextField.setText(fileOption.getChangedName().or(""));
			fileOptionsMIMETypeComboBox.getModel().setSelectedItem(fileOption.getMimeType());
		} else {
			defaultFileCheckBox.setSelected(false);
			fileOptionsInsertCheckBox.setSelected(true);
			fileOptionsForceInsertCheckBox.setEnabled(false);
			fileOptionsForceInsertCheckBox.setSelected(false);
			fileOptionsInsertRedirectCheckBox.setEnabled(false);
			fileOptionsInsertRedirectCheckBox.setSelected(false);
			fileOptionsCustomKeyTextField.setEnabled(false);
			fileOptionsCustomKeyTextField.setText("CHK@");
			fileOptionsRenameCheckBox.setEnabled(false);
			fileOptionsRenameCheckBox.setSelected(false);
			fileOptionsRenameTextField.setEnabled(false);
			fileOptionsRenameTextField.setText("");
			fileOptionsMIMETypeComboBox.getModel().setSelectedItem(MimeTypes.DEFAULT_CONTENT_TYPE);
		}
	}

	//
	// INTERFACE DocumentListener
	//

	/**
	 * Updates the options of the currently selected file with the changes made
	 * in the “custom key” textfield.
	 *
	 * @param documentEvent
	 *            The document event to process
	 */
	private void processDocumentUpdate(DocumentEvent documentEvent) {
		ScannedFile scannedFile = (ScannedFile) projectFileList.getSelectedValue();
		if (scannedFile == null) {
			return;
		}
		FileOption fileOption = project.getFileOption(scannedFile.getFilename());
		Document document = documentEvent.getDocument();
		try {
			String text = document.getText(0, document.getLength());
			fileOption.setCustomKey(text);
		} catch (BadLocationException ble1) {
			/* ignore. */
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void changedUpdate(DocumentEvent documentEvent) {
		processDocumentUpdate(documentEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insertUpdate(DocumentEvent documentEvent) {
		processDocumentUpdate(documentEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeUpdate(DocumentEvent documentEvent) {
		processDocumentUpdate(documentEvent);
	}

}

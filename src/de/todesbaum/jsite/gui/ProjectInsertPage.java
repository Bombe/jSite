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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.todesbaum.jsite.application.Freenet7Interface;
import de.todesbaum.jsite.application.InsertListener;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.application.ProjectInserter;
import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.jsite.i18n.I18nContainer;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;

/**
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ProjectInsertPage extends TWizardPage implements InsertListener, ClipboardOwner {

	protected ProjectInserter projectInserter;

	protected Action copyURIAction;
	protected JTextField requestURITextField;
	protected JLabel startTimeLabel;
	protected JProgressBar progressBar;
	protected long startTime = 0;

	public ProjectInsertPage(final TWizard wizard) {
		super(wizard);
		createActions();
		pageInit();
		setHeading(I18n.getMessage("jsite.insert.heading"));
		setDescription(I18n.getMessage("jsite.insert.description"));
		I18nContainer.getInstance().registerRunnable(new Runnable() {

			public void run() {
				setHeading(I18n.getMessage("jsite.insert.heading"));
				setDescription(I18n.getMessage("jsite.insert.description"));
			}
		});
		projectInserter = new ProjectInserter();
		projectInserter.addInsertListener(this);
	}

	private void createActions() {
		copyURIAction = new AbstractAction(I18n.getMessage("jsite.project.action.copy-uri")) {
			public void actionPerformed(ActionEvent actionEvent) {
				actionCopyURI();
			}
		};
		copyURIAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project.action.copy-uri.tooltip"));
		copyURIAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
		copyURIAction.setEnabled(false);

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			public void run() {
				copyURIAction.putValue(Action.NAME, I18n.getMessage("jsite.project.action.copy-uri"));
				copyURIAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project.action.copy-uri.tooltip"));
			}
		});
	}

	private void pageInit() {
		setLayout(new BorderLayout(12, 12));
		add(createProjectInsertPanel(), BorderLayout.CENTER);
	}

	private JComponent createProjectInsertPanel() {
		JComponent projectInsertPanel = new JPanel(new GridBagLayout());

		requestURITextField = new JTextField();
		requestURITextField.setEditable(false);

		startTimeLabel = new JLabel();

		progressBar = new JProgressBar(0, 1);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);

		final JLabel projectInformationLabel = new JLabel("<html><b>" + I18n.getMessage("jsite.insert.project-information") + "</b></html>");
		projectInsertPanel.add(projectInformationLabel, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		final JLabel requestURILabel = new JLabel(I18n.getMessage("jsite.insert.request-uri") + ":");
		projectInsertPanel.add(requestURILabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));
		projectInsertPanel.add(requestURITextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		final JLabel startTimeLeftLabel = new JLabel(I18n.getMessage("jsite.insert.start-time") + ":");
		projectInsertPanel.add(startTimeLeftLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));
		projectInsertPanel.add(startTimeLabel, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		final JLabel progressLabel = new JLabel(I18n.getMessage("jsite.insert.progress") + ":");
		projectInsertPanel.add(progressLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));
		projectInsertPanel.add(progressBar, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		projectInsertPanel.add(new JButton(copyURIAction), new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			public void run() {
				projectInformationLabel.setText("<html><b>" + I18n.getMessage("jsite.insert.project-information") + "</b></html>");
				requestURILabel.setText(I18n.getMessage("jsite.insert.request-uri") + ":");
				startTimeLeftLabel.setText(I18n.getMessage("jsite.insert.start-time") + ":");
				if (startTime != 0) {
					startTimeLabel.setText(DateFormat.getDateTimeInstance().format(new Date(startTime)));
				} else {
					startTimeLabel.setText("");
				}
				progressLabel.setText(I18n.getMessage("jsite.insert.progress") + ":");
			}
		});

		return projectInsertPanel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pageAdded(TWizard wizard) {
		this.wizard.setPreviousName(I18n.getMessage("jsite.wizard.previous"));
		this.wizard.setPreviousEnabled(false);
		this.wizard.setNextName(I18n.getMessage("jsite.wizard.next"));
		this.wizard.setQuitName(I18n.getMessage("jsite.wizard.quit"));
	}

	public void startInsert() {
		wizard.setNextEnabled(false);
		copyURIAction.setEnabled(false);
		progressBar.setValue(0);
		progressBar.setString(I18n.getMessage("jsite.insert.starting"));
		progressBar.setFont(progressBar.getFont().deriveFont(Font.PLAIN));
		projectInserter.start();
	}

	/**
	 * @param debug
	 *            The debug to set.
	 */
	public void setDebug(boolean debug) {
		projectInserter.setDebug(debug);
	}

	/**
	 * @param project
	 *            The project to set.
	 */
	public void setProject(final Project project) {
		projectInserter.setProject(project);
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				requestURITextField.setText(project.getFinalRequestURI(1));
			}
		});
	}

	public void setFreenetInterface(Freenet7Interface freenetInterface) {
		projectInserter.setFreenetInterface(freenetInterface);
	}

	//
	// INTERFACE InsertListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void projectInsertStarted(final Project project) {
		startTime = System.currentTimeMillis();
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				startTimeLabel.setText(DateFormat.getDateTimeInstance().format(new Date(startTime)));
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void projectURIGenerated(Project project, final String uri) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				copyURIAction.setEnabled(true);
				requestURITextField.setText(uri);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void projectInsertProgress(Project project, final int succeeded, final int failed, final int fatal, final int total, final boolean finalized) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				progressBar.setMaximum(total);
				progressBar.setValue(succeeded + failed + fatal);
				int progress = (succeeded + failed + fatal) * 100 / total;
				StringBuilder progressString = new StringBuilder();
				progressString.append(progress).append("% (");
				progressString.append(succeeded + failed + fatal).append("/").append(total);
				progressString.append(")");
				progressBar.setString(progressString.toString());
				if (finalized) {
					progressBar.setFont(progressBar.getFont().deriveFont(Font.BOLD));
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void projectInsertFinished(Project project, boolean success, Throwable cause) {
		if (success) {
			JOptionPane.showMessageDialog(this, I18n.getMessage("jsite.insert.inserted"), null, JOptionPane.INFORMATION_MESSAGE);
		} else {
			if (cause == null) {
				JOptionPane.showMessageDialog(this, I18n.getMessage("jsite.insert.insert-failed"), null, JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, MessageFormat.format(I18n.getMessage("jsite.insert.insert-failed-with-cause"), cause.getMessage()), null, JOptionPane.ERROR_MESSAGE);
			}
		}
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				progressBar.setValue(progressBar.getMaximum());
				progressBar.setString(I18n.getMessage("jsite.insert.done"));
				wizard.setNextEnabled(true);
				wizard.setQuitEnabled(true);
			}
		});
	}

	//
	// ACTIONS
	//

	protected void actionCopyURI() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(requestURITextField.getText()), this);
	}

	//
	// INTERFACE ClipboardOwner
	//

	/**
	 * {@inheritDoc}
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}

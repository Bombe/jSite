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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.todesbaum.jsite.application.EditionProject;
import de.todesbaum.jsite.application.Freenet7Interface;
import de.todesbaum.jsite.application.InsertListener;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.application.ProjectInserter;
import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id: ProjectInsertPage.java 408 2006-03-29 09:31:10Z bombe $
 */
public class ProjectInsertPage extends TWizardPage implements InsertListener {

	protected TWizard wizard;
	protected ProjectInserter projectInserter;

	protected JTextField requestURITextField;
	protected JLabel startTimeLabel;
	protected JProgressBar progressBar;
	protected long startTime;

	public ProjectInsertPage() {
		super();
		pageInit();
		setHeading(I18n.getMessage("jsite.insert.heading"));
		setDescription(I18n.getMessage("jsite.insert.description"));
		projectInserter = new ProjectInserter();
		projectInserter.addInsertListener(this);
	}

	private void pageInit() {
		setLayout(new BorderLayout(12, 12));
		add(createProjectInsertPanel(), BorderLayout.CENTER);
	}

	private JComponent createProjectInsertPanel() {
		JComponent projectInsertPanel = new JPanel(new GridBagLayout());

		requestURITextField = new JTextField();
		requestURITextField.setEditable(false);
		requestURITextField.setBackground(getBackground());
		requestURITextField.setBorder(null);

		startTimeLabel = new JLabel();

		progressBar = new JProgressBar(0, 1);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);

		projectInsertPanel.add(new JLabel("<html><b>" + I18n.getMessage("jsite.insert.project-information") + "</b></html>"), new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		projectInsertPanel.add(new JLabel(I18n.getMessage("jsite.insert.request-uri") + ":"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));
		projectInsertPanel.add(requestURITextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		projectInsertPanel.add(new JLabel(I18n.getMessage("jsite.insert.start-time") + ":"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));
		projectInsertPanel.add(startTimeLabel, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		projectInsertPanel.add(new JLabel(I18n.getMessage("jsite.insert.progress") + ":"), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 18, 0, 0), 0, 0));
		projectInsertPanel.add(progressBar, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		return projectInsertPanel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pageAdded(TWizard wizard) {
		this.wizard = wizard;
		wizard.setPreviousEnabled(false);
		wizard.setNextEnabled(false);
		wizard.setQuitEnabled(false);
		progressBar.setValue(0);
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
				StringBuffer uriBuffer = new StringBuffer();
				uriBuffer.append(project.getRequestURI());
				uriBuffer.append(project.getPath());
				if (project instanceof EditionProject) {
					uriBuffer.append('-').append(((EditionProject) project).getEdition());
				}
				uriBuffer.append('/');
				requestURITextField.setText(uriBuffer.toString());
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
	public void projectInsertProgress(Project project, final int succeeded, final int failed, final int fatal, final int total, final boolean finalized) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				progressBar.setMaximum(total);
				progressBar.setValue(succeeded + failed + fatal);
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
				wizard.setNextEnabled(true);
				wizard.setQuitEnabled(true);
			}
		});
	}

}

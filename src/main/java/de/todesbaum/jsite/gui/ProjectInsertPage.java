/*
 * jSite - ProjectInsertPage.java - Copyright © 2006–2014 David Roden
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
import java.util.logging.Level;
import java.util.logging.Logger;

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

import net.pterodactylus.util.io.StreamCopier.ProgressListener;
import de.todesbaum.jsite.application.AbortedException;
import de.todesbaum.jsite.application.Freenet7Interface;
import de.todesbaum.jsite.application.InsertListener;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.application.ProjectInserter;
import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.jsite.i18n.I18nContainer;
import de.todesbaum.util.freenet.fcp2.ClientPutDir.ManifestPutter;
import de.todesbaum.util.freenet.fcp2.PriorityClass;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;

/**
 * Wizard page that shows the progress of an insert.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ProjectInsertPage extends TWizardPage implements InsertListener, ClipboardOwner {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(ProjectInsertPage.class.getName());

	/** The project inserter. */
	private ProjectInserter projectInserter;

	/** The “copy URI” action. */
	private Action copyURIAction;

	/** The “request URI” textfield. */
	private JTextField requestURITextField;

	/** The “start time” label. */
	private JLabel startTimeLabel;

	/** The progress bar. */
	private JProgressBar progressBar;

	/** The start time of the insert. */
	private long startTime = 0;

	/** The number of inserted blocks. */
	private volatile int insertedBlocks;

	/** Whether the “copy URI to clipboard” button was used. */
	private boolean uriCopied;

	/** Whether the insert is currently running. */
	private volatile boolean running = false;

	/**
	 * Creates a new progress insert wizard page.
	 *
	 * @param wizard
	 *            The wizard this page belongs to
	 */
	public ProjectInsertPage(final TWizard wizard) {
		super(wizard);
		createActions();
		pageInit();
		setHeading(I18n.getMessage("jsite.insert.heading"));
		setDescription(I18n.getMessage("jsite.insert.description"));
		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			public void run() {
				setHeading(I18n.getMessage("jsite.insert.heading"));
				setDescription(I18n.getMessage("jsite.insert.description"));
			}
		});
		projectInserter = new ProjectInserter();
		projectInserter.addInsertListener(this);
	}

	/**
	 * Creates all used actions.
	 */
	private void createActions() {
		copyURIAction = new AbstractAction(I18n.getMessage("jsite.project.action.copy-uri")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				actionCopyURI();
			}
		};
		copyURIAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project.action.copy-uri.tooltip"));
		copyURIAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
		copyURIAction.setEnabled(false);

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				copyURIAction.putValue(Action.NAME, I18n.getMessage("jsite.project.action.copy-uri"));
				copyURIAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.project.action.copy-uri.tooltip"));
			}
		});
	}

	/**
	 * Initializes the page.
	 */
	private void pageInit() {
		setLayout(new BorderLayout(12, 12));
		add(createProjectInsertPanel(), BorderLayout.CENTER);
	}

	/**
	 * Creates the main panel.
	 *
	 * @return The main panel
	 */
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
		projectInsertPanel.add(new JButton(copyURIAction), new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(12, 18, 0, 0), 0, 0));

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
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
		this.wizard.setNextName(I18n.getMessage("jsite.general.cancel"));
		this.wizard.setQuitName(I18n.getMessage("jsite.wizard.quit"));
	}

	/**
	 * Starts the insert.
	 */
	public void startInsert() {
		running = true;
		copyURIAction.setEnabled(false);
		progressBar.setValue(0);
		progressBar.setString(I18n.getMessage("jsite.insert.starting"));
		progressBar.setFont(progressBar.getFont().deriveFont(Font.PLAIN));
		projectInserter.start(new ProgressListener() {

			@Override
			public void onProgress(final long copied, final long length) {
				SwingUtilities.invokeLater(new Runnable() {

					/**
					 * {@inheritDoc}
					 */
					@Override
					@SuppressWarnings("synthetic-access")
					public void run() {
						int divisor = 1;
						while (((copied / divisor) > Integer.MAX_VALUE) || ((length / divisor) > Integer.MAX_VALUE)) {
							divisor *= 10;
						}
						progressBar.setMaximum((int) (length / divisor));
						progressBar.setValue((int) (copied / divisor));
						progressBar.setString("Uploaded: " + copied + " / " + length);
					}
				});
			}
		});
	}

	/**
	 * Stops the currently running insert.
	 */
	public void stopInsert() {
		if (running) {
			wizard.setNextEnabled(false);
			projectInserter.stop();
		}
	}

	/**
	 * Returns whether the insert is currently running.
	 *
	 * @return {@code true} if the insert is currently running, {@code false}
	 *         otherwise
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Sets the project to insert.
	 *
	 * @param project
	 *            The project to insert
	 */
	public void setProject(final Project project) {
		projectInserter.setProject(project);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				requestURITextField.setText(project.getFinalRequestURI(1));
			}
		});
	}

	/**
	 * Sets the freenet interface to use.
	 *
	 * @param freenetInterface
	 *            The freenet interface to use
	 */
	public void setFreenetInterface(Freenet7Interface freenetInterface) {
		projectInserter.setFreenetInterface(freenetInterface);
	}

	/**
	 * Sets the project inserter’s temp directory.
	 *
	 * @see ProjectInserter#setTempDirectory(String)
	 * @param tempDirectory
	 *            The temp directory to use, or {@code null} to use the system
	 *            default
	 */
	public void setTempDirectory(String tempDirectory) {
		projectInserter.setTempDirectory(tempDirectory);
	}

	/**
	 * Returns whether the “copy URI to clipboard” button was used.
	 *
	 * @return {@code true} if an URI was copied to clipboard, {@code false}
	 *         otherwise
	 */
	public boolean wasUriCopied() {
		return uriCopied;
	}

	/**
	 * Sets whether to use the “early encode“ flag for the insert.
	 *
	 * @param useEarlyEncode
	 *            {@code true} to set the “early encode” flag for the insert,
	 *            {@code false} otherwise
	 */
	public void setUseEarlyEncode(boolean useEarlyEncode) {
		projectInserter.setUseEarlyEncode(useEarlyEncode);
	}

	/**
	 * Sets the insert priority.
	 *
	 * @param priority
	 *            The insert priority
	 */
	public void setPriority(PriorityClass priority) {
		projectInserter.setPriority(priority);
	}

	/**
	 * Sets the manifest putter to use for the insert.
	 *
	 * @see ProjectInserter#setManifestPutter(ManifestPutter)
	 * @param manifestPutter
	 *            The manifest putter
	 */
	public void setManifestPutter(ManifestPutter manifestPutter) {
		projectInserter.setManifestPutter(manifestPutter);
	}

	//
	// INTERFACE InsertListener
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void projectInsertStarted(final Project project) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				startTimeLabel.setText(DateFormat.getDateTimeInstance().format(new Date()));
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void projectUploadFinished(Project project) {
		startTime = System.currentTimeMillis();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				progressBar.setString(I18n.getMessage("jsite.insert.starting"));
				progressBar.setValue(0);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void projectURIGenerated(Project project, final String uri) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				copyURIAction.setEnabled(true);
				requestURITextField.setText(uri);
			}
		});
		logger.log(Level.FINEST, "Insert generated URI: " + uri);
		int slash = uri.indexOf('/');
		slash = uri.indexOf('/', slash + 1);
		int secondSlash = uri.indexOf('/', slash + 1);
		if (secondSlash == -1) {
			secondSlash = uri.length();
		}
		String editionNumber = uri.substring(slash + 1, secondSlash);
		logger.log(Level.FINEST, "Extracted edition number: " + editionNumber);
		int edition = -1;
		try {
			edition = Integer.valueOf(editionNumber);
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		logger.log(Level.FINEST, "Insert edition: " + edition + ", Project edition: " + project.getEdition());
		if ((edition != -1) && (edition == project.getEdition())) {
			JOptionPane.showMessageDialog(this, I18n.getMessage("jsite.insert.reinserted-edition"), I18n.getMessage("jsite.insert.reinserted-edition.title"), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void projectInsertProgress(Project project, final int succeeded, final int failed, final int fatal, final int total, final boolean finalized) {
		insertedBlocks = succeeded;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				if (total == 0) {
					return;
				}
				progressBar.setMaximum(total);
				progressBar.setValue(succeeded + failed + fatal);
				int progress = (succeeded + failed + fatal) * 100 / total;
				StringBuilder progressString = new StringBuilder();
				progressString.append(progress).append("% (");
				progressString.append(succeeded + failed + fatal).append('/').append(total);
				progressString.append(") (");
				progressString.append(getTransferRate());
				progressString.append(' ').append(I18n.getMessage("jsite.insert.k-per-s")).append(')');
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
	@Override
	public void projectInsertFinished(Project project, boolean success, Throwable cause) {
		running = false;
		if (success) {
			String copyURILabel = I18n.getMessage("jsite.insert.okay-copy-uri");
			int selectedValue = JOptionPane.showOptionDialog(this, I18n.getMessage("jsite.insert.inserted"), I18n.getMessage("jsite.insert.done.title"), 0, JOptionPane.INFORMATION_MESSAGE, null, new Object[] { I18n.getMessage("jsite.general.ok"), copyURILabel }, copyURILabel);
			if (selectedValue == 1) {
				actionCopyURI();
			}
		} else {
			if (cause == null) {
				JOptionPane.showMessageDialog(this, I18n.getMessage("jsite.insert.insert-failed"), I18n.getMessage("jsite.insert.insert-failed.title"), JOptionPane.ERROR_MESSAGE);
			} else {
				if (cause instanceof AbortedException) {
					JOptionPane.showMessageDialog(this, I18n.getMessage("jsite.insert.insert-aborted"), I18n.getMessage("jsite.insert.insert-aborted.title"), JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this, MessageFormat.format(I18n.getMessage("jsite.insert.insert-failed-with-cause"), cause.getMessage()), I18n.getMessage("jsite.insert.insert-failed.title"), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				progressBar.setValue(progressBar.getMaximum());
				progressBar.setString(I18n.getMessage("jsite.insert.done") + " (" + getTransferRate() + " " + I18n.getMessage("jsite.insert.k-per-s") + ")");
				wizard.setNextName(I18n.getMessage("jsite.wizard.next"));
				wizard.setNextEnabled(true);
				wizard.setQuitEnabled(true);
			}
		});
	}

	//
	// ACTIONS
	//

	/**
	 * Copies the request URI of the project to the clipboard.
	 */
	private void actionCopyURI() {
		uriCopied = true;
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(requestURITextField.getText()), this);
	}

	/**
	 * Formats the given number so that it always has the the given number of
	 * fractional digits.
	 *
	 * @param number
	 *            The number to format
	 * @param digits
	 *            The number of fractional digits
	 * @return The formatted number
	 */
	private static String formatNumber(double number, int digits) {
		int multiplier = (int) Math.pow(10, digits);
		String formattedNumber = String.valueOf((int) (number * multiplier) / (double) multiplier);
		if (formattedNumber.indexOf('.') == -1) {
			formattedNumber += '.';
			for (int digit = 0; digit < digits; digit++) {
				formattedNumber += "0";
			}
		}
		return formattedNumber;
	}

	/**
	 * Returns the formatted transfer rate at this point.
	 *
	 * @return The formatted transfer rate
	 */
	private String getTransferRate() {
		return formatNumber(insertedBlocks * 32.0 / ((System.currentTimeMillis() - startTime) / 1000), 1);
	}

	//
	// INTERFACE ClipboardOwner
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		/* ignore. */
	}

}

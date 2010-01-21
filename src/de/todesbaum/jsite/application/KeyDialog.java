/*
 * jSite - KeyDialog.java - Copyright © 2010 David Roden
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

package de.todesbaum.jsite.application;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.jsite.i18n.I18nContainer;

/**
 * A dialog that lets the user edit the private and public key for a project.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class KeyDialog extends JDialog {

	/** Interface to the freenet node. */
	private final Freenet7Interface freenetInterface;

	/** The public key. */
	private String publicKey;

	/** The private key. */
	private String privateKey;

	/** The “OK” button’s action. */
	private Action okAction;

	/** The “Cancel” button’s action. */
	private Action cancelAction;

	/** The “Regenerate” button’s action. */
	private Action generateAction;

	/** The text field for the private key. */
	private JTextField privateKeyTextField;

	/** The text field for the public key. */
	private JTextField publicKeyTextField;

	/** Whether the dialog was cancelled. */
	private boolean cancelled;

	/**
	 * Creates a new key dialog.
	 *
	 * @param freenetInterface
	 *            Interface to the freenet node
	 * @param parent
	 *            The parent frame
	 */
	public KeyDialog(Freenet7Interface freenetInterface, JFrame parent) {
		super(parent, I18n.getMessage("jsite.key-dialog.title"), true);
		this.freenetInterface = freenetInterface;
		addWindowListener(new WindowAdapter() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void windowClosing(WindowEvent windowEvent) {
				actionCancel();
			}
		});
		initDialog();
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether the dialog was cancelled.
	 *
	 * @return {@code true} if the dialog was cancelled, {@code false} otherwise
	 */
	public boolean wasCancelled() {
		return cancelled;
	}

	/**
	 * Returns the public key.
	 *
	 * @return The public key
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * Sets the public key.
	 *
	 * @param publicKey
	 *            The public key
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
		publicKeyTextField.setText(publicKey);
		pack();
	}

	/**
	 * Returns the private key.
	 *
	 * @return The private key
	 */
	public String getPrivateKey() {
		return privateKey;
	}

	/**
	 * Sets the private key.
	 *
	 * @param privateKey
	 *            The private key
	 */
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
		privateKeyTextField.setText(privateKey);
		pack();
	}

	//
	// ACTIONS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pack() {
		super.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates all necessary actions.
	 */
	private void createActions() {
		okAction = new AbstractAction(I18n.getMessage("jsite.general.ok")) {

			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				actionOk();
			}
		};
		okAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.key-dialog.button.ok.tooltip"));
		okAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_ENTER);

		cancelAction = new AbstractAction(I18n.getMessage("jsite.general.cancel")) {

			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				actionCancel();
			}
		};
		cancelAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.key-dialog.button.cancel.tooltip"));
		cancelAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_ESCAPE);

		generateAction = new AbstractAction(I18n.getMessage("jsite.key-dialog.button.generate")) {

			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				actionGenerate();
			}
		};
		generateAction.putValue(Action.SHORT_DESCRIPTION, I18n.getMessage("jsite.key-dialog.button.generate.tooltip"));
		generateAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
	}

	/**
	 * Initializes the dialog and all its components.
	 */
	private void initDialog() {
		createActions();
		JPanel dialogPanel = new JPanel(new BorderLayout(12, 12));
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		JPanel contentPanel = new JPanel(new GridBagLayout());
		dialogPanel.add(contentPanel, BorderLayout.CENTER);

		final JLabel keysLabel = new JLabel(I18n.getMessage("jsite.key-dialog.label.keys"));
		contentPanel.add(keysLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		final JLabel privateKeyLabel = new JLabel(I18n.getMessage("jsite.key-dialog.label.private-key"));
		contentPanel.add(privateKeyLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(12, 18, 0, 0), 0, 0));

		privateKeyTextField = new JTextField();
		contentPanel.add(privateKeyTextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 12, 0, 0), 0, 0));

		final JLabel publicKeyLabel = new JLabel(I18n.getMessage("jsite.key-dialog.label.public-key"));
		contentPanel.add(publicKeyLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));

		publicKeyTextField = new JTextField();
		contentPanel.add(publicKeyTextField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 12, 0, 0), 0, 0));

		final JLabel actionsLabel = new JLabel(I18n.getMessage("jsite.key-dialog.label.actions"));
		contentPanel.add(actionsLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(12, 0, 0, 0), 0, 0));

		JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 12));
		actionButtonPanel.setBorder(BorderFactory.createEmptyBorder(-12, -12, -12, -12));
		contentPanel.add(actionButtonPanel, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(12, 18, 0, 0), 0, 0));

		actionButtonPanel.add(new JButton(generateAction));

		JPanel separatorPanel = new JPanel(new BorderLayout(12, 12));
		dialogPanel.add(separatorPanel, BorderLayout.PAGE_END);
		separatorPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.PAGE_START);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 12, 12));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(-12, -12, -12, -12));
		separatorPanel.add(buttonPanel, BorderLayout.CENTER);
		buttonPanel.add(new JButton(okAction));
		buttonPanel.add(new JButton(cancelAction));

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			public void run() {
				keysLabel.setText(I18n.getMessage("jsite.key-dialog.label.keys"));
				privateKeyLabel.setText(I18n.getMessage("jsite.key-dialog.label.private-key"));
				publicKeyLabel.setText(I18n.getMessage("jsite.key-dialog.label.public-key"));
				actionsLabel.setText(I18n.getMessage("jsite.key-dialog.label.actions"));
			}
		});

		getContentPane().add(dialogPanel, BorderLayout.CENTER);
		pack();
		setResizable(false);
	}

	//
	// PRIVATE ACTIONS
	//

	/**
	 * Quits the dialog, accepting all changes.
	 */
	private void actionOk() {
		publicKey = publicKeyTextField.getText();
		privateKey = privateKeyTextField.getText();
		cancelled = false;
		setVisible(false);
	}

	/**
	 * Quits the dialog, discarding all changes.
	 */
	private void actionCancel() {
		cancelled = true;
		setVisible(false);
	}

	/**
	 * Generates a new key pair.
	 */
	private void actionGenerate() {
		if (JOptionPane.showConfirmDialog(this, I18n.getMessage("jsite.project.warning.generate-new-key"), null, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
			return;
		}
		String[] keyPair = null;
		try {
			keyPair = freenetInterface.generateKeyPair();
		} catch (IOException ioe1) {
			JOptionPane.showMessageDialog(this, MessageFormat.format(I18n.getMessage("jsite.project.keygen.io-error"), ioe1.getMessage()), null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		publicKeyTextField.setText(keyPair[1].substring(keyPair[1].indexOf('@') + 1, keyPair[1].lastIndexOf('/')));
		privateKeyTextField.setText(keyPair[0].substring(keyPair[0].indexOf('@') + 1, keyPair[0].lastIndexOf('/')));
		pack();
	}

}

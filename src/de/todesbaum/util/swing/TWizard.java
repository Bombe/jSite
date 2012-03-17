/*
 * jSite - TWizard.java - Copyright © 2006–2012 David Roden
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

package de.todesbaum.util.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class TWizard extends JFrame implements WindowListener {

	protected List<WizardListener> wizardListeners = new ArrayList<WizardListener>();

	private Action previousAction;
	private Action nextAction;
	private Action quitAction;
	private JLabel pageIcon;
	private JPanel pagePanel;
	private JLabel pageHeading;
	private JLabel pageDescription;

	@Override
	protected void frameInit() {
		super.frameInit();
		setResizable(false);
		addWindowListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		createActions();

		pageIcon = new JLabel();
		pageIcon.setVerticalAlignment(SwingConstants.TOP);
		pageHeading = new JLabel();
		pageHeading.setFont(pageHeading.getFont().deriveFont(pageHeading.getFont().getSize() * 2.0f).deriveFont(Font.BOLD));
		pageDescription = new JLabel();

		JPanel contentPane = new JPanel(new BorderLayout(12, 12));
		contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));

		JPanel topPanel = new JPanel(new BorderLayout(12, 12));
		contentPane.add(topPanel, BorderLayout.PAGE_START);

		topPanel.add(pageIcon, BorderLayout.LINE_START);

		JPanel textPanel = new JPanel(new BorderLayout(12, 12));
		topPanel.add(textPanel, BorderLayout.CENTER);
		textPanel.add(pageHeading, BorderLayout.PAGE_START);
		textPanel.add(pageDescription, BorderLayout.CENTER);

		pagePanel = new JPanel(new BorderLayout(12, 12));
		contentPane.add(pagePanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 12, 12));
		buttonPanel.setBorder(new EmptyBorder(-12, -12, -12, -12));
		buttonPanel.add(new JButton(previousAction));
		buttonPanel.add(new JButton(nextAction));
		buttonPanel.add(new JButton(quitAction));
		contentPane.add(buttonPanel, BorderLayout.PAGE_END);

		setContentPane(contentPane);
	}

	@Override
	public void pack() {
		super.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		// System.out.println("resized to: " + getWidth() + "x" + getHeight());
	}

	private void createActions() {
		previousAction = new AbstractAction("Previous") {
			public void actionPerformed(ActionEvent actionEvent) {
				actionPrevious();
			}
		};

		nextAction = new AbstractAction("Next") {
			public void actionPerformed(ActionEvent actionEvent) {
				actionNext();
			}
		};

		quitAction = new AbstractAction("Quit") {
			public void actionPerformed(ActionEvent actionEvent) {
				actionQuit();
			}
		};
	}

	public void addWizardListener(WizardListener wizardListener) {
		wizardListeners.add(wizardListener);
	}

	public void removeWizardListener(WizardListener wizardListener) {
		wizardListeners.remove(wizardListener);
	}

	protected void fireWizardPreviousPressed() {
		for (WizardListener wizardListener: wizardListeners) {
			wizardListener.wizardPreviousPressed(this);
		}
	}

	protected void fireWizardNextPressed() {
		for (WizardListener wizardListener: wizardListeners) {
			wizardListener.wizardNextPressed(this);
		}
	}

	protected void fireWizardQuitPressed() {
		for (WizardListener wizardListener: wizardListeners) {
			wizardListener.wizardQuitPressed(this);
		}
	}

	public void setIcon(Icon icon) {
		pageIcon.setIcon(icon);
	}

	public void setPage(TWizardPage page) {
		setVisible(false);
		pageHeading.setText(page.getHeading());
		pageDescription.setText(page.getDescription());
		if (pagePanel.getComponentCount() > 0) {
			if (pagePanel.getComponent(0) instanceof TWizardPage) {
				((TWizardPage) pagePanel.getComponent(0)).pageDeleted(this);
			}
		}
		pagePanel.removeAll();
		pagePanel.add(page, BorderLayout.CENTER);
		page.pageAdded(this);
		pack();
		setTitle(page.getHeading());
		setVisible(true);
	}

	public TWizardPage getPage() {
		return (TWizardPage) pagePanel.getComponent(0);
	}

	public void setPreviousEnabled(boolean previousEnabled) {
		previousAction.setEnabled(previousEnabled);
	}

	public void setPreviousName(String previousName) {
		previousAction.putValue(Action.NAME, previousName);
	}

	public void setNextEnabled(boolean nextEnabled) {
		nextAction.setEnabled(nextEnabled);
	}

	public void setNextName(String nextName) {
		nextAction.putValue(Action.NAME, nextName);
	}

	public void setQuitEnabled(boolean quitEnabled) {
		quitAction.setEnabled(quitEnabled);
	}

	public void setQuitName(String quitName) {
		quitAction.putValue(Action.NAME, quitName);
	}

	protected void actionPrevious() {
		fireWizardPreviousPressed();
	}

	protected void actionNext() {
		fireWizardNextPressed();
	}

	protected void actionQuit() {
		fireWizardQuitPressed();
	}

	//
	// INTERFACE WindowListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void windowOpened(WindowEvent e) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void windowClosing(WindowEvent e) {
		fireWizardQuitPressed();
	}

	/**
	 * {@inheritDoc}
	 */
	public void windowClosed(WindowEvent e) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void windowIconified(WindowEvent e) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void windowDeiconified(WindowEvent e) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void windowActivated(WindowEvent e) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void windowDeactivated(WindowEvent e) {
	}

}

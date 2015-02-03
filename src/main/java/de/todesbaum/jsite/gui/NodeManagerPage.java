/*
 * jSite - NodeManagerPage.java - Copyright © 2006–2014 David Roden
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import de.todesbaum.jsite.application.Node;
import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.jsite.i18n.I18nContainer;
import de.todesbaum.util.swing.TLabel;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;

/**
 * Wizard page that lets the user edit his nodes.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class NodeManagerPage extends TWizardPage implements ListSelectionListener, DocumentListener, ChangeListener {

	/** List of node manager listeners. */
	private List<NodeManagerListener> nodeManagerListeners = new ArrayList<NodeManagerListener>();

	/** The “add node” action. */
	protected Action addNodeAction;

	/** The “delete node” action. */
	protected Action deleteNodeAction;

	/** The node list model. */
	private DefaultListModel nodeListModel;

	/** The node list. */
	private JList nodeList;

	/** The node name textfield. */
	private JTextField nodeNameTextField;

	/** The node hostname textfield. */
	private JTextField nodeHostnameTextField;

	/** The spinner for the node port. */
	private JSpinner nodePortSpinner;

	/**
	 * Creates a new node manager wizard page.
	 *
	 * @param wizard
	 *            The wizard this page belongs to
	 */
	public NodeManagerPage(final TWizard wizard) {
		super(wizard);
		pageInit();
		setHeading(I18n.getMessage("jsite.node-manager.heading"));
		setDescription(I18n.getMessage("jsite.node-manager.description"));
		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			public void run() {
				setHeading(I18n.getMessage("jsite.node-manager.heading"));
				setDescription(I18n.getMessage("jsite.node-manager.description"));
			}
		});
	}

	/**
	 * Adds a listener for node manager events.
	 *
	 * @param nodeManagerListener
	 *            The listener to add
	 */
	public void addNodeManagerListener(NodeManagerListener nodeManagerListener) {
		nodeManagerListeners.add(nodeManagerListener);
	}

	/**
	 * Removes a listener for node manager events.
	 *
	 * @param nodeManagerListener
	 *            The listener to remove
	 */
	public void removeNodeManagerListener(NodeManagerListener nodeManagerListener) {
		nodeManagerListeners.remove(nodeManagerListener);
	}

	/**
	 * Notifies all listeners that the node configuration has changed.
	 *
	 * @param nodes
	 *            The new list of nodes
	 */
	protected void fireNodesUpdated(Node[] nodes) {
		for (NodeManagerListener nodeManagerListener : nodeManagerListeners) {
			nodeManagerListener.nodesUpdated(nodes);
		}
	}

	/**
	 * Notifies all listeners that a new node was selected.
	 *
	 * @param node
	 *            The newly selected node
	 */
	protected void fireNodeSelected(Node node) {
		for (NodeManagerListener nodeManagerListener : nodeManagerListeners) {
			nodeManagerListener.nodeSelected(node);
		}
	}

	/**
	 * Creates all actions.
	 */
	private void createActions() {
		addNodeAction = new AbstractAction(I18n.getMessage("jsite.node-manager.add-node")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				addNode();
			}
		};

		deleteNodeAction = new AbstractAction(I18n.getMessage("jsite.node-manager.delete-node")) {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				deleteNode();
			}
		};
		deleteNodeAction.setEnabled(false);

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			public void run() {
				addNodeAction.putValue(Action.NAME, I18n.getMessage("jsite.node-manager.add-node"));
				deleteNodeAction.putValue(Action.NAME, I18n.getMessage("jsite.node-manager.delete-node"));
			}
		});
	}

	/**
	 * Initializes the page and all components in it.
	 */
	private void pageInit() {
		createActions();
		nodeListModel = new DefaultListModel();
		nodeList = new JList(nodeListModel);
		nodeList.setName("node-list");
		nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeList.addListSelectionListener(this);

		nodeNameTextField = new JTextField("");
		nodeNameTextField.getDocument().putProperty("Name", "node-name");
		nodeNameTextField.getDocument().addDocumentListener(this);
		nodeNameTextField.setEnabled(false);

		nodeHostnameTextField = new JTextField("localhost");
		nodeHostnameTextField.getDocument().putProperty("Name", "node-hostname");
		nodeHostnameTextField.getDocument().addDocumentListener(this);
		nodeHostnameTextField.setEnabled(false);

		nodePortSpinner = new JSpinner(new SpinnerNumberModel(9481, 1, 65535, 1));
		nodePortSpinner.setName("node-port");
		nodePortSpinner.addChangeListener(this);
		nodePortSpinner.setEnabled(false);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 12));
		buttonPanel.setBorder(new EmptyBorder(-12, -12, -12, -12));
		buttonPanel.add(new JButton(addNodeAction));
		buttonPanel.add(new JButton(deleteNodeAction));

		JPanel centerPanel = new JPanel(new BorderLayout());
		JPanel nodeInformationPanel = new JPanel(new GridBagLayout());
		centerPanel.add(nodeInformationPanel, BorderLayout.PAGE_START);
		nodeInformationPanel.add(buttonPanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		final JLabel nodeInformationLabel = new JLabel("<html><b>" + I18n.getMessage("jsite.node-manager.node-information") + "</b></html>");
		nodeInformationPanel.add(nodeInformationLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
		final TLabel nodeNameLabel = new TLabel(I18n.getMessage("jsite.node-manager.name") + ":", KeyEvent.VK_N, nodeNameTextField);
		nodeInformationPanel.add(nodeNameLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		nodeInformationPanel.add(nodeNameTextField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		final TLabel nodeHostnameLabel = new TLabel(I18n.getMessage("jsite.node-manager.hostname") + ":", KeyEvent.VK_H, nodeHostnameTextField);
		nodeInformationPanel.add(nodeHostnameLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		nodeInformationPanel.add(nodeHostnameTextField, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		final TLabel nodePortLabel = new TLabel(I18n.getMessage("jsite.node-manager.port") + ":", KeyEvent.VK_P, nodePortSpinner);
		nodeInformationPanel.add(nodePortLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		nodeInformationPanel.add(nodePortSpinner, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));

		setLayout(new BorderLayout(12, 12));
        final JScrollPane nodeListScrollPane = new JScrollPane(nodeList);
        nodeListScrollPane.setPreferredSize(new Dimension(250, -1));
        add(nodeListScrollPane, BorderLayout.LINE_START);
		add(centerPanel, BorderLayout.CENTER);

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@Override
			public void run() {
				nodeInformationLabel.setText("<html><b>" + I18n.getMessage("jsite.node-manager.node-information") + "</b></html>");
				nodeNameLabel.setText(I18n.getMessage("jsite.node-manager.name") + ":");
				nodeHostnameLabel.setText(I18n.getMessage("jsite.node-manager.hostname") + ":");
				nodePortLabel.setText(I18n.getMessage("jsite.node-manager.port") + ":");
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pageAdded(TWizard wizard) {
		this.wizard.setNextEnabled(nodeListModel.getSize() > 0);
		this.wizard.setPreviousName(I18n.getMessage("jsite.wizard.previous"));
		this.wizard.setNextName(I18n.getMessage("jsite.wizard.next"));
		this.wizard.setQuitName(I18n.getMessage("jsite.wizard.quit"));
	}

	/**
	 * Sets the node list.
	 *
	 * @param nodes
	 *            The list of nodes
	 */
	public void setNodes(Node[] nodes) {
		nodeListModel.clear();
		for (Node node : nodes) {
			nodeListModel.addElement(node);
		}
		nodeList.repaint();
		fireNodesUpdated(nodes);
	}

	/**
	 * Returns the node list.
	 *
	 * @return The list of nodes
	 */
	public Node[] getNodes() {
		Node[] returnNodes = new Node[nodeListModel.getSize()];
		for (int nodeIndex = 0, nodeCount = nodeListModel.getSize(); nodeIndex < nodeCount; nodeIndex++) {
			returnNodes[nodeIndex] = (Node) nodeListModel.get(nodeIndex);
		}
		return returnNodes;
	}

	/**
	 * Returns the currently selected node.
	 *
	 * @return The selected node, or <code>null</code> if no node is selected
	 */
	private Node getSelectedNode() {
		return (Node) nodeList.getSelectedValue();
	}

	/**
	 * Updates node name or hostname when the user types into the textfields.
	 *
	 * @see #insertUpdate(DocumentEvent)
	 * @see #removeUpdate(DocumentEvent)
	 * @see #changedUpdate(DocumentEvent)
	 * @see DocumentListener
	 * @param documentEvent
	 *            The document event
	 */
	private void updateTextField(DocumentEvent documentEvent) {
		Node node = getSelectedNode();
		if (node == null) {
			return;
		}
		Document document = documentEvent.getDocument();
		String documentText = null;
		try {
			documentText = document.getText(0, document.getLength());
		} catch (BadLocationException ble1) {
			/* ignore. */
		}
		if (documentText == null) {
			return;
		}
		String documentName = (String) document.getProperty("Name");
		if ("node-name".equals(documentName)) {
			node.setName(documentText);
			nodeList.repaint();
			fireNodesUpdated(getNodes());
		} else if ("node-hostname".equals(documentName)) {
			node.setHostname(documentText);
			nodeList.repaint();
			fireNodesUpdated(getNodes());
		}
	}

	//
	// ACTIONS
	//

	/**
	 * Adds a new node to the list of nodes.
	 */
	private void addNode() {
		Node node = new Node("localhost", 9481, I18n.getMessage("jsite.node-manager.new-node"));
		nodeListModel.addElement(node);
        nodeList.setSelectedIndex(nodeListModel.size() - 1);
		deleteNodeAction.setEnabled(nodeListModel.size() > 1);
		wizard.setNextEnabled(true);
		fireNodesUpdated(getNodes());
	}

	/**
	 * Deletes the currently selected node from the list of nodes.
	 */
	private void deleteNode() {
		Node node = getSelectedNode();
		if (node == null) {
			return;
		}
		if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.node-manager.delete-node.warning"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
			return;
		}
		int nodeIndex = nodeListModel.indexOf(node);
		nodeListModel.removeElement(node);
		nodeList.repaint();
		fireNodeSelected((Node) nodeListModel.get(Math.min(nodeIndex, nodeListModel.size() - 1)));
		fireNodesUpdated(getNodes());
		deleteNodeAction.setEnabled(nodeListModel.size() > 1);
		wizard.setNextEnabled(nodeListModel.size() > 0);
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
		Object source = e.getSource();
		if (source instanceof JList) {
			JList sourceList = (JList) source;
			if ("node-list".equals(sourceList.getName())) {
				Node node = (Node) sourceList.getSelectedValue();
				boolean enabled = (node != null);
				nodeNameTextField.setEnabled(enabled);
				nodeHostnameTextField.setEnabled(enabled);
				nodePortSpinner.setEnabled(enabled);
				deleteNodeAction.setEnabled(enabled && (nodeListModel.size() > 1));
				if (enabled) {
					nodeNameTextField.setText(node.getName());
					nodeHostnameTextField.setText(node.getHostname());
					nodePortSpinner.setValue(node.getPort());
				} else {
					nodeNameTextField.setText("");
					nodeHostnameTextField.setText("localhost");
					nodePortSpinner.setValue(9481);
				}
			}
		}
	}

	//
	// INTERFACE DocumentListener
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		updateTextField(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		updateTextField(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
		updateTextField(e);
	}

	//
	// INTERFACE ChangeListener
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		Node selectedNode = getSelectedNode();
		if (selectedNode == null) {
			return;
		}
		if (source instanceof JSpinner) {
			JSpinner sourceSpinner = (JSpinner) source;
			if ("node-port".equals(sourceSpinner.getName())) {
				selectedNode.setPort((Integer) sourceSpinner.getValue());
				fireNodeSelected(selectedNode);
				nodeList.repaint();
			}
		}
	}

}

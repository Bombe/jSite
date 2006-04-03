/*
 * jSite-0.7 - 
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
import de.todesbaum.util.swing.TLabel;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id: NodeManagerPage.java 418 2006-03-29 17:49:16Z bombe $
 */
public class NodeManagerPage extends TWizardPage implements ListSelectionListener, DocumentListener, ChangeListener {

	private List<NodeManagerListener> nodeManagerListeners = new ArrayList<NodeManagerListener>();
	private TWizard wizard;

	private Action addNodeAction;
	private Action deleteNodeAction;
	private DefaultListModel nodeListModel;
	private JList nodeList;
	private JTextField nodeNameTextField;
	private JTextField nodeHostnameTextField;
	private JSpinner nodePortSpinner;

	public NodeManagerPage() {
		super();
		pageInit();
		setHeading(I18n.getMessage("jsite.node-manager.heading"));
		setDescription(I18n.getMessage("jsite.node-manager.description"));
	}
	
	public void addNodeManagerListener(NodeManagerListener nodeManagerListener) {
		nodeManagerListeners.add(nodeManagerListener);
	}
	
	public void removeNodeManagerListener(NodeManagerListener nodeManagerListener) {
		nodeManagerListeners.remove(nodeManagerListener);
	}
	
	protected void fireNodesUpdated(Node[] nodes) {
		for (NodeManagerListener nodeManagerListener: nodeManagerListeners) {
			nodeManagerListener.nodesUpdated(nodes);
		}
	}

	private void createActions() {
		addNodeAction = new AbstractAction(I18n.getMessage("jsite.node-manager.add-node")) {

			public void actionPerformed(ActionEvent actionEvent) {
				addNode();
			}
		};

		deleteNodeAction = new AbstractAction(I18n.getMessage("jsite.node-manager.delete-node")) {

			public void actionPerformed(ActionEvent actionEvent) {
				deleteNode();
			}
		};
		deleteNodeAction.setEnabled(false);
	}

	private void pageInit() {
		createActions();
		nodeListModel = new DefaultListModel();
		nodeList = new JList(nodeListModel);
		nodeList.setName("node-list");
		nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeList.addListSelectionListener(this);
		nodeList.setPreferredSize(new Dimension(250, -1));

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
		nodeInformationPanel.add(new JLabel("<html><b>" + I18n.getMessage("jsite.node-manager.node-information") + "</b></html>"), new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
		nodeInformationPanel.add(new TLabel(I18n.getMessage("jsite.node-manager.name"), KeyEvent.VK_N, nodeNameTextField), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		nodeInformationPanel.add(nodeNameTextField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		nodeInformationPanel.add(new TLabel(I18n.getMessage("jsite.node-manager.hostname"), KeyEvent.VK_H, nodeHostnameTextField), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		nodeInformationPanel.add(nodeHostnameTextField, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		nodeInformationPanel.add(new TLabel(I18n.getMessage("jsite.node-manager.port"), KeyEvent.VK_P, nodePortSpinner), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 18, 0, 0), 0, 0));
		nodeInformationPanel.add(nodePortSpinner, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));

		setLayout(new BorderLayout(12, 12));
		add(new JScrollPane(nodeList), BorderLayout.LINE_START);
		add(centerPanel, BorderLayout.CENTER);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pageAdded(TWizard wizard) {
		this.wizard = wizard;
		wizard.setNextEnabled(nodeListModel.getSize() > 0);
	}

	public void setNodes(Node[] nodes) {
		nodeListModel.clear();
		for (Node node: nodes) {
			nodeListModel.addElement(node);
		}
		nodeList.repaint();
		fireNodesUpdated(nodes);
	}

	public Node[] getNodes() {
		Node[] returnNodes = new Node[nodeListModel.getSize()];
		for (int nodeIndex = 0, nodeCount = nodeListModel.getSize(); nodeIndex < nodeCount; nodeIndex++) {
			returnNodes[nodeIndex] = (Node) nodeListModel.get(nodeIndex);
		}
		return returnNodes;
	}

	private Node getSelectedNode() {
		return (Node) nodeList.getSelectedValue();
	}
	
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
		}
	}

	//
	// ACTIONS
	//

	protected void addNode() {
		Node node = new Node("localhost", 9481, I18n.getMessage("jsite.node-manager.new-node"));
		nodeListModel.addElement(node);
		wizard.setNextEnabled(true);
		fireNodesUpdated(getNodes());
	}

	protected void deleteNode() {
		Node node = getSelectedNode();
		if (node == null) {
			return;
		}
		if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.node-manager.delete-node.warning"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
			return;
		}
		nodeListModel.removeElement(node);
		nodeList.repaint();
		fireNodesUpdated(getNodes());
		wizard.setNextEnabled(nodeListModel.size() > 0);
	}

	//
	// INTERFACE ListSelectionListener
	//

	/**
	 * {@inheritDoc}
	 */
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
				deleteNodeAction.setEnabled(enabled);
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
	public void insertUpdate(DocumentEvent e) {
		updateTextField(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeUpdate(DocumentEvent e) {
		updateTextField(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public void changedUpdate(DocumentEvent e) {
		updateTextField(e);
	}

	//
	// INTERFACE ChangeListener
	//

	/**
	 * {@inheritDoc}
	 */
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
				nodeList.repaint();
			}
		}
	}

}

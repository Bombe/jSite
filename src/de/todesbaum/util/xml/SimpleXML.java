/*
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

package de.todesbaum.util.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * SimpleXML is a helper class to construct XML trees in a fast and simple way.
 * Construct a new XML tree by calling {@link #SimpleXML(String)} and append new
 * nodes by calling {@link #append(String)}.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id:SimpleXML.java 221 2006-03-06 14:46:49Z bombe $
 */
public class SimpleXML {

	/**
	 * A {@link List} containing all child nodes of this node.
	 */
	private List<SimpleXML> children = new ArrayList<SimpleXML>();

	/**
	 * The name of this node.
	 */
	private String name = null;

	/**
	 * The value of this node.
	 */
	private String value = null;

	/**
	 * Constructs a new XML node without a name.
	 */
	public SimpleXML() {
		super();
	}

	/**
	 * Constructs a new XML node with the specified name.
	 *
	 * @param name
	 *            The name of the new node
	 */
	public SimpleXML(String name) {
		this.name = name;
	}

	/**
	 * Returns the child node of this node with the specified name. If there are
	 * several child nodes with the specified name only the first node is
	 * returned.
	 *
	 * @param nodeName
	 *            The name of the child node
	 * @return The child node, or <code>null</code> if there is no child node
	 *         with the specified name
	 */
	public SimpleXML getNode(String nodeName) {
		for (int index = 0, count = children.size(); index < count; index++) {
			if (children.get(index).name.equals(nodeName)) {
				return children.get(index);
			}
		}
		return null;
	}

	/**
	 * Returns the child node that is specified by the names. The first element
	 * of <code>nodeNames</code> is the name of the child node of this node, the
	 * second element of <code>nodeNames</code> is the name of a child node's
	 * child node, and so on. By using this method you can descend into an XML
	 * tree pretty fast.
	 *
	 * <pre>
	 *
	 * SimpleXML deepNode = topNode.getNodes(new String[] { &quot;person&quot;, &quot;address&quot;, &quot;number&quot; });
	 * </pre>
	 *
	 * @param nodeNames
	 * @return A node that is a deep child of this node, or <code>null</code> if
	 *         the specified node does not eixst
	 */
	public SimpleXML getNode(String[] nodeNames) {
		SimpleXML node = this;
		for (String nodeName : nodeNames) {
			node = node.getNode(nodeName);
		}
		return node;
	}

	/**
	 * Returns all child nodes of this node.
	 *
	 * @return All child nodes of this node
	 */
	public SimpleXML[] getNodes() {
		return getNodes(null);
	}

	/**
	 * Returns all child nodes of this node with the specified name. If there
	 * are no child nodes with the specified name an empty array is returned.
	 *
	 * @param nodeName
	 *            The name of the nodes to retrieve, or <code>null</code> to
	 *            retrieve all nodes
	 * @return All child nodes with the specified name
	 */
	public SimpleXML[] getNodes(String nodeName) {
		List<SimpleXML> resultList = new ArrayList<SimpleXML>();
		for (SimpleXML child : children) {
			if ((nodeName == null) || child.name.equals(nodeName)) {
				resultList.add(child);
			}
		}
		return resultList.toArray(new SimpleXML[resultList.size()]);
	}

	/**
	 * Appends a new XML node with the specified name and returns the new node.
	 * With this method you can create deep structures very fast.
	 *
	 * <pre>
	 *
	 * SimpleXML mouseNode = topNode.append(&quot;computer&quot;).append(&quot;bus&quot;).append(&quot;usb&quot;).append(&quot;mouse&quot;);
	 * </pre>
	 *
	 * @param nodeName
	 *            The name of the node to append as a child to this node
	 * @return The new node
	 */
	public SimpleXML append(String nodeName) {
		return append(new SimpleXML(nodeName));
	}

	/**
	 * Appends a new XML node with the specified name and value and returns the
	 * new node.
	 *
	 * @param nodeName
	 *            The name of the node to append
	 * @param nodeValue
	 *            The value of the node to append
	 * @return The newly appended node
	 */
	public SimpleXML append(String nodeName, String nodeValue) {
		return append(nodeName).setValue(nodeValue);
	}

	/**
	 * Appends the node with all its child nodes to this node and returns the
	 * child node.
	 *
	 * @param newChild
	 *            The node to append as a child
	 * @return The child node that was appended
	 */
	public SimpleXML append(SimpleXML newChild) {
		children.add(newChild);
		return newChild;
	}

	public void remove(SimpleXML child) {
		children.remove(child);
	}

	public void remove(String childName) {
		SimpleXML child = getNode(childName);
		if (child != null) {
			remove(child);
		}
	}

	public void replace(String childName, String value) {
		remove(childName);
		append(childName, value);
	}

	public void replace(SimpleXML childNode) {
		remove(childNode.getName());
		append(childNode);
	}

	public void removeAll() {
		children.clear();
	}

	/**
	 * Sets the value of this node.
	 *
	 * @param nodeValue
	 *            The new value of this node
	 * @return This node
	 */
	public SimpleXML setValue(String nodeValue) {
		value = nodeValue;
		return this;
	}

	/**
	 * Returns the name of this node.
	 *
	 * @return The name of this node
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value of this node.
	 *
	 * @return The value of this node
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the value of this node. If the node does not have a value, the
	 * given default value is returned.
	 *
	 *@param defaultValue
	 *            The default value to return if the node does not have a value
	 * @return The value of this node
	 */
	public String getValue(String defaultValue) {
		return (value == null) ? defaultValue : value;
	}

	/**
	 * Creates a {@link Document} from this node and all its child nodes.
	 *
	 * @return The {@link Document} created from this node
	 */
	public Document getDocument() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			Element rootElement = document.createElement(name);
			document.appendChild(rootElement);
			addChildren(rootElement);
			return document;
		} catch (ParserConfigurationException e) {
		}
		return null;
	}

	/**
	 * Appends all children of this node to the specified {@link Element}. If a
	 * node has a value that is not <code>null</code> the value is appended as a
	 * text node.
	 *
	 * @param rootElement
	 *            The element to attach this node's children to
	 */
	private void addChildren(Element rootElement) {
		for (SimpleXML child : children) {
			Element childElement = rootElement.getOwnerDocument().createElement(child.name);
			rootElement.appendChild(childElement);
			if (child.value != null) {
				Text childText = rootElement.getOwnerDocument().createTextNode(child.value);
				childElement.appendChild(childText);
			} else {
				child.addChildren(childElement);
			}
		}
	}

	/**
	 * Creates a SimpleXML node from the specified {@link Document}. The
	 * SimpleXML node of the document's top-level node is returned.
	 *
	 * @param document
	 *            The {@link Document} to create a SimpleXML node from
	 * @return The SimpleXML node created from the document's top-level node
	 */
	public static SimpleXML fromDocument(Document document) {
		SimpleXML xmlDocument = new SimpleXML(document.getFirstChild().getNodeName());
		document.normalizeDocument();
		return addDocumentChildren(xmlDocument, document.getFirstChild());
	}

	/**
	 * Appends the child nodes of the specified {@link Document} to this node.
	 * Text nodes are converted into a node's value.
	 *
	 * @param xmlDocument
	 *            The SimpleXML node to append the child nodes to
	 * @param document
	 *            The document whose child nodes to append
	 * @return The SimpleXML node the child nodes were appended to
	 */
	private static SimpleXML addDocumentChildren(SimpleXML xmlDocument, Node document) {
		NodeList childNodes = document.getChildNodes();
		for (int childIndex = 0, childCount = childNodes.getLength(); childIndex < childCount; childIndex++) {
			Node childNode = childNodes.item(childIndex);
			if ((childNode.getChildNodes().getLength() == 1) && (childNode.getFirstChild().getNodeName().equals("#text"))) {
				xmlDocument.append(childNode.getNodeName(), childNode.getFirstChild().getNodeValue());
			} else {
				if (!childNode.getNodeName().equals("#text") || (childNode.getChildNodes().getLength() != 0)) {
					SimpleXML newXML = xmlDocument.append(childNode.getNodeName());
					addDocumentChildren(newXML, childNode);
				}
			}
		}
		return xmlDocument;
	}

}

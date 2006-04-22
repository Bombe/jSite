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

package de.todesbaum.jsite.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import de.todesbaum.jsite.application.EditionProject;
import de.todesbaum.jsite.application.FileOption;
import de.todesbaum.jsite.application.Node;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.util.io.StreamCopier;
import de.todesbaum.util.xml.SimpleXML;
import de.todesbaum.util.xml.XML;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id: Configuration.java 418 2006-03-29 17:49:16Z bombe $
 */
public class Configuration {

	private String filename;
	private String lockFilename;
	private SimpleXML rootNode;

	public Configuration() {
		filename = System.getProperty("user.home") + "/.jSite/config7";
		lockFilename = System.getProperty("user.home") + "/.jSite/lock7";
		readConfiguration();
	}
	
	private boolean createConfigDirectory() {
		File configDirectory = new File(System.getProperty("user.home"), ".jSite");
		return (configDirectory.exists() && configDirectory.isDirectory()) || configDirectory.mkdirs();
	}

	public boolean createLockFile() {
		if (!createConfigDirectory()) {
			return false;
		}
		File lockFile = new File(lockFilename);
		lockFile.deleteOnExit();
		try {
			return lockFile.createNewFile();
		} catch (IOException e) {
		}
		return false;
	}

	private void readConfiguration() {
		File configurationFile = new File(filename);
		if (configurationFile.exists()) {
			ByteArrayOutputStream fileByteOutputStream = null;
			FileInputStream fileInputStream = null;
			try {
				fileByteOutputStream = new ByteArrayOutputStream();
				fileInputStream = new FileInputStream(configurationFile);
				StreamCopier.copy(fileInputStream, fileByteOutputStream, configurationFile.length());
				fileByteOutputStream.close();
				byte[] fileBytes = fileByteOutputStream.toByteArray();
				rootNode = SimpleXML.fromDocument(XML.transformToDocument(fileBytes));
				return;
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} finally {
				if (fileInputStream != null) {
					try {
						fileInputStream.close();
					} catch (IOException ioe1) {
					}
				}
				if (fileByteOutputStream != null) {
					try {
						fileByteOutputStream.close();
					} catch (IOException ioe1) {
					}
				}
			}
		}
		rootNode = new SimpleXML("configuration");
	}

	public boolean save() {
		File configurationFile = new File(filename);
		if (!configurationFile.exists()) {
			File configurationFilePath = configurationFile.getParentFile();
			if (!configurationFilePath.exists() && !configurationFilePath.mkdirs()) {
				return false;
			}
		}
		FileOutputStream fileOutputStream = null;
		ByteArrayInputStream configurationInputStream = null;
		try {
			byte[] configurationBytes = XML.transformToByteArray(rootNode.getDocument());
			configurationInputStream = new ByteArrayInputStream(configurationBytes);
			fileOutputStream = new FileOutputStream(configurationFile);
			StreamCopier.copy(configurationInputStream, fileOutputStream, configurationBytes.length);
			return true;
		} catch (IOException ioe1) {
		} finally {
			if (configurationInputStream != null) {
				try {
					configurationInputStream.close();
				} catch (IOException ioe1) {
				}
			}
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException ioe1) {
				}
			}
		}
		return false;
	}

	private String getNodeValue(String[] nodeNames, String defaultValue) {
		SimpleXML node = rootNode;
		int nodeIndex = 0;
		while ((node != null) && (nodeIndex < nodeNames.length)) {
			node = node.getNode(nodeNames[nodeIndex++]);
		}
		if (node == null) {
			return defaultValue;
		}
		return node.getValue();
	}

	private int getNodeIntValue(String[] nodeNames, int defaultValue) {
		try {
			return Integer.parseInt(getNodeValue(nodeNames, String.valueOf(defaultValue)));
		} catch (NumberFormatException nfe1) {
		}
		return defaultValue;
	}

	private boolean getNodeBooleanValue(String[] nodeNames, boolean defaultValue) {
		String nodeValue = getNodeValue(nodeNames, null);
		if (nodeValue == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(nodeValue);
	}

	/**
	 * Returns the hostname of the node.
	 * @return The hostname of the node
	 * @deprecated Use {@link #getSelectedNode()} instead
	 */
	public String getNodeAddress() {
		return getNodeValue(new String[] { "node-address" }, "localhost");
	}

	/**
	 * Sets the hostname of the node.
	 * @param nodeAddress The hostname of the node
	 * @deprecated Use {@link #setSelectedNode(Node)} instead
	 */
	public void setNodeAddress(String nodeAddress) {
		rootNode.replace("node-address", nodeAddress);
	}

	/**
	 * The port number of the node
	 * @return The port number of the node
	 * @deprecated Use {@link #getSelectedNode()} instead. 
	 */
	public int getNodePort() {
		return getNodeIntValue(new String[] { "node-port" }, 9481);
	}

	/**
	 * Sets the port number of the node.
	 * @param nodePort The port number of the node
	 * @deprecated Use {@link #setSelectedNode(Node)} instead
	 */
	public void setNodePort(int nodePort) {
		rootNode.replace("node-port", String.valueOf(nodePort));
	}

	public boolean isSkipNodePage() {
		return getNodeBooleanValue(new String[] { "skip-node-page" }, false);
	}

	public void setSkipNodePage(boolean skipNodePage) {
		rootNode.replace("skip-node-page", String.valueOf(skipNodePage));
	}

	public Project[] getProjects() {
		List<Project> projects = new ArrayList<Project>();
		SimpleXML projectsNode = rootNode.getNode("project-list");
		if (projectsNode != null) {
			SimpleXML[] projectNodes = projectsNode.getNodes("project");
			for (SimpleXML projectNode: projectNodes) {
				try {
					Project project = null;
					SimpleXML typeNode = projectNode.getNode("type");
					if ("edition".equals(typeNode.getValue())) {
						EditionProject editionProject = new EditionProject();
						project = editionProject;
						editionProject.setEdition(Integer.parseInt(projectNode.getNode("edition").getValue()));
					}
					projects.add(project);
					project.setDescription(projectNode.getNode("description").getValue());
					project.setIndexFile(projectNode.getNode("index-file").getValue());
					project.setLastInsertionTime(Long.parseLong(projectNode.getNode("last-insertion-time").getValue()));
					project.setLocalPath(projectNode.getNode("local-path").getValue());
					project.setName(projectNode.getNode("name").getValue());
					project.setPath(projectNode.getNode("path").getValue());
					project.setInsertURI(projectNode.getNode("insert-uri").getValue());
					project.setRequestURI(projectNode.getNode("request-uri").getValue());
					SimpleXML fileOptionsNode = projectNode.getNode("file-options");
					Map<String, FileOption> fileOptions = new HashMap<String, FileOption>();
					if (fileOptionsNode != null) {
						SimpleXML[] fileOptionNodes = fileOptionsNode.getNodes("file-option");
						for (SimpleXML fileOptionNode: fileOptionNodes) {
							String filename = fileOptionNode.getNode("filename").getValue();
							FileOption fileOption = project.getFileOption(filename);
							fileOption.setInsert(Boolean.parseBoolean(fileOptionNode.getNode("insert").getValue()));
							fileOption.setCustomKey(fileOptionNode.getNode("custom-key").getValue());
							fileOption.setMimeType(fileOptionNode.getNode("mime-type").getValue());
							fileOption.setContainer(fileOptionNode.getNode("container").getValue());
							if (fileOptionNode.getNode("replace-edition") != null) {
								fileOption.setReplaceEdition(Boolean.parseBoolean(fileOptionNode.getNode("replace-edition").getValue()));
								fileOption.setEditionRange(Integer.parseInt(fileOptionNode.getNode("edition-range").getValue()));
							}
							fileOptions.put(filename, fileOption);
						}
					}
					project.setFileOptions(fileOptions);
				} catch (NumberFormatException nfe1) {
					nfe1.printStackTrace();
				}
			}
		}
		return projects.toArray(new Project[projects.size()]);
	}

	public void setProjects(Project[] projects) {
		SimpleXML projectsNode = new SimpleXML("project-list");
		for (Project project: projects) {
			SimpleXML projectNode = projectsNode.append("project");
			if (project instanceof EditionProject) {
				projectNode.append("type", "edition");
				projectNode.append("edition", String.valueOf(((EditionProject) project).getEdition()));
			}
			projectNode.append("description", project.getDescription());
			projectNode.append("index-file", project.getIndexFile());
			projectNode.append("last-insertion-time", String.valueOf(project.getLastInsertionTime()));
			projectNode.append("local-path", project.getLocalPath());
			projectNode.append("name", project.getName());
			projectNode.append("path", project.getPath());
			projectNode.append("insert-uri", project.getInsertURI());
			projectNode.append("request-uri", project.getRequestURI());
			SimpleXML fileOptionsNode = projectNode.append("file-options");
			Iterator<Entry<String, FileOption>> entries = project.getFileOptions().entrySet().iterator();
			while (entries.hasNext()) {
				Entry<String, FileOption> entry = entries.next();
				FileOption fileOption = entry.getValue();
				if (fileOption.isCustom()) {
					SimpleXML fileOptionNode = fileOptionsNode.append("file-option");
					fileOptionNode.append("filename", entry.getKey());
					fileOptionNode.append("insert", String.valueOf(fileOption.isInsert()));
					fileOptionNode.append("custom-key", fileOption.getCustomKey());
					fileOptionNode.append("mime-type", fileOption.getMimeType());
					fileOptionNode.append("container", fileOption.getContainer());
					fileOptionNode.append("replace-edition", String.valueOf(fileOption.getReplaceEdition()));
					fileOptionNode.append("edition-range", String.valueOf(fileOption.getEditionRange()));
				}
			}
		}
		rootNode.replace(projectsNode);
	}

	public Locale getLocale() {
		String language = getNodeValue(new String[] { "i18n", "language" }, "en");
		String country = getNodeValue(new String[] { "i18n", "country" }, null);
		if (country != null) {
			return new Locale(language, country);
		}
		return new Locale(language);
	}

	public void setLocale(Locale locale) {
		SimpleXML i18nNode = new SimpleXML("i18n");
		if (locale.getCountry().length() != 0) {
			i18nNode.append("country", locale.getCountry());
		}
		i18nNode.append("language", locale.getLanguage());
		rootNode.replace(i18nNode);
		return;
	}
	
	public Node[] getNodes() {
		SimpleXML nodesNode = rootNode.getNode("nodes");
		if (nodesNode == null) {
			String hostname = getNodeAddress();
			int port = getNodePort();
			if (hostname == null) {
				hostname = "127.0.0.1";
				port = 9481;
			}
			return new Node[] { new Node(hostname, port, "Node") };
		}
		SimpleXML[] nodeNodes = nodesNode.getNodes("node");
		Node[] returnNodes = new Node[nodeNodes.length];
		int nodeIndex = 0;
		for (SimpleXML nodeNode: nodeNodes) {
			String name = nodeNode.getNode("name").getValue();
			String hostname = nodeNode.getNode("hostname").getValue();
			int port = Integer.parseInt(nodeNode.getNode("port").getValue());
			Node node = new Node(hostname, port, name);
			returnNodes[nodeIndex++] = node;
		}
		return returnNodes;
	}
	
	public void setNodes(Node[] nodes) {
		SimpleXML nodesNode = new SimpleXML("nodes");
		for (Node node: nodes) {
			SimpleXML nodeNode = nodesNode.append("node");
			nodeNode.append("name", node.getName());
			nodeNode.append("hostname", node.getHostname());
			nodeNode.append("port", String.valueOf(node.getPort()));
		}
		rootNode.replace(nodesNode);
		rootNode.remove("node-address");
		rootNode.remove("node-port");
	}

	public void setSelectedNode(Node selectedNode) {
		SimpleXML selectedNodeNode = new SimpleXML("selected-node");
		selectedNodeNode.append("name", selectedNode.getName());
		selectedNodeNode.append("hostname", selectedNode.getHostname());
		selectedNodeNode.append("port", String.valueOf(selectedNode.getPort()));
		rootNode.replace(selectedNodeNode);
	}
	
	public Node getSelectedNode() {
		SimpleXML selectedNodeNode = rootNode.getNode("selected-node");
		if (selectedNodeNode == null) {
			String hostname = getNodeAddress();
			int port = getNodePort();
			if (hostname == null) {
				hostname = "127.0.0.1";
				port = 9481;
			}
			return new Node(hostname, port, "Node");
		}
		String name = selectedNodeNode.getNode("name").getValue();
		String hostname = selectedNodeNode.getNode("hostname").getValue();
		int port = Integer.valueOf(selectedNodeNode.getNode("port").getValue());
		return new Node(hostname, port, name);
	}
	
}

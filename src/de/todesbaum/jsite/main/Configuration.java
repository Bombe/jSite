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

import de.todesbaum.jsite.application.FileOption;
import de.todesbaum.jsite.application.Node;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.util.io.Closer;
import de.todesbaum.util.io.StreamCopier;
import de.todesbaum.util.xml.SimpleXML;
import de.todesbaum.util.xml.XML;

/**
 * The configuration.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Configuration {

	/** The name of the file the configuration is stored to. */
	private String filename;

	/** The name of the lock file. */
	private String lockFilename;

	/** The root node of the configuration. */
	private SimpleXML rootNode;

	/**
	 * Creates a new configuration with the default name of the configuration
	 * file.
	 */
	public Configuration() {
		this(System.getProperty("user.home") + "/.jSite/config7");
	}

	/**
	 * Creates a new configuration that is read from the given file.
	 *
	 * @param filename
	 *            The name of the configuration file
	 */
	public Configuration(String filename) {
		this(filename, filename + ".lock");
	}

	/**
	 * Creates a new configuration that is read from the given file and uses the
	 * given lock file.
	 *
	 * @param filename
	 *            The name of the configuration file
	 * @param lockFilename
	 *            The name of the lock file
	 */
	public Configuration(String filename, String lockFilename) {
		this.filename = filename;
		this.lockFilename = lockFilename;
		readConfiguration();
	}

	/**
	 * Creates the directory of the configuration file.
	 *
	 * @return <code>true</code> if the directory exists, or if it could be
	 *         created, <code>false</code> otherwise
	 */
	private boolean createConfigDirectory() {
		File configDirectory = new File(filename).getAbsoluteFile().getParentFile();
		return (configDirectory.exists() && configDirectory.isDirectory()) || configDirectory.mkdirs();
	}

	/**
	 * Creates the lock file.
	 *
	 * @return <code>true</code> if the lock file did not already exist and
	 *         could be created, <code>false</code> otherwise
	 */
	public boolean createLockFile() {
		if (!createConfigDirectory()) {
			return false;
		}
		File lockFile = new File(lockFilename);
		try {
			boolean fileLocked = lockFile.createNewFile();
			if (fileLocked) {
				lockFile.deleteOnExit();
			}
			return fileLocked;
		} catch (IOException e) {
			/* ignore. */
		}
		return false;
	}

	/**
	 * Tells the VM to remove the lock file on program exit.
	 */
	public void removeLockfileOnExit() {
		new File(lockFilename).deleteOnExit();
	}

	/**
	 * Reads the configuration from the file.
	 */
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
				/* ignore. */
			} catch (IOException e) {
				/* ignore. */
			} finally {
				Closer.close(fileInputStream);
				Closer.close(fileByteOutputStream);
			}
		}
		rootNode = new SimpleXML("configuration");
	}

	/**
	 * Saves the configuration.
	 *
	 * @return <code>true</code> if the configuration could be saved,
	 *         <code>false</code> otherwise
	 */
	public boolean save() {
		File configurationFile = new File(filename);
		if (!configurationFile.exists()) {
			File configurationFilePath = configurationFile.getAbsoluteFile().getParentFile();
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
			/* ignore. */
		} finally {
			Closer.close(configurationInputStream);
			Closer.close(fileOutputStream);
		}
		return false;
	}

	/**
	 * Returns the value of a node.
	 *
	 * @param nodeNames
	 *            The name of all nodes in the chain
	 * @param defaultValue
	 *            The default value to return if the node could not be found
	 * @return The value of the node, or the default value if the node could not
	 *         be found
	 */
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

	/**
	 * Returns the integer value of a node.
	 *
	 * @param nodeNames
	 *            The names of all nodes in the chain
	 * @param defaultValue
	 *            The default value to return if the node can not be found
	 * @return The parsed integer value, or the default value if the node can
	 *         not be found or the value can not be parsed into an integer
	 */
	private int getNodeIntValue(String[] nodeNames, int defaultValue) {
		try {
			return Integer.parseInt(getNodeValue(nodeNames, String.valueOf(defaultValue)));
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		return defaultValue;
	}

	/**
	 * Returns the boolean value of a node.
	 *
	 * @param nodeNames
	 *            The names of all nodes in the chain
	 * @param defaultValue
	 *            The default value to return if the node can not be found
	 * @return The parsed boolean value, or the default value if the node can
	 *         not be found
	 */
	private boolean getNodeBooleanValue(String[] nodeNames, boolean defaultValue) {
		String nodeValue = getNodeValue(nodeNames, null);
		if (nodeValue == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(nodeValue);
	}

	/**
	 * Returns the hostname of the node.
	 *
	 * @return The hostname of the node
	 * @deprecated Use {@link #getSelectedNode()} instead
	 */
	@Deprecated
	public String getNodeAddress() {
		return getNodeValue(new String[] { "node-address" }, "localhost");
	}

	/**
	 * Sets the hostname of the node.
	 *
	 * @param nodeAddress
	 *            The hostname of the node
	 * @deprecated Use {@link #setSelectedNode(Node)} instead
	 */
	@Deprecated
	public void setNodeAddress(String nodeAddress) {
		rootNode.replace("node-address", nodeAddress);
	}

	/**
	 * The port number of the node
	 *
	 * @return The port number of the node
	 * @deprecated Use {@link #getSelectedNode()} instead.
	 */
	@Deprecated
	public int getNodePort() {
		return getNodeIntValue(new String[] { "node-port" }, 9481);
	}

	/**
	 * Sets the port number of the node.
	 *
	 * @param nodePort
	 *            The port number of the node
	 * @deprecated Use {@link #setSelectedNode(Node)} instead
	 */
	@Deprecated
	public void setNodePort(int nodePort) {
		rootNode.replace("node-port", String.valueOf(nodePort));
	}

	/**
	 * Returns whether the node configuration page should be skipped on startup.
	 *
	 * @return <code>true</code> to skip the node configuration page on startup,
	 *         <code>false</code> to show it
	 */
	public boolean isSkipNodePage() {
		return getNodeBooleanValue(new String[] { "skip-node-page" }, false);
	}

	/**
	 * Sets whether the node configuration page should be skipped on startup.
	 *
	 * @param skipNodePage
	 *            <code>true</code> to skip the node configuration page on
	 *            startup, <code>false</code> to show it
	 */
	public void setSkipNodePage(boolean skipNodePage) {
		rootNode.replace("skip-node-page", String.valueOf(skipNodePage));
	}

	/**
	 * Returns all configured projects.
	 *
	 * @return A list of all projects
	 */
	public Project[] getProjects() {
		List<Project> projects = new ArrayList<Project>();
		SimpleXML projectsNode = rootNode.getNode("project-list");
		if (projectsNode != null) {
			SimpleXML[] projectNodes = projectsNode.getNodes("project");
			for (SimpleXML projectNode : projectNodes) {
				try {
					Project project = new Project();
					projects.add(project);
					project.setDescription(projectNode.getNode("description").getValue(""));
					project.setIndexFile(projectNode.getNode("index-file").getValue(""));
					project.setLastInsertionTime(Long.parseLong(projectNode.getNode("last-insertion-time").getValue("0")));
					project.setLocalPath(projectNode.getNode("local-path").getValue(""));
					project.setName(projectNode.getNode("name").getValue(""));
					project.setPath(projectNode.getNode("path").getValue(""));
					if ((project.getPath() != null) && (project.getPath().indexOf("/") != -1)) {
						project.setPath(project.getPath().replaceAll("/", ""));
					}
					project.setEdition(Integer.parseInt(projectNode.getNode("edition").getValue("0")));
					project.setInsertURI(projectNode.getNode("insert-uri").getValue(""));
					project.setRequestURI(projectNode.getNode("request-uri").getValue(""));
					if (projectNode.getNode("ignore-hidden-files") != null) {
						project.setIgnoreHiddenFiles(Boolean.parseBoolean(projectNode.getNode("ignore-hidden-files").getValue("true")));
					} else {
						project.setIgnoreHiddenFiles(true);
					}
					SimpleXML fileOptionsNode = projectNode.getNode("file-options");
					Map<String, FileOption> fileOptions = new HashMap<String, FileOption>();
					if (fileOptionsNode != null) {
						SimpleXML[] fileOptionNodes = fileOptionsNode.getNodes("file-option");
						for (SimpleXML fileOptionNode : fileOptionNodes) {
							String filename = fileOptionNode.getNode("filename").getValue();
							FileOption fileOption = project.getFileOption(filename);
							fileOption.setInsert(Boolean.parseBoolean(fileOptionNode.getNode("insert").getValue()));
							if (fileOptionNode.getNode("insert-redirect") != null) {
								fileOption.setInsertRedirect(Boolean.parseBoolean(fileOptionNode.getNode("insert-redirect").getValue()));
							}
							fileOption.setCustomKey(fileOptionNode.getNode("custom-key").getValue(""));
							if (fileOptionNode.getNode("changed-name") != null) {
								fileOption.setChangedName(fileOptionNode.getNode("changed-name").getValue());
							}
							fileOption.setMimeType(fileOptionNode.getNode("mime-type").getValue(""));
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

	/**
	 * Sets the list of all projects.
	 *
	 * @param projects
	 *            The list of all projects
	 */
	public void setProjects(Project[] projects) {
		SimpleXML projectsNode = new SimpleXML("project-list");
		for (Project project : projects) {
			SimpleXML projectNode = projectsNode.append("project");
			projectNode.append("edition", String.valueOf(project.getEdition()));
			projectNode.append("description", project.getDescription());
			projectNode.append("index-file", project.getIndexFile());
			projectNode.append("last-insertion-time", String.valueOf(project.getLastInsertionTime()));
			projectNode.append("local-path", project.getLocalPath());
			projectNode.append("name", project.getName());
			projectNode.append("path", project.getPath());
			projectNode.append("insert-uri", project.getInsertURI());
			projectNode.append("request-uri", project.getRequestURI());
			projectNode.append("ignore-hidden-files", String.valueOf(project.isIgnoreHiddenFiles()));
			SimpleXML fileOptionsNode = projectNode.append("file-options");
			Iterator<Entry<String, FileOption>> entries = project.getFileOptions().entrySet().iterator();
			while (entries.hasNext()) {
				Entry<String, FileOption> entry = entries.next();
				FileOption fileOption = entry.getValue();
				if (fileOption.isCustom()) {
					SimpleXML fileOptionNode = fileOptionsNode.append("file-option");
					fileOptionNode.append("filename", entry.getKey());
					fileOptionNode.append("insert", String.valueOf(fileOption.isInsert()));
					fileOptionNode.append("insert-redirect", String.valueOf(fileOption.isInsertRedirect()));
					fileOptionNode.append("custom-key", fileOption.getCustomKey());
					fileOptionNode.append("changed-name", fileOption.getChangedName());
					fileOptionNode.append("mime-type", fileOption.getMimeType());
					fileOptionNode.append("container", fileOption.getContainer());
					fileOptionNode.append("replace-edition", String.valueOf(fileOption.getReplaceEdition()));
					fileOptionNode.append("edition-range", String.valueOf(fileOption.getEditionRange()));
				}
			}
		}
		rootNode.replace(projectsNode);
	}

	/**
	 * Returns the stored locale.
	 *
	 * @return The stored locale
	 */
	public Locale getLocale() {
		String language = getNodeValue(new String[] { "i18n", "language" }, "en");
		String country = getNodeValue(new String[] { "i18n", "country" }, null);
		if (country != null) {
			return new Locale(language, country);
		}
		return new Locale(language);
	}

	/**
	 * Sets the locale to store.
	 *
	 * @param locale
	 *            The locale to store
	 */
	public void setLocale(Locale locale) {
		SimpleXML i18nNode = new SimpleXML("i18n");
		if (locale.getCountry().length() != 0) {
			i18nNode.append("country", locale.getCountry());
		}
		i18nNode.append("language", locale.getLanguage());
		rootNode.replace(i18nNode);
		return;
	}

	/**
	 * Returns a list of configured nodes.
	 *
	 * @return The list of the configured nodes
	 */
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
		for (SimpleXML nodeNode : nodeNodes) {
			String name = nodeNode.getNode("name").getValue();
			String hostname = nodeNode.getNode("hostname").getValue();
			int port = Integer.parseInt(nodeNode.getNode("port").getValue());
			Node node = new Node(hostname, port, name);
			returnNodes[nodeIndex++] = node;
		}
		return returnNodes;
	}

	/**
	 * Sets the list of configured nodes.
	 *
	 * @param nodes
	 *            The list of configured nodes
	 */
	public void setNodes(Node[] nodes) {
		SimpleXML nodesNode = new SimpleXML("nodes");
		for (Node node : nodes) {
			SimpleXML nodeNode = nodesNode.append("node");
			nodeNode.append("name", node.getName());
			nodeNode.append("hostname", node.getHostname());
			nodeNode.append("port", String.valueOf(node.getPort()));
		}
		rootNode.replace(nodesNode);
		rootNode.remove("node-address");
		rootNode.remove("node-port");
	}

	/**
	 * Sets the selected node.
	 *
	 * @param selectedNode
	 *            The selected node
	 */
	public void setSelectedNode(Node selectedNode) {
		SimpleXML selectedNodeNode = new SimpleXML("selected-node");
		selectedNodeNode.append("name", selectedNode.getName());
		selectedNodeNode.append("hostname", selectedNode.getHostname());
		selectedNodeNode.append("port", String.valueOf(selectedNode.getPort()));
		rootNode.replace(selectedNodeNode);
	}

	/**
	 * Returns the selected node.
	 *
	 * @return The selected node
	 */
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

	/**
	 * Returns the temp directory to use.
	 *
	 * @return The temp directoy, or {@code null} to use the default temp
	 *         directory
	 */
	public String getTempDirectory() {
		return getNodeValue(new String[] { "temp-directory" }, null);
	}

	/**
	 * Sets the temp directory to use.
	 *
	 * @param tempDirectory
	 *            The temp directory to use, or {@code null} to use the default
	 *            temp directory
	 */
	public void setTempDirectory(String tempDirectory) {
		if (tempDirectory != null) {
			SimpleXML tempDirectoryNode = new SimpleXML("temp-directory");
			tempDirectoryNode.setValue(tempDirectory);
			rootNode.replace(tempDirectoryNode);
		} else {
			rootNode.remove("temp-directory");
		}
	}

}

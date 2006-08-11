/*
 * jSite - 
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

import java.io.PrintWriter;

import de.todesbaum.jsite.application.Freenet7Interface;
import de.todesbaum.jsite.application.InsertListener;
import de.todesbaum.jsite.application.Node;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.application.ProjectInserter;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class CLI implements InsertListener {

	private Object lockObject = new Object();
	private PrintWriter outputWriter = new PrintWriter(System.out, true);
	private Freenet7Interface freenetInterface;
	private ProjectInserter projectInserter = new ProjectInserter();
	private Node[] nodes;
	private Project[] projects;
	private boolean finished = false;
	private boolean success;

	private CLI(String[] args) {

		if ((args.length == 0) || args[0].equals("-h") || args[0].equals("--help")) {
			outputWriter.println("\nParameters:\n");
			outputWriter.println("  --node=<node name>");
			outputWriter.println("  --project=<project name>");
			outputWriter.println("  --local-directory=<local directory>");
			outputWriter.println("  --path=<path>");
			outputWriter.println("  --edition=<edition>");
			outputWriter.println("\nA project gets inserted when a new project is loaded on the command line,");
			outputWriter.println("or when the command line is finished. --local-directory, --path, and --edition");
			outputWriter.println("override the parameters in the project.");
			return;
		}

		Configuration configuration = new Configuration();
		if (!configuration.createLockFile()) {
			outputWriter.println("Lock file found!");
			return;
		}

		projectInserter.addInsertListener(this);
		projects = configuration.getProjects();
		Node node = configuration.getSelectedNode();
		nodes = configuration.getNodes();

		freenetInterface = new Freenet7Interface();
		freenetInterface.setNode(node);

		projectInserter.setFreenetInterface(freenetInterface);

		Project currentProject = null;
		for (String argument: args) {
			String value = argument.substring(argument.indexOf('=') + 1).trim();
			if (argument.startsWith("--node=")) {
				Node newNode = getNode(value);
				if (newNode == null) {
					outputWriter.println("Node \"" + value + "\" not found.");
					return;
				}
				node = newNode;
				freenetInterface.setNode(node);
			} else if (argument.startsWith("--project=")) {
				if (currentProject != null) {
					if (insertProject(currentProject)) {
						outputWriter.println("Project \"" + currentProject.getName() + "\" successfully inserted.");
					} else {
						outputWriter.println("Project \"" + currentProject.getName() + "\" was not successfully inserted.");
					}
					currentProject = null;
				}
				currentProject = getProject(value);
				if (currentProject == null) {
					outputWriter.println("Project \"" + value + "\" not found.");
				}
			} else if (argument.startsWith("--local-directory")) {
				if (currentProject == null) {
					outputWriter.println("You can't specifiy --local-directory before --project.");
					return;
				}
				currentProject.setLocalPath(value);
			} else if (argument.startsWith("--path=")) {
				if (currentProject == null) {
					outputWriter.println("You can't specify --path before --project.");
					return;
				}
				currentProject.setPath(value);
			} else if (argument.startsWith("--edition=")) {
				if (currentProject == null) {
					outputWriter.println("You can't specify --edition before --project.");
					return;
				}
				currentProject.setEdition(Integer.parseInt(value));
			} else {
				outputWriter.println("Unknown parameter: " + argument);
				return;
			}
		}

		if (currentProject != null) {
			if (insertProject(currentProject)) {
				outputWriter.println("Project \"" + currentProject.getName() + "\" successfully inserted.");
			} else {
				outputWriter.println("Project \"" + currentProject.getName() + "\" was not successfully inserted.");
			}
		}

		configuration.setProjects(projects);
		configuration.save();
	}

	private Project getProject(String name) {
		for (Project project: projects) {
			if (project.getName().equals(name)) {
				return project;
			}
		}
		return null;
	}

	private Node getNode(String name) {
		for (Node node: nodes) {
			if (node.getName().equals(name)) {
				return node;
			}
		}
		return null;
	}

	private boolean insertProject(Project currentProject) {
		if (!freenetInterface.hasNode()) {
			outputWriter.println("Node is not running!");
			return false;
		}
		projectInserter.setProject(currentProject);
		projectInserter.start();
		synchronized (lockObject) {
			while (!finished) {
				try {
					lockObject.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		return success;
	}

	//
	// INTERFACE InsertListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void projectInsertStarted(Project project) {
		outputWriter.println("Starting Insert of project \"" + project.getName() + "\".");
	}

	public void projectURIGenerated(Project project, String uri) {
		outputWriter.println("URI: " + uri);
	}

	/**
	 * {@inheritDoc}
	 */
	public void projectInsertProgress(Project project, int succeeded, int failed, int fatal, int total, boolean finalized) {
		outputWriter.println("Progress: " + succeeded + " done, " + failed + " failed, " + fatal + " fatal, " + total + " total" + (finalized ? " (finalized)" : "") + ", " + ((succeeded + failed + fatal) * 100 / total) + "%");
	}

	/**
	 * {@inheritDoc}
	 */
	public void projectInsertFinished(Project project, boolean success, Throwable cause) {
		outputWriter.println("Request URI: " + project.getFinalRequestURI(0));
		finished = true;
		this.success = success;
		synchronized (lockObject) {
			lockObject.notify();
		}
	}

	//
	// MAIN
	//

	public static void main(String[] args) {
		new CLI(args);
	}

}

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.todesbaum.jsite.application.FileOption;
import de.todesbaum.jsite.application.Freenet7Interface;
import de.todesbaum.jsite.application.Node;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.gui.NodeManagerListener;
import de.todesbaum.jsite.gui.NodeManagerPage;
import de.todesbaum.jsite.gui.ProjectFilesPage;
import de.todesbaum.jsite.gui.ProjectInsertPage;
import de.todesbaum.jsite.gui.ProjectPage;
import de.todesbaum.jsite.i18n.I18n;
import de.todesbaum.jsite.i18n.I18nContainer;
import de.todesbaum.util.image.IconLoader;
import de.todesbaum.util.swing.TWizard;
import de.todesbaum.util.swing.TWizardPage;
import de.todesbaum.util.swing.WizardListener;

/**
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Main implements ActionListener, ListSelectionListener, WizardListener, NodeManagerListener {

	private static boolean debug = false;
	private Configuration configuration;
	private Freenet7Interface freenetInterface = new Freenet7Interface();
	private Icon jSiteIcon;

	private static enum PageType {
		PAGE_NODE_MANAGER, PAGE_PROJECTS, PAGE_PROJECT_FILES, PAGE_INSERT_PROJECT
	}

	private static final Locale[] SUPPORTED_LOCALES = new Locale[] { Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH, Locale.ITALIAN, new Locale("pl") };
	private Map<Locale, Action> languageActions = new HashMap<Locale, Action>();
	private Action manageNodeAction;
	private Action aboutAction;
	private TWizard wizard;
	private JMenu nodeMenu;
	private Node selectedNode;
	private final Map<PageType, TWizardPage> pages = new HashMap<PageType, TWizardPage>();

	private Main() {
		this(null);
	}

	private Main(String configFilename) {
		if (configFilename != null) {
			configuration = new Configuration(configFilename);
		} else {
			configuration = new Configuration();
		}
		Locale.setDefault(configuration.getLocale());
		I18n.setLocale(configuration.getLocale());
		if (!configuration.createLockFile()) {
			JOptionPane.showMessageDialog(null, I18n.getMessage("jsite.main.already-running"), null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		wizard = new TWizard();
		createActions();
		wizard.setJMenuBar(createMenuBar());
		wizard.setQuitName(I18n.getMessage("jsite.wizard.quit"));
		wizard.setPreviousEnabled(false);
		wizard.setNextEnabled(true);
		wizard.addWizardListener(this);
		jSiteIcon = IconLoader.loadIcon("/jsite-icon.png");
		wizard.setIcon(jSiteIcon);

		initPages();
		showPage(PageType.PAGE_PROJECTS);
	}

	private void createActions() {
		for (final Locale locale : SUPPORTED_LOCALES) {
			languageActions.put(locale, new AbstractAction(I18n.getMessage("jsite.menu.language." + locale.getLanguage()), IconLoader.loadIcon("/flag-" + locale.getLanguage() + ".png")) {

				@SuppressWarnings("synthetic-access")
				public void actionPerformed(ActionEvent actionEvent) {
					switchLanguage(locale);
				}
			});
		}
		manageNodeAction = new AbstractAction(I18n.getMessage("jsite.menu.nodes.manage-nodes")) {

			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				showPage(PageType.PAGE_NODE_MANAGER);
				wizard.setPreviousName(I18n.getMessage("jsite.wizard.previous"));
				wizard.setNextName(I18n.getMessage("jsite.wizard.next"));
			}
		};
		aboutAction = new AbstractAction(I18n.getMessage("jsite.menu.help.about")) {

			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(wizard, MessageFormat.format(I18n.getMessage("jsite.about.message"), Version.getVersion()), null, JOptionPane.INFORMATION_MESSAGE, jSiteIcon);
			}
		};

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@SuppressWarnings("synthetic-access")
			public void run() {
				manageNodeAction.putValue(Action.NAME, I18n.getMessage("jsite.menu.nodes.manage-nodes"));
				aboutAction.putValue(Action.NAME, I18n.getMessage("jsite.menu.help.about"));
			}
		});
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		final JMenu languageMenu = new JMenu(I18n.getMessage("jsite.menu.languages"));
		menuBar.add(languageMenu);
		ButtonGroup languageButtonGroup = new ButtonGroup();
		for (Locale locale : SUPPORTED_LOCALES) {
			Action languageAction = languageActions.get(locale);
			JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(languageActions.get(locale));
			if (locale.equals(Locale.getDefault())) {
				menuItem.setSelected(true);
			}
			languageAction.putValue("menuItem", menuItem);
			languageButtonGroup.add(menuItem);
			languageMenu.add(menuItem);
		}
		nodeMenu = new JMenu(I18n.getMessage("jsite.menu.nodes"));
		menuBar.add(nodeMenu);
		selectedNode = configuration.getSelectedNode();
		nodesUpdated(configuration.getNodes());

		/* evil hack to right-align the help menu */
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		menuBar.add(panel);

		final JMenu helpMenu = new JMenu(I18n.getMessage("jsite.menu.help"));
		menuBar.add(helpMenu);
		helpMenu.add(aboutAction);

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@SuppressWarnings("synthetic-access")
			public void run() {
				languageMenu.setText(I18n.getMessage("jsite.menu.languages"));
				nodeMenu.setText(I18n.getMessage("jsite.menu.nodes"));
				helpMenu.setText(I18n.getMessage("jsite.menu.help"));
				for (Map.Entry<Locale, Action> languageActionEntry : languageActions.entrySet()) {
					languageActionEntry.getValue().putValue(Action.NAME, I18n.getMessage("jsite.menu.language." + languageActionEntry.getKey().getLanguage()));
				}
			}
		});

		return menuBar;
	}

	private void initPages() {
		NodeManagerPage nodeManagerPage = new NodeManagerPage(wizard);
		nodeManagerPage.setName("page.node-manager");
		nodeManagerPage.addNodeManagerListener(this);
		nodeManagerPage.setNodes(configuration.getNodes());
		pages.put(PageType.PAGE_NODE_MANAGER, nodeManagerPage);

		ProjectPage projectPage = new ProjectPage(wizard);
		projectPage.setName("page.project");
		projectPage.setProjects(configuration.getProjects());
		projectPage.setFreenetInterface(freenetInterface);
		projectPage.addListSelectionListener(this);
		pages.put(PageType.PAGE_PROJECTS, projectPage);

		ProjectFilesPage projectFilesPage = new ProjectFilesPage(wizard);
		projectFilesPage.setName("page.project.files");
		pages.put(PageType.PAGE_PROJECT_FILES, projectFilesPage);

		ProjectInsertPage projectInsertPage = new ProjectInsertPage(wizard);
		projectInsertPage.setDebug(debug);
		projectInsertPage.setName("page.project.insert");
		projectInsertPage.setFreenetInterface(freenetInterface);
		pages.put(PageType.PAGE_INSERT_PROJECT, projectInsertPage);
	}

	private void showPage(PageType pageType) {
		wizard.setPreviousEnabled(pageType.ordinal() > 0);
		wizard.setNextEnabled(pageType.ordinal() < (pages.size() - 1));
		wizard.setPage(pages.get(pageType));
		wizard.setTitle(pages.get(pageType).getHeading() + " - jSite");
	}

	private boolean saveConfiguration() {
		NodeManagerPage nodeManagerPage = (NodeManagerPage) pages.get(PageType.PAGE_NODE_MANAGER);
		configuration.setNodes(nodeManagerPage.getNodes());
		if (selectedNode != null) {
			configuration.setSelectedNode(selectedNode);
		}

		ProjectPage projectPage = (ProjectPage) pages.get(PageType.PAGE_PROJECTS);
		configuration.setProjects(projectPage.getProjects());

		return configuration.save();
	}

	private Locale findSupportedLocale(Locale forLocale) {
		for (Locale locale : SUPPORTED_LOCALES) {
			if (locale.equals(forLocale)) {
				return locale;
			}
		}
		for (Locale locale : SUPPORTED_LOCALES) {
			if (locale.getCountry().equals(forLocale.getCountry()) && locale.getLanguage().equals(forLocale.getLanguage())) {
				return locale;
			}
		}
		for (Locale locale : SUPPORTED_LOCALES) {
			if (locale.getLanguage().equals(forLocale.getLanguage())) {
				return locale;
			}
		}
		return SUPPORTED_LOCALES[0];
	}

	//
	// ACTIONS
	//

	private void switchLanguage(Locale locale) {
		Locale supportedLocale = findSupportedLocale(locale);
		Action languageAction = languageActions.get(supportedLocale);
		JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) languageAction.getValue("menuItem");
		menuItem.setSelected(true);
		I18n.setLocale(supportedLocale);
		for (Runnable i18nRunnable : I18nContainer.getInstance()) {
			try {
				i18nRunnable.run();
			} catch (Throwable t) {
				/* we probably shouldn't swallow this. */
			}
		}
		wizard.setPage(wizard.getPage());
		configuration.setLocale(supportedLocale);
	}

	//
	// INTERFACE ListSelectionListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void valueChanged(ListSelectionEvent e) {
		JList list = (JList) e.getSource();
		int selectedRow = list.getSelectedIndex();
		wizard.setNextEnabled(selectedRow > -1);
	}

	//
	// INTERFACE WizardListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void wizardNextPressed(TWizard wizard) {
		String pageName = wizard.getPage().getName();
		if ("page.node-manager".equals(pageName)) {
			showPage(PageType.PAGE_PROJECTS);
		} else if ("page.project".equals(pageName)) {
			ProjectPage projectPage = (ProjectPage) wizard.getPage();
			Project project = projectPage.getSelectedProject();
			if ((project.getLocalPath() == null) || (project.getLocalPath().trim().length() == 0)) {
				JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project.warning.no-local-path"), null, JOptionPane.ERROR_MESSAGE);
				return;
			}
			if ((project.getPath() == null) || (project.getPath().trim().length() == 0)) {
				JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project.warning.no-path"), null, JOptionPane.ERROR_MESSAGE);
				return;
			}
			((ProjectFilesPage) pages.get(PageType.PAGE_PROJECT_FILES)).setProject(project);
			((ProjectInsertPage) pages.get(PageType.PAGE_INSERT_PROJECT)).setProject(project);
			showPage(PageType.PAGE_PROJECT_FILES);
		} else if ("page.project.files".equals(pageName)) {
			ProjectPage projectPage = (ProjectPage) pages.get(PageType.PAGE_PROJECTS);
			Project project = projectPage.getSelectedProject();
			if (selectedNode == null) {
				JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project-files.no-node-selected"), null, JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (project.getIndexFile() == null) {
				if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.project-files.empty-index"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
					return;
				}
			} else {
				File indexFile = new File(project.getLocalPath(), project.getIndexFile());
				if (!indexFile.exists()) {
					JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project-files.index-missing"), null, JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			String indexFile = project.getIndexFile();
			boolean hasIndexFile = (indexFile != null);
			if (hasIndexFile && !project.getFileOption(indexFile).getContainer().equals("")) {
				if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.project-files.container-index"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
					return;
				}
			}
			if (hasIndexFile && !project.getFileOption(indexFile).getMimeType().equals("text/html")) {
				if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.project-files.index-not-html"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
					return;
				}
			}
			Map<String, FileOption> fileOptions = project.getFileOptions();
			Set<Entry<String, FileOption>> fileOptionEntries = fileOptions.entrySet();
			for (Entry<String, FileOption> fileOptionEntry : fileOptionEntries) {
				FileOption fileOption = fileOptionEntry.getValue();
				if (!fileOption.isInsert() && ((fileOption.getCustomKey().length() == 0) || "CHK@".equals(fileOption.getCustomKey()))) {
					JOptionPane.showMessageDialog(wizard, MessageFormat.format(I18n.getMessage("jsite.project-files.no-custom-key"), fileOptionEntry.getKey()), null, JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			boolean nodeRunning = false;
			try {
				nodeRunning = freenetInterface.isNodePresent();
			} catch (IOException e) {
			}
			if (!nodeRunning) {
				JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project-files.no-node-running"), null, JOptionPane.ERROR_MESSAGE);
				return;
			}
			configuration.save();
			showPage(PageType.PAGE_INSERT_PROJECT);
			((ProjectInsertPage) pages.get(PageType.PAGE_INSERT_PROJECT)).startInsert();
			nodeMenu.setEnabled(false);
		} else if ("page.project.insert".equals(pageName)) {
			showPage(PageType.PAGE_PROJECTS);
			nodeMenu.setEnabled(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void wizardPreviousPressed(TWizard wizard) {
		String pageName = wizard.getPage().getName();
		if ("page.project".equals(pageName)) {
			showPage(PageType.PAGE_NODE_MANAGER);
		} else if ("page.project.files".equals(pageName)) {
			showPage(PageType.PAGE_PROJECTS);
		} else if ("page.project.insert".equals(pageName)) {
			showPage(PageType.PAGE_PROJECT_FILES);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void wizardQuitPressed(TWizard wizard) {
		if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.quit.question"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
			if (saveConfiguration()) {
				System.exit(0);
			}
			if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.quit.config-not-saved"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
				System.exit(0);
			}
		}
	}

	//
	// INTERFACE NodeManagerListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void nodesUpdated(Node[] nodes) {
		nodeMenu.removeAll();
		ButtonGroup nodeButtonGroup = new ButtonGroup();
		Node newSelectedNode = null;
		for (Node node : nodes) {
			JRadioButtonMenuItem nodeMenuItem = new JRadioButtonMenuItem(node.getName());
			nodeMenuItem.putClientProperty("Node", node);
			nodeMenuItem.addActionListener(this);
			nodeButtonGroup.add(nodeMenuItem);
			if (node.equals(selectedNode)) {
				newSelectedNode = node;
				nodeMenuItem.setSelected(true);
			}
			nodeMenu.add(nodeMenuItem);
		}
		nodeMenu.addSeparator();
		nodeMenu.add(manageNodeAction);
		selectedNode = newSelectedNode;
		freenetInterface.setNode(selectedNode);
	}

	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof JRadioButtonMenuItem) {
			JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) source;
			Node node = (Node) menuItem.getClientProperty("Node");
			selectedNode = node;
			freenetInterface.setNode(selectedNode);
		}
	}

	//
	// MAIN METHOD
	//
	public static void main(String[] args) {
		String configFilename = null;
		boolean nextIsConfigFilename = false;
		for (String argument : args) {
			if (nextIsConfigFilename) {
				configFilename = argument;
				nextIsConfigFilename = false;
			}
			if ("--help".equals(argument)) {
				printHelp();
				return;
			} else if ("--debug".equals(argument)) {
				debug = true;
			} else if ("--config-file".equals(argument)) {
				nextIsConfigFilename = true;
			}
		}
		if (nextIsConfigFilename) {
			System.out.println("--config-file needs parameter!");
			return;
		}
		new Main(configFilename);
	}

	private static void printHelp() {
		System.out.println("--help\tshows this cruft");
		System.out.println("--debug\tenables some debug output");
		System.out.println("--config-file <file>\tuse specified configuration file");
	}

}

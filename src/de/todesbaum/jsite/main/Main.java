/*
 * jSite - a tool for uploading websites into Freenet
 * Copyright (C) 2006-2009 David Roden
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import de.todesbaum.jsite.application.UpdateChecker;
import de.todesbaum.jsite.application.UpdateListener;
import de.todesbaum.jsite.gui.NodeManagerListener;
import de.todesbaum.jsite.gui.NodeManagerPage;
import de.todesbaum.jsite.gui.PreferencesPage;
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
 * The main class that ties together everything.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Main implements ActionListener, ListSelectionListener, WizardListener, NodeManagerListener, UpdateListener {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(Main.class.getName());

	/** The version. */
	private static final Version VERSION = new Version(0, 8);

	/** The configuration. */
	private Configuration configuration;

	/** The freenet interface. */
	private Freenet7Interface freenetInterface = new Freenet7Interface();

	/** The update checker. */
	private final UpdateChecker updateChecker;

	/** The jSite icon. */
	private Icon jSiteIcon;

	/**
	 * Enumeration for all possible pages.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	private static enum PageType {

		/** The node manager page. */
		PAGE_NODE_MANAGER,

		/** The project page. */
		PAGE_PROJECTS,

		/** The project files page. */
		PAGE_PROJECT_FILES,

		/** The project insert page. */
		PAGE_INSERT_PROJECT,

		/** The preferences page. */
		PAGE_PREFERENCES

	}

	/** The supported locales. */
	private static final Locale[] SUPPORTED_LOCALES = new Locale[] { Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH, Locale.ITALIAN, new Locale("pl") };

	/** The actions that switch the language. */
	private Map<Locale, Action> languageActions = new HashMap<Locale, Action>();

	/** The “manage nodes” action. */
	private Action manageNodeAction;

	/** The “preferences” action. */
	private Action optionsPreferencesAction;

	/** The “check for updates” action. */
	private Action checkForUpdatesAction;

	/** The “about jSite” action. */
	private Action aboutAction;

	/** The wizard. */
	private TWizard wizard;

	/** The node menu. */
	private JMenu nodeMenu;

	/** The currently selected node. */
	private Node selectedNode;

	/** Mapping from page type to page. */
	private final Map<PageType, TWizardPage> pages = new HashMap<PageType, TWizardPage>();

	/**
	 * Creates a new core with the default configuration file.
	 */
	private Main() {
		this(null);
	}

	/**
	 * Creates a new core with the given configuration from the given file.
	 *
	 * @param configFilename
	 *            The name of the configuration file
	 */
	private Main(String configFilename) {
		if (configFilename != null) {
			configuration = new Configuration(configFilename);
		} else {
			configuration = new Configuration();
		}
		Locale.setDefault(configuration.getLocale());
		I18n.setLocale(configuration.getLocale());
		if (!configuration.createLockFile()) {
			int option = JOptionPane.showOptionDialog(null, I18n.getMessage("jsite.main.already-running"), "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] { I18n.getMessage("jsite.main.already-running.override"), I18n.getMessage("jsite.wizard.quit") }, I18n.getMessage("jsite.wizard.quit"));
			if (option != 0) {
				throw new IllegalStateException("Lockfile override not active, refusing start.");
			}
			configuration.removeLockfileOnExit();
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

		updateChecker = new UpdateChecker(freenetInterface);
		updateChecker.addUpdateListener(this);
		updateChecker.start();

		initPages();
		showPage(PageType.PAGE_PROJECTS);
	}

	/**
	 * Creates all actions.
	 */
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
		optionsPreferencesAction = new AbstractAction(I18n.getMessage("jsite.menu.options.preferences")) {

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				optionsPreferences();
			}
		};
		checkForUpdatesAction = new AbstractAction(I18n.getMessage("jsite.menu.help.check-for-updates")) {

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent actionEvent) {
				showLatestUpdate();
			}
		};
		aboutAction = new AbstractAction(I18n.getMessage("jsite.menu.help.about")) {

			@SuppressWarnings("synthetic-access")
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(wizard, MessageFormat.format(I18n.getMessage("jsite.about.message"), getVersion().toString()), null, JOptionPane.INFORMATION_MESSAGE, jSiteIcon);
			}
		};

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@SuppressWarnings("synthetic-access")
			public void run() {
				manageNodeAction.putValue(Action.NAME, I18n.getMessage("jsite.menu.nodes.manage-nodes"));
				optionsPreferencesAction.putValue(Action.NAME, I18n.getMessage("jsite.menu.options.preferences"));
				checkForUpdatesAction.putValue(Action.NAME, I18n.getMessage("jsite.menu.help.check-for-updates"));
				aboutAction.putValue(Action.NAME, I18n.getMessage("jsite.menu.help.about"));
			}
		});
	}

	/**
	 * Creates the menu bar.
	 *
	 * @return The menu bar
	 */
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

		final JMenu optionsMenu = new JMenu(I18n.getMessage("jsite.menu.options"));
		menuBar.add(optionsMenu);
		optionsMenu.add(optionsPreferencesAction);

		/* evil hack to right-align the help menu */
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		menuBar.add(panel);

		final JMenu helpMenu = new JMenu(I18n.getMessage("jsite.menu.help"));
		menuBar.add(helpMenu);
		helpMenu.add(checkForUpdatesAction);
		helpMenu.add(aboutAction);

		I18nContainer.getInstance().registerRunnable(new Runnable() {

			@SuppressWarnings("synthetic-access")
			public void run() {
				languageMenu.setText(I18n.getMessage("jsite.menu.languages"));
				nodeMenu.setText(I18n.getMessage("jsite.menu.nodes"));
				optionsMenu.setText(I18n.getMessage("jsite.menu.options"));
				helpMenu.setText(I18n.getMessage("jsite.menu.help"));
				for (Map.Entry<Locale, Action> languageActionEntry : languageActions.entrySet()) {
					languageActionEntry.getValue().putValue(Action.NAME, I18n.getMessage("jsite.menu.language." + languageActionEntry.getKey().getLanguage()));
				}
			}
		});

		return menuBar;
	}

	/**
	 * Initializes all pages.
	 */
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
		projectInsertPage.setName("page.project.insert");
		projectInsertPage.setFreenetInterface(freenetInterface);
		pages.put(PageType.PAGE_INSERT_PROJECT, projectInsertPage);

		PreferencesPage preferencesPage = new PreferencesPage(wizard);
		preferencesPage.setName("page.preferences");
		preferencesPage.setTempDirectory(configuration.getTempDirectory());
		pages.put(PageType.PAGE_PREFERENCES, preferencesPage);
	}

	/**
	 * Shows the page with the given type.
	 *
	 * @param pageType
	 *            The page type to show
	 */
	private void showPage(PageType pageType) {
		wizard.setPreviousEnabled(pageType.ordinal() > 0);
		wizard.setNextEnabled(pageType.ordinal() < (pages.size() - 1));
		wizard.setPage(pages.get(pageType));
		wizard.setTitle(pages.get(pageType).getHeading() + " - jSite");
	}

	/**
	 * Saves the configuration.
	 *
	 * @return <code>true</code> if the configuration could be saved,
	 *         <code>false</code> otherwise
	 */
	private boolean saveConfiguration() {
		NodeManagerPage nodeManagerPage = (NodeManagerPage) pages.get(PageType.PAGE_NODE_MANAGER);
		configuration.setNodes(nodeManagerPage.getNodes());
		if (selectedNode != null) {
			configuration.setSelectedNode(selectedNode);
		}

		ProjectPage projectPage = (ProjectPage) pages.get(PageType.PAGE_PROJECTS);
		configuration.setProjects(projectPage.getProjects());

		PreferencesPage preferencesPage = (PreferencesPage) pages.get(PageType.PAGE_PREFERENCES);
		configuration.setTempDirectory(preferencesPage.getTempDirectory());

		return configuration.save();
	}

	/**
	 * Finds a supported locale for the given locale.
	 *
	 * @param forLocale
	 *            The locale to find a supported locale for
	 * @return The supported locale that was found, or the default locale if no
	 *         supported locale could be found
	 */
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

	/**
	 * Returns the version.
	 *
	 * @return The version
	 */
	public static final Version getVersion() {
		return VERSION;
	}

	//
	// ACTIONS
	//

	/**
	 * Switches the language of the interface to the given locale.
	 *
	 * @param locale
	 *            The locale to switch to
	 */
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

	/**
	 * Shows a dialog with general preferences.
	 */
	private void optionsPreferences() {
		showPage(PageType.PAGE_PREFERENCES);
		optionsPreferencesAction.setEnabled(false);
		wizard.setNextEnabled(true);
		wizard.setNextName(I18n.getMessage("jsite.wizard.next"));
	}

	/**
	 * Shows a dialog box that shows the last version that was found by the
	 * {@link UpdateChecker}.
	 */
	private void showLatestUpdate() {
		Version latestVersion = updateChecker.getLatestVersion();
		int versionDifference = latestVersion.compareTo(VERSION);
		if (versionDifference > 0) {
			JOptionPane.showMessageDialog(wizard, MessageFormat.format(I18n.getMessage("jsite.update-checker.latest-version.newer.message"), VERSION, latestVersion), I18n.getMessage("jsite.update-checker.latest-version.title"), JOptionPane.INFORMATION_MESSAGE);
		} else if (versionDifference < 0) {
			JOptionPane.showMessageDialog(wizard, MessageFormat.format(I18n.getMessage("jsite.update-checker.latest-version.older.message"), VERSION, latestVersion), I18n.getMessage("jsite.update-checker.latest-version.title"), JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(wizard, MessageFormat.format(I18n.getMessage("jsite.update-checker.latest-version.okay.message"), VERSION, latestVersion), I18n.getMessage("jsite.update-checker.latest-version.title"), JOptionPane.INFORMATION_MESSAGE);
		}
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
			if ((project.getIndexFile() == null) || (project.getIndexFile().length() == 0)) {
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
			List<String> allowedIndexContentTypes = Arrays.asList("text/html", "application/xhtml+xml");
			if (hasIndexFile && !allowedIndexContentTypes.contains(project.getFileOption(indexFile).getMimeType())) {
				if (JOptionPane.showConfirmDialog(wizard, I18n.getMessage("jsite.project-files.index-not-html"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
					return;
				}
			}
			Map<String, FileOption> fileOptions = project.getFileOptions();
			Set<Entry<String, FileOption>> fileOptionEntries = fileOptions.entrySet();
			boolean insert = false;
			for (Entry<String, FileOption> fileOptionEntry : fileOptionEntries) {
				FileOption fileOption = fileOptionEntry.getValue();
				insert |= fileOption.isInsert() || fileOption.isInsertRedirect();
				if (!fileOption.isInsert() && fileOption.isInsertRedirect() && ((fileOption.getCustomKey().length() == 0) || "CHK@".equals(fileOption.getCustomKey()))) {
					JOptionPane.showMessageDialog(wizard, MessageFormat.format(I18n.getMessage("jsite.project-files.no-custom-key"), fileOptionEntry.getKey()), null, JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (!insert) {
				JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project-files.no-files-to-insert"), null, JOptionPane.ERROR_MESSAGE);
				return;
			}
			boolean nodeRunning = false;
			try {
				nodeRunning = freenetInterface.isNodePresent();
			} catch (IOException e) {
				/* ignore. */
			}
			if (!nodeRunning) {
				JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project-files.no-node-running"), null, JOptionPane.ERROR_MESSAGE);
				return;
			}
			configuration.save();
			showPage(PageType.PAGE_INSERT_PROJECT);
			ProjectInsertPage projectInsertPage = (ProjectInsertPage) pages.get(PageType.PAGE_INSERT_PROJECT);
			String tempDirectory = ((PreferencesPage) pages.get(PageType.PAGE_PREFERENCES)).getTempDirectory();
			projectInsertPage.setTempDirectory(tempDirectory);
			projectInsertPage.startInsert();
			nodeMenu.setEnabled(false);
			optionsPreferencesAction.setEnabled(false);
		} else if ("page.project.insert".equals(pageName)) {
			showPage(PageType.PAGE_PROJECTS);
			nodeMenu.setEnabled(true);
			optionsPreferencesAction.setEnabled(true);
		} else if ("page.preferences".equals(pageName)) {
			showPage(PageType.PAGE_PROJECTS);
			optionsPreferencesAction.setEnabled(true);
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
		if (((ProjectPage) pages.get(PageType.PAGE_PROJECTS)).wasUriCopied() || ((ProjectInsertPage) pages.get(PageType.PAGE_INSERT_PROJECT)).wasUriCopied()) {
			JOptionPane.showMessageDialog(wizard, I18n.getMessage("jsite.project.warning.use-clipboard-now"));
		}
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
	// INTERFACE UpdateListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void foundUpdateData(Version foundVersion, long versionTimestamp) {
		logger.log(Level.FINEST, "Found version {0} from {1,date}.", new Object[] { foundVersion, versionTimestamp });
		if (foundVersion.compareTo(VERSION) > 0) {
			JOptionPane.showMessageDialog(wizard, MessageFormat.format(I18n.getMessage("jsite.update-checker.found-version.message"), foundVersion.toString(), new Date(versionTimestamp)), I18n.getMessage("jsite.update-checker.found-version.title"), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	//
	// MAIN METHOD
	//

	/**
	 * Main method that is called by the VM.
	 *
	 * @param args
	 *            The command-line arguments
	 */
	public static void main(String[] args) {
		/* initialize logger. */
		Logger logger = Logger.getLogger("de.todesbaum");
		Handler handler = new ConsoleHandler();
		logger.addHandler(handler);
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
				logger.setLevel(Level.ALL);
				handler.setLevel(Level.ALL);
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

	/**
	 * Prints a small syntax help.
	 */
	private static void printHelp() {
		System.out.println("--help\tshows this cruft");
		System.out.println("--debug\tenables some debug output");
		System.out.println("--config-file <file>\tuse specified configuration file");
	}

}

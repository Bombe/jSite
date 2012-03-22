/*
 * jSite - ProjectInserter.java - Copyright © 2006–2012 David Roden
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.todesbaum.jsite.gui.FileScanner;
import de.todesbaum.jsite.gui.FileScanner.ScannedFile;
import de.todesbaum.jsite.gui.FileScannerListener;
import de.todesbaum.util.freenet.fcp2.Client;
import de.todesbaum.util.freenet.fcp2.ClientPutComplexDir;
import de.todesbaum.util.freenet.fcp2.ClientPutDir.ManifestPutter;
import de.todesbaum.util.freenet.fcp2.Connection;
import de.todesbaum.util.freenet.fcp2.DirectFileEntry;
import de.todesbaum.util.freenet.fcp2.FileEntry;
import de.todesbaum.util.freenet.fcp2.Message;
import de.todesbaum.util.freenet.fcp2.PriorityClass;
import de.todesbaum.util.freenet.fcp2.RedirectFileEntry;
import de.todesbaum.util.freenet.fcp2.Verbosity;
import de.todesbaum.util.io.StreamCopier.ProgressListener;

/**
 * Manages project inserts.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ProjectInserter implements FileScannerListener, Runnable {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(ProjectInserter.class.getName());

	/** Random number for FCP instances. */
	private static final int random = (int) (Math.random() * Integer.MAX_VALUE);

	/** Counter for FCP connection identifier. */
	private static int counter = 0;

	/** The list of insert listeners. */
	private List<InsertListener> insertListeners = new ArrayList<InsertListener>();

	/** The freenet interface. */
	protected Freenet7Interface freenetInterface;

	/** The project to insert. */
	protected Project project;

	/** The file scanner. */
	private FileScanner fileScanner;

	/** Object used for synchronization. */
	protected final Object lockObject = new Object();

	/** The temp directory. */
	private String tempDirectory;

	/** The current connection. */
	private Connection connection;

	/** Whether the insert is cancelled. */
	private volatile boolean cancelled = false;

	/** Progress listener for payload transfers. */
	private ProgressListener progressListener;

	/** Whether to use “early encode.” */
	private boolean useEarlyEncode;

	/** The insert priority. */
	private PriorityClass priority;

	/** The manifest putter. */
	private ManifestPutter manifestPutter;

	/**
	 * Adds a listener to the list of registered listeners.
	 *
	 * @param insertListener
	 *            The listener to add
	 */
	public void addInsertListener(InsertListener insertListener) {
		insertListeners.add(insertListener);
	}

	/**
	 * Removes a listener from the list of registered listeners.
	 *
	 * @param insertListener
	 *            The listener to remove
	 */
	public void removeInsertListener(InsertListener insertListener) {
		insertListeners.remove(insertListener);
	}

	/**
	 * Notifies all listeners that the project insert has started.
	 *
	 * @see InsertListener#projectInsertStarted(Project)
	 */
	protected void fireProjectInsertStarted() {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectInsertStarted(project);
		}
	}

	/**
	 * Notifies all listeners that the insert has generated a URI.
	 *
	 * @see InsertListener#projectURIGenerated(Project, String)
	 * @param uri
	 *            The generated URI
	 */
	protected void fireProjectURIGenerated(String uri) {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectURIGenerated(project, uri);
		}
	}

	/**
	 * Notifies all listeners that the insert has made some progress.
	 *
	 * @see InsertListener#projectUploadFinished(Project)
	 */
	protected void fireProjectUploadFinished() {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectUploadFinished(project);
		}
	}

	/**
	 * Notifies all listeners that the insert has made some progress.
	 *
	 * @see InsertListener#projectInsertProgress(Project, int, int, int, int,
	 *      boolean)
	 * @param succeeded
	 *            The number of succeeded blocks
	 * @param failed
	 *            The number of failed blocks
	 * @param fatal
	 *            The number of fatally failed blocks
	 * @param total
	 *            The total number of blocks
	 * @param finalized
	 *            <code>true</code> if the total number of blocks has already
	 *            been finalized, <code>false</code> otherwise
	 */
	protected void fireProjectInsertProgress(int succeeded, int failed, int fatal, int total, boolean finalized) {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectInsertProgress(project, succeeded, failed, fatal, total, finalized);
		}
	}

	/**
	 * Notifies all listeners the project insert has finished.
	 *
	 * @see InsertListener#projectInsertFinished(Project, boolean, Throwable)
	 * @param success
	 *            <code>true</code> if the project was inserted successfully,
	 *            <code>false</code> if it failed
	 * @param cause
	 *            The cause of the failure, if any
	 */
	protected void fireProjectInsertFinished(boolean success, Throwable cause) {
		for (InsertListener insertListener : insertListeners) {
			insertListener.projectInsertFinished(project, success, cause);
		}
	}

	/**
	 * Sets the project to insert.
	 *
	 * @param project
	 *            The project to insert
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Sets the freenet interface to use.
	 *
	 * @param freenetInterface
	 *            The freenet interface to use
	 */
	public void setFreenetInterface(Freenet7Interface freenetInterface) {
		this.freenetInterface = freenetInterface;
	}

	/**
	 * Sets the temp directory to use.
	 *
	 * @param tempDirectory
	 *            The temp directory to use, or {@code null} to use the system
	 *            default
	 */
	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	/**
	 * Sets whether to use the “early encode“ flag for the insert.
	 *
	 * @param useEarlyEncode
	 *            {@code true} to set the “early encode” flag for the insert,
	 *            {@code false} otherwise
	 */
	public void setUseEarlyEncode(boolean useEarlyEncode) {
		this.useEarlyEncode = useEarlyEncode;
	}

	/**
	 * Sets the insert priority.
	 *
	 * @param priority
	 *            The insert priority
	 */
	public void setPriority(PriorityClass priority) {
		this.priority = priority;
	}

	/**
	 * Sets the manifest putter to use for inserts.
	 *
	 * @param manifestPutter
	 *            The manifest putter to use
	 */
	public void setManifestPutter(ManifestPutter manifestPutter) {
		this.manifestPutter = manifestPutter;
	}

	/**
	 * Starts the insert.
	 *
	 * @param progressListener
	 *            Listener to notify on progress events
	 */
	public void start(ProgressListener progressListener) {
		cancelled = false;
		this.progressListener = progressListener;
		fileScanner = new FileScanner(project);
		fileScanner.addFileScannerListener(this);
		new Thread(fileScanner).start();
	}

	/**
	 * Stops the current insert.
	 */
	public void stop() {
		cancelled = true;
		synchronized (lockObject) {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * Creates an input stream that delivers the given file, replacing edition
	 * tokens in the file’s content, if necessary.
	 *
	 * @param filename
	 *            The name of the file
	 * @param fileOption
	 *            The file options
	 * @param edition
	 *            The current edition
	 * @param length
	 *            An array containing a single long which is used to
	 *            <em>return</em> the final length of the file, after all
	 *            replacements
	 * @return The input stream for the file
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private InputStream createFileInputStream(String filename, FileOption fileOption, int edition, long[] length) throws IOException {
		File file = new File(project.getLocalPath(), filename);
		length[0] = file.length();
		return new FileInputStream(file);
	}

	/**
	 * Creates a file entry suitable for handing in to
	 * {@link ClientPutComplexDir#addFileEntry(FileEntry)}.
	 *
	 * @param file
	 *            The name and hash of the file to insert
	 * @param edition
	 *            The current edition
	 * @return A file entry for the given file
	 */
	private FileEntry createFileEntry(ScannedFile file, int edition) {
		FileEntry fileEntry = null;
		String filename = file.getFilename();
		FileOption fileOption = project.getFileOption(filename);
		if (fileOption.isInsert()) {
			fileOption.setCurrentHash(file.getHash());
			/* check if file was modified. */
			if (!fileOption.isForceInsert() && file.getHash().equals(fileOption.getLastInsertHash())) {
				/* only insert a redirect. */
				logger.log(Level.FINE, String.format("Inserting redirect to edition %d for %s.", fileOption.getLastInsertEdition(), filename));
				return new RedirectFileEntry(filename, fileOption.getMimeType(), "SSK@" + project.getRequestURI() + "/" + project.getPath() + "-" + fileOption.getLastInsertEdition() + "/" + filename);
			}
			try {
				long[] fileLength = new long[1];
				InputStream fileEntryInputStream = createFileInputStream(filename, fileOption, edition, fileLength);
				fileEntry = new DirectFileEntry(fileOption.hasChangedName() ? fileOption.getChangedName() : filename, fileOption.getMimeType(), fileEntryInputStream, fileLength[0]);
			} catch (IOException ioe1) {
				/* ignore, null is returned. */
			}
		} else {
			if (fileOption.isInsertRedirect()) {
				fileEntry = new RedirectFileEntry(fileOption.hasChangedName() ? fileOption.getChangedName() : filename, fileOption.getMimeType(), fileOption.getCustomKey());
			}
		}
		return fileEntry;
	}

	/**
	 * Validates the given project. The project will be checked for any invalid
	 * conditions, such as invalid insert or request keys, missing path names,
	 * missing default file, and so on.
	 *
	 * @param project
	 *            The project to check
	 * @return The encountered warnings and errors
	 */
	public static CheckReport validateProject(Project project) {
		CheckReport checkReport = new CheckReport();
		if ((project.getLocalPath() == null) || (project.getLocalPath().trim().length() == 0)) {
			checkReport.addIssue("error.no-local-path", true);
		}
		if ((project.getPath() == null) || (project.getPath().trim().length() == 0)) {
			checkReport.addIssue("error.no-path", true);
		}
		if ((project.getIndexFile() == null) || (project.getIndexFile().length() == 0)) {
			checkReport.addIssue("warning.empty-index", false);
		} else {
			File indexFile = new File(project.getLocalPath(), project.getIndexFile());
			if (!indexFile.exists()) {
				checkReport.addIssue("error.index-missing", true);
			}
		}
		String indexFile = project.getIndexFile();
		boolean hasIndexFile = (indexFile != null) && (indexFile.length() > 0);
		List<String> allowedIndexContentTypes = Arrays.asList("text/html", "application/xhtml+xml");
		if (hasIndexFile && !allowedIndexContentTypes.contains(project.getFileOption(indexFile).getMimeType())) {
			checkReport.addIssue("warning.index-not-html", false);
		}
		Map<String, FileOption> fileOptions = project.getFileOptions();
		Set<Entry<String, FileOption>> fileOptionEntries = fileOptions.entrySet();
		boolean insert = fileOptionEntries.isEmpty();
		for (Entry<String, FileOption> fileOptionEntry : fileOptionEntries) {
			String fileName = fileOptionEntry.getKey();
			FileOption fileOption = fileOptionEntry.getValue();
			insert |= fileOption.isInsert() || fileOption.isInsertRedirect();
			if (fileName.equals(project.getIndexFile()) && !fileOption.isInsert() && !fileOption.isInsertRedirect()) {
				checkReport.addIssue("error.index-not-inserted", true);
			}
			if (!fileOption.isInsert() && fileOption.isInsertRedirect() && ((fileOption.getCustomKey().length() == 0) || "CHK@".equals(fileOption.getCustomKey()))) {
				checkReport.addIssue("error.no-custom-key", true, fileName);
			}
		}
		if (!insert) {
			checkReport.addIssue("error.no-files-to-insert", true);
		}
		Set<String> fileNames = new HashSet<String>();
		for (Entry<String, FileOption> fileOptionEntry : fileOptionEntries) {
			FileOption fileOption = fileOptionEntry.getValue();
			if (!fileOption.isInsert() && !fileOption.isInsertRedirect()) {
				logger.log(Level.FINEST, "Ignoring {0}.", fileOptionEntry.getKey());
				continue;
			}
			String fileName = fileOptionEntry.getKey();
			if (fileOption.hasChangedName()) {
				fileName = fileOption.getChangedName();
			}
			logger.log(Level.FINEST, "Adding “{0}” for {1}.", new Object[] { fileName, fileOptionEntry.getKey() });
			if (!fileNames.add(fileName)) {
				checkReport.addIssue("error.duplicate-file", true, fileName);
			}
		}
		return checkReport;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		fireProjectInsertStarted();
		List<ScannedFile> files = fileScanner.getFiles();

		/* create connection to node */
		synchronized (lockObject) {
			connection = freenetInterface.getConnection("project-insert-" + random + counter++);
		}
		connection.setTempDirectory(tempDirectory);
		boolean connected = false;
		Throwable cause = null;
		try {
			connected = connection.connect();
		} catch (IOException e1) {
			cause = e1;
		}

		if (!connected || cancelled) {
			fireProjectInsertFinished(false, cancelled ? new AbortedException() : cause);
			return;
		}

		Client client = new Client(connection);

		/* collect files */
		int edition = project.getEdition();
		String dirURI = "USK@" + project.getInsertURI() + "/" + project.getPath() + "/" + edition + "/";
		ClientPutComplexDir putDir = new ClientPutComplexDir("dir-" + counter++, dirURI, tempDirectory);
		if ((project.getIndexFile() != null) && (project.getIndexFile().length() > 0)) {
			putDir.setDefaultName(project.getIndexFile());
		}
		putDir.setVerbosity(Verbosity.ALL);
		putDir.setMaxRetries(-1);
		putDir.setEarlyEncode(useEarlyEncode);
		putDir.setPriorityClass(priority);
		putDir.setManifestPutter(manifestPutter);
		for (ScannedFile file : files) {
			FileEntry fileEntry = createFileEntry(file, edition);
			if (fileEntry != null) {
				try {
					putDir.addFileEntry(fileEntry);
				} catch (IOException ioe1) {
					fireProjectInsertFinished(false, ioe1);
					return;
				}
			}
		}

		/* start request */
		try {
			client.execute(putDir, progressListener);
			fireProjectUploadFinished();
		} catch (IOException ioe1) {
			fireProjectInsertFinished(false, ioe1);
			return;
		}

		/* parse progress and success messages */
		String finalURI = null;
		boolean success = false;
		boolean finished = false;
		boolean disconnected = false;
		while (!finished && !cancelled) {
			Message message = client.readMessage();
			finished = (message == null) || (disconnected = client.isDisconnected());
			logger.log(Level.FINE, "Received message: " + message);
			if (!finished) {
				@SuppressWarnings("null")
				String messageName = message.getName();
				if ("URIGenerated".equals(messageName)) {
					finalURI = message.get("URI");
					fireProjectURIGenerated(finalURI);
				}
				if ("SimpleProgress".equals(messageName)) {
					int total = Integer.parseInt(message.get("Total"));
					int succeeded = Integer.parseInt(message.get("Succeeded"));
					int fatal = Integer.parseInt(message.get("FatallyFailed"));
					int failed = Integer.parseInt(message.get("Failed"));
					boolean finalized = Boolean.parseBoolean(message.get("FinalizedTotal"));
					fireProjectInsertProgress(succeeded, failed, fatal, total, finalized);
				}
				success |= "PutSuccessful".equals(messageName);
				finished = (success && (finalURI != null)) || "PutFailed".equals(messageName) || messageName.endsWith("Error");
			}
		}

		/* post-insert work */
		if (success) {
			@SuppressWarnings("null")
			String editionPart = finalURI.substring(finalURI.lastIndexOf('/') + 1);
			int newEdition = Integer.parseInt(editionPart);
			project.setEdition(newEdition);
			project.setLastInsertionTime(System.currentTimeMillis());
			project.onSuccessfulInsert();
		}
		fireProjectInsertFinished(success, cancelled ? new AbortedException() : (disconnected ? new IOException("Connection terminated") : null));
	}

	//
	// INTERFACE FileScannerListener
	//

	/**
	 * {@inheritDoc}
	 */
	public void fileScannerFinished(FileScanner fileScanner) {
		if (!fileScanner.isError()) {
			new Thread(this).start();
		} else {
			fireProjectInsertFinished(false, null);
		}
		fileScanner.removeFileScannerListener(this);
	}

	/**
	 * Container class that collects all warnings and errors that occured during
	 * {@link ProjectInserter#validateProject(Project) project validation}.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class CheckReport implements Iterable<Issue> {

		/** The issures that occured. */
		private final List<Issue> issues = new ArrayList<Issue>();

		/**
		 * Adds an issue.
		 *
		 * @param issue
		 *            The issue to add
		 */
		public void addIssue(Issue issue) {
			issues.add(issue);
		}

		/**
		 * Creates an {@link Issue} from the given error key and fatality flag
		 * and {@link #addIssue(Issue) adds} it.
		 *
		 * @param errorKey
		 *            The error key
		 * @param fatal
		 *            {@code true} if the error is fatal, {@code false} if only
		 *            a warning should be generated
		 * @param parameters
		 *            Any additional parameters
		 */
		public void addIssue(String errorKey, boolean fatal, String... parameters) {
			addIssue(new Issue(errorKey, fatal, parameters));
		}

		/**
		 * {@inheritDoc}
		 */
		public Iterator<Issue> iterator() {
			return issues.iterator();
		}

		/**
		 * Returns whether this check report does not contain any errors.
		 *
		 * @return {@code true} if this check report does not contain any
		 *         errors, {@code false} if this check report does contain
		 *         errors
		 */
		public boolean isEmpty() {
			return issues.isEmpty();
		}

		/**
		 * Returns the number of issues in this check report.
		 *
		 * @return The number of issues
		 */
		public int size() {
			return issues.size();
		}

	}

	/**
	 * Container class for a single issue. An issue contains an error key
	 * that describes the error, and a fatality flag that determines whether
	 * the insert has to be aborted (if the flag is {@code true}) or if it
	 * can still be performed and only a warning should be generated (if the
	 * flag is {@code false}).
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’
	 *         Roden</a>
	 */
	public static class Issue {

		/** The error key. */
		private final String errorKey;

		/** The fatality flag. */
		private final boolean fatal;

		/** Additional parameters. */
		private String[] parameters;

		/**
		 * Creates a new issue.
		 *
		 * @param errorKey
		 *            The error key
		 * @param fatal
		 *            The fatality flag
		 * @param parameters
		 *            Any additional parameters
		 */
		protected Issue(String errorKey, boolean fatal, String... parameters) {
			this.errorKey = errorKey;
			this.fatal = fatal;
			this.parameters = parameters;
		}

		/**
		 * Returns the key of the encountered error.
		 *
		 * @return The error key
		 */
		public String getErrorKey() {
			return errorKey;
		}

		/**
		 * Returns whether the issue is fatal and the insert has to be
		 * aborted. Otherwise only a warning should be shown.
		 *
		 * @return {@code true} if the insert needs to be aborted, {@code
		 *         false} otherwise
		 */
		public boolean isFatal() {
			return fatal;
		}

		/**
		 * Returns any additional parameters.
		 *
		 * @return The additional parameters
		 */
		public String[] getParameters() {
			return parameters;
		}

	}

}

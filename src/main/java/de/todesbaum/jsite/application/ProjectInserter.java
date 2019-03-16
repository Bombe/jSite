/*
 * jSite - ProjectInserter.java - Copyright © 2006–2019 David Roden
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.io.StreamCopier.ProgressListener;

import de.todesbaum.jsite.gui.FileScanner;
import de.todesbaum.jsite.gui.ScannedFile;
import de.todesbaum.jsite.gui.FileScannerListener;
import de.todesbaum.util.freenet.fcp2.Client;
import de.todesbaum.util.freenet.fcp2.ClientPutComplexDir;
import de.todesbaum.util.freenet.fcp2.Connection;
import de.todesbaum.util.freenet.fcp2.DirectFileEntry;
import de.todesbaum.util.freenet.fcp2.FileEntry;
import de.todesbaum.util.freenet.fcp2.Message;
import de.todesbaum.util.freenet.fcp2.PriorityClass;
import de.todesbaum.util.freenet.fcp2.RedirectFileEntry;
import de.todesbaum.util.freenet.fcp2.Verbosity;

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
	private static final AtomicInteger counter = new AtomicInteger();

	private final ProjectInsertListeners projectInsertListeners = new ProjectInsertListeners();

	/** The freenet interface. */
	private Freenet7Interface freenetInterface;

	/** The project to insert. */
	private Project project;

	/** The file scanner. */
	private FileScanner fileScanner;

	/** Object used for synchronization. */
	private final Object lockObject = new Object();

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

	/**
	 * Adds a listener to the list of registered listeners.
	 *
	 * @param insertListener
	 *            The listener to add
	 */
	public void addInsertListener(InsertListener insertListener) {
		projectInsertListeners.addInsertListener(insertListener);
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
	 * Starts the insert.
	 *
	 * @param progressListener
	 *            Listener to notify on progress events
	 */
	public void start(ProgressListener progressListener) {
		cancelled = false;
		this.progressListener = progressListener;
		fileScanner = new FileScanner(project, this);
		fileScanner.startInBackground();
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
	 * Creates a file entry suitable for handing in to
	 * {@link ClientPutComplexDir#addFileEntry(FileEntry)}.
	 *
	 * @param file
	 * 		The name and hash of the file to insert
	 * @return A file entry for the given file
	 */
	private Optional<FileEntry> createFileEntry(ScannedFile file) {
		String filename = file.getFilename();
		FileOption fileOption = project.getFileOption(filename);
		if (fileOption.isInsert()) {
			fileOption.setCurrentHash(file.getHash());
			/* check if file was modified. */
			if (!project.isAlwaysForceInsert() && !fileOption.isForceInsert() && file.getHash().equals(fileOption.getLastInsertHash())) {
				/* only insert a redirect. */
				logger.log(Level.FINE, String.format("Inserting redirect to edition %d for %s.", fileOption.getLastInsertEdition(), filename));
				return Optional.of(new RedirectFileEntry(fileOption.getChangedName().orElse(filename), fileOption.getMimeType(), "SSK@" + project.getRequestURI() + "/" + project.getPath() + "-" + fileOption.getLastInsertEdition() + "/" + fileOption.getLastInsertFilename()));
			}
			try {
				return Optional.of(createFileEntry(filename, fileOption.getChangedName(), fileOption.getMimeType()));
			} catch (IOException ioe1) {
				/* ignore, null is returned. */
			}
		} else {
			if (fileOption.isInsertRedirect()) {
				return Optional.of(new RedirectFileEntry(fileOption.getChangedName().orElse(filename), fileOption.getMimeType(), fileOption.getCustomKey()));
			}
		}
		return Optional.empty();
	}

	private FileEntry createFileEntry(String filename, Optional<String> changedName, String mimeType) throws FileNotFoundException {
		File physicalFile = new File(project.getLocalPath(), filename);
		InputStream fileEntryInputStream = new FileInputStream(physicalFile);
		return new DirectFileEntry(changedName.orElse(filename), mimeType, fileEntryInputStream, physicalFile.length());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		projectInsertListeners.fireProjectInsertStarted(project);
		List<ScannedFile> files = fileScanner.getFiles();

		/* create connection to node */
		synchronized (lockObject) {
			connection = freenetInterface.getConnection("project-insert-" + random + counter.getAndIncrement());
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
			projectInsertListeners.fireProjectInsertFinished(project, false, cancelled ? new AbortedException() : cause);
			return;
		}

		Client client = new Client(connection);

		/* collect files */
		int edition = project.getEdition();
		String dirURI = "USK@" + project.getInsertURI() + "/" + project.getPath() + "/" + edition + "/";
		ClientPutComplexDir putDir = new ClientPutComplexDir("dir-" + counter.getAndIncrement(), dirURI, tempDirectory);
		if ((project.getIndexFile() != null) && (project.getIndexFile().length() > 0)) {
			FileOption indexFileOption = project.getFileOption(project.getIndexFile());
			Optional<String> changedName = indexFileOption.getChangedName();
			if (changedName.isPresent()) {
				putDir.setDefaultName(changedName.get());
			} else {
				putDir.setDefaultName(project.getIndexFile());
			}
		}
		putDir.setVerbosity(Verbosity.ALL);
		putDir.setMaxRetries(-1);
		putDir.setEarlyEncode(useEarlyEncode);
		putDir.setPriorityClass(priority);
		for (ScannedFile file : files) {
			Optional<FileEntry> fileEntry = createFileEntry(file);
			if (fileEntry.isPresent()) {
				try {
					putDir.addFileEntry(fileEntry.get());
				} catch (IOException ioe1) {
					projectInsertListeners.fireProjectInsertFinished(project, false, ioe1);
					return;
				}
			}
		}

		/* start request */
		try {
			client.execute(putDir, progressListener);
			projectInsertListeners.fireProjectUploadFinished(project);
		} catch (IOException ioe1) {
			projectInsertListeners.fireProjectInsertFinished(project, false, ioe1);
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
					projectInsertListeners.fireProjectURIGenerated(project, finalURI);
				}
				if ("SimpleProgress".equals(messageName)) {
					int total = Integer.parseInt(message.get("Total"));
					int succeeded = Integer.parseInt(message.get("Succeeded"));
					int fatal = Integer.parseInt(message.get("FatallyFailed"));
					int failed = Integer.parseInt(message.get("Failed"));
					boolean finalized = Boolean.parseBoolean(message.get("FinalizedTotal"));
					projectInsertListeners.fireProjectInsertProgress(project, succeeded, failed, fatal, total, finalized);
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
		projectInsertListeners.fireProjectInsertFinished(project, success, cancelled ? new AbortedException() : (disconnected ? new IOException("Connection terminated") : null));
	}

	//
	// INTERFACE FileScannerListener
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fileScannerFinished(boolean error, Collection<ScannedFile> files) {
		if (!error) {
			new Thread(this).start();
		} else {
			projectInsertListeners.fireProjectInsertFinished(project, false, null);
		}
	}

}

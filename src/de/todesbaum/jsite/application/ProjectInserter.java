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

package de.todesbaum.jsite.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.todesbaum.jsite.gui.FileScanner;
import de.todesbaum.jsite.gui.FileScannerListener;
import de.todesbaum.util.freenet.fcp2.Client;
import de.todesbaum.util.freenet.fcp2.ClientPutComplexDir;
import de.todesbaum.util.freenet.fcp2.Connection;
import de.todesbaum.util.freenet.fcp2.DirectFileEntry;
import de.todesbaum.util.freenet.fcp2.FileEntry;
import de.todesbaum.util.freenet.fcp2.Message;
import de.todesbaum.util.freenet.fcp2.RedirectFileEntry;
import de.todesbaum.util.freenet.fcp2.Verbosity;
import de.todesbaum.util.io.Closer;
import de.todesbaum.util.io.ReplacingOutputStream;
import de.todesbaum.util.io.StreamCopier;

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
	 * Starts the insert.
	 */
	public void start() {
		fileScanner = new FileScanner(project);
		fileScanner.addFileScannerListener(this);
		new Thread(fileScanner).start();
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
		if (!fileOption.getReplaceEdition()) {
			return new FileInputStream(file);
		}
		ByteArrayOutputStream filteredByteOutputStream = new ByteArrayOutputStream(Math.min(Integer.MAX_VALUE, (int) length[0]));
		ReplacingOutputStream outputStream = new ReplacingOutputStream(filteredByteOutputStream);
		FileInputStream fileInput = new FileInputStream(file);
		try {
			outputStream.addReplacement("$[EDITION]", String.valueOf(edition));
			outputStream.addReplacement("$[URI]", project.getFinalRequestURI(0));
			for (int index = 1; index <= fileOption.getEditionRange(); index++) {
				outputStream.addReplacement("$[URI+" + index + "]", project.getFinalRequestURI(index));
				outputStream.addReplacement("$[EDITION+" + index + "]", String.valueOf(edition + index));
			}
			StreamCopier.copy(fileInput, outputStream, length[0]);
		} finally {
			Closer.close(fileInput);
			Closer.close(outputStream);
			Closer.close(filteredByteOutputStream);
		}
		byte[] filteredBytes = filteredByteOutputStream.toByteArray();
		length[0] = filteredBytes.length;
		return new ByteArrayInputStream(filteredBytes);
	}

	/**
	 * Creates an input stream for a container.
	 *
	 * @param containerFiles
	 *            All container definitions
	 * @param containerName
	 *            The name of the container to create
	 * @param edition
	 *            The current edition
	 * @param containerLength
	 *            An array containing a single long which is used to
	 *            <em>return</em> the final length of the container stream,
	 *            after all replacements
	 * @return The input stream for the container
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private InputStream createContainerInputStream(Map<String, List<String>> containerFiles, String containerName, int edition, long[] containerLength) throws IOException {
		File tempFile = File.createTempFile("jsite", ".zip", (tempDirectory == null) ? null : new File(tempDirectory));
		tempFile.deleteOnExit();
		FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
		ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
		try {
			for (String filename : containerFiles.get(containerName)) {
				File dataFile = new File(project.getLocalPath(), filename);
				if (dataFile.exists()) {
					ZipEntry zipEntry = new ZipEntry(filename);
					long[] fileLength = new long[1];
					InputStream wrappedInputStream = createFileInputStream(filename, project.getFileOption(filename), edition, fileLength);
					try {
						zipOutputStream.putNextEntry(zipEntry);
						StreamCopier.copy(wrappedInputStream, zipOutputStream, fileLength[0]);
					} finally {
						zipOutputStream.closeEntry();
						wrappedInputStream.close();
					}
				}
			}
		} finally {
			zipOutputStream.closeEntry();
			Closer.close(zipOutputStream);
			Closer.close(fileOutputStream);
		}

		containerLength[0] = tempFile.length();
		return new FileInputStream(tempFile);
	}

	/**
	 * Creates a file entry suitable for handing in to
	 * {@link ClientPutComplexDir#addFileEntry(FileEntry)}.
	 *
	 * @param filename
	 *            The name of the file to insert
	 * @param edition
	 *            The current edition
	 * @param containerFiles
	 *            The container definitions
	 * @return A file entry for the given file
	 */
	private FileEntry createFileEntry(String filename, int edition, Map<String, List<String>> containerFiles) {
		FileEntry fileEntry = null;
		FileOption fileOption = project.getFileOption(filename);
		if (filename.startsWith("/container/:")) {
			String containerName = filename.substring("/container/:".length());
			try {
				long[] containerLength = new long[1];
				InputStream containerInputStream = createContainerInputStream(containerFiles, containerName, edition, containerLength);
				fileEntry = new DirectFileEntry(containerName + ".zip", "application/zip", containerInputStream, containerLength[0]);
			} catch (IOException ioe1) {
				/* ignore, null is returned. */
			}
		} else {
			if (fileOption.isInsert()) {
				try {
					long[] fileLength = new long[1];
					InputStream fileEntryInputStream = createFileInputStream(filename, fileOption, edition, fileLength);
					fileEntry = new DirectFileEntry(filename, project.getFileOption(filename).getMimeType(), fileEntryInputStream, fileLength[0]);
				} catch (IOException ioe1) {
					/* ignore, null is returned. */
				}
			} else {
				if (fileOption.isInsertRedirect()) {
					fileEntry = new RedirectFileEntry(filename, fileOption.getMimeType(), fileOption.getCustomKey());
				}
			}
		}
		return fileEntry;
	}

	/**
	 * Creates container definitions.
	 *
	 * @param files
	 *            The list of all files
	 * @param containers
	 *            The list of all containers
	 * @param containerFiles
	 *            Empty map that will be filled with container definitions
	 */
	private void createContainers(List<String> files, List<String> containers, Map<String, List<String>> containerFiles) {
		for (String filename : new ArrayList<String>(files)) {
			FileOption fileOption = project.getFileOption(filename);
			String containerName = fileOption.getContainer();
			if (!containerName.equals("")) {
				if (!containers.contains(containerName)) {
					containers.add(containerName);
					containerFiles.put(containerName, new ArrayList<String>());
					/* hmm. looks like a hack to me. */
					files.add("/container/:" + containerName);
				}
				containerFiles.get(containerName).add(filename);
				files.remove(filename);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		fireProjectInsertStarted();
		List<String> files = fileScanner.getFiles();

		/* create connection to node */
		Connection connection = freenetInterface.getConnection("project-insert-" + random + counter++);
		connection.setTempDirectory(tempDirectory);
		boolean connected = false;
		Throwable cause = null;
		try {
			connected = connection.connect();
		} catch (IOException e1) {
			cause = e1;
		}

		if (!connected) {
			fireProjectInsertFinished(false, cause);
			return;
		}

		Client client = new Client(connection);

		/* create containers */
		final List<String> containers = new ArrayList<String>();
		final Map<String, List<String>> containerFiles = new HashMap<String, List<String>>();
		createContainers(files, containers, containerFiles);

		/* collect files */
		int edition = project.getEdition();
		String dirURI = "USK@" + project.getInsertURI() + "/" + project.getPath() + "/" + edition + "/";
		ClientPutComplexDir putDir = new ClientPutComplexDir("dir-" + counter++, dirURI, tempDirectory);
		if ((project.getIndexFile() != null) && (project.getIndexFile().length() > 0)) {
			putDir.setDefaultName(project.getIndexFile());
		}
		putDir.setVerbosity(Verbosity.ALL);
		putDir.setMaxRetries(-1);
		for (String filename : files) {
			FileEntry fileEntry = createFileEntry(filename, edition, containerFiles);
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
			client.execute(putDir);
		} catch (IOException ioe1) {
			fireProjectInsertFinished(false, ioe1);
			return;
		}

		/* parse progress and success messages */
		String finalURI = null;
		boolean firstMessage = true;
		boolean success = false;
		boolean finished = false;
		boolean disconnected = false;
		while (!finished) {
			Message message = client.readMessage();
			finished = (message == null) || (disconnected = client.isDisconnected());
			if (firstMessage) {
				fireProjectUploadFinished();
				firstMessage = false;
			}
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
				success = "PutSuccessful".equals(messageName);
				finished = success || "PutFailed".equals(messageName) || messageName.endsWith("Error");
			}
		}

		/* post-insert work */
		fireProjectInsertFinished(success, disconnected ? new IOException("Connection terminated") : null);
		if (success) {
			@SuppressWarnings("null")
			String editionPart = finalURI.substring(finalURI.lastIndexOf('/') + 1);
			int newEdition = Integer.parseInt(editionPart);
			project.setEdition(newEdition);
			project.setLastInsertionTime(System.currentTimeMillis());
		}
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

}

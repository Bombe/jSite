package de.todesbaum.jsite.application.validation;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.todesbaum.jsite.application.FileOption;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.gui.FileScanner;
import de.todesbaum.jsite.gui.ScannedFile;

/**
 * Validates a project and returns a number of {@link Issue}s in a {@link CheckReport}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ProjectValidator {

	private static final Logger logger = Logger.getLogger(ProjectValidator.class.getName());

	public static CheckReport validateProject(Project project) {
		CheckReport checkReport = new CheckReport();
		if ((project.getLocalPath() == null) || (project.getLocalPath().trim().length() == 0)) {
			checkReport.addIssue("error.no-local-path", true);
			return checkReport;
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
			if (!fileOption.isInsert() && fileOption.isInsertRedirect() && ((fileOption.getCustomKey().length() == 0) || "CHK@".equals(
					fileOption.getCustomKey()))) {
				checkReport.addIssue("error.no-custom-key", true, fileName);
			}
		}
		if (!insert) {
			checkReport.addIssue("error.no-files-to-insert", true);
		}
		Set<String> fileNames = new HashSet<>();
		for (Entry<String, FileOption> fileOptionEntry : fileOptionEntries) {
			FileOption fileOption = fileOptionEntry.getValue();
			if (!fileOption.isInsert() && !fileOption.isInsertRedirect()) {
				logger.log(Level.FINEST, "Ignoring {0}.", fileOptionEntry.getKey());
				continue;
			}
			String fileName = fileOption.getChangedName().orElse(fileOptionEntry.getKey());
			logger.log(Level.FINEST, "Adding “{0}” for {1}.", new Object[] { fileName, fileOptionEntry.getKey() });
			if (!fileNames.add(fileName)) {
				checkReport.addIssue("error.duplicate-file", true, fileName);
			}
		}
		long totalSize = 0;
		final CountDownLatch completionLatch = new CountDownLatch(1);
		FileScanner fileScanner = new FileScanner(project, (error, files) -> completionLatch.countDown());
		fileScanner.startInBackground();
		while (completionLatch.getCount() > 0) {
			try {
				completionLatch.await();
			} catch (InterruptedException ie1) {
				/* TODO: logging */
			}
		}
		for (ScannedFile scannedFile : fileScanner.getFiles()) {
			String fileName = scannedFile.getFilename();
			FileOption fileOption = project.getFileOption(fileName);
			if ((fileOption != null) && !fileOption.isInsert()) {
				continue;
			}
			totalSize += new File(project.getLocalPath(), fileName).length();
		}
		if (totalSize > 2 * 1024 * 1024) {
			checkReport.addIssue("warning.site-larger-than-2-mib", false);
		}
		return checkReport;
	}

}

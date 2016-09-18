package de.todesbaum.jsite.application.validation;

import static de.todesbaum.jsite.application.validation.ProjectValidator.validateProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import de.todesbaum.jsite.application.Project;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test for {@link ProjectValidator}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ProjectValidatorTest {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private Project project = new Project();

	@Before
	public void setupProject() throws Exception {
		project.setLocalPath(temporaryFolder.getRoot().getPath());
		project.setPath("temp");
		project.setIndexFile("index.html");
		temporaryFolder.newFile("index.html").createNewFile();
	}

	@Test
	public void completelyValidProjectDoesNotGenerateAnyIssues() {
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), empty());
	}

	@Test
	public void missingLocalPathResultsInError() {
		project.setLocalPath(null);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.no-local-path", true)));
	}

	@Test
	public void emptyLocalPathResultsInError() {
		project.setLocalPath("   ");
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.no-local-path", true)));
	}

	@Test
	public void missingPathResultsInError() {
		project.setPath(null);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.no-path", true)));
	}

	@Test
	public void emptyPathResultsInError() {
		project.setPath("   ");
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.no-path", true)));
	}

	@Test
	public void missingIndexFileResultsInWarning() {
		project.setIndexFile(null);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("warning.empty-index", false)));
	}

	@Test
	public void emptyIndexFileResultsInWarning() {
		project.setIndexFile("");
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("warning.empty-index", false)));
	}

	@Test
	public void settingIndexFileWithoutExistingFileResultsInWarning() {
		project.setIndexFile("test.html");
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.index-missing", true)));
	}

	@Test
	public void usingNonHtmlMimeTypeForIndexFileResultsInWarning() {
		project.getFileOption("index.html").setMimeType("text/plain");
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("warning.index-not-html", false)));
	}

	@Test
	public void notInsertingTheIndexFileAnyFileResultsInError() {
		project.getFileOption("index.html").setInsert(false);
		project.getFileOption("index.html").setInsertRedirect(false);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.index-not-inserted", true)));
	}

	@Test
	public void insertingARedirectWithRedirectUrlResultsInError() {
		project.getFileOption("index.html").setInsert(false);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.no-custom-key", true, "index.html")));
	}

	@Test
	public void insertingARedirectEmptyKeyUrlResultsInError() {
		project.getFileOption("index.html").setInsert(false);
		project.getFileOption("index.html").setCustomKey(null);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.no-custom-key", true, "index.html")));
	}

	@Test
	public void insertingARedirectToCHKUrlResultsInError() {
		project.getFileOption("index.html").setInsert(false);
		project.getFileOption("index.html").setCustomKey("CHK@");
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.no-custom-key", true, "index.html")));
	}

	@Test
	public void notInsertingAnyFileResultsInError() {
		project.getFileOption("index.html").setInsert(false);
		project.getFileOption("index.html").setInsertRedirect(false);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.no-files-to-insert", true)));
	}

	@Test
	public void filesWithDuplicateTargetNamesResultInAnError() {
		project.getFileOption("second.html").setChangedName("index.html");
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("error.duplicate-file", true, "index.html")));
	}

	@Test
	public void filesWithLessThenOrEqualTo2MegabytesDoNotResultInWarning() throws Exception {
		File largeFile = new File(temporaryFolder.getRoot(), "large.html");
		Files.write(largeFile.toPath(), new byte[2 * 1048576], StandardOpenOption.CREATE_NEW);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), not(hasItem(new Issue("warning.site-larger-than-2-mib", false))));
	}

	@Test
	public void filesWithSizeOfMoreThan2MegabytesDoNotResultInWarning() throws Exception {
		File largeFile = new File(temporaryFolder.getRoot(), "large.html");
		Files.write(largeFile.toPath(), new byte[2 * 1048576 + 1], StandardOpenOption.CREATE_NEW);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), hasItem(new Issue("warning.site-larger-than-2-mib", false)));
	}

	@Test
	public void filesAreCorrectlyIgnoreForSizeCheckIfNotInserted() throws Exception {
		File largeFile = new File(temporaryFolder.getRoot(), "large.html");
		Files.write(largeFile.toPath(), new byte[2 * 1048576 + 1], StandardOpenOption.CREATE_NEW);
		project.getFileOption("large.html").setInsert(false);
		CheckReport checkReport = validateProject(project);
		assertThat(checkReport.getIssues(), not(hasItem(new Issue("warning.site-larger-than-2-mib", false))));
	}

}

package de.todesbaum.jsite.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Unit test for {@link ProjectTest}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ProjectTest {

	@Test
	public void mimeTypeForTarBz2IsRecognizedCorrectly() {
		Project project = new Project();
		FileOption fileOption = project.getFileOption("foo.tar.bz2");
		assertThat(fileOption.getMimeType(), is("application/x-gtar"));
	}

}

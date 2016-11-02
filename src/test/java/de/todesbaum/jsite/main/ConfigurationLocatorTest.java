package de.todesbaum.jsite.main;

import static de.todesbaum.jsite.main.ConfigurationLocator.ConfigurationLocation.CUSTOM;
import static de.todesbaum.jsite.main.ConfigurationLocator.ConfigurationLocation.HOME_DIRECTORY;
import static de.todesbaum.jsite.main.ConfigurationLocator.ConfigurationLocation.NEXT_TO_JAR_FILE;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test for {@link ConfigurationLocator}.
 */
public class ConfigurationLocatorTest {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void configurationLocatorPrefersHomeDirectoryIfJarFileCanNotBeFound() {
		JarFileLocator jarFileLocator = mock(JarFileLocator.class);
		when(jarFileLocator.locateJarFile()).thenReturn(empty());
		ConfigurationLocator locator = new ConfigurationLocator(jarFileLocator);
		assertThat(locator.findPreferredLocation(), is(HOME_DIRECTORY));
		assertThat(locator.getFile(HOME_DIRECTORY), endsWith("/config7"));
		assertThat(locator.isValidLocation(HOME_DIRECTORY), is(true));
	}

	@Test
	public void configurationLocatorUsesFileNextToJarFileIfJarFileIsFound() throws Exception {
		File jarFile = temporaryFolder.newFile("test.jar");
		temporaryFolder.newFile("jSite.conf");
		JarFileLocator jarFileLocator = mock(JarFileLocator.class);
		when(jarFileLocator.locateJarFile()).thenReturn(of(jarFile));
		ConfigurationLocator locator = new ConfigurationLocator(jarFileLocator);
		assertThat(locator.findPreferredLocation(), is(NEXT_TO_JAR_FILE));
		assertThat(locator.getFile(NEXT_TO_JAR_FILE), endsWith("/jSite.conf"));
		assertThat(locator.isValidLocation(NEXT_TO_JAR_FILE), is(true));
	}

	@Test
	public void customLocationCanBeSet() throws Exception {
		File configFile = temporaryFolder.newFile("jSite.conf");
		JarFileLocator jarFileLocator = mock(JarFileLocator.class);
		when(jarFileLocator.locateJarFile()).thenReturn(empty());
		ConfigurationLocator locator = new ConfigurationLocator(jarFileLocator);
		locator.setCustomLocation(configFile.getPath());
		assertThat(locator.findPreferredLocation(), is(CUSTOM));
		assertThat(locator.getFile(CUSTOM), is(configFile.getPath()));
		assertThat(locator.isValidLocation(CUSTOM), is(true));
	}

}

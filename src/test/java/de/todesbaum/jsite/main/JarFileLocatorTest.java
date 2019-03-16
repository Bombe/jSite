package de.todesbaum.jsite.main;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import net.pterodactylus.util.io.StreamCopier;

import de.todesbaum.jsite.main.JarFileLocator.DefaultJarFileLocator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test for {@link JarFileLocator}.
 */
public class JarFileLocatorTest {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static final Class<?> CLASS_TO_LOAD = Main.class;
	private static final String RESOURCE_TO_COPY = CLASS_TO_LOAD.getName().replace('.', '/') + ".class";
	private static final String PACKAGE_NAME = CLASS_TO_LOAD.getPackage().getName();
	private static final String CLASS_FILENAME = CLASS_TO_LOAD.getSimpleName() + ".class";

	@Test
	public void jarFileCanBeLocatedOnPathWithNonUsAsciiCharacters() throws Exception {
		File jarFilePath = temporaryFolder.newFolder("Фото café");
		File jarFile = createJarFile(jarFilePath);
		URLClassLoader urlClassLoader = createClassLoader(jarFile.toURI().toURL());
		JarFileLocator jarFileLocator = new DefaultJarFileLocator(urlClassLoader);
		File locatedJarFile = jarFileLocator.locateJarFile().get();
		assertThat(locatedJarFile, is(jarFile));
	}

	private File createJarFile(File folder) throws Exception {
		File jarFile = new File(folder, "test.jar");
		copyClassFileToStream(RESOURCE_TO_COPY, new FileOutputStream(jarFile));
		return jarFile;
	}

	private void copyClassFileToStream(String fileToCopy, FileOutputStream outputStream) throws IOException {
		try (JarOutputStream jarOutputStream = new JarOutputStream(outputStream);
			 InputStream inputStream = getClass().getResourceAsStream("/" + fileToCopy)) {
			jarOutputStream.putNextEntry(new JarEntry(fileToCopy));
			StreamCopier.copy(inputStream, jarOutputStream);
			jarOutputStream.closeEntry();
		}
	}

	private URLClassLoader createClassLoader(URL url) throws MalformedURLException {
		return new URLClassLoader(new URL[] { url }) {
			@Override
			public URL getResource(String name) {
				/* ignore parent class loader here. */
				return findResource(name);
			}
		};
	}

	@Test
	public void jarFileCanNotBeLocatedWhenLoadedFromFile() throws Exception {
		File folder = temporaryFolder.newFolder(PACKAGE_NAME.split("\\."));
		createClassFile(folder);
		ClassLoader classLoader = createClassLoader(temporaryFolder.getRoot().toURI().toURL());
		JarFileLocator jarFileLocator = new DefaultJarFileLocator(classLoader);
		Optional<File> locatedJarFile = jarFileLocator.locateJarFile();
		assertThat(locatedJarFile.isPresent(), is(false));
	}

	private void createClassFile(File folder) throws IOException {
		File classFile = new File(folder, CLASS_FILENAME);
		try (FileOutputStream outputStream = new FileOutputStream(classFile)) {
			copyClassFileToStream(RESOURCE_TO_COPY, outputStream);
		}
	}

	@Test
	public void jarFileCanNotBeLoadedIfClasspathIsSuperWeirdAndClassDoesNotExist() throws Exception {
		ClassLoader classLoader = createClassLoader(temporaryFolder.getRoot().toURI().toURL());
		JarFileLocator jarFileLocator = new DefaultJarFileLocator(classLoader);
		Optional<File> locatedJarFile = jarFileLocator.locateJarFile();
		assertThat(locatedJarFile.isPresent(), is(false));
	}

}

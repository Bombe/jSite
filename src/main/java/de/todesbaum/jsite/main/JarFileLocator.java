package de.todesbaum.jsite.main;

import static java.util.Optional.empty;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Optional;

/**
 * Locates the JAR file used to load jSite in the filesystem.
 */
public interface JarFileLocator {

	Optional<File> locateJarFile();

	class DefaultJarFileLocator implements JarFileLocator {

		private final ClassLoader classLoader;

		public DefaultJarFileLocator(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@Override
		public Optional<File> locateJarFile() {
			URL resourceUrl = classLoader.getResource(Main.class.getName().replace(".", "/") + ".class");
			if (resourceUrl == null) {
				return empty();
			}
			String resource = resourceUrl.toString();
			if (resource.startsWith("jar:")) {
				try {
					String jarFileLocation = URLDecoder.decode(resource.substring(9, resource.indexOf(".jar!") + 4), "UTF-8");
					return Optional.of(new File(jarFileLocation));
				} catch (UnsupportedEncodingException e) {
					/* location is not available, ignore. */
				}
			}
			return empty();
		}
	}

}

/*
 * jSite - ConfigurationLocator.java - Copyright © 2011–2014 David Roden
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Locator for configuration files in different places.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ConfigurationLocator {

	/**
	 * The location of the configuration directory.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	public enum ConfigurationLocation {

		/** The configuration is in the same directory as the JAR file. */
		NEXT_TO_JAR_FILE,

		/**
		 * The configuration is in the user’s home directory. This is the
		 * pre-0.9.3 default.
		 */
		HOME_DIRECTORY,

		/** Custom location. */
		CUSTOM,

	}

	/** The possible configuration locations. */
	private final Map<ConfigurationLocation, String> configurationFiles = new HashMap<ConfigurationLocation, String>();

	/**
	 * Creates a new configuration locator. If this class is loaded from a JAR
	 * file, {@link ConfigurationLocation#NEXT_TO_JAR_FILE} is added to the list
	 * of possible configuration file locations.
	 * {@link ConfigurationLocation#HOME_DIRECTORY} is always added to this
	 * list, {@link ConfigurationLocation#CUSTOM} has to be enabled by calling
	 * {@link #setCustomLocation(String)}.
	 */
	public ConfigurationLocator(JarFileLocator jarFileLocator) {
		/* are we executed from a JAR file? */
		Optional<File> jarFile = jarFileLocator.locateJarFile();
		if (jarFile.isPresent()) {
			File configurationFile = new File(jarFile.get().getParent(), "jSite.conf");
			configurationFiles.put(ConfigurationLocation.NEXT_TO_JAR_FILE, configurationFile.getPath());
		}
		File homeDirectoryFile = new File(System.getProperty("user.home"), ".jSite/config7");
		configurationFiles.put(ConfigurationLocation.HOME_DIRECTORY, homeDirectoryFile.getPath());
	}

	//
	// ACCESSORS
	//

	/**
	 * Sets the location of the custom configuration file.
	 *
	 * @param customFile
	 *            The custom location of the configuration file
	 */
	public void setCustomLocation(String customFile) {
		configurationFiles.put(ConfigurationLocation.CUSTOM, customFile);
	}

	/**
	 * Returns whether the given location is valid. Certain locations (such as
	 * {@link ConfigurationLocation#NEXT_TO_JAR_FILE}) may be invalid in certain
	 * circumstances (such as the application not being run from a JAR file). A
	 * location being valid does not imply that a configuration file does exist
	 * at the given location, use {@link #hasFile(ConfigurationLocation)} to
	 * check for a configuration file at the desired location.
	 *
	 * @param configurationLocation
	 *            The configuration location
	 * @return {@code true} if the location is valid, {@code false} otherwise
	 */
	public boolean isValidLocation(ConfigurationLocation configurationLocation) {
		return configurationFiles.containsKey(configurationLocation);
	}

	/**
	 * Checks whether a configuration file exists at the given location.
	 *
	 * @param configurationLocation
	 *            The configuration location
	 * @return {@code true} if a configuration file exists at the given
	 *         location, {@code false} otherwise
	 */
	public boolean hasFile(ConfigurationLocation configurationLocation) {
		if (!isValidLocation(configurationLocation)) {
			return false;
		}
		return new File(configurationFiles.get(configurationLocation)).exists();
	}

	/**
	 * Returns the configuration file for the given location.
	 *
	 * @param configurationLocation
	 *            The location to get the file for
	 * @return The name of the configuration file at the given location, or
	 *         {@code null} if the given location is invalid
	 */
	public String getFile(ConfigurationLocation configurationLocation) {
		return configurationFiles.get(configurationLocation);
	}

	//
	// ACTIONS
	//

	/**
	 * Finds the preferred location of the configuration file.
	 *
	 * @see #findPreferredLocation(ConfigurationLocation)
	 * @return The preferred location of the configuration file
	 */
	public ConfigurationLocation findPreferredLocation() {
		return findPreferredLocation(ConfigurationLocation.NEXT_TO_JAR_FILE);
	}

	/**
	 * Finds the preferred location of the configuration file. The following
	 * checks are performed: if a custom configuration location has been defined
	 * (by calling {@link #setCustomLocation(String)})
	 * {@link ConfigurationLocation#CUSTOM} is returned. If the application is
	 * run from a JAR file and a configuration file is found next to the JAR
	 * file (i.e. in the same directory),
	 * {@link ConfigurationLocation#NEXT_TO_JAR_FILE} is returned. If a
	 * configuration file exists in the user’s home directory,
	 * {@link ConfigurationLocation#HOME_DIRECTORY} is returned. Otherwise, the
	 * given {@code defaultLocation} is returned.
	 *
	 * @param defaultLocation
	 *            The default location to return if no other configuration file
	 *            is found
	 * @return The configuration location to load the configuration from
	 */
	public ConfigurationLocation findPreferredLocation(ConfigurationLocation defaultLocation) {
		if (hasFile(ConfigurationLocation.CUSTOM)) {
			return ConfigurationLocation.CUSTOM;
		}
		if (hasFile(ConfigurationLocation.NEXT_TO_JAR_FILE)) {
			return ConfigurationLocation.NEXT_TO_JAR_FILE;
		}
		if (hasFile(ConfigurationLocation.HOME_DIRECTORY)) {
			return ConfigurationLocation.HOME_DIRECTORY;
		}
		if (isValidLocation(defaultLocation)) {
			return defaultLocation;
		}
		return ConfigurationLocation.HOME_DIRECTORY;
	}

}

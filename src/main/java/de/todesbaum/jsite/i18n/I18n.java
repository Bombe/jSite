/*
 * jSite - I18n.java - Copyright © 2006–2014 David Roden
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

package de.todesbaum.jsite.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Maps i18n keys to translated texts, depending on a current locale.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class I18n {

	/** The default locale, English. */
	private static Locale defaultLocale = new Locale("en");

	/** The current locale. */
	private static Locale currentLocale;

	/**
	 * Returns the currently set locale.
	 *
	 * @return The current locale
	 */
	public static Locale getLocale() {
		if (currentLocale == null) {
			currentLocale = Locale.getDefault();
		}
		return currentLocale;
	}

	/**
	 * Sets the current locale.
	 *
	 * @param locale
	 *            The new current locale
	 */
	public static void setLocale(Locale locale) {
		currentLocale = locale;
		Locale.setDefault(locale);
	}

	/**
	 * Returns the resource bundle for the current locale.
	 *
	 * @return The resource bundle for the current locale
	 */
	public static ResourceBundle getResourceBundle() {
		return getResourceBundle(getLocale());
	}

	/**
	 * Returns the resource bundle for the given locale.
	 *
	 * @param locale
	 *            The locale to get the resource bundle for
	 * @return The resource bundle for the given locale
	 */
	public static ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle("de.todesbaum.jsite.i18n.jSite", locale);
	}

	/**
	 * Retrieves a translated text for the given i18n key. If the resource
	 * bundle for the current locale does not have a translation for the given
	 * key, the default locale is tried. If that fails, the key is returned.
	 *
	 * @param key
	 *            The key to get the translation for
	 * @return The translated value, or the key itself if not translation can be
	 *         found
	 */
	public static String getMessage(String key) {
		try {
			return getResourceBundle().getString(key);
		} catch (MissingResourceException mre1) {
			try {
				return getResourceBundle(defaultLocale).getString(key);
			} catch (MissingResourceException mre2) {
				return key;
			}
		}
	}

}

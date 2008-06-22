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

package de.todesbaum.jsite.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class I18n {

	private static Locale defaultLocale = new Locale("en");
	private static Locale currentLocale;

	public static Locale getLocale() {
		if (currentLocale == null) {
			currentLocale = Locale.getDefault();
		}
		return currentLocale;
	}

	public static void setLocale(Locale locale) {
		currentLocale = locale;
		Locale.setDefault(locale);
	}

	public static ResourceBundle getResourceBundle() {
		return getResourceBundle(getLocale());
	}

	public static ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle("de.todesbaum.jsite.i18n.jSite", locale);
	}

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

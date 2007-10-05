/*
 * jSite-remote - I18nContainer.java Copyright © 2007 David Roden
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package de.todesbaum.jsite.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class I18nContainer implements Iterable<Runnable> {

	private static final I18nContainer singleton = new I18nContainer();
	private final List<Runnable> i18nRunnables = Collections.synchronizedList(new ArrayList<Runnable>());
	private final List<Runnable> i18nPostRunnables = Collections.synchronizedList(new ArrayList<Runnable>());

	public static I18nContainer getInstance() {
		return singleton;
	}

	public void registerRunnable(Runnable i18nRunnable) {
		i18nRunnables.add(i18nRunnable);
	}

	public void registerPostRunnable(Runnable i18nPostRunnable) {
		i18nPostRunnables.add(i18nPostRunnable);
	}

	public Iterator<Runnable> iterator() {
		List<Runnable> allRunnables = new ArrayList<Runnable>();
		allRunnables.addAll(i18nRunnables);
		allRunnables.addAll(i18nPostRunnables);
		return allRunnables.iterator();
	}

}

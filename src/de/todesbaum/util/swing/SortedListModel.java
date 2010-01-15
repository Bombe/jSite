/*
 * todesbaum-lib -
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

package de.todesbaum.util.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractListModel;

/**
 * @param <T>
 *            The type of the elements
 * @author David Roden &lt;droden@gmail.com&gt;
 */
public class SortedListModel<T extends Comparable<T>> extends AbstractListModel implements List<T> {

	/** The elements. */
	private List<T> elements = new ArrayList<T>();

	/**
	 * {@inheritDoc}
	 */
	public int getSize() {
		return size();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getElementAt(int index) {
		return elements.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public void add(int index, T element) {
		elements.add(index, element);
		Collections.sort(elements);
		fireContentsChanged(this, 0, size());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean add(T o) {
		boolean result = elements.add(o);
		Collections.sort(elements);
		fireContentsChanged(this, 0, size());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAll(Collection<? extends T> c) {
		boolean result = elements.addAll(c);
		Collections.sort(elements);
		fireContentsChanged(this, 0, size());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean result = elements.addAll(index, c);
		Collections.sort(elements);
		fireContentsChanged(this, 0, size());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		elements.clear();
		fireContentsChanged(this, 0, size());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Object o) {
		return elements.contains(o);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsAll(Collection<?> c) {
		return elements.containsAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		return elements.equals(o);
	}

	/**
	 * {@inheritDoc}
	 */
	public T get(int index) {
		return elements.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public int indexOf(Object o) {
		return elements.indexOf(o);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public int lastIndexOf(Object o) {
		return elements.lastIndexOf(o);
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<T> listIterator() {
		return elements.listIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<T> listIterator(int index) {
		return elements.listIterator(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public T remove(int index) {
		fireContentsChanged(this, 0, size());
		return elements.remove(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(Object o) {
		fireContentsChanged(this, 0, size());
		return elements.remove(o);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeAll(Collection<?> c) {
		fireContentsChanged(this, 0, size());
		return elements.removeAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean retainAll(Collection<?> c) {
		fireContentsChanged(this, 0, size());
		return elements.retainAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	public T set(int index, T element) {
		T result = elements.set(index, element);
		Collections.sort(elements);
		fireContentsChanged(this, 0, size());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return elements.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<T> subList(int fromIndex, int toIndex) {
		return elements.subList(fromIndex, toIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] toArray() {
		return elements.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public <U extends Object> U[] toArray(U[] a) {
		return elements.toArray(a);
	}

}

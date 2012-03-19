/*
 * jSite - TLabel.java - Copyright © 2006–2012 David Roden
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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id$
 */
public class TLabel extends JLabel {

	public TLabel() {
		super();
	}

	public TLabel(int mnemonic, Component labelFor) {
		super();
		setDisplayedMnemonic(mnemonic);
		setLabelFor(labelFor);
	}

	public TLabel(Icon image) {
		super(image);
	}

	public TLabel(Icon image, int mnemonic, Component labelFor) {
		super(image);
		setDisplayedMnemonic(mnemonic);
		setLabelFor(labelFor);
	}

	public TLabel(Icon image, int horizontalAlignment) {
		super(image);
	}

	public TLabel(Icon image, int horizontalAlignment, int mnemonic, Component labelFor) {
		super(image);
		setDisplayedMnemonic(mnemonic);
		setLabelFor(labelFor);
	}

	public TLabel(String text) {
		super(text);
	}

	public TLabel(String text, int mnemonic, Component labelFor) {
		super(text);
		setDisplayedMnemonic(mnemonic);
		setLabelFor(labelFor);
		setAlignmentX(0.0f);
	}

	public TLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public TLabel(String text, Icon icon, int horizontalAlignment, int mnemonic, Component labelFor) {
		super(text, icon, horizontalAlignment);
		setDisplayedMnemonic(mnemonic);
		setLabelFor(labelFor);
	}

	public TLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public TLabel(String text, int horizontalAlignment, int mnemonic, Component labelFor) {
		super(text, horizontalAlignment);
		setDisplayedMnemonic(mnemonic);
		setLabelFor(labelFor);
	}

}

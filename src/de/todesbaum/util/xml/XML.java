/*
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

package de.todesbaum.util.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;


/**
 * Contains method to transform DOM XML trees to byte arrays and vice versa.
 *
 * @author David Roden &lt;droden@gmail.com&gt;
 * @version $Id:XML.java 221 2006-03-06 14:46:49Z bombe $
 */
public class XML {

	/** Cached document builder factory. */
	private static DocumentBuilderFactory documentBuilderFactory = null;

	/** Cached document builder. */
	private static DocumentBuilder documentBuilder = null;

	/** Cached transformer factory. */
	private static TransformerFactory transformerFactory = null;

	/** Does nothing. */
	private XML() {
	}

	/**
	 * Returns a document builder factory. If possible the cached instance will be returned.
	 *
	 * @return A document builder factory
	 */
	private static DocumentBuilderFactory getDocumentBuilderFactory() {
		if (documentBuilderFactory != null) {
			return documentBuilderFactory;
		}
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		return documentBuilderFactory;
	}

	/**
	 * Returns a document builder. If possible the cached instance will be returned.
	 *
	 * @return A document builder
	 */
	private static DocumentBuilder getDocumentBuilder() {
		if (documentBuilder != null) {
			return documentBuilder;
		}
		try {
			documentBuilder = getDocumentBuilderFactory().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
		}
		return documentBuilder;
	}

	/**
	 * Returns a transformer factory. If possible the cached instance will be returned.
	 *
	 * @return A transformer factory
	 */
	private static TransformerFactory getTransformerFactory() {
		if (transformerFactory != null) {
			return transformerFactory;
		}
		transformerFactory = TransformerFactory.newInstance();
		return transformerFactory;
	}

	/**
	 * Creates a new XML document.
	 *
	 * @return A new XML document
	 */
	public static Document createDocument() {
		return getDocumentBuilder().newDocument();
	}

	/**
	 * Transforms the DOM XML document into a byte array.
	 *
	 * @param document
	 *            The document to transform
	 * @return The byte array containing the XML representation
	 */
	public static byte[] transformToByteArray(Document document) {
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		OutputStreamWriter converter = new OutputStreamWriter(byteOutput, Charset.forName("UTF-8"));
		Result transformResult = new StreamResult(converter);
		Source documentSource = new DOMSource(document);
		try {
			Transformer transformer = getTransformerFactory().newTransformer();
			transformer.transform(documentSource, transformResult);
			byteOutput.close();
			return byteOutput.toByteArray();
		} catch (IOException ioe1) {
		} catch (TransformerConfigurationException tce1) {
		} catch (TransformerException te1) {
		} finally {
			try {
				byteOutput.close();
			} catch (IOException ioe1) {
			}
		}
		return null;
	}

	/**
	 * Transforms the byte array into a DOM XML document.
	 *
	 * @param data
	 *            The byte array to parse
	 * @return The DOM XML document
	 */
	public static Document transformToDocument(byte[] data) {
		ByteArrayInputStream byteInput = new ByteArrayInputStream(data);
		InputStreamReader converter = new InputStreamReader(byteInput, Charset.forName("UTF-8"));
		Source xmlSource = new StreamSource(converter);
		Result xmlResult = new DOMResult();
		try {
			Transformer transformer = getTransformerFactory().newTransformer();
			transformer.transform(xmlSource, xmlResult);
			return (Document) ((DOMResult) xmlResult).getNode();
		} catch (TransformerConfigurationException tce1) {
		} catch (TransformerException te1) {
		} finally {
			if (byteInput != null)
				try {
					byteInput.close();
				} catch (IOException ioe1) {
				}
			if (converter != null)
				try {
					converter.close();
				} catch (IOException ioe1) {
				}
		}
		return null;
	}

}

package de.todesbaum.jsite.gui;

/**
 * Container for a scanned file, consisting of the name of the file and its
 * hash.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ScannedFile implements Comparable<ScannedFile> {

	/** The name of the file. */
	private final String filename;

	/** The hash of the file. */
	private final String hash;

	/**
	 * Creates a new scanned file.
	 *
	 * @param filename
	 *            The name of the file
	 * @param hash
	 *            The hash of the file
	 */
	public ScannedFile(String filename, String hash) {
		this.filename = filename;
		this.hash = hash;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the name of the file.
	 *
	 * @return The name of the file
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the hash of the file.
	 *
	 * @return The hash of the file
	 */
	public String getHash() {
		return hash;
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return filename.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		return filename.equals(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return filename;
	}

	//
	// COMPARABLE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(ScannedFile scannedFile) {
		return filename.compareTo(scannedFile.filename);
	}

}

package de.todesbaum.jsite.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import org.junit.Test;

/**
 * Unit test for {@link FileOption}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FileOptionTest {

	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
	private static final String DEFAULT_CUSTOM_KEY = "CHK@";
	private static final String CUSTOM_KEY = "KSK@custom-key";
	private static final String EMPTY_CUSTOM_KEY = "";
	private static final boolean DEFAULT_INSERT = true;
	private static final boolean CUSTOM_INSERT = false;
	private static final boolean DEFAULT_FORCE_INSERT = false;
	private static final boolean CUSTOM_FORCE_INSERT = true;
	private static final boolean DEFAULT_INSERT_REDIRECT = true;
	private static final boolean CUSTOM_INSERT_REDIRECT = false;
	private static final String DEFAULT_LAST_INSERT_HASH = null;
	private static final String CUSTOM_LAST_INSERT_HASH = "last-insert-hash";
	private static final int DEFAULT_LAST_INSERT_EDITION = 0;
	private static final int CUSTOM_LAST_INSERT_EDITION = 12345;
	private static final String DEFAULT_LAST_INSERT_FILENAME = null;
	private static final String CUSTOM_LAST_INSERT_FILENAME = "filename.dat";
	private static final String DEFAULT_CURRENT_HASH = null;
	private static final String CUSTOM_CURRENT_HASH = "current-hash";
	private static final Optional<?> DEFAULT_CHANGED_NAME = Optional.empty();
	private static final String CUSTOM_CHANGED_NAME = "changed-name";
	private static final String NULL_CHANGED_NAME = null;
	private static final String ZERO_LENGTH_CHANGED_NAME = "";
	private static final String CUSTOM_MIME_TYPE = "custom/mime-type";
	private static final String NULL_MIME_TYPE = null;
	private final FileOption fileOption = new FileOption(DEFAULT_MIME_TYPE);

	@Test
	public void defaultCustomKeyIsCHK() {
		assertThat(fileOption.getCustomKey(), is(DEFAULT_CUSTOM_KEY));
	}

	@Test
	public void customKeyIsRetainedCorrectly() {
		fileOption.setCustomKey(CUSTOM_KEY);
		assertThat(fileOption.getCustomKey(), is(CUSTOM_KEY));
	}

	@Test
	public void nullCustomKeyIsTurnedIntoEmptyCustomKey() {
		fileOption.setCustomKey(null);
		assertThat(fileOption.getCustomKey(), is(EMPTY_CUSTOM_KEY));
	}

	@Test
	public void defaultInsertIsTrue() {
		assertThat(fileOption.isInsert(), is(DEFAULT_INSERT));
	}

	@Test
	public void insertIsRetainedCorrectly() {
		fileOption.setInsert(CUSTOM_INSERT);
		assertThat(fileOption.isInsert(), is(CUSTOM_INSERT));
	}

	@Test
	public void defaultForceInsertIsFalse() {
		assertThat(fileOption.isForceInsert(), is(DEFAULT_FORCE_INSERT));
	}

	@Test
	public void customForceInsertIsRetainedCorrectly() {
		fileOption.setForceInsert(CUSTOM_FORCE_INSERT);
		assertThat(fileOption.isForceInsert(), is(CUSTOM_FORCE_INSERT));
	}

	@Test
	public void defaultInsertRedirectIsTrue() {
		assertThat(fileOption.isInsertRedirect(), is(DEFAULT_INSERT_REDIRECT));
	}

	@Test
	public void customInsertRedirectIsRetainedCorrectly() {
		fileOption.setInsertRedirect(CUSTOM_INSERT_REDIRECT);
		assertThat(fileOption.isInsertRedirect(), is(CUSTOM_INSERT_REDIRECT));
	}

	@Test
	public void defaultLastInsertHashIsNull() {
		assertThat(fileOption.getLastInsertHash(), is(DEFAULT_LAST_INSERT_HASH));
	}

	@Test
	public void lastInsertHashIsRetainedCorrectly() {
		fileOption.setLastInsertHash(CUSTOM_LAST_INSERT_HASH);
		assertThat(fileOption.getLastInsertHash(), is(CUSTOM_LAST_INSERT_HASH));
	}

	@Test
	public void defaultLastInsertEditionIsZero() {
		assertThat(fileOption.getLastInsertEdition(), is(DEFAULT_LAST_INSERT_EDITION));
	}

	@Test
	public void lastInsertEditionIsRetainedCorrectly() {
		fileOption.setLastInsertEdition(CUSTOM_LAST_INSERT_EDITION);
		assertThat(fileOption.getLastInsertEdition(), is(CUSTOM_LAST_INSERT_EDITION));
	}

	@Test
	public void defaultLastInsertFilenameIsNull() {
		assertThat(fileOption.getLastInsertFilename(), is(DEFAULT_LAST_INSERT_FILENAME));
	}

	@Test
	public void lastInsertFilenameIsRetainedCorrectly() {
		fileOption.setLastInsertFilename(CUSTOM_LAST_INSERT_FILENAME);
		assertThat(fileOption.getLastInsertFilename(), is(CUSTOM_LAST_INSERT_FILENAME));
	}

	@Test
	public void defaultCurrentHashIsNull() {
		assertThat(fileOption.getCurrentHash(), is(DEFAULT_CURRENT_HASH));
	}

	@Test
	public void currentHashIsRetainedCorrectly() {
		fileOption.setCurrentHash(CUSTOM_CURRENT_HASH);
		assertThat(fileOption.getCurrentHash(), is(CUSTOM_CURRENT_HASH));
	}

	@Test
	public void defaultChangedNameIsEmpty() {
		assertThat(fileOption.getChangedName(), is(DEFAULT_CHANGED_NAME));
	}

	@Test
	public void changedNameIsRetainedCorrectly() {
		fileOption.setChangedName(CUSTOM_CHANGED_NAME);
		assertThat(fileOption.getChangedName().get(), is(CUSTOM_CHANGED_NAME));
	}

	@Test
	public void nullSetsChangedNameToEmpty() {
		fileOption.setChangedName(NULL_CHANGED_NAME);
		assertThat(fileOption.getChangedName(), is(DEFAULT_CHANGED_NAME));
	}

	@Test
	public void zeroLengthStringSetsChangedNameToEmpty() {
		fileOption.setChangedName(ZERO_LENGTH_CHANGED_NAME);
		assertThat(fileOption.getChangedName(), is(DEFAULT_CHANGED_NAME));
	}

	@Test
	public void defaultMimeTypeIsTheOneGivenInTheConstructor() {
		assertThat(fileOption.getMimeType(), is(DEFAULT_MIME_TYPE));
	}

	@Test
	public void mimeTypeIsRetainedCorrectly() {
		fileOption.setMimeType(CUSTOM_MIME_TYPE);
		assertThat(fileOption.getMimeType(), is(CUSTOM_MIME_TYPE));
	}

	@Test
	public void nullSetsMimeTypeBackToTheOneGivenInConstructor() {
		fileOption.setMimeType(NULL_MIME_TYPE);
		assertThat(fileOption.getMimeType(), is(DEFAULT_MIME_TYPE));
	}

	@Test
	public void fileWithCustomInsertIsCustom() {
		fileOption.setInsert(CUSTOM_INSERT);
		assertThat(fileOption.isCustom(), is(true));
	}

	@Test
	public void fileWithCustomKeyIsCustom() {
		fileOption.setCustomKey(CUSTOM_KEY);
		assertThat(fileOption.isCustom(), is(true));
	}

	@Test
	public void fileWithChangedNameIsCustom() {
		fileOption.setChangedName(CUSTOM_CHANGED_NAME);
		assertThat(fileOption.isCustom(), is(true));
	}

	@Test
	public void fileWithCustomMimeTypeIsCustom() {
		fileOption.setMimeType(CUSTOM_MIME_TYPE);
		assertThat(fileOption.isCustom(), is(true));
	}

	@Test
	public void fileWithCustomInsertRedirectIsCustom() {
		fileOption.setInsertRedirect(CUSTOM_INSERT_REDIRECT);
		assertThat(fileOption.isCustom(), is(true));
	}

	@Test
	public void unchangedFileIsNotCustom() {
		assertThat(fileOption.isCustom(), is(false));
	}

	@Test
	public void copyConstructorCopiesAllProperties() {
		fileOption.setChangedName(CUSTOM_CHANGED_NAME);
		fileOption.setInsertRedirect(CUSTOM_INSERT_REDIRECT);
		fileOption.setInsert(CUSTOM_INSERT);
		fileOption.setMimeType(CUSTOM_MIME_TYPE);
		fileOption.setCurrentHash(CUSTOM_CURRENT_HASH);
		fileOption.setCustomKey(CUSTOM_KEY);
		fileOption.setForceInsert(CUSTOM_FORCE_INSERT);
		fileOption.setLastInsertEdition(CUSTOM_LAST_INSERT_EDITION);
		fileOption.setLastInsertFilename(CUSTOM_LAST_INSERT_FILENAME);
		fileOption.setLastInsertHash(CUSTOM_LAST_INSERT_HASH);
		fileOption.setCurrentHash(CUSTOM_CURRENT_HASH);
		FileOption copiedFileOption = new FileOption(fileOption);
		assertThat(copiedFileOption.getChangedName().get(), is(CUSTOM_CHANGED_NAME));
		assertThat(copiedFileOption.isInsertRedirect(), is(CUSTOM_INSERT_REDIRECT));
		assertThat(copiedFileOption.isInsert(), is(CUSTOM_INSERT));
		assertThat(copiedFileOption.getMimeType(), is(CUSTOM_MIME_TYPE));
		assertThat(copiedFileOption.getCurrentHash(), is(CUSTOM_CURRENT_HASH));
		assertThat(copiedFileOption.getCustomKey(), is(CUSTOM_KEY));
		assertThat(copiedFileOption.isForceInsert(), is(CUSTOM_FORCE_INSERT));
		assertThat(copiedFileOption.getLastInsertEdition(), is(CUSTOM_LAST_INSERT_EDITION));
		assertThat(copiedFileOption.getLastInsertFilename(), is(CUSTOM_LAST_INSERT_FILENAME));
		assertThat(copiedFileOption.getLastInsertHash(), is(CUSTOM_LAST_INSERT_HASH));
		assertThat(copiedFileOption.getCurrentHash(), is(CUSTOM_CURRENT_HASH));
	}

}

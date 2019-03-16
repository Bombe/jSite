package de.todesbaum.jsite.application.validation;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Objects;

/**
 * Container class for a single issue. An issue contains an error key that describes the error,
 * and a fatality flag that determines whether the insert has to be aborted (if the flag is
 * {@code true}) or if it can still be performed and only a warning should be generated (if the
 * flag is {@code false}).
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Issue {

	private final String errorKey;
	private final boolean fatal;
	private final String[] parameters;

	Issue(String errorKey, boolean fatal, String... parameters) {
		this.errorKey = errorKey;
		this.fatal = fatal;
		this.parameters = parameters;
	}

	public String getErrorKey() {
		return errorKey;
	}

	public boolean isFatal() {
		return fatal;
	}

	public String[] getParameters() {
		return parameters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Issue issue = (Issue) o;
		return fatal == issue.fatal &&
				Objects.equals(errorKey, issue.errorKey) &&
				Arrays.equals(parameters, issue.parameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(errorKey, fatal, parameters);
	}

	@Override
	public String toString() {
		return format("Issue{errorKey=\"%s\", fatal=%s, parameters=%s}", errorKey, fatal, Arrays.toString(parameters));
	}

}

package de.todesbaum.jsite.application.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.todesbaum.jsite.application.Project;

/**
 * Container class that collects all warnings and errors that occured during
 * {@link ProjectValidator#validateProject(Project) project validation}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CheckReport implements Iterable<Issue> {

	private final List<Issue> issues = new ArrayList<>();

	void addIssue(String errorKey, boolean fatal, String... parameters) {
		issues.add(new Issue(errorKey, fatal, parameters));
	}

	public Collection<Issue> getIssues() {
		return issues;
	}

	@Override
	public Iterator<Issue> iterator() {
		return issues.iterator();
	}

	public boolean isEmpty() {
		return issues.isEmpty();
	}

}

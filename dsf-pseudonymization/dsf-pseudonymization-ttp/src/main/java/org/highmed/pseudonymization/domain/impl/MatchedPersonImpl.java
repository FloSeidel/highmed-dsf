package org.highmed.pseudonymization.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MatchedPersonImpl implements MatchedPerson<PersonWithMdat>
{
	private final List<PersonWithMdat> matches = new ArrayList<>();

	public MatchedPersonImpl(PersonWithMdat person)
	{
		if (person != null)
			matches.add(person);
	}

	@JsonCreator
	public MatchedPersonImpl(@JsonProperty("matches") Collection<? extends PersonWithMdat> matches)
	{
		if (matches != null)
			this.matches.addAll(matches);
	}

	@Override
	public List<PersonWithMdat> getMatches()
	{
		return Collections.unmodifiableList(matches);
	}

	@Override
	public void addMatch(PersonWithMdat person)
	{
		if (person != null)
			matches.add(person);
	}
}

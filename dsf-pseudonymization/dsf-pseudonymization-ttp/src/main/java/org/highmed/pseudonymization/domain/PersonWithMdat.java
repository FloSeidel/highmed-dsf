package org.highmed.pseudonymization.domain;

import org.highmed.pseudonymization.recordlinkage.Person;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "@type")
public interface PersonWithMdat extends Person
{
	MdatContainer getMdatContainer();
}

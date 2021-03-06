package org.highmed.dsf.fhir.service;

import java.util.stream.Stream;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;

public interface ReferenceExtractor
{
	Stream<ResourceReference> getReferences(Resource resource);

	Stream<ResourceReference> getReferences(ActivityDefinition resource);

	Stream<ResourceReference> getReferences(Binary resource);

	// Not implemented yet, special rules apply for tmp ids
	// Stream<ResourceReference> getReferences(Bundle bundle);

	Stream<ResourceReference> getReferences(CodeSystem resource);

	Stream<ResourceReference> getReferences(Endpoint resource);

	Stream<ResourceReference> getReferences(Group group);

	Stream<ResourceReference> getReferences(HealthcareService resource);

	Stream<ResourceReference> getReferences(Location resource);

	Stream<ResourceReference> getReferences(NamingSystem resource);

	Stream<ResourceReference> getReferences(Organization resource);

	Stream<ResourceReference> getReferences(Patient resource);

	Stream<ResourceReference> getReferences(Practitioner resource);

	Stream<ResourceReference> getReferences(PractitionerRole resource);

	Stream<ResourceReference> getReferences(Provenance resource);

	Stream<ResourceReference> getReferences(ResearchStudy resource);

	Stream<ResourceReference> getReferences(StructureDefinition resource);

	Stream<ResourceReference> getReferences(Subscription resource);

	Stream<ResourceReference> getReferences(Task resource);

	Stream<ResourceReference> getReferences(ValueSet resource);
}
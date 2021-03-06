package org.highmed.dsf.fhir.dao.command;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;

public class ResourceReferenceTest
{
	private static final String serverBase = "http://foo.bar/baz";

	@Test
	public void testGetTypeTemporary() throws Exception
	{
		var r1 = new ResourceReference("Foo.bar",
				new Reference(Command.URL_UUID_PREFIX + UUID.randomUUID().toString()));
		var r2 = new ResourceReference("Foo.bar", new Reference(UUID.randomUUID().toString()));

		assertEquals(ReferenceType.TEMPORARY, r1.getType(serverBase));
		assertEquals(ReferenceType.UNKNOWN, r2.getType(serverBase));
	}

	@Test
	public void testGetTypeLiteralInternal() throws Exception
	{
		var r1 = new ResourceReference("Foo.bar", new Reference("Patient/" + UUID.randomUUID().toString()));
		var r2 = new ResourceReference("Foo.bar",
				new Reference("Patient/" + UUID.randomUUID().toString() + "/_history/123"));
		var r3 = new ResourceReference("Foo.bar",
				new Reference(serverBase + "/Patient/" + UUID.randomUUID().toString()));
		var r4 = new ResourceReference("Foo.bar",
				new Reference(serverBase + "/Patient/" + UUID.randomUUID().toString() + "/_history/123"));
		var r5 = new ResourceReference("Foo.bar", new Reference("Patient/" + UUID.randomUUID().toString() + "/foo"));
		var r6 = new ResourceReference("Foo.bar",
				new Reference("Patient/" + UUID.randomUUID().toString() + "/_history/123/foo"));
		var r7 = new ResourceReference("Foo.bar",
				new Reference(serverBase + "/Patient/" + UUID.randomUUID().toString() + "/foo"));
		var r8 = new ResourceReference("Foo.bar",
				new Reference(serverBase + "/Patient/" + UUID.randomUUID().toString() + "/_history/123/foo"));

		assertEquals(ReferenceType.LITERAL_INTERNAL, r1.getType(serverBase));
		assertEquals(ReferenceType.LITERAL_INTERNAL, r2.getType(serverBase));
		assertEquals(ReferenceType.LITERAL_INTERNAL, r3.getType(serverBase));
		assertEquals(ReferenceType.LITERAL_INTERNAL, r4.getType(serverBase));

		assertEquals(ReferenceType.UNKNOWN, r5.getType(serverBase));
		assertEquals(ReferenceType.UNKNOWN, r6.getType(serverBase));
		assertEquals(ReferenceType.UNKNOWN, r7.getType(serverBase));
		assertEquals(ReferenceType.UNKNOWN, r8.getType(serverBase));
	}

	@Test
	public void testGetTypeLiteralExternal() throws Exception
	{
		var r1 = new ResourceReference("Foo.bar",
				new Reference("http://blub.com/fhir/Patient/" + UUID.randomUUID().toString()));
		var r2 = new ResourceReference("Foo.bar",
				new Reference("http://blub.com/fhir/Patient/" + UUID.randomUUID().toString() + "/_history/123"));
		var r3 = new ResourceReference("Foo.bar",
				new Reference("http://blub.com/fhir/Patient/" + UUID.randomUUID().toString() + "/foo"));
		var r4 = new ResourceReference("Foo.bar",
				new Reference("http://blub.com/fhir/Patient/" + UUID.randomUUID().toString() + "/_history/123/foo"));

		assertEquals(ReferenceType.LITERAL_EXTERNAL, r1.getType(serverBase));
		assertEquals(ReferenceType.LITERAL_EXTERNAL, r2.getType(serverBase));

		assertEquals(ReferenceType.UNKNOWN, r3.getType(serverBase));
		assertEquals(ReferenceType.UNKNOWN, r4.getType(serverBase));
	}

	@Test
	public void testGetTypeConditional() throws Exception
	{
		var r1 = new ResourceReference("Foo.bar", new Reference("Patient?foo=bar"));
		var r2 = new ResourceReference("Foo.bar", new Reference("?foo=bar"));

		assertEquals(ReferenceType.CONDITIONAL, r1.getType(serverBase));

		assertEquals(ReferenceType.UNKNOWN, r2.getType(serverBase));
	}

	@Test
	public void testGetTypeLogical() throws Exception
	{
		var r1 = new ResourceReference("Foo.bar", new Reference().setType("Patient")
				.setIdentifier(new Identifier().setSystem("system").setValue("value")));
		var r2 = new ResourceReference("Foo.bar",
				new Reference().setIdentifier(new Identifier().setSystem("system").setValue("value")));
		var r3 = new ResourceReference("Foo.bar", new Reference().setType("Patient"));
		var r4 = new ResourceReference("Foo.bar", new Reference().setType("Patient").setIdentifier(new Identifier()));
		var r5 = new ResourceReference("Foo.bar",
				new Reference().setType("Patient").setIdentifier(new Identifier().setValue("value")));
		var r6 = new ResourceReference("Foo.bar",
				new Reference().setType("Patient").setIdentifier(new Identifier().setSystem("system")));

		assertEquals(ReferenceType.LOGICAL, r1.getType(serverBase));

		assertEquals(ReferenceType.UNKNOWN, r2.getType(serverBase));
		assertEquals(ReferenceType.UNKNOWN, r3.getType(serverBase));
		assertEquals(ReferenceType.UNKNOWN, r4.getType(serverBase));
		assertEquals(ReferenceType.UNKNOWN, r5.getType(serverBase));
		assertEquals(ReferenceType.UNKNOWN, r6.getType(serverBase));
	}
}

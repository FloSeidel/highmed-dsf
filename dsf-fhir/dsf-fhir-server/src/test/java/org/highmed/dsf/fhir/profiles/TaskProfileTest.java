package org.highmed.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.highmed.dsf.fhir.service.ResourceValidator;
import org.highmed.dsf.fhir.service.ResourceValidatorImpl;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.dsf.fhir.service.SnapshotGeneratorImpl;
import org.highmed.dsf.fhir.service.StructureDefinitionReader;
import org.highmed.dsf.fhir.service.ValidationSupportWithCustomResources;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.hl7.fhir.r4.model.UnsignedIntType;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class TaskProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-task-base-0.3.0.xml", "highmed-task-start-ping-process-0.3.0.xml",
					"highmed-task-ping-0.3.0.xml", "highmed-task-pong-0.3.0.xml",
					"highmed-task-update-allow-list-0.3.0.xml", "highmed-task-request-update-resources-0.3.0.xml",
					"highmed-task-execute-update-resources-0.3.0.xml", "highmed-group-0.3.0.xml",
					"highmed-extension-group-id-0.3.0.xml", "highmed-research-study-feasibility-0.3.0.xml",
					"highmed-task-request-simple-feasibility-0.3.0.xml",
					"highmed-task-execute-simple-feasibility-0.3.0.xml",
					"highmed-task-single-medic-result-simple-feasibility-0.3.0.xml",
					"highmed-task-compute-simple-feasibility-0.3.0.xml",
					"highmed-task-multi-medic-result-simple-feasibility-0.3.0.xml",
					"highmed-task-error-simple-feasibility-0.3.0.xml",
					"highmed-task-local-services-integration-0.3.0.xml", "highmed-task-download-allow-list-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "bpmn-message-0.3.0.xml", "update-allow-list-0.3.0.xml",
					"update-resources-0.3.0.xml", "feasibility-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "bpmn-message-0.3.0.xml", "update-allow-list-0.3.0.xml",
					"update-resources-0.3.0.xml", "feasibility-0.3.0.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testGenerateSnapshotNotWorkingWithoutBaseSnapshot() throws Exception
	{
		var reader = new StructureDefinitionReader(validationRule.getFhirContext());

		StructureDefinition base = reader
				.readXml(Paths.get("src/main/resources/fhir/StructureDefinition/highmed-task-base-0.3.0.xml"));
		StructureDefinition differential = reader.readXml(Paths.get(
				"src/main/resources/fhir/StructureDefinition/highmed-task-execute-update-resources-0.3.0.xml"));

		var validationSupport = new ValidationSupportChain(
				new InMemoryTerminologyServerValidationSupport(validationRule.getFhirContext()),
				new ValidationSupportWithCustomResources(validationRule.getFhirContext(), Arrays.asList(base),
						Collections.emptyList(), Collections.emptyList()),
				new DefaultProfileValidationSupport(validationRule.getFhirContext()));
		var snapshotGenerator = new SnapshotGeneratorImpl(validationRule.getFhirContext(), validationSupport);

		SnapshotWithValidationMessages messages = snapshotGenerator.generateSnapshot(differential);
		assertFalse(messages.getMessages().isEmpty());
	}

	@Test
	public void testTaskStartPingProcessProfileValid() throws Exception
	{
		Task task = createValidTaskStartPingProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid1() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid2() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setIntent(TaskIntent.FILLERORDER);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartProcessProfileNotValid3() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setAuthoredOn(null);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskStartPingProcess()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-ping-process");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("startPingProcessMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");

		return task;
	}

	@Test
	public void testTaskPingValid() throws Exception
	{
		Task task = createValidTaskPing();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskPing()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-ping");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/pong/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("pingMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("correlation-key");

		return task;
	}

	@Test
	public void testTaskPongValid() throws Exception
	{
		Task task = createValidTaskPong();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskPong()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-pong");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("pongMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("correlation-key");

		return task;
	}

	@Test
	public void testTaskUpdateAllowListValid() throws Exception
	{
		Task task = createValidTaskUpdateAllowList();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskUpdateAllowlistValidWithOutput() throws Exception
	{
		Task task = createValidTaskUpdateAllowList();
		task.addOutput().setValue(new Reference(new IdType("Bundle", UUID.randomUUID().toString(), "1"))).getType()
				.addCoding().setSystem("http://highmed.org/fhir/CodeSystem/update-allow-list")
				.setCode("highmed_allow_list");

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskUpdateAllowList()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-update-allow-list");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/updateAllowList/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("updateAllowListMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");

		return task;
	}

	@Test
	public void testTaskRequestUpdateResourcesAllowListValid() throws Exception
	{
		Task task = createValidTaskRequestUpdateAllowListResources();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestUpdateAllowListResources()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-update-allow-list");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/updateAllowList/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("updateAllowListMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");

		return task;
	}

	@Test
	public void testTaskRequestUpdateResourcesValid() throws Exception
	{
		Task task = createValidTaskRequestUpdateResources();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestUpdateResources()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-request-update-resources");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestUpdateResources/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("requestUpdateResourcesMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new Reference("Bundle/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/update-resources").setCode("bundle-reference");
		task.addInput().setValue(new StringType("http://highmed.org/fhir/NamingSystem/organization-identifier|"))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/update-resources")
				.setCode("organization-identifier-search-parameter");

		return task;
	}

	@Test
	public void testTaskExecuteUpdateResourcesValid() throws Exception
	{
		Task task = createValidTaskExecuteUpdateResources();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskExecuteUpdateResources()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-execute-update-resources");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/executeUpdateResources/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("executeUpdateResourcesMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new Reference("Bundle/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/update-resources").setCode("bundle-reference");

		return task;
	}

	@Test
	public void testTaskRequestSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskRequestSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskRequestSimpleFeasibilityValidWithOutput() throws Exception
	{
		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		Task task = createValidTaskRequestSimpleFeasibility();

		TaskOutputComponent outParticipatingMedics1 = task.addOutput();
		outParticipatingMedics1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("participating-medics");
		outParticipatingMedics1
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId1));
		TaskOutputComponent outMultiMedicResult1 = task.addOutput();
		outMultiMedicResult1.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("multi-medic-result");
		outMultiMedicResult1
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId1));

		TaskOutputComponent outParticipatingMedics2 = task.addOutput();
		outParticipatingMedics2.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("participating-medics");
		outParticipatingMedics2
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId2));
		TaskOutputComponent outMultiMedicResult2 = task.addOutput();
		outMultiMedicResult2.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("multi-medic-result");
		outMultiMedicResult2
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId2));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-request-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestSimpleFeasibility/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("requestSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new Reference("ResearchStudy/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("research-study-reference");
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-consent-check");

		return task;
	}

	@Test
	public void testTaskExecuteSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskExecuteSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskExecuteSimpleFeasibilityValidWithBloomFilterConfig() throws Exception
	{
		Task task = createValidTaskExecuteSimpleFeasibility();
		task.addInput().setValue(new Base64BinaryType("TEST".getBytes(StandardCharsets.UTF_8))).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("bloom-filter-configuration");

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskExecuteSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-execute-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/executeSimpleFeasibility/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_2");

		task.addInput().setValue(new StringType("executeSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("correlation-key");

		task.addInput().setValue(new Reference("ResearchStudy/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("research-study-reference");
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-consent-check");

		return task;
	}

	@Test
	public void testTaskSingleMedicResultSimpleFeasibilityUnsignedIntResultValid() throws Exception
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibilityUnsignedIntResult();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskSingleMedicResultSimpleFeasibilityReferenceResultValid() throws Exception
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibilityReferenceResult();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskSingleMedicResultSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(
				"http://highmed.org/fhir/StructureDefinition/highmed-task-single-medic-result-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/computeSimpleFeasibility/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_2");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("resultSingleMedicSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("correlation-key");

		return task;
	}

	private Task createValidTaskSingleMedicResultSimpleFeasibilityUnsignedIntResult()
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibility();

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inSingleMedicResult1 = task.addInput();
		inSingleMedicResult1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("single-medic-result");
		inSingleMedicResult1
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId1));
		ParameterComponent inSingleMedicResult2 = task.addInput();
		inSingleMedicResult2.setValue(new UnsignedIntType(10)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("single-medic-result");
		inSingleMedicResult2
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId2));

		return task;
	}

	private Task createValidTaskSingleMedicResultSimpleFeasibilityReferenceResult()
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibility();

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inSingleMedicResult1 = task.addInput();
		inSingleMedicResult1.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("single-medic-result-reference");
		inSingleMedicResult1
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId1));
		ParameterComponent inSingleMedicResult2 = task.addInput();
		inSingleMedicResult2.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("single-medic-result-reference");
		inSingleMedicResult2
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskComputeSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskComputeSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskComputeSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-compute-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/computeSimpleFeasibility/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("computeSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");

		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("medic-correlation-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("medic-correlation-key");

		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");

		return task;
	}

	@Test
	public void testTaskMultiMedicResultSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskMultiMedicResultSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskMultiMedicResultSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(
				"http://highmed.org/fhir/StructureDefinition/highmed-task-multi-medic-result-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestSimpleFeasibility/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("resultMultiMedicSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inParticipatingMedics1 = task.addInput();
		inParticipatingMedics1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("participating-medics");
		inParticipatingMedics1
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId1));
		ParameterComponent inMultiMedicResult1 = task.addInput();
		inMultiMedicResult1.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("multi-medic-result");
		inMultiMedicResult1
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId1));

		ParameterComponent inParticipatingMedics2 = task.addInput();
		inParticipatingMedics2.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("participating-medics");
		inParticipatingMedics2
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId2));
		ParameterComponent inMultiMedicResult2 = task.addInput();
		inMultiMedicResult2.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("multi-medic-result");
		inMultiMedicResult2
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskErrorSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskErrorSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskErrorSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-error-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestSimpleFeasibility/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("errorMultiMedicSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");

		ParameterComponent error = task.addInput();
		error.setValue(new StringType(
				"An error occurred while calculating the multi medic feasibility result for all defined cohorts"))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("error");

		return task;
	}

	@Test
	public void testTaskLocalServiceIntegrationValid() throws Exception
	{
		Task task = createValidTaskLocalServiceIntegration();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskLocalServiceIntegration()
	{
		Task task = new Task();

		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-local-services-integration");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/localServicesIntegration/0.3.0");
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setIntent(Task.TaskIntent.ORDER);
		task.setAuthoredOn(new Date());

		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("localServicesIntegrationMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");

		task.addInput().setValue(new StringType("SELECT COUNT(e) FROM EHR e;")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/query-type").setCode("application/x-aql-query");
		task.addInput().setValue(new BooleanType(true)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-consent-check");
		task.addInput().setValue(new BooleanType(true)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");

		byte[] bloomFilterConfig = Base64.getDecoder()
				.decode("CIw/x19d3Oj+GLOKgYAX5KrFAl11q6qMi0qkDiyUOCvMXuF2KffVvSnjUjkTvqh4z8Xs+MuQdK6FqTedM5FY9t4qm+k92A+P");

		task.addInput().setValue(new Base64BinaryType(bloomFilterConfig)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("bloom-filter-configuration");

		return task;
	}

	@Test
	public void testTaskDownloadAllowListValid() throws Exception
	{
		Task task = createValidTaskDownloadAllowList();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskDownloadAllowList()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-download-allow-list");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/downloadAllowList/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("downloadAllowListMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput()
				.setValue(
						new Reference(new IdType("https://foo.bar/fhir", "Bundle", UUID.randomUUID().toString(), "1")))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/update-allow-list")
				.setCode("highmed_allow_list");

		return task;
	}
}

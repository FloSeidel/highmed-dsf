package org.highmed.dsf.bpe.spring.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendMedicRequest;
import org.highmed.dsf.bpe.message.SendMultiMedicErrors;
import org.highmed.dsf.bpe.message.SendMultiMedicResults;
import org.highmed.dsf.bpe.message.SendSingleMedicResults;
import org.highmed.dsf.bpe.message.SendTtpRequest;
import org.highmed.dsf.bpe.plugin.FeasibilityPlugin;
import org.highmed.dsf.bpe.service.CalculateMultiMedicResults;
import org.highmed.dsf.bpe.service.CheckFeasibilityResources;
import org.highmed.dsf.bpe.service.CheckMultiMedicResults;
import org.highmed.dsf.bpe.service.CheckQueries;
import org.highmed.dsf.bpe.service.CheckSingleMedicResults;
import org.highmed.dsf.bpe.service.CheckTtpComputedMultiMedicResults;
import org.highmed.dsf.bpe.service.DownloadFeasibilityResources;
import org.highmed.dsf.bpe.service.DownloadResearchStudyResource;
import org.highmed.dsf.bpe.service.DownloadResultSets;
import org.highmed.dsf.bpe.service.ExecuteQueries;
import org.highmed.dsf.bpe.service.ExecuteRecordLink;
import org.highmed.dsf.bpe.service.FilterQueryResultsByConsent;
import org.highmed.dsf.bpe.service.GenerateBloomFilters;
import org.highmed.dsf.bpe.service.GenerateCountFromIds;
import org.highmed.dsf.bpe.service.HandleErrorMultiMedicResults;
import org.highmed.dsf.bpe.service.ModifyQueries;
import org.highmed.dsf.bpe.service.SelectRequestTargets;
import org.highmed.dsf.bpe.service.SelectResponseTargetMedic;
import org.highmed.dsf.bpe.service.SelectResponseTargetTtp;
import org.highmed.dsf.bpe.service.StoreCorrelationKeys;
import org.highmed.dsf.bpe.service.StoreResults;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.MasterPatientIndexClientFactory;
import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.client.OpenEhrClientFactory;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicRbfOnly;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicRbfOnlyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class FeasibilityConfig
{
	@Autowired
	private FhirWebserviceClientProvider fhirClientProvider;

	@Autowired
	private MasterPatientIndexClientFactory masterPatientIndexClientFactory;

	@Autowired
	private OpenEhrClientFactory openEhrClientFactory;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private GroupHelper groupHelper;

	@Autowired
	private FhirContext fhirContext;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private Environment environment;

	@Value("${org.highmed.dsf.bpe.openehr.subject_external_id.path:/ehr_status/subject/external_ref/id/value}")
	private String ehrIdColumnPath;

	@Bean
	public ProcessEnginePlugin feasibilityPlugin()
	{
		return new FeasibilityPlugin();
	}

	//
	// process requestSimpleFeasibility implementations
	//

	@Bean
	public DownloadResearchStudyResource downloadResearchStudyResource()
	{
		return new DownloadResearchStudyResource(organizationProvider, fhirClientProvider, taskHelper);
	}

	@Bean
	public SelectRequestTargets selectRequestTargets()
	{
		return new SelectRequestTargets(fhirClientProvider, taskHelper, organizationProvider, bouncyCastleProvider());
	}

	@Bean
	public SendTtpRequest sendTtpRequest()
	{
		return new SendTtpRequest(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SendMedicRequest sendMedicRequest()
	{
		return new SendMedicRequest(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public CheckMultiMedicResults checkMultiMedicResults()
	{
		return new CheckMultiMedicResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public HandleErrorMultiMedicResults handleErrorMultiMedicResults()
	{
		return new HandleErrorMultiMedicResults(fhirClientProvider, taskHelper);
	}

	//
	// process executeSimpleFeasibility implementations
	//

	@Bean
	public DownloadFeasibilityResources downloadFeasibilityResources()
	{
		return new DownloadFeasibilityResources(organizationProvider, fhirClientProvider, taskHelper);
	}

	@Bean
	public CheckFeasibilityResources checkFeasibilityResources()
	{
		return new CheckFeasibilityResources(fhirClientProvider, taskHelper);
	}

	@Bean
	public CheckQueries checkQueries()
	{
		return new CheckQueries(fhirClientProvider, taskHelper, groupHelper);
	}

	@Bean
	public ModifyQueries modifyQueries()
	{
		return new ModifyQueries(fhirClientProvider, taskHelper, ehrIdColumnPath);
	}

	@Bean
	public ExecuteQueries executeQueries()
	{
		return new ExecuteQueries(fhirClientProvider, openEhrClient(), taskHelper, organizationProvider);
	}

	@Bean
	public FilterQueryResultsByConsent filterQueryResultsByConsent()
	{
		return new FilterQueryResultsByConsent(fhirClientProvider, taskHelper);
	}

	@Bean
	public GenerateCountFromIds generateCountFromIds()
	{
		return new GenerateCountFromIds(fhirClientProvider, taskHelper);
	}

	@Bean
	public MasterPatientIndexClient masterPatientIndexClient()
	{
		return masterPatientIndexClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public OpenEhrClient openEhrClient()
	{
		return openEhrClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public GenerateBloomFilters generateBloomFilters()
	{
		return new GenerateBloomFilters(fhirClientProvider, taskHelper, ehrIdColumnPath, masterPatientIndexClient(),
				objectMapper, bouncyCastleProvider());
	}

	@Bean
	public BouncyCastleProvider bouncyCastleProvider()
	{
		return new BouncyCastleProvider();
	}

	@Bean
	public CheckSingleMedicResults checkSingleMedicResults()
	{
		return new CheckSingleMedicResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public SelectResponseTargetTtp selectResponseTargetTtp()
	{
		return new SelectResponseTargetTtp(fhirClientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public SendSingleMedicResults sendSingleMedicResults()
	{
		return new SendSingleMedicResults(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}

	//
	// process computeSimpleFeasibility implementations
	//

	@Bean
	public StoreCorrelationKeys storeCorrelationKeys()
	{
		return new StoreCorrelationKeys(fhirClientProvider, taskHelper);
	}

	@Bean
	public StoreResults storeResults()
	{
		return new StoreResults(fhirClientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public DownloadResultSets downloadResultSets()
	{
		return new DownloadResultSets(fhirClientProvider, taskHelper, objectMapper);
	}

	@Bean
	public ResultSetTranslatorFromMedicRbfOnly resultSetTranslatorFromMedicRbfOnly()
	{
		return new ResultSetTranslatorFromMedicRbfOnlyImpl();
	}

	@Bean
	public ExecuteRecordLink executeRecordLink()
	{
		return new ExecuteRecordLink(fhirClientProvider, taskHelper, resultSetTranslatorFromMedicRbfOnly());
	}

	@Bean
	public CalculateMultiMedicResults calculateMultiMedicResults()
	{
		return new CalculateMultiMedicResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public CheckTtpComputedMultiMedicResults checkTtpComputedMultiMedicResults()
	{
		return new CheckTtpComputedMultiMedicResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public SelectResponseTargetMedic selectResponseTargetMedic()
	{
		return new SelectResponseTargetMedic(fhirClientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public SendMultiMedicResults sendMultiMedicResults()
	{
		return new SendMultiMedicResults(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SendMultiMedicErrors sendMultiMedicErrors()
	{
		return new SendMultiMedicErrors(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}
}

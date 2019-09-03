package org.highmed.dsf.bpe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.bpe.variables.MultiInstanceResults;
import org.highmed.dsf.bpe.variables.SimpleCohortSizeResult;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class CalculateMultiMedicCohortSizeResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CalculateMultiMedicCohortSizeResults.class);

	public CalculateMultiMedicCohortSizeResults(WebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		// TODO: distinguish between simple and complex query
		// TODO: add percentage filter over result

		MultiInstanceResults resultsWrapper = (MultiInstanceResults) execution.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS);
		List<Group> cohortDefinitions = (List<Group>) execution.getVariable(Constants.VARIABLE_COHORTS);

		List<String> cohortIds = getCohortIds(cohortDefinitions);
		List<MultiInstanceResult> locationBasedResults = resultsWrapper.getResults();
		List<SimpleCohortSizeResult> finalResult = calculateResults(cohortIds, locationBasedResults);

		execution.setVariable(Constants.VARIABLE_SIMPLE_COHORT_SIZE_QUERY_FINAL_RESULT, finalResult);
	}

	private List<SimpleCohortSizeResult> calculateResults(List<String> ids, List<MultiInstanceResult> results)
	{
		List<Map.Entry<String, String>> combinedResults = results.stream().flatMap(result -> result.getQueryResults().entrySet().stream()).collect(Collectors.toList());

		List<SimpleCohortSizeResult> resultsByCohortId = new ArrayList<>();

		for(String id : ids) {
			long participatingMedics = combinedResults.stream().filter(resultEntry -> resultEntry.getKey().equals(id)).count();
			long result = combinedResults.stream().filter(resultEntry -> resultEntry.getKey().equals(id)).mapToInt(resultEntry -> Integer.parseInt(resultEntry.getValue())).sum();
			resultsByCohortId.add(new SimpleCohortSizeResult(id, participatingMedics, result));
		}

		return resultsByCohortId;
	}

	private List<String> getCohortIds(List<Group> cohortDefinitions)
	{
		return cohortDefinitions.stream().map(cohort -> {
			IdType cohortId = new IdType(cohort.getId());
			return cohortId.getResourceType() + "/" + cohortId.getIdPart();
		}).collect(Collectors.toList());
	}
}
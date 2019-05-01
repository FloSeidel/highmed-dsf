package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.TaskDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;

public class TaskServiceImpl extends AbstractServiceImpl<TaskDao, Task> implements TaskService
{
	public TaskServiceImpl(String resourceTypeName, String serverBase, String path, int defaultPageCount, TaskDao dao,
			ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter)
	{
		super(Task.class, resourceTypeName, serverBase, path, defaultPageCount, dao, validator, eventManager,
				exceptionHandler, eventGenerator, responseGenerator, parameterConverter);
	}
}

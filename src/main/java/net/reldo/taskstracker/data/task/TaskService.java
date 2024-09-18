package net.reldo.taskstracker.data.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.ManifestClient;
import net.reldo.taskstracker.data.jsondatastore.TaskDataClient;
import net.reldo.taskstracker.data.jsondatastore.types.TaskFromStruct;
import net.reldo.taskstracker.data.jsondatastore.types.TaskV2;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskTypeDefinition;

@Singleton
@Slf4j
public class TaskService
{
	@Inject
	private ManifestClient manifestClient;
	@Inject
	private TaskDataClient taskDataClient;

	private List<TaskV2> tasks = new ArrayList<>();
	private HashMap<String, TaskTypeDefinition> _taskTypes = new HashMap<>();

	public List<TaskV2> getTasks()

	{
		return this.tasks;
	}

	public void setTaskType(TaskTypeDefinition taskType)
	{
		try
		{
			List<TaskDefinition> taskDefinitions = this.taskDataClient.getTasks(taskType.getTaskJsonName());
			this.tasks = taskDefinitions.stream()
				.map(definition -> new TaskFromStruct(taskType, definition))
				.collect(Collectors.toList());

		}
		catch (Exception ex)
		{
			log.error("Unable to set task type");
		}
	}

	/**
	 * Get a map of task type json names to task definition
	 *
	 * @return Hashmap of TaskTypeDefinition indexed by task type json name
	 */
	public HashMap<String, TaskTypeDefinition> getTaskTypes()
	{
		if (this._taskTypes.size() > 0)
		{
			return this._taskTypes;
		}

		try
		{
			this._taskTypes = this.taskDataClient.getTaskTypes();
			return this._taskTypes;
		}
		catch (Exception ex)
		{
			log.error("Unable to populate task types from data client", ex);
			return new HashMap<>();
		}
	}
}

package net.reldo.taskstracker.data.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.ManifestClient;
import net.reldo.taskstracker.data.jsondatastore.TaskDataClient;
import net.reldo.taskstracker.data.jsondatastore.types.TaskFromStruct;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskTypeDefinition;

@Singleton
@Slf4j
public class TaskService
{
	@Inject
	private ManifestClient manifestClient;
	@Inject
	private TaskDataClient taskDataClient;

	@Getter
	private TaskTypeDefinition currentTaskType;
	@Getter
	private HashSet<Integer> currentTaskTypeVarps = new HashSet<>();
	@Getter
	private HashMap<Integer, List<TaskFromStruct>> currentTasksByVarp = new HashMap<>();
	@Getter
	private List<TaskFromStruct> tasks = new ArrayList<>();

	private HashMap<String, TaskTypeDefinition> _taskTypes = new HashMap<>();

	public void setTaskType(TaskTypeDefinition taskType)
	{
		try
		{
			this.currentTaskType = taskType;

			this.tasks = this.taskDataClient.getTasks(taskType.getTaskJsonName()).stream()
				.map(definition -> new TaskFromStruct(taskType, definition))
				.collect(Collectors.toList());

			// TODO: Simplify
			this.currentTaskTypeVarps = IntStream.of(taskType.getTaskVarps())
				.boxed()
				.collect(Collectors.toCollection(HashSet::new));

			this.currentTasksByVarp = this.tasks.stream()
				.collect(Collectors.groupingBy(
					TaskFromStruct::getTaskVarp,
					HashMap::new,
					Collectors.toList()
				));
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

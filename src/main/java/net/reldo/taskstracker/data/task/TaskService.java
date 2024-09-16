package net.reldo.taskstracker.data.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.ManifestClient;
import net.reldo.taskstracker.data.jsondatastore.TaskDataClient;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;

@Singleton
@Slf4j
public class TaskService
{
	@Inject private ManifestClient manifestClient;
	@Inject	private TaskDataClient taskDataClient;

	@Getter	private List<TaskV2> tasks = new ArrayList<>();
	private HashMap<String, TaskTypeDefinition> _taskTypes = new HashMap<>();

	public TaskService()
	{
	}

	public void setTaskType(String taskSlug)
	{
		try
		{
			TaskTypeDefinition taskType = this.getTaskTypes().get(taskSlug);
			if (taskType == null)
			{
				throw new Exception("Invalid task slug " + taskSlug);
			}
			this.tasks = this.taskDataClient.getTasks(taskType.jsonFilename);
		} catch (Exception ex) {
			log.error("Unable to set task type");
		}
	}

	public HashMap<String, TaskTypeDefinition> getTaskTypes() throws Exception
	{
		if (this._taskTypes.size() > 0)
		{
			return this._taskTypes;
		}

		try
		{
			this._taskTypes = this.taskDataClient.getTaskTypes();
			return this._taskTypes;
		} catch (Exception ex) {
			log.error("Unable to populate task types from data client", ex);
			throw ex;
		}
	}
}

package net.reldo.taskstracker.data.task;

import com.google.gson.annotations.Expose;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
import net.reldo.taskstracker.data.reldo.ReldoTaskSave;
import net.runelite.api.Client;
import net.runelite.api.StructComposition;

@Slf4j
public class TaskFromStruct
{
	@Getter
	private final Integer structId;
	@Getter
	private final Integer sortId;
	@Getter
	private final TaskTypeDefinition taskTypeDefinition;
	@Getter
	private TaskType taskType;
	@Getter
	private final TaskDefinition taskDefinition;
	@Getter
	private boolean structLoaded;
	@Expose @Getter @Setter
	private long completedOn;
	@Expose @Getter @Setter
	private long trackedOn;
	@Expose @Getter @Setter
	private long ignoredOn;

	private StructComposition _struct;
	private final Map<String, String> _stringParams = new HashMap<>();
	private final Map<String, Integer> _intParams = new HashMap<>();

	public TaskFromStruct(TaskTypeDefinition taskTypeDefinition, TaskType taskType, TaskDefinition taskDefinition)
	{
		this.taskTypeDefinition = taskTypeDefinition;
		this.taskType = taskType;
		this.taskDefinition = taskDefinition;
		this.structId = taskDefinition.getStructId();
		this.sortId = taskDefinition.getSortId();
	}

	public String getStringParam(String paramName)
	{
		return this._stringParams.get(paramName);
	}

	public Integer getIntParam(String paramName)
	{
		return this._intParams.get(paramName);
	}

	public int getTaskTypeVarpIndex()
	{
		return this.getSortId() / 32;
	}

	public int getTaskVarp()
	{
		int index = getTaskTypeVarpIndex();
		return taskTypeDefinition.getTaskVarps().get(index);
	}

	// TODO: Remove client from params
	public boolean loadStructData(Client client)
	{
		assert client.isClientThread();

		if (structLoaded) {
			return true;
		}
		try
		{
			log.debug("LOADING STRUCT DATA " + structId);
			_struct = client.getStructComposition(structId);
			taskTypeDefinition.getIntParamMap().forEach((paramName, paramId) -> {
				int value = _struct.getIntValue(paramId);
				log.debug("{} {}", paramName, value);
				_intParams.put(paramName, value);
			});
			taskTypeDefinition.getStringParamMap().forEach((paramName, paramId) -> {
				String value = _struct.getStringValue(paramId);
				log.debug("{} {}", paramName, value);
				_stringParams.put(paramName, value);
			});
		}
		catch (Exception ex)
		{
			log.error("error loading struct data {}", ex, ex);
			return false;
		}

		structLoaded = true;
		return true;
	}

	public boolean isCompleted()
	{
		return completedOn > 0;
	}

	public int getPoints()
	{
		return 0;
	}

	public void setCompleted(boolean completed)
	{
		long now = Instant.now().toEpochMilli();
		if (completed && completedOn > 0 && completedOn <= now)
		{
			return;
		}
		completedOn = completed ? now : 0;
	}

	public boolean isTracked()
	{
		return trackedOn > 0;
	}

	public void setTracked(boolean state)
	{
		long now = Instant.now().toEpochMilli();
		if (state && trackedOn > 0 && trackedOn <= now)
		{
			return;
		}
		trackedOn = state ? now : 0;
	}

	public boolean isIgnored()
	{
		return ignoredOn > 0;
	}

	public void setIgnored(boolean state)
	{
		long now = Instant.now().toEpochMilli();
		if (state && ignoredOn > 0 && ignoredOn <= now)
		{
			return;
		}
		ignoredOn = state ? now : 0;
	}

	// TODO: reimplement
	public void loadConfigSave(TaskFromStruct loadedData)
	{
//		setDates(loadedData.getCompletedOn(), loadedData.getIgnoredOn(), loadedData.getTrackedOn());
	}

	public void loadReldoSave(ReldoTaskSave loadedData)
	{
		setMostRecentDates(loadedData.getCompleted(), loadedData.getIgnored(), loadedData.getTodo());
	}

	private void setDates(long completedOn, long ignoredOn, long trackedOn)
	{
		// Set all dates regardless of how they compare
		this.setCompletedOn(completedOn);
		this.setIgnoredOn(ignoredOn);
		this.setTrackedOn(trackedOn);
	}

	private void setMostRecentDates(long completedOn, long ignoredOn, long trackedOn)
	{
		// Older completions take priority; incomplete (0) also takes priority
		if (completedOn < this.getCompletedOn())
		{
			this.setCompletedOn(completedOn);
		}
		// Newer ignores take priority
		if (ignoredOn > this.getIgnoredOn())
		{
			this.setIgnoredOn(ignoredOn);
		}
		// Newer tracks take priority
		if (trackedOn > this.getTrackedOn())
		{
			this.setTrackedOn(trackedOn);
		}
	}

	public String getName()
	{
		return getStringParam("name");
	}

	public int getTier()
	{
		return getIntParam("tier");
	}

	public String getDescription()
	{
		return getStringParam("description");
	}
}
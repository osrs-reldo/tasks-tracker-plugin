package net.reldo.taskstracker.data.task;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinition;
import net.reldo.taskstracker.data.reldo.ReldoTaskSave;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

@Slf4j
public class TaskFromDbRow implements ITask
{
	@Getter
	private final Integer dbRowId;
	@Getter
	private final Integer sortId;
	@Getter
	private final int varpIndex;
	@Getter
	private final ITaskType taskType;
	@Getter
	private final TaskDefinition taskDefinition;
	@Getter
	private boolean dbRowLoaded;
	@Getter
	@Setter
	private long completedOn;
	@Getter
	@Setter
	private long trackedOn;
	@Getter
	@Setter
	private long ignoredOn;
	@Getter
	@Setter
	private String note;

	private final Map<String, String> _stringParams = new HashMap<>();
	private final Map<String, Integer> _intParams = new HashMap<>();
	@Getter
	@Setter
	@Nullable
	private WorldPoint completionLocation;

	public TaskFromDbRow(ITaskType taskType, TaskDefinition taskDefinition)
	{
		this.taskType = taskType;
		this.taskDefinition = taskDefinition;
		this.dbRowId = taskDefinition.getDbRowId();
		this.sortId = taskDefinition.getSortId();
		// For DBROW tasks, the sortId in the TaskDefinition IS the varp bit index (pre-scraped flat index)
		// Will need to verify for leagues 6+ when we have actual data, but this is how the cs2 script suggests it will work - consistent with the gridmaster tasks which also use sortId as the varp bit index
		this.varpIndex = taskDefinition.getSortId();
	}

	public int getTaskId()
	{
		return dbRowId;
	}

	public String getName()
	{
		return getStringParam("name");
	}

	public String getDescription()
	{
		return getStringParam("description");
	}

	public boolean isCompleted()
	{
		return completedOn > 0;
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

	public int getPoints()
	{
		if (taskType.getTierPoints().size() == 0)
		{
			return 0;
		}
		Integer points = taskType.getTierPoints().get(getTier());
		if (points == null)
		{
			return 0;
		}
		return points;
	}

	public int getTier()
	{
		Integer tier = getIntParam("tier");
		return tier != null ? tier : 0;
	}

	public boolean hasNote()
	{
		return note != null && !note.isEmpty();
	}

	public Integer getIntParam(String paramName)
	{
		return _intParams.get(paramName);
	}

	public String getStringParam(String paramName)
	{
		return _stringParams.get(paramName);
	}

	public ConfigTaskSave getSaveData()
	{
		return new ConfigTaskSave(this);
	}

	@Nullable
	public Float getCompletionPercent()
	{
		return getTaskDefinition().getCompletionPercent();
	}

	public void loadConfigSave(ConfigTaskSave loadedData)
	{
		setCompletedOn(loadedData.completed);
		setIgnoredOn(loadedData.ignored);
		setTrackedOn(loadedData.tracked);
		setNote(loadedData.note);
		if (loadedData.completionLocation != null)
		{
			setCompletionLocation(loadedData.completionLocation.toWorldPoint());
		}
	}

	public void loadReldoSave(ReldoTaskSave loadedData)
	{
		// Older completions take priority; incomplete (0) also takes priority
		if (loadedData.getCompleted() < this.getCompletedOn())
		{
			this.setCompletedOn(loadedData.getCompleted());
		}
		if (loadedData.getIgnored() > this.getIgnoredOn())
		{
			this.setIgnoredOn(loadedData.getIgnored());
		}
		if (loadedData.getTodo() > this.getTrackedOn())
		{
			this.setTrackedOn(loadedData.getTodo());
		}
		setNote(loadedData.getNotes());
	}

	/**
	 * Loads column data from the action DBRow via client.getDBTableField.
	 * Uses intParamMap/stringParamMap from the ITaskType to source column IDs for DBROW types.
	 */
	public boolean loadData(Client client)
	{
		assert client.isClientThread();

		if (dbRowLoaded)
		{
			return true;
		}

		HashMap<String, Integer> intParamMap = taskType.getIntParamMap();
		HashMap<String, Integer> stringParamMap = taskType.getStringParamMap();

		if (intParamMap != null)
		{
			intParamMap.forEach((paramName, column) -> {
				try
				{
					Object[] result = client.getDBTableField(dbRowId, column, 0);
					if (result != null && result.length > 0 && result[0] instanceof Integer)
					{
						_intParams.put(paramName, (Integer) result[0]);
					}
				}
				catch (Exception ex)
				{
					// Sparse columns throw when not populated for a given row; skip silently.
				}
			});
		}

		if (stringParamMap != null)
		{
			stringParamMap.forEach((paramName, column) -> {
				try
				{
					Object[] result = client.getDBTableField(dbRowId, column, 0);
					if (result != null && result.length > 0 && result[0] instanceof String)
					{
						_stringParams.put(paramName, (String) result[0]);
					}
				}
				catch (Exception ex)
				{
					// Sparse columns throw when not populated for a given row; skip silently.
				}
			});
		}

		dbRowLoaded = true;
		return true;
	}
}

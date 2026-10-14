package net.reldo.taskstracker.data.task;

import javax.annotation.Nullable;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinition;
import net.reldo.taskstracker.data.reldo.ReldoTaskSave;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

public interface ITask
{
	/** Stable save key: structId for STRUCT tasks, action dbrow ID for DBROW tasks */
	int getTaskId();

	/** Bit position within taskVarps for completion check */
	int getVarpIndex();

	String getName();

	String getDescription();

	boolean isCompleted();

	void setCompleted(boolean completed);

	long getCompletedOn();

	void setCompletedOn(long completedOn);

	long getTrackedOn();

	void setTrackedOn(long trackedOn);

	long getIgnoredOn();

	void setIgnoredOn(long ignoredOn);

	boolean isTracked();

	void setTracked(boolean state);

	boolean isIgnored();

	void setIgnored(boolean state);

	int getPoints();

	int getTier();

	@Nullable
	String getNote();

	void setNote(String note);

	boolean hasNote();

	Integer getIntParam(String paramName);

	String getStringParam(String paramName);

	ConfigTaskSave getSaveData();

	TaskDefinition getTaskDefinition();

	ITaskType getTaskType();

	Integer getSortId();

	@Nullable
	Float getCompletionPercent();

	void loadConfigSave(ConfigTaskSave loadedData);

	void loadReldoSave(ReldoTaskSave loadedData);

	@Nullable
	WorldPoint getCompletionLocation();

	void setCompletionLocation(@Nullable WorldPoint location);

	/** Loads task data from the game cache (struct or dbrow). Must be called on client thread. */
	boolean loadData(Client client);
}

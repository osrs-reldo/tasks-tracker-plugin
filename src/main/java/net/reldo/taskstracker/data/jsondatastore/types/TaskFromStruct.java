package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskTypeDefinition;
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
	private final TaskDefinition taskDefinition;

	private StructComposition _struct;
	private final Map<String, String> _stringParams = new HashMap<>();
	private final Map<String, Integer> _intParams = new HashMap<>();

	public TaskFromStruct(TaskTypeDefinition taskTypeDefinition, TaskDefinition taskDefinition)
	{
		this.taskTypeDefinition = taskTypeDefinition;
		this.taskDefinition = taskDefinition;
		this.structId = taskDefinition.getStructId();
		this.sortId = taskDefinition.getSortId();
	}

	public String getString(String paramName)
	{
		return this._stringParams.get(paramName);
	}

	public int getInt(String paramName)
	{
		return this._intParams.get(paramName);
	}

	public int getTaskVarpPosition()
	{
		return this.getSortId() / 32;
	}

	public int getTaskVarp()
	{
		int ordinal = getTaskVarpPosition();
		return taskTypeDefinition.getTaskVarps()[ordinal];
	}

	// TODO: Remove client from params
	public boolean loadStructData(Client client)
	{
		assert client.isClientThread();

		try
		{
			_struct = client.getStructComposition(structId);
			taskTypeDefinition.getIntParamMap().forEach((paramName, paramId) -> {
				int value = _struct.getIntValue(paramId);
				_intParams.put(paramName, value);
			});
			taskTypeDefinition.getStringParamMap().forEach((paramName, paramId) -> {
				String value = _struct.getStringValue(paramId);
				_stringParams.put(paramName, value);
			});
		}
		catch (Exception ex)
		{
			log.error("error loading struct data {}", ex, ex);
			throw ex;
		}

		return true;
	}
}

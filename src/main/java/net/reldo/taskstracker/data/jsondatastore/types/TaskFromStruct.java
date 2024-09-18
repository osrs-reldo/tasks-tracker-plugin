package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.Getter;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskTypeDefinition;
import net.runelite.api.Client;
import net.runelite.api.StructComposition;
import net.runelite.client.callback.ClientThread;

public class TaskFromStruct implements TaskV2
{
	private StructComposition _struct;

	@Getter
	private final Integer structId;
	@Getter
	private final Integer sortId;
	@Getter
	private final TaskTypeDefinition taskTypeDefinition;
	@Getter
	private final TaskDefinition taskDefinition;

	private Integer _id;
	private String _name;
	private String _description;
	private final Map<String, String> _stringParams = new HashMap<>();
	private final Map<String, Integer> _intParams = new HashMap<>();

	public TaskFromStruct(TaskTypeDefinition taskTypeDefinition, TaskDefinition taskDefinition)
	{
		this.taskTypeDefinition = taskTypeDefinition;
		this.taskDefinition = taskDefinition;
		this.structId = taskDefinition.getStructId();
		this.sortId = taskDefinition.getSortId();
	}

	public Integer getId(Client client, ClientThread clientThread)
	{
		if (this._id != null)
		{
			return this._id;
		}

		StructComposition struct = this.getStruct(client, clientThread);
		int paramId = this.taskTypeDefinition.getParamMap().get("id");
		this._id = struct.getIntValue(paramId);
		return this._id;
	}

	public String getName(Client client, ClientThread clientThread)
	{
		if (this._name != null)
		{
			return this._name;
		}

		StructComposition struct = this.getStruct(client, clientThread);
		int paramId = this.taskTypeDefinition.getParamMap().get("name");
		this._name = struct.getStringValue(paramId);
		return this._name;
	}

	public String getDescription(Client client, ClientThread clientThread)
	{
		if (this._description != null)
		{
			return this._description;
		}

		StructComposition struct = this.getStruct(client, clientThread);
		int paramId = this.taskTypeDefinition.getParamMap().get("description");
		this._description = struct.getStringValue(paramId);
		return this._description;
	}

	public String getStringParam(Client client, ClientThread clientThread, String paramName)
	{
		String paramValue = this._stringParams.get(paramName);
		if (paramValue != null)
		{
			return paramValue;
		}

		StructComposition struct = this.getStruct(client, clientThread);
		int paramId = this.taskTypeDefinition.getParamMap().get(paramName);
		paramValue = struct.getStringValue(paramId);
		this._stringParams.put(paramName, paramValue);
		return paramValue;
	}

	public Integer getIntParam(Client client, ClientThread clientThread, String paramName)
	{
		Integer paramValue = this._intParams.get(paramName);
		if (paramValue != null)
		{
			return paramValue;
		}

		StructComposition struct = this.getStruct(client, clientThread);
		int paramId = this.taskTypeDefinition.getParamMap().get(paramName);
		paramValue = struct.getIntValue(paramId);
		this._intParams.put(paramName, paramValue);
		return paramValue;
	}

	private StructComposition getStruct(Client client, ClientThread clientThread)
	{
		if (this._struct != null)
		{
			return this._struct;
		}
		CompletableFuture<StructComposition> future = new CompletableFuture<>();
		clientThread.invoke(() -> {
			this._struct = client.getStructComposition(this.structId);
			future.complete(this._struct);
		});
		try
		{
			StructComposition struct = future.get();
			if (struct == null)
			{
				throw new Error(String.format(
					"struct %s not found for %s task",
					this.structId,
					this.taskTypeDefinition.getTaskJsonName()
				));
			}
			return future.get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
			throw new Error(String.format(
				"error while executing get struct %s from client for %s task",
				this.structId,
				this.taskTypeDefinition.getTaskJsonName()
			));
		}
	}
}

package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskTypeDefinition;
import net.runelite.api.Client;
import net.runelite.api.StructComposition;
import net.runelite.client.callback.ClientThread;

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

	public TaskFromStruct(TaskTypeDefinition taskTypeDefinition, TaskDefinition taskDefinition) {
		this.taskTypeDefinition = taskTypeDefinition;
		this.taskDefinition = taskDefinition;
		this.structId = taskDefinition.getStructId();
		this.sortId = taskDefinition.getSortId();
	}

	public int getTaskVarpPosition() {
		return this.getSortId() / 32;
	}

	public int getTaskVarp() {
		int ordinal = this.getTaskVarpPosition();
		return this.taskTypeDefinition.getTaskVarps()[ordinal];
	}

	public CompletableFuture<Integer> getIdAsync(Client client, ClientThread clientThread) {
		Integer cachedId = this._intParams.get("id");
		if (cachedId != null) {
			return CompletableFuture.completedFuture(cachedId);
		}

		int paramId = this.taskTypeDefinition.getParamMap().get("id");
		return this.getIntValueAsync(client, clientThread, paramId)
			.thenApply(id -> {
				this._intParams.put("id", id);
				return id;
			});
	}

	public CompletableFuture<String> getNameAsync(Client client, ClientThread clientThread) {
		String cachedName = this._stringParams.get("name");
		if (cachedName != null) {
			return CompletableFuture.completedFuture(cachedName);
		}

		int paramId = this.taskTypeDefinition.getParamMap().get("name");
		return this.getStringValueAsync(client, clientThread, paramId)
			.thenApply(name -> {
				this._stringParams.put("name", name);
				return name;
			});
	}

	public CompletableFuture<String> getDescriptionAsync(Client client, ClientThread clientThread) {
		String cachedDescription = this._stringParams.get("description");
		if (cachedDescription != null) {
			return CompletableFuture.completedFuture(cachedDescription);
		}

		int paramId = this.taskTypeDefinition.getParamMap().get("description");
		return this.getStringValueAsync(client, clientThread, paramId)
			.thenApply(description -> {
				this._stringParams.put("description", description);
				return description;
			});
	}

	public CompletableFuture<String> getStringParamAsync(Client client, ClientThread clientThread, String paramName) {
		String paramValue = this._stringParams.get(paramName);
		if (paramValue != null) {
			return CompletableFuture.completedFuture(paramValue);
		}

		int paramId = this.taskTypeDefinition.getParamMap().get(paramName);
		return this.getStringValueAsync(client, clientThread, paramId)
			.thenApply(value -> {
				this._stringParams.put(paramName, value);
				return value;
			});
	}

	public CompletableFuture<Integer> getIntParamAsync(Client client, ClientThread clientThread, String paramName) {
		Integer paramValue = this._intParams.get(paramName);
		if (paramValue != null) {
			return CompletableFuture.completedFuture(paramValue);
		}

		int paramId = this.taskTypeDefinition.getParamMap().get(paramName);
		return this.getIntValueAsync(client, clientThread, paramId)
			.thenApply(value -> {
				this._intParams.put(paramName, value);
				return value;
			});
	}

	private CompletableFuture<StructComposition> loadStructAsync(Client client, ClientThread clientThread) {
		CompletableFuture<StructComposition> future = new CompletableFuture<>();
		clientThread.invoke(() -> {
			this._struct = client.getStructComposition(this.structId);
			future.complete(this._struct);
		});
		return future;
	}

	private CompletableFuture<StructComposition> getStructAsync(Client client, ClientThread clientThread) {
		if (this._struct != null) {
			return CompletableFuture.completedFuture(this._struct);
		}
		return this.loadStructAsync(client, clientThread);
	}

	private CompletableFuture<Integer> getIntValueAsync(Client client, ClientThread clientThread, int paramId) {
		return CompletableFuture.supplyAsync(() -> {
			StructComposition struct = this.getStructAsync(client, clientThread).join();
			return struct.getIntValue(paramId);
		}, clientThread::invoke);
	}

	private CompletableFuture<String> getStringValueAsync(Client client, ClientThread clientThread, int paramId) {
		return CompletableFuture.supplyAsync(() -> {
			StructComposition struct = this.getStructAsync(client, clientThread).join();
			return struct.getStringValue(paramId);
		}, clientThread::invoke);
	}
}

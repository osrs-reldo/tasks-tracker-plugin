package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.concurrent.CompletableFuture;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

public interface TaskV2
{
	Integer getSortId();
	Integer getStructId();
	CompletableFuture<Integer> getIdAsync(Client client, ClientThread clientThread);
	CompletableFuture<String> getNameAsync(Client client, ClientThread clientThread);
	CompletableFuture<String> getDescriptionAsync(Client client, ClientThread clientThread);
	CompletableFuture<String> getStringParamAsync(Client client, ClientThread clientThread, String paramName);
	CompletableFuture<Integer> getIntParamAsync(Client client, ClientThread clientThread, String paramName);
}

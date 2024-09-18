package net.reldo.taskstracker.data.jsondatastore.types;

import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

public interface TaskV2
{
	Integer getId(Client client, ClientThread clientThread);
	Integer getSortId();
	Integer getStructId();
	String getName(Client client, ClientThread clientThread);
	String getDescription(Client client, ClientThread clientThread);
	String getStringParam(Client client, ClientThread clientThread, String paramName);
	Integer getIntParam(Client client, ClientThread clientThread, String paramName);
}

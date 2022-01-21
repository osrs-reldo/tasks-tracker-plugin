package net.reldo.taskstracker.quests;

import java.util.HashMap;
import net.runelite.api.Client;
import net.runelite.api.QuestState;

public class QuestData extends HashMap<String, QuestState>
{
	public QuestData(Client client)
	{
		for (QuestHelperQuest quest : QuestHelperQuest.values())
		{
			this.put(quest.getName(), quest.getState(client));
		}
	}
}

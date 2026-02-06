package net.reldo.taskstracker.quests;

import java.util.HashMap;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

public class QuestData extends HashMap<Integer, QuestState>
{
	public QuestData(Client client)
	{
		for (Quest quest : Quest.values()) {
			this.put(quest.getId(), quest.getState(client));
		}
	}
}

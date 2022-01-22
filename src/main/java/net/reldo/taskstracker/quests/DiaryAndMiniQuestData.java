package net.reldo.taskstracker.quests;

import java.util.HashMap;
import net.runelite.api.Client;
import net.runelite.api.QuestState;

// FIXME: Added this class so chaiin could quickly build out quest data with ids.
// TODO: Goal is to rid this entire package (sans QuestVarbits & QuestVarPlayer) and just send varbs/varps only. Consumers will contain parsing logic
public class DiaryAndMiniQuestData extends HashMap<String, QuestState>
{
	public DiaryAndMiniQuestData(Client client)
	{
		for (QuestHelperQuest quest : QuestHelperQuest.values())
		{
			if (quest.getId() == -1)
			{
				this.put(quest.getName(), quest.getState(client));
			}
		}
	}
}

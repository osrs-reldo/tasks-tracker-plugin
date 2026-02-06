package net.reldo.taskstracker.quests;

import java.util.HashMap;
import net.runelite.api.Client;

public class DiaryData extends HashMap<Integer, Integer>
{
	public DiaryData(Client client)
	{
		for (DiaryVarbits diary : DiaryVarbits.values()) {
			this.put(diary.id, diary.getProgress(client));
		}
	}
}

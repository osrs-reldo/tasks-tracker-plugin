package net.reldo.taskstracker.data.route;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Quest;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.plugins.PluginManager;

/**
 * Starts Quest Helper helpers via the "questhelper" / "start" PluginMessage
 * (see https://github.com/Zoinkwiz/quest-helper/pull/2756).
 */
@Singleton
public class QuestHelperService
{
	private static final String QUEST_HELPER_PLUGIN_NAME = "Quest Helper";
	private static final String SOURCE = "Tasks Tracker";

	// Quests whose RuneLite name doesn't match their Quest Helper helper name
	private static final Map<Quest, String> QUEST_HELPER_NAME_OVERRIDES = Map.ofEntries(
		Map.entry(Quest.RECIPE_FOR_DISASTER__ANOTHER_COOKS_QUEST, "RFD - Start"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__MOUNTAIN_DWARF, "RFD - Mountain Dwarf"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__WARTFACE__BENTNOZE, "RFD - Wartface & Bentnoze"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__PIRATE_PETE, "RFD - Pirate Pete"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__LUMBRIDGE_GUIDE, "RFD - Lumbridge Guide"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__EVIL_DAVE, "RFD - Evil Dave"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__SKRACH_UGLOGWEE, "RFD - Skrach Uglogwee"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__SIR_AMIK_VARZE, "RFD - Sir Amik Varze"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__KING_AWOWOGEI, "RFD - King Awowogei"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__CULINAROMANCER, "RFD - Finale")
	);

	@Inject
	private EventBus eventBus;
	@Inject
	private PluginManager pluginManager;

	public boolean isQuestHelperEnabled()
	{
		return pluginManager.getPlugins().stream()
			.anyMatch(p -> p.getName().equals(QUEST_HELPER_PLUGIN_NAME) && pluginManager.isPluginEnabled(p));
	}

	public void startQuestHelper(Quest quest)
	{
		Map<String, Object> data = new HashMap<>();
		data.put("quest", QUEST_HELPER_NAME_OVERRIDES.getOrDefault(quest, quest.getName()));
		data.put("source", SOURCE);
		eventBus.post(new PluginMessage("questhelper", "start", data));
	}
}

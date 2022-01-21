package net.reldo.taskstracker.data;

import java.util.EnumSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.WorldType;

@AllArgsConstructor
@Getter
public enum TrackerWorldType
{
	DEFAULT(""),
	LEAGUE("League"),
	TOURNAMENT("Tournament"),
	DEADMAN("DMM"),
	DEADMAN_TOURNAMENT("DMM Tournament");

	String displayString;

	public static TrackerWorldType forWorld(EnumSet<WorldType> worldTypes)
	{
		if (worldTypes.contains(WorldType.SEASONAL))
		{
			return worldTypes.contains(WorldType.DEADMAN) ? TrackerWorldType.DEADMAN_TOURNAMENT : TrackerWorldType.LEAGUE;
		}
		if (worldTypes.contains(WorldType.TOURNAMENT_WORLD))
		{
			return TrackerWorldType.TOURNAMENT;
		}
		if (worldTypes.contains(WorldType.DEADMAN))
		{
			return TrackerWorldType.DEADMAN;
		}
		return TrackerWorldType.DEFAULT;
	}
}

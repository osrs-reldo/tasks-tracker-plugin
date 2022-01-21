package net.reldo.taskstracker.tasktypes.league3;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum League3TaskTier
{
	BEGINNER(2316, 5, 1),
	EASY(2317, 5, 1),
	MEDIUM(2318, 25, 2),
	HARD(2319, 50, 4),
	ELITE(2320, 125, 5),
	MASTER(3739, 250, 5);

	private final int spriteId;
	private final int points;
	private final int renown;

	public static League3TaskTier getTierByName(String name)
	{
		for (League3TaskTier value : values())
		{
			if (value.name().equalsIgnoreCase(name))
			{
				return value;
			}
		}

		return null;
	}
}

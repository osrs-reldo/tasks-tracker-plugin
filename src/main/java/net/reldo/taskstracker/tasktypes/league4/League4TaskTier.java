package net.reldo.taskstracker.tasktypes.league4;

import java.util.HashMap;

public enum League4TaskTier
{
	EASY(2316, 10),
	MEDIUM(2318, 40),
	HARD(2319, 80),
	ELITE(2320, 200),
	MASTER(3739, 400);

	public final int spriteId;
	public final int points;
	public static final HashMap<String, League4TaskTier> tiersByName = new HashMap<>();

	League4TaskTier(int spriteId, int points)
	{
		this.spriteId = spriteId;
		this.points = points;
	}

	static
	{
		for (League4TaskTier tier : values())
		{
			tiersByName.put(tier.name().toLowerCase(), tier);
		}
	}
}

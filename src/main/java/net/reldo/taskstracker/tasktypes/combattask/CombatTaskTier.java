package net.reldo.taskstracker.tasktypes.combattask;

import java.util.HashMap;

public enum CombatTaskTier
{
	EASY(3399, 1),
	MEDIUM(3400, 2),
	HARD(3401, 3),
	ELITE(3402, 4),
	MASTER(3403, 5),
	GRANDMASTER(3404, 6);

	public final int spriteId;
	public final int points;
	public static final HashMap<String, CombatTaskTier> tiersByName = new HashMap<>();

	CombatTaskTier(int spriteId, int points)
	{
		this.spriteId = spriteId;
		this.points = points;
	}

	static
	{
		for (CombatTaskTier tier : values())
		{
			tiersByName.put(tier.name().toLowerCase(), tier);
		}
	}
}

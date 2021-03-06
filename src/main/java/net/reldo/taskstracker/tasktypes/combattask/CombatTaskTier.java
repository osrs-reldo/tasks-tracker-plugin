package net.reldo.taskstracker.tasktypes.combattask;

public enum CombatTaskTier
{
	EASY(3399),
	MEDIUM(3400),
	HARD(3401),
	ELITE(3402),
	MASTER(3403),
	GRANDMASTER(3404);

	public final int spriteId;

	CombatTaskTier(int spriteId)
	{
		this.spriteId = spriteId;
	}

	public static CombatTaskTier getTierByName(String name)
	{
		for (CombatTaskTier value : values())
		{
			if (value.name().equalsIgnoreCase(name))
			{
				return value;
			}
		}

		return null;
	}
}

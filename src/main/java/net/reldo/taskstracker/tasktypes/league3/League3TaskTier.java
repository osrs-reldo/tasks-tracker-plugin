package net.reldo.taskstracker.tasktypes.league3;

public enum League3TaskTier
{
	BEGINNER(2316),
	EASY(2317),
	MEDIUM(2318),
	HARD(2319),
	ELITE(2320),
	MASTER(3739);

	public final int spriteId;

	League3TaskTier(int spriteId)
	{
		this.spriteId = spriteId;
	}

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

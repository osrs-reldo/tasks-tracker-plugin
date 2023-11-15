package net.reldo.taskstracker.tasktypes.league4;

public enum League4TaskTier
{
	EASY(2316),
	MEDIUM(2318),
	HARD(2319),
	ELITE(2320),
	MASTER(3739);

	public final int spriteId;

	League4TaskTier(int spriteId)
	{
		this.spriteId = spriteId;
	}

	public static League4TaskTier getTierByName(String name)
	{
		for (League4TaskTier value : values())
		{
			if (value.name().equalsIgnoreCase(name))
			{
				return value;
			}
		}

		return null;
	}
}

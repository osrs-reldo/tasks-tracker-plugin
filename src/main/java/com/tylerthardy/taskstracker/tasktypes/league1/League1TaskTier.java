package com.tylerthardy.taskstracker.tasktypes.league1;

public enum League1TaskTier
{
	EASY(2316),
	MEDIUM(2317),
	HARD(2318),
	ELITE(2319),
	MASTER(2320);

	public final int spriteId;

	League1TaskTier(int spriteId)
	{
		this.spriteId = spriteId;
	}

	public static League1TaskTier getTierByName(String name)
	{
		for (League1TaskTier value : values())
		{
			if (value.name().equalsIgnoreCase(name))
			{
				return value;
			}
		}

		return null;
	}
}

package com.tylerthardy.taskstracker.tasktypes.league3;

public enum League3TaskTier
{
    EASY(2316),
    MEDIUM(2317),
    HARD(2318),
    ELITE(2319),
    MASTER(2320);

    public final int spriteId;

    League3TaskTier(int spriteId)
    {
        this.spriteId = spriteId;
    }

    public static League3TaskTier getTierByName(String name)
    {
        for (League3TaskTier value : values()) {
            if (value.name().equalsIgnoreCase(name))
            {
                return value;
            }
        }

        return null;
    }
}

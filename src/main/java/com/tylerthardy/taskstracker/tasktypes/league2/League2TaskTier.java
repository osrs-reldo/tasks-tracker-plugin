package com.tylerthardy.taskstracker.tasktypes.league2;

public enum League2TaskTier
{
    EASY(2316),
    MEDIUM(2317),
    HARD(2318),
    ELITE(2319),
    MASTER(2320);

    public final int spriteId;

    League2TaskTier(int spriteId)
    {
        this.spriteId = spriteId;
    }

    public static League2TaskTier getTierByName(String name)
    {
        for (League2TaskTier value : values()) {
            if (value.name().toLowerCase().equals(name.toLowerCase()))
            {
                return value;
            }
        }

        return null;
    }
}

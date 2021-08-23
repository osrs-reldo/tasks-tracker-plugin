package com.tylerthardy.combattaskstracker;

public enum TaskTier {
    EASY(3399),
    MEDIUM(3400),
    HARD(3401),
    ELITE(3402),
    MASTER(3403),
    GRANDMASTER(3404);

    public final int spriteId;

    TaskTier(int spriteId)
    {
        this.spriteId = spriteId;
    }
}

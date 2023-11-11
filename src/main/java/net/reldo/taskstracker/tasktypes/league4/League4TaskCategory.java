package net.reldo.taskstracker.tasktypes.league4;

public enum League4TaskCategory
{
    //@todo Add sprite IDs
    ACHIEVEMENT(0),
    COMBAT(0),
    QUEST(0),
    SKILL(0),
    MINIGAME(0),
    OTHER(0);

    public final int spriteId;

    League4TaskCategory(int spriteId)
    {
        this.spriteId = spriteId;
    }

    public static League4TaskCategory getCategoryByName(String name)
    {
        for (League4TaskCategory value : values())
        {
            if (value.name().equalsIgnoreCase(name))
            {
                return value;
            }
        }

        return null;
    }
}

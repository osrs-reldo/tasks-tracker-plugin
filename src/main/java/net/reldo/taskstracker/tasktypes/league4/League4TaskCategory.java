package net.reldo.taskstracker.tasktypes.league4;

public enum League4TaskCategory
{
    ACHIEVEMENT(3739),
    COMBAT(0),
    QUEST(2316),
    SKILL(2317),
    MINIGAME(2318),
    OTHER(2319);

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

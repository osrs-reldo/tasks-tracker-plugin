package net.reldo.taskstracker.tasktypes.league4;

public enum League4TaskArea
{
    //@todo Add sprite IDs
    GLOBAL(0),
    MISTHALIN(0),
    KARAMJA(0),
    ASGARNIA(0),
    FREMENNIK(0),
    KANDARIN(0),
    DESERT(0),
    KOUREND(0),
    MORYTANIA(0),
    TIRANNWN(0),
    WILDERNESS(0);

    public final int spriteId;

    League4TaskArea(int spriteId)
    {
        this.spriteId = spriteId;
    }

    public static League4TaskArea getAreaByName(String name)
    {
        for (League4TaskArea value : values())
        {
            if (value.name().equalsIgnoreCase(name))
            {
                return value;
            }
        }

        return null;
    }
}

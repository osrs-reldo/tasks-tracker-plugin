package net.reldo.taskstracker.tasktypes.league4;

public enum League4TaskArea
{
    COMMON(0),
    MISTHALIN(2316),
    KARAMJA(2317),
    ASGARNIA(2318),
    FREMENNIK(2319),
    KANDARIN(3739),
    KHARIDIAN_DESERT(2320),
    KOUREND(3739),
    MORYTANIA(3739),
    TIRANNWN(3739),
    WILDERNESS(3739);

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

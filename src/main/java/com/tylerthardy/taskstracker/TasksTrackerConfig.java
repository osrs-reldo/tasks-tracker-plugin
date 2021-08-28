package com.tylerthardy.taskstracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tasks-tracker")
public interface TasksTrackerConfig extends Config {
    @ConfigItem
            (
                    position = 0,
                    keyName = "sprite_id",
                    name = "Sprite Id",
                    description = "Sprite Id"
            )
    default int spriteId() { return 0; }
}

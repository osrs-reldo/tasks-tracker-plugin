package com.tylerthardy.taskstracker.types.combattask;

import com.tylerthardy.taskstracker.types.Task;

import java.awt.image.BufferedImage;

public class CombatTask extends Task
{
    public String area;

    public CombatTask(String name, String description, String tier, BufferedImage icon) {
        super(name, description, tier, icon);
    }
}

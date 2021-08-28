package com.tylerthardy.taskstracker.types.league1;

import com.tylerthardy.taskstracker.types.Task;

import java.awt.image.BufferedImage;

public class League1Task extends Task
{
    public String[] skills;
    public String other;

    public League1Task(String name, String description, String tier, BufferedImage icon)
    {
        super(name, description, tier, icon);
    }
}

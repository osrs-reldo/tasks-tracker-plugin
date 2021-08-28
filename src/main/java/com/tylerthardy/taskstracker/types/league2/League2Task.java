package com.tylerthardy.taskstracker.types.league2;

import com.tylerthardy.taskstracker.types.Task;

import java.awt.image.BufferedImage;

public class League2Task extends Task
{
    public String area;
    public String[] skills;
    public String other;

    public League2Task(String name, String description, String tier, BufferedImage icon)
    {
        super(name, description, tier, icon);
    }
}

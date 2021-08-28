package com.tylerthardy.taskstracker.types.testtask;

import com.tylerthardy.taskstracker.types.Task;

import java.awt.image.BufferedImage;

public class TestTask extends Task
{
    public String filter1;
    public String filter2;
    public String filter3;
    public TestTask(String name, String description, String tier, BufferedImage icon) {
        super(name, description, tier, icon);
    }
}

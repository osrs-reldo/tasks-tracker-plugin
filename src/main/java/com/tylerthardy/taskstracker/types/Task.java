package com.tylerthardy.taskstracker.types;

import lombok.Data;

import java.awt.image.BufferedImage;

@Data
public abstract class Task
{
    private static String dataFileName;
    private static TaskType type;

    private final String name;
    private final String description;
    private final String tier;

    private final BufferedImage icon;
}

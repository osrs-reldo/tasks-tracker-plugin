package com.tylerthardy.taskstracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskLoader
{
    private static final HashMap<TaskType, ArrayList<Task>> taskCache = new HashMap<>();

    public static ArrayList<Task> getTasks(TaskType taskType)
    {
        if (taskType == null)
        {
            return new ArrayList<>();
        }

        if (taskCache.containsKey(taskType))
        {
            return taskCache.get(taskType);
        }

        try (InputStream dataFile = TasksTrackerPlugin.class.getResourceAsStream(taskType.getDataFileName()))
        {
            Type classType = taskType.getClassType();
            Type listType = TypeToken.getParameterized(ArrayList.class, classType).getType();
            ArrayList<Task> tasks = new Gson().fromJson(new InputStreamReader(dataFile, StandardCharsets.UTF_8), listType);
            taskCache.put(taskType, tasks);
            return tasks;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}

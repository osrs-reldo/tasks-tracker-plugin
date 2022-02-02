package net.reldo.taskstracker.panel.filters;

import java.util.Arrays;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.tasktypes.RequiredSkill;
import net.reldo.taskstracker.tasktypes.Task;

public class SkillFilter extends Filter
{
    private final TasksTrackerConfig config;

    public SkillFilter(TasksTrackerConfig config)
    {
        this.config = config;
    }

    @Override
    public boolean meetsCriteria(Task task)
    {
        if(task.getSkills() == null) return true;

        String skillFilter = config.skillFilter();

        if (task.getSkills().length > 0 && !Arrays.stream(task.getSkills())
                .allMatch((RequiredSkill skill) -> skillFilter.contains(skill.getSkill().toLowerCase())))
        {
            return false;
        }

        return task.getSkills().length != 0 || skillFilter.contains("noskill");
    }
}

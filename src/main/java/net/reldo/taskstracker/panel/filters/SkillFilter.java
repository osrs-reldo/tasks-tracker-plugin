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
                .filter((RequiredSkill skill) -> !skill.getSkill().equalsIgnoreCase("combat")) //@todo replace with combat level requirement filtering
                .allMatch((RequiredSkill skill) -> skillFilter.contains(skill.getSkill().toLowerCase())))
        {
            return false;
        }

        return Arrays.stream(task.getSkills()).anyMatch((RequiredSkill skill) -> !skill.getSkill().equalsIgnoreCase("combat")) || skillFilter.contains("noskill");
    }
}

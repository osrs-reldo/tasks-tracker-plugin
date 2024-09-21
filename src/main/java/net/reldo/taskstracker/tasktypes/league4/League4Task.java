package net.reldo.taskstracker.tasktypes.league4;

import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;

public class League4Task extends Task
{

	public League4Task(int id, String name, String description, String tier, int clientSortId)
	{
		super(id, name, description, tier, clientSortId);
	}

	@Override
	public int getPoints()
	{
		League4TaskTier tierType = League4TaskTier.tiersByName.get(this.getTier().toLowerCase());
		return tierType != null ? tierType.points : 0;
	}

	@Override
	public TaskType getType()
	{
		return TaskType.LEAGUE_4;
	}
}

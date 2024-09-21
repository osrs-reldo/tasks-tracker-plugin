package net.reldo.taskstracker.tasktypes.combattask;

import lombok.Getter;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;

public class CombatTask extends Task
{
	@Getter
	public String monster;
	@Getter
	public String category;

	public CombatTask(int id, String name, String description, String tier, int clientSortId)
	{
		super(id, name, description, tier, clientSortId);
	}

	@Override
	public int getPoints()
	{
		CombatTaskTier tierType = CombatTaskTier.tiersByName.get(this.getTier().toLowerCase());
		return tierType != null ? tierType.points : 0;
	}

	@Override
	public TaskType getType()
	{
		return TaskType.COMBAT;
	}
}

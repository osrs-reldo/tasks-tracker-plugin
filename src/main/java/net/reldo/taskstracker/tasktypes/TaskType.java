package net.reldo.taskstracker.tasktypes;

import net.reldo.taskstracker.tasktypes.combattask.CombatTask;
import net.reldo.taskstracker.tasktypes.league3.League3Task;
import java.lang.reflect.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.reldo.taskstracker.tasktypes.league4.League4Task;

@AllArgsConstructor
@Getter
public enum TaskType
{

	COMBAT("Combat Tasks", "combat_tasks.min.json", CombatTask.class),
	LEAGUE_3("League III: Shattered Relics", "league3_tasks.min.json", League3Task.class),
	LEAGUE_4("League IV: Trailblazer Reloaded", "league4_tasks.min.json", League4Task.class);
	public final String displayString;
	public final String dataFileName;
	public final Type classType;

	@Override
	public String toString()
	{
		return displayString;
	}
}

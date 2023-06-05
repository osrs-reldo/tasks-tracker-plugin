package net.reldo.taskstracker.tasktypes.combattask;

import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CombatTaskVarps
{
	TASK_PROGRESS_0(3116),
	TASK_PROGRESS_1(3117),
	TASK_PROGRESS_2(3118),
	TASK_PROGRESS_3(3119),
	TASK_PROGRESS_4(3120),
	TASK_PROGRESS_5(3121),
	TASK_PROGRESS_6(3122),
	TASK_PROGRESS_7(3123),
	TASK_PROGRESS_8(3124),
	TASK_PROGRESS_9(3125),
	TASK_PROGRESS_10(3126),
	TASK_PROGRESS_11(3127),
	TASK_PROGRESS_12(3128),
	TASK_PROGRESS_13(3387),
	TASK_PROGRESS_14(3718),
	TASK_PROGRESS_15(3773),
	TASK_PROGRESS_16(3774);

	private int varpId;

	@Getter
	private static final HashMap<Integer, CombatTaskVarps> idToVarpMap = new HashMap<>();

	static
	{
		for (CombatTaskVarps varp : CombatTaskVarps.values())
		{
			idToVarpMap.put(varp.getVarpId(), varp);
		}
	}
}

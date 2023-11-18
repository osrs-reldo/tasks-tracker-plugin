package net.reldo.taskstracker.tasktypes.league4;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum League4Varbits
{
	// Relics
	RELIC_SLOT_1(10049),
	RELIC_SLOT_2(10050),
	RELIC_SLOT_3(10051),
	RELIC_SLOT_4(10052),
	RELIC_SLOT_5(10053),
	RELIC_SLOT_6(11696),
	RELIC_SLOT_7(17301),
	RELIC_SLOT_8(17302),

	// Regions

	// Task data
	TASKS_COMPLETED(10046),
	LIBRARY_BOOKS(10039);

	private final int varbitId;

	@Getter
	private static final List<Integer> allVarbitIds = new ArrayList<>();

	static
	{
		for (League4Varbits varbit : League4Varbits.values())
		{
			allVarbitIds.add(varbit.getVarbitId());
		}
	}
}

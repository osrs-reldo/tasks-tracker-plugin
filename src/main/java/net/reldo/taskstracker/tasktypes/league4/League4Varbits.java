package net.reldo.taskstracker.tasktypes.league4;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum League4Varbits
{
	// Fragments
	FRAGMENT_SLOT_1(13395),
	FRAGMENT_SLOT_2(13396),
	FRAGMENT_SLOT_3(13397),
	FRAGMENT_SLOT_4(13398),
	FRAGMENT_SLOT_5(13399),
	FRAGMENT_SLOT_6(13401),
	FRAGMENT_SLOT_7(13402),

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

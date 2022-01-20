package com.tylerthardy.taskstracker.tasktypes.league3;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum League3Varbits
{
	FRAGMENT_SLOT_1(13395),
	FRAGMENT_SLOT_2(13396),
	FRAGMENT_SLOT_3(13397),
	FRAGMENT_SLOT_4(13398),
	FRAGMENT_SLOT_5(13399),
	FRAGMENT_SLOT_6(13401),
	FRAGMENT_SLOT_7(13402),
	TASKS_COMPLETED(10046);

	private final int varbitId;
}

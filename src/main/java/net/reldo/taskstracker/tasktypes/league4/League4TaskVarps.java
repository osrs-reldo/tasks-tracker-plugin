package net.reldo.taskstracker.tasktypes.league4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum League4TaskVarps
{
	TASK_PROGRESS_0(2616),
	TASK_PROGRESS_1(2617),
	TASK_PROGRESS_2(2618),
	TASK_PROGRESS_3(2619),
	TASK_PROGRESS_4(2620),
	TASK_PROGRESS_5(2621),
	TASK_PROGRESS_6(2622),
	TASK_PROGRESS_7(2623),
	TASK_PROGRESS_8(2624),
	TASK_PROGRESS_9(2625),
	TASK_PROGRESS_10(2626),
	TASK_PROGRESS_11(2627),
	TASK_PROGRESS_12(2628),
	TASK_PROGRESS_13(2629),
	TASK_PROGRESS_14(2630),
	TASK_PROGRESS_15(2631),
	TASK_PROGRESS_16(2808),
	TASK_PROGRESS_17(2809),
	TASK_PROGRESS_18(2810),
	TASK_PROGRESS_19(2811),
	TASK_PROGRESS_20(2812),
	TASK_PROGRESS_21(2813),
	TASK_PROGRESS_22(2814),
	TASK_PROGRESS_23(2815),
	TASK_PROGRESS_24(2816),
	TASK_PROGRESS_25(2817),
	TASK_PROGRESS_26(2818),
	TASK_PROGRESS_27(2819),
	TASK_PROGRESS_28(2820),
	TASK_PROGRESS_29(2821),
	TASK_PROGRESS_30(2822),
	TASK_PROGRESS_31(2823),
	TASK_PROGRESS_32(2824),
	TASK_PROGRESS_33(2825),
	TASK_PROGRESS_34(2826),
	TASK_PROGRESS_35(2827),
	TASK_PROGRESS_36(2828),
	TASK_PROGRESS_37(2829),
	TASK_PROGRESS_38(2830),
	TASK_PROGRESS_39(2831),
	TASK_PROGRESS_40(2832),
	TASK_PROGRESS_41(2833),
	TASK_PROGRESS_42(2834),
	TASK_PROGRESS_43(2835),
	TASK_PROGRESS_44(3339),
	TASK_PROGRESS_45(3340),
	TASK_PROGRESS_46(3341),
	TASK_PROGRESS_47(3342);

	private int varpId;

	@Getter
	private static final HashMap<Integer, League4TaskVarps> idToVarpMap = new HashMap<>();

	@Getter
	private static final List<Integer> allVarpIds = new ArrayList<>();

	static
	{
		for (League4TaskVarps varp : League4TaskVarps.values())
		{
			idToVarpMap.put(varp.getVarpId(), varp);
			allVarpIds.add(varp.getVarpId());
		}
	}
}

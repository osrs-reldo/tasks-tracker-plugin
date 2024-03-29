package net.reldo.taskstracker.tasktypes.league4;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum League4Varps
{
	LEAGUE_POINTS(2614),
	SAGES_RENOWN(3276);

	private final int varpId;

	@Getter
	private static final List<Integer> allVarpIds = new ArrayList<>();

	static
	{
		for (League4Varps varp : League4Varps.values())
		{
			allVarpIds.add(varp.getVarpId());
		}
	}
}

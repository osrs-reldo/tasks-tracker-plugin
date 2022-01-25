package net.reldo.taskstracker.tasktypes.league3;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum League3Varps
{
	LEAGUE_POINTS(2614),
	SAGES_RENOWN(3276);

	private final int varpId;

	@Getter
	private static final List<Integer> allVarpIds = new ArrayList<>();

	static
	{
		for (League3Varps varp : League3Varps.values())
		{
			allVarpIds.add(varp.getVarpId());
		}
	}
}

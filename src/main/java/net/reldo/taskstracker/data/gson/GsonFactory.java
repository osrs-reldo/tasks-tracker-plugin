package net.reldo.taskstracker.data.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.runelite.api.coords.WorldPoint;

public class GsonFactory
{
	public static GsonBuilder newBuilder(Gson base)
	{
		return base.newBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.registerTypeAdapter(float.class, new LongSerializer())
			.registerTypeAdapter(WorldPoint.class, new WorldPointAdapter());
	}
}

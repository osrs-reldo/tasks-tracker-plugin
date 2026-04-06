package net.reldo.taskstracker.data.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import net.runelite.api.coords.WorldPoint;

public class WorldPointAdapter extends TypeAdapter<WorldPoint>
{
	@Override
	public void write(JsonWriter out, WorldPoint wp) throws IOException
	{
		if (wp == null)
		{
			out.nullValue();
			return;
		}
		out.beginObject();
		out.name("x").value(wp.getX());
		out.name("y").value(wp.getY());
		out.name("plane").value(wp.getPlane());
		out.endObject();
	}

	@Override
	public WorldPoint read(JsonReader in) throws IOException
	{
		if (in.peek() == com.google.gson.stream.JsonToken.NULL)
		{
			in.nextNull();
			return null;
		}
		int x = 0, y = 0, plane = 0;
		in.beginObject();
		while (in.hasNext())
		{
			switch (in.nextName())
			{
				case "x": x = in.nextInt(); break;
				case "y": y = in.nextInt(); break;
				case "plane": plane = in.nextInt(); break;
				default: in.skipValue();
			}
		}
		in.endObject();
		return new WorldPoint(x, y, plane);
	}
}

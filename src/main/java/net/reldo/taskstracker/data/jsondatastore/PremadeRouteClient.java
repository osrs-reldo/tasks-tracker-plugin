package net.reldo.taskstracker.data.jsondatastore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.gson.GsonFactory;
import net.reldo.taskstracker.data.jsondatastore.reader.DataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.types.PremadeRouteEntry;
import net.reldo.taskstracker.data.route.CustomRoute;

@Singleton
@Slf4j
public class PremadeRouteClient
{
	@Inject
	private Gson gson;
	@Inject
	private DataStoreReader dataStoreReader;

	public List<PremadeRouteEntry> getManifest() throws Exception
	{
		try (InputStream stream = this.dataStoreReader.readPremadeRouteManifest();
			InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8))
		{
			Type listType = TypeToken.getParameterized(ArrayList.class, PremadeRouteEntry.class).getType();
			List<PremadeRouteEntry> entries = this.gson.fromJson(reader, listType);
			if (entries == null)
			{
				return new ArrayList<>();
			}
			return entries;
		}
	}

	public CustomRoute getRoute(String filename) throws Exception
	{
		try (InputStream stream = this.dataStoreReader.readPremadeRoute(filename);
			InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8))
		{
			Gson routeGson = GsonFactory.newBuilder(gson).create();
			return routeGson.fromJson(reader, CustomRoute.class);
		}
	}
}

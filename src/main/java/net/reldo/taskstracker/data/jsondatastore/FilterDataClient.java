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
import net.reldo.taskstracker.data.jsondatastore.types.FilterDefinition;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
@Slf4j
public class FilterDataClient
{
	@Inject	private OkHttpClient okHttpClient;
	@Inject private Gson gson;

	public List<FilterDefinition> getFilters(String filterFilename) throws Exception
	{
		String filtersJsonUrl = JsonDataStore.baseUrl + "/" + filterFilename;
		log.debug("getFilters json from {} ...", filtersJsonUrl);
		Request request = new Request.Builder()
			.url(filtersJsonUrl)
			.build();
		Response response = this.okHttpClient.newCall(request).execute();
		if (!response.isSuccessful())
		{
			String unsuccessful = "getFilters json request unsuccessful with status " + response.code();
			if (response.body() != null)
			{
				unsuccessful += " and body \n" + response.body();
			}
			log.error(unsuccessful);
			throw new Exception(unsuccessful);
		}
		if (response.body() == null)
		{
			log.error("getFilters returned without body");
			throw new Exception("getFilters returned without body");
		}
		log.debug("getFilters json fetched successfully, deserializing result");
		InputStream stream = response.body().byteStream();
		InputStreamReader responseReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		Type listType = TypeToken.getParameterized(ArrayList.class, FilterDefinition.class).getType();
		return this.gson.fromJson(responseReader, listType);
	}
}

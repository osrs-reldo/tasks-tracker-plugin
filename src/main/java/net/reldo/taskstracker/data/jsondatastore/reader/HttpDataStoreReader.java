package net.reldo.taskstracker.data.jsondatastore.reader;

import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.JsonDataStore;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
@Slf4j
public class HttpDataStoreReader implements DataStoreReader
{
	@Inject
	private OkHttpClient okHttpClient;

	@Override
	public InputStream readManifestData() throws Exception
	{
		String manifestUrl = JsonDataStore.baseUrl + "/manifest.json";
		log.debug("getManifest json from {} ...", manifestUrl);
		Request request = new Request.Builder()
			.url(manifestUrl)
			.build();
		Response response = this.okHttpClient.newCall(request).execute();
		if (!response.isSuccessful())
		{
			String unsuccessful = "getManifest json request unsuccessful with status " + response.code();
			if (response.body() != null)
			{
				unsuccessful += " and body \n" + response.body();
			}
			log.error(unsuccessful);
			throw new Exception(unsuccessful);
		}
		if (response.body() == null)
		{
			log.error("getManifest returned without body");
			throw new Exception("getManifest returned without body");
		}
		log.debug("getManifest json fetched successfully, deserializing result");
		return response.body().byteStream();
	}

	@Override
	public InputStream readTaskTypes(String taskTypeFilename) throws Exception
	{
		String taskJsonUrl = JsonDataStore.baseUrl + "/" + taskTypeFilename;
		log.debug("getTaskTypes json from {} ...", taskJsonUrl);
		Request request = new Request.Builder()
			.url(taskJsonUrl)
			.build();
		Response response = this.okHttpClient.newCall(request).execute();
		if (!response.isSuccessful())
		{
			String unsuccessful = "getTaskTypes json request unsuccessful with status " + response.code();
			if (response.body() != null)
			{
				unsuccessful += " and body \n" + response.body();
			}
			log.error(unsuccessful);
			throw new Exception(unsuccessful);
		}
		if (response.body() == null)
		{
			log.error("getTaskTypes returned without body");
			throw new Exception("getTaskTypes returned without body");
		}
		log.debug("getTaskTypes json fetched successfully, deserializing result");
		return response.body().byteStream();
	}

	@Override
	public InputStream readTasks(String jsonFilename) throws Exception
	{
		String taskJsonUrl = String.format("%s/tasks/%s.min.json", JsonDataStore.baseUrl, jsonFilename);
		log.debug("getTasks json from {} ...", taskJsonUrl);
		Request request = new Request.Builder()
			.url(taskJsonUrl)
			.build();
		Response response = this.okHttpClient.newCall(request).execute();
		if (!response.isSuccessful())
		{
			String unsuccessful = "getTasks json request unsuccessful with status " + response.code();
			if (response.body() != null)
			{
				unsuccessful += " and body \n" + response.body();
			}
			log.error(unsuccessful);
			throw new Exception(unsuccessful);
		}
		if (response.body() == null)
		{
			log.error("getTasks returned without body");
			throw new Exception("getTasks returned without body");
		}
		log.debug("getTasks json fetched successfully, deserializing result");
		return response.body().byteStream();
	}

	@Override
	public InputStream readFilterConfigs(String filterFilename) throws Exception
	{
		String filterJsonUrl = JsonDataStore.baseUrl + "/" + filterFilename;
		log.debug("getTaskTypes json from {} ...", filterJsonUrl);
		Request request = new Request.Builder()
			.url(filterJsonUrl)
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
		return response.body().byteStream();
	}
}

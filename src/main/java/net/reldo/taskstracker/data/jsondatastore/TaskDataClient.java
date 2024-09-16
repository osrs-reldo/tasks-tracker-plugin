package net.reldo.taskstracker.data.jsondatastore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.types.Manifest;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
import net.reldo.taskstracker.data.task.TaskV2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
@Slf4j
public class TaskDataClient
{
	@Inject private ManifestClient manifestClient;
	@Inject	private OkHttpClient okHttpClient;
	@Inject private Gson gson;

	private String _taskTypesJsonFilename;

	public TaskDataClient()
	{
		System.out.println("init task data client");
	}

	public HashMap<String, TaskTypeDefinition> getTaskTypes() throws Exception
	{
		String taskJsonUrl = JsonDataStore.baseUrl + "/" + this.getTaskTypesJsonFilename();
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
		InputStream stream = response.body().byteStream();
		InputStreamReader responseReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		Type listType = TypeToken.getParameterized(ArrayList.class, TaskTypeDefinition.class).getType();

		List<TaskTypeDefinition> taskTypes = this.gson.fromJson(responseReader, listType);
		HashMap<String, TaskTypeDefinition> taskTypesBySlug = new HashMap<>();
		for (TaskTypeDefinition taskType : taskTypes)
		{
			taskTypesBySlug.put(taskType.slug, taskType);
		}
		return taskTypesBySlug;
	}

	public List<TaskV2> getTasks(String jsonFilename) throws Exception
	{
		String taskJsonUrl = JsonDataStore.baseUrl + "/tasks/" + jsonFilename;
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
		InputStream stream = response.body().byteStream();
		InputStreamReader responseReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		Type listType = TypeToken.getParameterized(ArrayList.class, TaskV2.class).getType();
		return this.gson.fromJson(responseReader, listType);
	}

	private String getTaskTypesJsonFilename() throws Exception
	{
		if (this._taskTypesJsonFilename != null && !this._taskTypesJsonFilename.isEmpty())
		{
			return this._taskTypesJsonFilename;
		}
		try
		{
			Manifest manifest = this.manifestClient.getManifest();
			this._taskTypesJsonFilename = manifest.taskTypeMetadataFilename;
			return this._taskTypesJsonFilename;
		} catch (Exception ex) {
			log.error("Could not get manifest", ex);
			throw ex;
		}
	}
}

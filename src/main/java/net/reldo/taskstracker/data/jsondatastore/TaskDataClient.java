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
import net.reldo.taskstracker.data.jsondatastore.reader.DataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.types.TaskV2;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskTypeDefinition;
import okhttp3.OkHttpClient;

@Singleton
@Slf4j
public class TaskDataClient
{
	@Inject private ManifestClient manifestClient;
	@Inject	private OkHttpClient okHttpClient;
	@Inject private Gson gson;
	@Inject private DataStoreReader dataStoreReader;

	public TaskDataClient()
	{
		System.out.println("init task data client");
	}

	public HashMap<String, TaskTypeDefinition> getTaskTypes() throws Exception
	{
		InputStream stream = this.dataStoreReader.readTaskTypes(this.manifestClient.getManifest().taskTypeMetadata);
		InputStreamReader responseReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		Type listType = TypeToken.getParameterized(ArrayList.class, TaskTypeDefinition.class).getType();

		List<TaskTypeDefinition> taskTypes = this.gson.fromJson(responseReader, listType);
		HashMap<String, TaskTypeDefinition> taskTypesBySlug = new HashMap<>();
		for (TaskTypeDefinition taskType : taskTypes)
		{
			taskTypesBySlug.put(taskType.getTaskJsonName(), taskType);
		}
		return taskTypesBySlug;
	}

	public List<TaskV2> getTasks(String jsonFilename) throws Exception
	{
		InputStream stream = this.dataStoreReader.readTasks(jsonFilename);
		InputStreamReader responseReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		Type listType = TypeToken.getParameterized(ArrayList.class, TaskV2.class).getType();
		return this.gson.fromJson(responseReader, listType);
	}
}

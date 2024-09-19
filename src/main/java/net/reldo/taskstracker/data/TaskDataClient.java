package net.reldo.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
@Slf4j
public class TaskDataClient
{
	@Inject
	private OkHttpClient okHttpClient;
	@Inject
	private Gson gson;

	private static final String BASE_URL = "https://raw.githubusercontent.com/osrs-reldo/task-json-store/main/";
	private static final String JSON_MIN_PATH = BASE_URL + "json/min/";

	public CompletableFuture<ArrayList<Task>> loadTaskSourceData(TaskType taskType)
	{
		Type classType = taskType.getClassType();
		Type listType = TypeToken.getParameterized(ArrayList.class, classType).getType();

		return this.getTaskJson(taskType.getDataFileName()).thenApply(jsonResponse -> {
			ArrayList<Task> result = this.gson.fromJson(new InputStreamReader(jsonResponse, StandardCharsets.UTF_8), listType);
			return result;
		});
	}

	private CompletableFuture<InputStream> getTaskJson(String jsonName)
	{
		CompletableFuture<InputStream> future = new CompletableFuture<>();
		String url = JSON_MIN_PATH + jsonName;
		log.debug("Fetching task json {} from {}...", jsonName, url);

		Request request = new Request.Builder()
			.url(url)
			.build();

		this.okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e)
			{
				log.error("Error retrieving task json {}", jsonName, e);
				future.completeExceptionally(e);
			}

			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response)
			{
				if (response.isSuccessful())
				{
					if (response.body() == null)
					{
						log.error("Task json request returned no body");
						future.completeExceptionally(new IOException("Response body is null"));
					}
					else
					{
						log.debug("Task json {} fetched successfully", jsonName);
						future.complete(response.body().byteStream());
					}
				}
				else
				{
					String unsuccessful = "Task json request unsuccessful with status {}" + response.code();
					if (response.body() != null)
					{
						unsuccessful += " and body \n" + response.body();
					}
					log.error(unsuccessful);
					future.completeExceptionally(new IOException(unsuccessful));
				}
				response.close();
			}
		});
		return future;
	}
}
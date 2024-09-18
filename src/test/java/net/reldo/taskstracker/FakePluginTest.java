package net.reldo.taskstracker;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import net.reldo.taskstracker.data.jsondatastore.ManifestClient;
import net.reldo.taskstracker.data.jsondatastore.jsonreader.DataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.jsonreader.FileDataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.types.Manifest;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.data.task.TaskV2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FakePluginTest
{

	@Inject
	public TaskService taskService;

	@Inject
	public Gson gson;

	@Inject
	public ManifestClient manifestClient;

	@Before
	public void before()
	{
		Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				this.bind(DataStoreReader.class).to(FileDataStoreReader.class);
			}
		}, BoundFieldModule.of(this)).injectMembers(this);
	}

	@Test
	public void doTheThing() throws Exception
	{
		// load manifest
		Manifest manifest = this.manifestClient.getManifest();
		System.out.println("manifest = " + manifest);
		// load task types
		HashMap<String, TaskTypeDefinition> taskTypes = this.taskService.getTaskTypes();
		System.out.println("taskTypes = " + taskTypes);
		// load a task type TaskTypeDefinition
		TaskTypeDefinition taskTypeDefinition = taskTypes.get("LEAGUE_3");
		System.out.println("taskTypeDefinition = " + taskTypeDefinition);
		// load tasks TaskV2[]
		this.taskService.setTaskType("LEAGUE_3");
		List<TaskV2> tasks = this.taskService.getTasks();
		System.out.println("task size = " + tasks.size());
		// check task completion
		TaskV2 taskV2 = tasks.get(0);
		System.out.println("taskV2 = " + taskV2);
	}
}
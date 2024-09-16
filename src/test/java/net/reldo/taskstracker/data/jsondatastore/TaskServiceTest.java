package net.reldo.taskstracker.data.jsondatastore;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import javax.inject.Inject;
import net.reldo.taskstracker.data.task.TaskService;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaskServiceTest
{
	@Rule
	public MockWebServer server;

	@Inject
	public TaskService taskService;

	@Before
	public void before() throws Exception
	{
		System.out.println("starting server");
		this.server = new MockWebServer();
		this.server.start();
		JsonDataStore.baseUrl = this.server.url("/").toString();
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
	}

	@After
	public void after() throws Exception
	{
		this.server.shutdown();
	}

	@Test
	public void serviceInitializes() throws Exception
	{
		assertNotNull(this.taskService);
//		assertTrue(this.taskService.getTaskTypes().size() > 0);
	}
}

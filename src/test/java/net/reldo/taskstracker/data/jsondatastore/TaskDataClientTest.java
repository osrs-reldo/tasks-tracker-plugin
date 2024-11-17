package net.reldo.taskstracker.data.jsondatastore;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import net.reldo.taskstracker.data.jsondatastore.reader.DataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.reader.FileDataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.types.Manifest;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinition;
import net.reldo.taskstracker.data.task.TaskType;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaskDataClientTest
{
	private final String oldBaseUrl = JsonDataStore.baseUrl;

	@Rule
	public MockWebServer server;

	@Inject
	TaskDataClient taskDataClient;

	@Inject
	ManifestClient manifestClient;

	@Before
	public void before() throws Exception
	{
		this.server = new MockWebServer();
		this.server.start();
		JsonDataStore.baseUrl = this.server.url("/").toString();

		Injector injector = Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				this.bind(OkHttpClient.class).toInstance(new OkHttpClient());
				this.bind(Gson.class).toInstance(new Gson());
				this.bind(DataStoreReader.class).to(FileDataStoreReader.class);
				this.bind(ManifestClient.class).toInstance(mock(ManifestClient.class));
			}
		}, BoundFieldModule.of(this));

		injector.injectMembers(this);
	}

	@After
	public void after() throws Exception
	{
		this.server.shutdown();
		JsonDataStore.baseUrl = this.oldBaseUrl;
	}

	@Test
	public void clientInitializes()
	{
		assertNotNull(this.taskDataClient);
	}

	@Test
	public void getTasks() throws Exception
	{
		// TODO: Redo this mock response once task-json-store is complete
		String mockResponse = "[{\"id\":766,\"clientSortId\":36,\"name\":\"Steal From the Varrock Tea Stall\",\"description\":\"Steal from the Tea Stall in Varrock.\",\"points\":1,\"skills\":[{\"skill\":\"Thieving\",\"level\":5}],\"properties\":{\"area\":\"Misthalin\",\"type\":\"Skill\",\"tier\":\"Beginner\",\"other\":\"\"}}]";
		this.server.enqueue(new MockResponse().setBody(mockResponse));

		// TODO: Let this fail until tasks are refactored
		List<TaskDefinition> result = this.taskDataClient.getTaskDefinitions("COMBAT");
		assertEquals(568, result.size());
		// TODO: Assert one
	}

	@Test
	public void getTaskTypes() throws Exception
	{
		when(this.manifestClient.getManifest()).thenReturn(this.getMockManifest());

		// TODO: Redo this mock response once task-json-store is complete
		String mockTypesResponse = "[{\"slug\":\"LEAGUE_3\",\"name\":\"League III: Shattered Relics\",\"json\":\"LEAGUE_3.json\",\"enabled\":true,\"skillFilter\":true,\"properties\":[{\"key\":\"tier\",\"name\":\"Tier\",\"filter\":\"LEAGUE3_TIER\"},{\"key\":\"area\",\"name\":\"Area\",\"filter\":\"LEAGUE3_AREA\"},{\"key\":\"type\",\"name\":\"Task Type\",\"filter\":\"LEAGUE3_TYPE\"},{\"key\":\"category\",\"name\":\"Skill Category\",\"filter\":\"LEAGUE3_SKILL_CATEGORY\"},{\"key\":\"other\",\"name\":\"Addl. Wiki Information\"}],\"taskVarps\":[ 2616, 2617, 2618, 2619],\"otherVarps\":[2614, 3276],\"varbits\":[ 13395, 13396, 13397, 13398]}]";
		this.server.enqueue(new MockResponse().setBody(mockTypesResponse));

		HashMap<String, TaskType> taskTypes = this.taskDataClient.getTaskTypes();

//		TaskTypeDefinition result = taskTypes.get("COMBAT");
//		assertEquals("Combat Achievements", result.getName());
//		assertEquals("A set of combat-based tasks and challenges intended to test the player's prowess in combat related content.", result.getDescription());
//		assertEquals("COMBAT", result.getTaskJsonName());
//		assertTrue(result.isEnabled());
//
//		assertEquals(3, result.getFilters().size());
////		assertEquals(new FilterConfig("Tier", FilterType.BUTTON_FILTER, FilterValueType.PARAM, "tier", "tier"), result.getFilters().get("tier"));
////		assertEquals(new FilterConfig("Type", FilterType.DROPDOWN_FILTER, FilterValueType.PARAM, "type", "type"), result.getFilters().get("type"));
////		assertEquals(new FilterConfig("Monster", FilterType.DROPDOWN_FILTER, FilterValueType.PARAM, "monster", "monster"), result.getFilters().get("monster"));
//
//		assertEquals((Integer) 1306, result.getIntParamMap().get("id"));
//		assertEquals((Integer) 1308, result.getStringParamMap().get("name"));
//		assertEquals((Integer) 1309, result.getStringParamMap().get("description"));
//		assertEquals((Integer) 1310, result.getIntParamMap().get("tier"));
//
////		assertArrayEquals(new int[]{3116, 3117, 3118, 3119, 3120, 3121, 3122, 3123, 3124, 3125, 3126, 3127, 3128, 3387, 3718, 3773, 3774}, result.getTaskVarps());
//		assertArrayEquals(new int[0], result.getOtherVarps());
//		assertArrayEquals(new int[0], result.getVarbits());
	}

	private Manifest getMockManifest()
	{
		Manifest mockManifest = new Manifest();
		mockManifest.filterMetadata = "filters.json";
		mockManifest.taskTypeMetadata = "task-types.json";
		return mockManifest;
	}
}

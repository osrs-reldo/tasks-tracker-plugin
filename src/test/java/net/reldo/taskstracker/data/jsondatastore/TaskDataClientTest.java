package net.reldo.taskstracker.data.jsondatastore;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import net.reldo.taskstracker.data.jsondatastore.jsonreader.DataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.jsonreader.HttpDataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.types.Manifest;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypePropertyDefinition;
import net.reldo.taskstracker.data.task.RequiredSkillV2;
import net.reldo.taskstracker.data.task.TaskV2;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
				this.bind(DataStoreReader.class).to(HttpDataStoreReader.class);
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
	public void getTasks()
	{
		String mockResponse = "[{\"id\":766,\"clientSortId\":36,\"name\":\"Steal From the Varrock Tea Stall\",\"description\":\"Steal from the Tea Stall in Varrock.\",\"points\":1,\"skills\":[{\"skill\":\"Thieving\",\"level\":5}],\"properties\":{\"area\":\"Misthalin\",\"type\":\"Skill\",\"tier\":\"Beginner\",\"other\":\"\"}}]";
		this.server.enqueue(new MockResponse().setBody(mockResponse));

		List<TaskV2> result;
		try
		{
			result = this.taskDataClient.getTasks("fake.json");

			TaskV2 resultTask = result.get(0);
			assertEquals(766, resultTask.getId());
			assertEquals(36, resultTask.getClientSortId());
			assertEquals("Steal From the Varrock Tea Stall", resultTask.getName());
			assertEquals("Steal from the Tea Stall in Varrock.", resultTask.getDescription());
			assertEquals(1, resultTask.getPoints());

			RequiredSkillV2 resultSkill = resultTask.getSkills().get(0);
			assertEquals(resultSkill.skill, "Thieving");
			assertEquals(resultSkill.level, 5);

			assertEquals("Misthalin", resultTask.getPropertyValue("area"));
			assertEquals("Skill", resultTask.getPropertyValue("type"));
			assertEquals("Beginner", resultTask.getPropertyValue("tier"));
			assertEquals("", resultTask.getPropertyValue("other"));
		}
		catch (Exception e)
		{
			fail();
		}
	}

	@Test
	public void getTaskTypes() throws Exception
	{
		when(this.manifestClient.getManifest()).thenReturn(this.getMockManifest());

		String mockTypesResponse = "[{\"slug\":\"LEAGUE_3\",\"name\":\"League III: Shattered Relics\",\"json\":\"LEAGUE_3.json\",\"enabled\":true,\"skillFilter\":true,\"properties\":[{\"key\":\"tier\",\"name\":\"Tier\",\"filter\":\"LEAGUE3_TIER\"},{\"key\":\"area\",\"name\":\"Area\",\"filter\":\"LEAGUE3_AREA\"},{\"key\":\"type\",\"name\":\"Task Type\",\"filter\":\"LEAGUE3_TYPE\"},{\"key\":\"category\",\"name\":\"Skill Category\",\"filter\":\"LEAGUE3_SKILL_CATEGORY\"},{\"key\":\"other\",\"name\":\"Addl. Wiki Information\"}],\"taskVarps\":[ 2616, 2617, 2618, 2619],\"otherVarps\":[2614, 3276],\"varbits\":[ 13395, 13396, 13397, 13398]}]";
		this.server.enqueue(new MockResponse().setBody(mockTypesResponse));

		HashMap<String, TaskTypeDefinition> result;
		try
		{
			result = this.taskDataClient.getTaskTypes();

			TaskTypeDefinition resultTaskType = result.get("LEAGUE_3");
			assertEquals("LEAGUE_3", resultTaskType.getSlug());
			assertEquals("League III: Shattered Relics", resultTaskType.getName());
			assertEquals("LEAGUE_3.json", resultTaskType.getJsonFilename());
			assertTrue(resultTaskType.isEnabled());
			assertTrue(resultTaskType.isSkillFilter());

			TaskTypePropertyDefinition property = resultTaskType.getProperties()[0];
			assertEquals("tier", property.key);
			assertEquals("Tier", property.name);
			assertEquals("LEAGUE3_TIER", property.filter);
			property = resultTaskType.getProperties()[1];
			assertEquals("area", property.key);
			assertEquals("Area", property.name);
			assertEquals("LEAGUE3_AREA", property.filter);
			property = resultTaskType.getProperties()[4];
			assertEquals("other", property.key);
			assertEquals("Addl. Wiki Information", property.name);
		}
		catch (Exception e)
		{
			fail();
		}
	}

	private Manifest getMockManifest()
	{
		Manifest mockManifest = new Manifest();
		mockManifest.additionalVarbits = new int[]{123, 456};
		mockManifest.diaryVarbits = new int[]{1234, 5678};
		mockManifest.filterMetadata = "filters.json";
		mockManifest.taskTypeMetadata = "task-types.json";
		return mockManifest;
	}
}

package net.reldo.taskstracker.data.jsondatastore;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import javax.inject.Inject;
import net.reldo.taskstracker.data.jsondatastore.types.Manifest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ManifestClientTest
{
	@Rule
	public MockWebServer server;

	@Inject	ManifestClient client;

	@Before
	public void before() throws Exception
	{
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
	public void clientInitializes()
	{
		assertNotNull(this.client);
	}

	@Test
	public void getManifest()
	{
		String mockResponse = "{\"taskTypeMetadataFilename\":\"task-types.json\",\"filterMetadataFilename\":\"filters.json\",\"diaryVarbits\":[1234, 5678],\"additionalVarbits\":[123, 456]}";
		this.server.enqueue(new MockResponse().setBody(mockResponse));

		Manifest result;
		try
		{
			result = this.client.getManifest();

			assertEquals("filters.json", result.filterMetadataFilename);
			assertEquals("task-types.json", result.taskTypeMetadataFilename);
			assertEquals(1234, result.diaryVarbits[0]);
			assertEquals(5678, result.diaryVarbits[1]);
			assertEquals(123, result.additionalVarbits[0]);
			assertEquals(456, result.additionalVarbits[1]);
		}
		catch (Exception e)
		{
			fail();
		}
	}
}

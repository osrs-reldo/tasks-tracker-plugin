package net.reldo.taskstracker.data.jsondatastore;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import javax.inject.Inject;
import net.reldo.taskstracker.data.jsondatastore.reader.DataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.reader.FileDataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.types.Manifest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ManifestClientTest
{
	private final String oldBaseUrl = JsonDataStore.baseUrl;

	@Rule
	public MockWebServer server;

	@Inject
	ManifestClient client;

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
				this.bind(DataStoreReader.class).to(FileDataStoreReader.class);
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
		assertNotNull(this.client);
	}

	@Test
	public void getManifest() throws Exception
	{
		// TODO: Redo this mock response once task-json-store is complete
		String mockResponse = "{\"taskTypeMetadata\":\"task-types.json\",\"filterMetadata\":\"filters.json\",\"additionalVarbits\":[4458,4459]}";
		this.server.enqueue(new MockResponse().setBody(mockResponse));

		Manifest result = this.client.getManifest();

		assertEquals("filters.json", result.filterMetadata);
		assertEquals("task-types.json", result.taskTypeMetadata);
	}
}

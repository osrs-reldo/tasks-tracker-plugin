package net.reldo.taskstracker.data.jsondatastore;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import java.util.List;
import javax.inject.Inject;
import net.reldo.taskstracker.data.jsondatastore.types.FilterDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.FilterValueDefinition;
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
public class FilterDataClientTest
{
	private final String oldBaseUrl = JsonDataStore.baseUrl;

	@Rule
	public MockWebServer server;

	@Inject	FilterDataClient client;

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
		JsonDataStore.baseUrl = this.oldBaseUrl;
	}

	@Test
	public void clientInitializes()
	{
		assertNotNull(this.client);
	}

	@Test
	public void getFilters() throws Exception
	{
		String mockResponse = "[{\"slug\":\"SKILL\",\"name\":\"Skill\",\"controlType\":\"button-panel\",\"options\":{\"someOptionValue\":\"7\"},\"values\":[{\"name\":\"Attack\",\"spriteId\":197,\"value\":\"attack\"},{\"name\":\"Strength\",\"spriteId\":198,\"value\":\"strength\"},{\"name\":\"Defence\",\"spriteId\":199,\"value\":\"defence\"},{\"name\":\"Ranged\",\"spriteId\":200,\"value\":\"ranged\"},{\"name\":\"Prayer\",\"spriteId\":201,\"value\":\"prayer\"},{\"name\":\"Magic\",\"spriteId\":202,\"value\":\"magic\"},{\"name\":\"Hitpoints\",\"spriteId\":203,\"value\":\"hitpoints\"},{\"name\":\"Agility\",\"spriteId\":204,\"value\":\"agility\"},{\"name\":\"Herblore\",\"spriteId\":205,\"value\":\"herblore\"},{\"name\":\"Thieving\",\"spriteId\":206,\"value\":\"thieving\"},{\"name\":\"Crafting\",\"spriteId\":207,\"value\":\"crafting\"},{\"name\":\"Fletching\",\"spriteId\":208,\"value\":\"fletching\"},{\"name\":\"Mining\",\"spriteId\":209,\"value\":\"mining\"},{\"name\":\"Smithing\",\"spriteId\":210,\"value\":\"smithing\"},{\"name\":\"Fishing\",\"spriteId\":211,\"value\":\"fishing\"},{\"name\":\"Cooking\",\"spriteId\":212,\"value\":\"cooking\"},{\"name\":\"Firemaking\",\"spriteId\":213,\"value\":\"firemaking\"},{\"name\":\"Woodcutting\",\"spriteId\":214,\"value\":\"woodcutting\"},{\"name\":\"Runecrafting\",\"spriteId\":215,\"value\":\"runecrafting\"},{\"name\":\"Slayer\",\"spriteId\":216,\"value\":\"slayer\"},{\"name\":\"Farming\",\"spriteId\":217,\"value\":\"farming\"},{\"name\":\"Hunter\",\"spriteId\":220,\"value\":\"hunter\"},{\"name\":\"Construction\",\"spriteId\":221,\"value\":\"construction\"}]}]";
		this.server.enqueue(new MockResponse().setBody(mockResponse));

		List<FilterDefinition> result = this.client.getFilters("fake.json");

		FilterDefinition resultTask = result.get(0);
		assertEquals("SKILL", resultTask.getSlug());
		assertEquals("Skill", resultTask.getName());
		assertEquals("button-panel", resultTask.getControlType());
		assertEquals("7", resultTask.getOptions().get("someOptionValue"));
		FilterValueDefinition[] values = resultTask.getValues();
		assertEquals("Attack", values[0].getName());
		assertEquals(197, values[0].getSpriteId());
		assertEquals("attack", values[0].getValue());
		assertEquals("Strength", values[1].getName());
		assertEquals(198, values[1].getSpriteId());
		assertEquals("strength", values[1].getValue());
	}

	@Test
	public void getFilters_empty_filter_values() throws Exception
	{
		String mockResponse = "[{\"slug\":\"SKILL\",\"name\":\"Skill\",\"controlType\":\"button-panel\",\"options\":{\"someOptionValue\":\"7\"},\"values\":[]}]";
		this.server.enqueue(new MockResponse().setBody(mockResponse));

		List<FilterDefinition> result = this.client.getFilters("fake.json");

		FilterDefinition resultTask = result.get(0);
		FilterValueDefinition[] values = resultTask.getValues();
		assertEquals(values.length, 0);
	}

	@Test
	public void getFilters_filter_value_has_no_sprite_id() throws Exception
	{
		String mockResponse = "[{\"slug\":\"LEAGUE3_TYPE\",\"name\":\"Task Type\",\"controlType\": \"dropdown\",\"options\":{},\"values\":[{\"name\":\"Skill\",\"value\":\"skill\"},{\"name\":\"Combat\",\"value\":\"combat\"},{\"name\":\"Quest\",\"value\":\"quest\"},{\"name\":\"Achievement\",\"value\":\"achievement\"},{\"name\":\"Minigame\",\"value\":\"minigame\"},{\"name\":\"Other\",\"value\":\"other\"}]}]";
		this.server.enqueue(new MockResponse().setBody(mockResponse));

		List<FilterDefinition> result = this.client.getFilters("fake.json");

		FilterDefinition resultTask = result.get(0);
		FilterValueDefinition[] values = resultTask.getValues();
		assertEquals(0, values[0].getSpriteId());
		assertEquals(0, values[1].getSpriteId());
	}
}

//package net.reldo.taskstracker.data.jsondatastore.types;
//
//import com.google.inject.testing.fieldbinder.Bind;
//import java.util.Map;
//import static java.util.Map.entry;
//import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinition;
//import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
//import net.runelite.api.Client;
//import net.runelite.api.StructComposition;
//import net.runelite.client.callback.ClientThread;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import org.mockito.Mock;
//import static org.mockito.Mockito.doAnswer;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import org.mockito.junit.MockitoJUnitRunner;
//
//@RunWith(MockitoJUnitRunner.class)
//public class TaskFromStructTest
//{
//	public static final int ONCE_EVEN_THOUGH_TWO_GETS = 1;
//
//	@Mock
//	@Bind
//	Client client;
//	@Mock
//	@Bind
//	ClientThread clientThread;
//
//	private TaskTypeDefinition mockTypeDefinition;
//	private TaskDefinition mockTaskDefinition;
//
//	@Before
//	public void before()
//	{
//		this.mockTypeDefinition = new TaskTypeDefinition();
//		this.mockTypeDefinition.setName("mock type");
//		this.mockTypeDefinition.setTaskJsonName("json_name");
//		this.mockTypeDefinition.setIntParamMap(Map.ofEntries(
//			entry("id", 123),
//			entry("an_int_param", 1010),
//			entry("a_string_param", 1212)
//		));
//		this.mockTypeDefinition.setStringParamMap(Map.ofEntries(
//			entry("name", 456),
//			entry("description", 789)
//		));
//		this.mockTaskDefinition = new TaskDefinition();
//		this.mockTaskDefinition.setStructId(1234);
//		this.mockTaskDefinition.setSortId(3333);
//
//		StructComposition mockStruct = mock(StructComposition.class);
//		when(mockStruct.getIntValue(123)).thenReturn(55);
//		when(mockStruct.getStringValue(456)).thenReturn("task name");
//		when(mockStruct.getStringValue(789)).thenReturn("task description");
//		when(mockStruct.getIntValue(1010)).thenReturn(123);
//		when(mockStruct.getStringValue(1212)).thenReturn("stringo bingo");
//		when(this.client.getStructComposition(anyInt())).thenReturn(mockStruct);
//
//		doAnswer(invocation -> {
//			Runnable runnable = invocation.getArgument(0);
//			runnable.run();
//			return null;
//		}).when(this.clientThread).invoke(any(Runnable.class));
//	}
//
//	@Test
//	public void classInitializes()
//	{
//		TaskFromStruct task = new TaskFromStruct(this.mockTypeDefinition, this.mockTaskDefinition);
//
//		assertNotNull(task);
//		assertEquals(this.mockTypeDefinition, task.getTaskTypeDefinition());
//		assertEquals(this.mockTaskDefinition, task.getTaskDefinition());
//		assertEquals((Integer) 1234, task.getStructId());
//		assertEquals((Integer) 3333, task.getSortId());
//	}
//
//	@Test
//	public void getId() throws Exception
//	{
//		TaskFromStruct task = new TaskFromStruct(this.mockTypeDefinition, this.mockTaskDefinition);
//
//		int id = task.getIdAsync(this.client, this.clientThread).get();
//		assertEquals(55, id);
//
//		int idAgain = task.getIdAsync(this.client, this.clientThread).get();
//		assertEquals(55, idAgain);
//
//		verify(this.clientThread, times(ONCE_EVEN_THOUGH_TWO_GETS)).invoke(any(Runnable.class));
//	}
//
//	@Test
//	public void getSortId()
//	{
//		TaskFromStruct task = new TaskFromStruct(this.mockTypeDefinition, this.mockTaskDefinition);
//		assertEquals(this.mockTaskDefinition.getSortId(), task.getSortId());
//	}
//
//	@Test
//	public void getStructId()
//	{
//		TaskFromStruct task = new TaskFromStruct(this.mockTypeDefinition, this.mockTaskDefinition);
//		assertEquals(this.mockTaskDefinition.getStructId(), task.getStructId());
//	}
//
//	@Test
//	public void getName() throws Exception
//	{
//		TaskFromStruct task = new TaskFromStruct(this.mockTypeDefinition, this.mockTaskDefinition);
//
//		String name = task.getNameAsync(this.client, this.clientThread).get();
//		assertEquals("task name", name);
//
//		String nameAgain = task.getNameAsync(this.client, this.clientThread).get();
//		assertEquals("task name", nameAgain);
//
//		verify(this.clientThread, times(ONCE_EVEN_THOUGH_TWO_GETS)).invoke(any(Runnable.class));
//	}
//
//	@Test
//	public void getDescription() throws Exception
//	{
//		TaskFromStruct task = new TaskFromStruct(this.mockTypeDefinition, this.mockTaskDefinition);
//
//		String description = task.getDescriptionAsync(this.client, this.clientThread).get();
//		assertEquals("task description", description);
//
//		String descriptionAgain = task.getDescriptionAsync(this.client, this.clientThread).get();
//		assertEquals("task description", descriptionAgain);
//
//		verify(this.clientThread, times(ONCE_EVEN_THOUGH_TWO_GETS)).invoke(any(Runnable.class));
//	}
//
//	@Test
//	public void getStringParam() throws Exception
//	{
//		TaskFromStruct task = new TaskFromStruct(this.mockTypeDefinition, this.mockTaskDefinition);
//
//		String paramName = "a_string_param";
//		String value = task.getStringParamAsync(this.client, this.clientThread, paramName).get();
//		assertEquals("stringo bingo", value);
//
//		String valueAgain = task.getStringParamAsync(this.client, this.clientThread, paramName).get();
//		assertEquals("stringo bingo", valueAgain);
//
//		verify(this.clientThread, times(ONCE_EVEN_THOUGH_TWO_GETS)).invoke(any(Runnable.class));
//	}
//
//	@Test
//	public void getIntParam() throws Exception
//	{
//		TaskFromStruct task = new TaskFromStruct(this.mockTypeDefinition, this.mockTaskDefinition);
//
//		String paramName = "an_int_param";
//		int value = task.getIntParamAsync(this.client, this.clientThread, paramName).get();
//		assertEquals(123, value);
//
//		int valueAgain = task.getIntParamAsync(this.client, this.clientThread, paramName).get();
//		assertEquals(123, valueAgain);
//
//		verify(this.clientThread, times(ONCE_EVEN_THOUGH_TWO_GETS)).invoke(any(Runnable.class));
//	}
//}

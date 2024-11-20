package net.reldo.taskstracker.data.task;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class TaskTrackerTaskModule extends AbstractModule
{
	@Override
	protected void configure() {
		// TODO: TaskFromStruct
		this.install(new FactoryModuleBuilder()
			.implement(TaskType.class, TaskType.class)
			.build(TaskTypeFactory.class));
	}
}

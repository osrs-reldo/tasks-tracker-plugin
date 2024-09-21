package net.reldo.taskstracker.panel;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class TaskTrackerPanelModule extends AbstractModule {
	@Override
	protected void configure() {
		this.install(new FactoryModuleBuilder()
			.implement(TaskPanel.class, TaskPanel.class)
			.build(TaskPanelFactory.class));

		this.install(new FactoryModuleBuilder()
			.implement(TaskPanel.class, TaskPanel.class)
			.build(TaskPanelFactory.class));
	}
}
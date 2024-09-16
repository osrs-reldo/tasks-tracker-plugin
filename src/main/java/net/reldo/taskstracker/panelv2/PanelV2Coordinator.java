package net.reldo.taskstracker.panelv2;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.ManifestClient;
import net.reldo.taskstracker.data.jsondatastore.types.Manifest;
import net.reldo.taskstracker.data.task.TaskService;

@Singleton
@Slf4j
public class PanelV2Coordinator
{
	@Inject	private ManifestClient manifestClient;
	@Inject private TaskService taskService;

	public void init()
	{
		try
		{
			Manifest manifest = this.manifestClient.getManifest();
			this.taskService.getTasks();
		} catch (Exception ex) {
			log.error("cannot load manifest", ex);
		}
	}
}

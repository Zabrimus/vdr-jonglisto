package vdr.jonglisto.web.pages;

import java.util.List;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.VdrDataService;
import vdr.jonglisto.lib.model.Device;
import vdr.jonglisto.lib.model.Plugin;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.model.VDRView;
import vdr.jonglisto.lib.model.VDRView.Type;

/**
 * Start page of application VDR Jonglisto app.
 */
@Import(stylesheet = "META-INF/assets/css/Index.css")
public class Index {
	
	@Inject
	private ConfigurationService configuration;

	@Inject
	private VdrDataService dataService;

	@Property
	private VDR vdr;

	@Property
	private Plugin plugin;

	@Property
	private Device device;

	@SessionAttribute
	@Property
	private VDRView currentVdrView;

	public void onActivate() {
		if (currentVdrView == null) {
			// session is empty. re-init...
			currentVdrView = configuration.getConfiguredViews().values().stream().filter(s -> s.getType() == Type.View)
					.findFirst().get();
		}
	}

	public List<VDR> getConfiguredVdrs() {
		return configuration.getSortedVdrList();
	}

	public void onWol(String uuid) {
		configuration.sendWol(uuid);
	}

	public boolean pingHost() {
		return configuration.pingHost(vdr.getIp());
	}

	public boolean testSvdrp() {
		return configuration.testSvdrp(vdr.getIp(), vdr.getSvdrpPort());
	}

	public boolean testRestfulApi() {
		return configuration.testRestfulApi(vdr.getIp(), vdr.getRestfulApiPort());
	}

	public List<Device> getDevices() {
		return dataService.getDevices(vdr.getUuid());
	}

	public List<Plugin> getPlugins() {
		return dataService.getPlugins(vdr.getUuid());
	}
}

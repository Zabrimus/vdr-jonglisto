package vdr.jonglisto.lib.impl;

import java.util.List;
import java.util.Map;

import org.sql2o.Sql2o;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.model.VDRView;

public class ConfigurationServiceImpl implements ConfigurationService {
	
	public void triggerInitialization() {
		// do nothing
	}
	
	public void shutdown() {
		configuration.shutdown();
	}
	
	@Override
	public List<VDR> getSortedVdrList() {
		return configuration.getSortedVdrList();
	}

	@Override
	public Map<String, VDRView> getConfiguredViews() {
		return configuration.getConfiguredViews();
	}
	
	@Override
	public void sendWol(String uuid) {
		configuration.sendWol(uuid);
	}

	@Override
	public boolean pingHost(String ip) {
		return configuration.pingHost(ip);
	}

	@Override
	public boolean testSvdrp(String ip, int svdrpPort) {
		return configuration.testSvdrp(ip, svdrpPort);
	}

	@Override
	public boolean testRestfulApi(String ip, int restfulApiPort) {
		return configuration.testResfulApi(ip, restfulApiPort);
	}
	@Override
	public VDR getVdr(String uuid) {
		return configuration.getVdr(uuid);
	}

	@Override
	public Sql2o getSql2oEpg2vdr() {
		return configuration.getSql2oEpg2vdr();
	}

	@Override
	public Sql2o getSql2oHsqldb() {
		return configuration.getSql2oHsqldb();
	}

	@Override
	public String getChannelImagePath() {
		return configuration.getChannelImagePath();
	}
	
	public boolean useRecordingSyncMap() {
		return configuration.isUseSyncMap();
	}

}

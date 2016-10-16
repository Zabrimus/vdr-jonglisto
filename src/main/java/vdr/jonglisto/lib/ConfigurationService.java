package vdr.jonglisto.lib;

import java.util.List;
import java.util.Map;

import org.sql2o.Sql2o;

import vdr.jonglisto.lib.internal.Configuration;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.model.VDRView;

public interface ConfigurationService {

	Configuration configuration = Configuration.getInstance();
	
	public void triggerInitialization();
	public void shutdown();
	
	public VDR getVdr(String uuid);	
	public List<VDR> getSortedVdrList();
	public Map<String, VDRView> getConfiguredViews();

	public void sendWol(String uuid);
	public boolean pingHost(String ip);
	public boolean testSvdrp(String ip, int svdrpPort);
	public boolean testRestfulApi(String ip, int restfulApiPort);

	public Sql2o getSql2oEpg2vdr();
	public Sql2o getSql2oHsqldb();
	
	public String getChannelImagePath();
	
	public boolean useRecordingSyncMap();
}

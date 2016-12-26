package vdr.jonglisto.lib.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        return configuration.testRestfulApi(ip, restfulApiPort);
    }

    @Override
    public void testAllConnections() {
        ExecutorService executor = Executors.newWorkStealingPool();

        List<Callable<Boolean>> callables = new ArrayList<>();
        configuration.getSortedVdrList().stream().forEach(v -> {
            
            callables.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return configuration.pingHost(v.getIp());
                }
            });
                        
            callables.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return configuration.testRestfulApi(v.getIp(), v.getRestfulApiPort());
                }
            });
            
            callables.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return configuration.testSvdrp(v.getIp(), v.getSvdrpPort());
                }
            });
        });
        
        try {
            executor.invokeAll(callables)            
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
        } catch (InterruptedException e) {
            // do nothing. This is only a faster method to check all VDR.
        }
    }
    
    @Override
    public VDR getVdr(String uuid) {
        return configuration.getVdr(uuid);
    }

    @Override
    public VDR getVdrByAlias(String alias) {
        Optional<VDR> v = configuration.getSortedVdrList().stream().filter(s -> s.getAlias().equals(alias)).findFirst();
        if (v.isPresent()) {
            return v.get();
        } else {
            return null;
        }
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

    public boolean isDeveloperMode() {
        return configuration.isDeveloperMode();
    }

    public String getVersion() {
        return configuration.getVersion();
    }

    public long getRemoteOsdSleepTime() {
        return configuration.getRemoteOsdSleepTime();
    }

    public long getRemoteOsdIncSleepTime() {
        return configuration.getRemoteOsdIncSleepTime();
    }

    public String getSvdrpScript() {
        return configuration.getSvdrpScript();
    }

    public String getEpg2VdrScript() {
        return configuration.getEpg2VdrScript();
    }

}

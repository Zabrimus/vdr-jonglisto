package vdr.jonglisto.lib.svdrp.osdimpl;

import java.net.Socket;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.VdrDataService;
import vdr.jonglisto.lib.impl.ConfigurationServiceImpl;
import vdr.jonglisto.lib.impl.VdrDataServiceImpl;
import vdr.jonglisto.lib.model.osd.TextOsd;
import vdr.jonglisto.lib.svdrp.OsdProvider;
import vdr.jonglisto.lib.svdrp.OsdProviderCache;

public class RemoteOsdProvider implements OsdProvider {

    private TextOsd osd;
    private String vdrUuid;
    private Socket client;

    private VdrDataService vdrService = new VdrDataServiceImpl();
    private ConfigurationService configService = new ConfigurationServiceImpl();

    public RemoteOsdProvider(Socket client, String vdrUuid) {
        this.vdrUuid = vdrUuid;
        this.client = client;               
    }
    
    public TextOsd getOsd() {
        osd = vdrService.getOsd(vdrUuid);
        
        if (osd == null) {
            // 3 retries
            int i = 3;
            
            // the OSD is currently not open, but we need an open OSD. 
            // => Hit Menu-Key and get new OSD after sleep time
            processKey("Menu");
            
            long sleepTime = configService.getRemoteOsdSleepTime();
            
            sleep(sleepTime);
            while (i > 0) {
                osd = vdrService.getOsd(vdrUuid);
                if (osd == null) {
                    sleep(sleepTime);
                } else {
                    break;
                }
                
                sleepTime = sleepTime + configService.getRemoteOsdIncSleepTime();
                --i;
            }
        }              

        OsdProviderCache.changeOsdProvider(client, this);
        
        return osd;
    }

    public void processKey(String key) {
        vdrService.processKey(vdrUuid, key);
        
        long waitTime = 0;
        
        if ("Ok".equalsIgnoreCase(key)) {
            // wait longer, because the OSD needs some time
            waitTime = configService.getRemoteOsdSleepTime() + 2 * configService.getRemoteOsdIncSleepTime();
        } else {
            waitTime = configService.getRemoteOsdSleepTime();
        }

        sleep(waitTime);

        if ("Menu".equalsIgnoreCase(key) && vdrService.getOsd(vdrUuid) == null) {
            OsdProviderCache.changeOsdProvider(client, null);
        }
    }
    
    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
}

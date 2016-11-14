package vdr.jonglisto.lib.impl;

import java.io.IOException;

import org.hampelratte.svdrp.commands.CHAN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.CommandService;
import vdr.jonglisto.lib.model.VDR;

public class CommandServiceImpl extends ServiceBase implements CommandService {

    private Logger log = LoggerFactory.getLogger(CommandService.class);

    public boolean switchChannel(String vdrUuid, String channelId) {
        try {
            VDR vdr = configuration.getVdr(vdrUuid);

            org.hampelratte.svdrp.Connection svdrpVdr = new org.hampelratte.svdrp.Connection(vdr.getIp(),
                    vdr.getSvdrpPort(), 5000);
            svdrpVdr.send(new CHAN(channelId));
            svdrpVdr.close();

            return true;
        } catch (IOException e) {
            log.error("SVDRP Connection failed", e);
            // alertManager.alert(Duration.SINGLE, Severity.ERROR, "SVDRP
            // Connection zu '" + vdr.getName() + "' nicht erfolgreich: " +
            // e.getMessage());
            return false;
        }
    }
}

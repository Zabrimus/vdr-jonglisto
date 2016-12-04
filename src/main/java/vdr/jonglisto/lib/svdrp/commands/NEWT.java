package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.model.Timer;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.model.js.SvdrpOutput;

public class NEWT extends CommandBase {
    
    private static Logger log = LoggerFactory.getLogger(NEWT.class);
    
    @Override
    public void doTheWork(Socket client, BufferedWriter writer, String command, String subCommand) throws IOException {
        // convert VDR timer to Jonglisto Timer
        Timer timer = new Timer();
        timer.convertFromVdrTimer(subCommand);

        SvdrpOutput output = updateTimerByScript(client, timer, "NEWT");
        
        if (output == null) {
            return;
        }
        
        // get VDR data for destination vdr
        VDR vdr = configurationService.getVdrByAlias(output.getRoute_to_alias());
        
        // execute command
        try {
            vdrService.createTimer(vdr.getUuid(), timer);
            send(writer, 250, timer.convertToVdrTimer());            
        } catch (Exception e) {
            try {
                send(writer, 550, e.getLocalizedMessage());
            } catch (Exception ex) {
                // ignore this
                log.error("Unknown error: ", ex);
            }
        }                
    }
}

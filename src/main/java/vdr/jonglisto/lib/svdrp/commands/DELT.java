package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.model.Timer;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.model.js.SvdrpInput;
import vdr.jonglisto.lib.model.js.SvdrpOutput;

public class DELT extends CommandBase {

    private static Logger log = LoggerFactory.getLogger(DELT.class);
    
    @Override
    public void doTheWork(Socket client, BufferedWriter writer, String command, String subCommand) throws IOException {
        int index = Integer.parseInt(subCommand);
        
        // prepare input object
        SvdrpInput input = new SvdrpInput();
        input.setHost(getHostname(client));
        input.setSvdrp_command("DELT");
        input.setSvdrp_param(null);
        
        // call script and get result
        SvdrpOutput output;
        try {
             output = nashorn.callRouteScript(input);
        } catch (NoSuchMethodException | ScriptException e) {
            // do nothing here
            log.error("script error", e);
            return;
        }
        
        // get VDR data for destination vdr
        VDR vdr = configurationService.getVdrByAlias(output.getRoute_to_alias());

        // read all timers
        Optional<List<Timer>> timers = vdrService.getTimer(vdr.getUuid());
        
        if (!timers.isPresent()) {
            // cannot update non-existing timer
            sendTimerError(writer, "timer does not exist");
            return;
        }
        
        // find desired timer
        Optional<Timer> optTimer = timers.get().stream().filter(s -> s.getIndex() == index).findFirst();
        
        if (!optTimer.isPresent()) {
            // cannot update non-existing timer
            sendTimerError(writer, "timer does not exist");
            return;
        }       
        
        vdrService.deleteTimer(vdr.getUuid(), optTimer.get().getId());
        
        try {
            send(writer, 250, "timer deleted");
        } catch (Exception e) {
            // bad
            log.error("send error", e);
            return;
        }
    }
}

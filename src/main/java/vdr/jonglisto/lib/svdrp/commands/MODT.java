package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.model.Timer;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.model.js.SvdrpInput;
import vdr.jonglisto.lib.model.js.SvdrpOutput;

public class MODT extends CommandBase {

    static Logger log = LoggerFactory.getLogger(MODT.class);
    
    @Override
    public void doTheWork(Socket client, BufferedWriter writer, String command, String subCommand) throws IOException {
        final Integer index;
        
        Matcher matcher = cmdPattern.matcher(subCommand);
        if (matcher.matches()) {
            index = Integer.parseInt(matcher.group(1));
            subCommand = matcher.group(4);
        } else {
            index = null;
        }
        
        // prepare input object
        SvdrpInput input = new SvdrpInput();
        input.setHost(getHostname(client));
        input.setSvdrp_command("MODT");
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
        
        Timer oldTimer = optTimer.get();        
        Timer newTimer = new Timer(oldTimer);        
        
        if ("on".equalsIgnoreCase(subCommand)) {
            newTimer.setIsActive(true);
        } else if ("off".equalsIgnoreCase(subCommand)) {
            newTimer.setIsActive(false);
        } else {
            newTimer.convertFromVdrTimer(subCommand);     
            
            output = updateTimerByScript(client, newTimer, "MODT");
            
            if (output == null) {
                return;
            }
        }
        
        // execute command
        try { 
            vdrService.updateTimer(vdr.getUuid(), oldTimer, newTimer);
            send(writer, 250, newTimer.convertToVdrTimer());            
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

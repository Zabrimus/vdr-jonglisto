package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.model.Timer;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.model.js.SvdrpInput;
import vdr.jonglisto.lib.model.js.SvdrpOutput;

public class LSTT extends CommandBase {

    private static Logger log = LoggerFactory.getLogger(LSTT.class);

    @Override
    public void doTheWork(Socket client, BufferedWriter writer, String command, String subCommand) throws IOException {

        Integer index = null;

        if (subCommand != null) {
            Matcher matcher = cmdPattern.matcher(subCommand);
            if (matcher.matches()) {
                String a = matcher.group(1);
                String b = matcher.group(4);

                if ("id".equalsIgnoreCase(a)) {
                    if (b != null) {
                        index = Integer.valueOf(b);
                    }
                } else if ("id".equalsIgnoreCase(b)) {
                    if (a != null) {
                        index = Integer.valueOf(a);
                    }
                } else {
                    index = Integer.valueOf(subCommand);
                }
            }
        }

        // prepare input object
        SvdrpInput input = new SvdrpInput();
        input.setHost(getHostname(client));
        input.setSvdrp_command("LSTT");
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
            // no timers defined
            sendTimerError(writer, "no timers defined");
            return;
        } else {
            List<String> tim = new ArrayList<>();

            if (index == null) {
                timers.get().stream().forEach(s -> tim.add(s.getIndex() + " " + s.convertToVdrTimer()));
            } else {
                AtomicInteger ai = new AtomicInteger(index);
                timers.get().stream().filter(s -> s.getIndex() == ai.get())
                        .forEach(s -> tim.add(s.getIndex() + " " + s.convertToVdrTimer()));
            }

            try {
                send(writer, 250, tim);
            } catch (Exception e) {
                // bad
                log.error("send error", e);
                return;
            }
        }
    }
}

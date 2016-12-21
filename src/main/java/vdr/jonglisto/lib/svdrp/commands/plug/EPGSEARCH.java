package vdr.jonglisto.lib.svdrp.commands.plug;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.svdrp.commands.CommandBase;

public class EPGSEARCH extends CommandBase {

    private static Logger log = LoggerFactory.getLogger(EPGSEARCH.class);

    @Override
    public void doTheWork(Socket client, BufferedWriter writer, String command, String subCommand) throws IOException {
        switch (subCommand.toUpperCase()) {
        case "LSCC":
            // RemoteTimers needs this
            try {
                send(writer, 901, "no conflicts found");
            } catch (Exception e) {
                // very bad
                log.debug("send error", e);
            }
            break;

        default:
            doNothing(writer, command, subCommand);
            break;
        }
    }
}

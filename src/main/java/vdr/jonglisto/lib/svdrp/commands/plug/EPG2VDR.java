package vdr.jonglisto.lib.svdrp.commands.plug;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import vdr.jonglisto.lib.svdrp.commands.CommandBase;

public class EPG2VDR extends CommandBase {

    @Override
    public void doTheWork(Socket client, BufferedWriter writer, String command, String subCommand) throws IOException {
        
        switch (subCommand.toUpperCase()) {
        case "RELOAD":
            doNothing(writer, command, subCommand);
            break;
            
        case "STATE":
            doNothing(writer, command, subCommand);
            break;
            
        case "STOREINFO":
            doNothing(writer, command, subCommand);
            break;
            
        case "TIMERJOB":
            doNothing(writer, command, subCommand);
            break;
        
        case "UPDATE":
            doNothing(writer, command, subCommand);
            break;
            
        case "UPDREC":
            doNothing(writer, command, subCommand);
            break;
        }
    }
}



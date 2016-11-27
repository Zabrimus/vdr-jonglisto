package vdr.jonglisto.lib.svdrp.commands.plug;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import vdr.jonglisto.lib.svdrp.commands.CommandBase;

public class EPG2VDR extends CommandBase {

    @Override
    public void doTheWork(Socket client, BufferedWriter writer, List<String> args) throws IOException {
        String command = args.remove(0).toUpperCase();
        
        switch (command) {
        case "RELOAD":
            doNothing("EPG2VDR RELOAD", writer, args);
            break;
            
        case "STATE":
            doNothing("EPG2VDR STATE", writer, args);
            break;
            
        case "STOREINFO":
            doNothing("EPG2VDR STOREINFO", writer, args);
            break;
            
        case "TIMERJOB":
            doNothing("EPG2VDR TIMERJOB", writer, args);
            break;
        
        case "UPDATE":
            doNothing("EPG2VDR UPDATE", writer, args);
            break;
            
        case "UPDREC":
            doNothing("EPG2VDR UPDREC", writer, args);
            break;
        }
    }
}



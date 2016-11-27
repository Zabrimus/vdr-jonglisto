package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import vdr.jonglisto.lib.svdrp.OsdProviderCache;

public class HITK extends CommandBase {

    @Override
    public void doTheWork(Socket client, BufferedWriter writer, List<String> args) throws IOException {
        String key = args.remove(0).toUpperCase();
        
        OsdProviderCache.getOsdProvider(client).processKey(key);

        writer.write("250 okay\n");
        writer.flush();
    }
}

package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import vdr.jonglisto.lib.svdrp.OsdProviderCache;

public class HITK extends CommandBase {

    @Override
    public void doTheWork(Socket client, BufferedWriter writer, String command, String subCommand) throws IOException {
        String key = subCommand.toUpperCase();

        OsdProviderCache.getOsdProvider(client).processKey(key);

        writer.write("250 okay\n");
        writer.flush();
    }
}

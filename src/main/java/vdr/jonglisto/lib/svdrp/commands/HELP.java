package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class HELP extends CommandBase {

    @Override
    public void doTheWork(Socket client, BufferedWriter writer, List<String> args) throws IOException {
        doNothing("HELP", writer, args);
    }
}

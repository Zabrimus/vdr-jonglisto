package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedWriter;
import java.net.Socket;
import java.util.List;

public interface Command {
    
    public void doTheWork(Socket client, BufferedWriter writer, List<String> args) throws Exception;

    public void printHelp(String command, String subCommandName, BufferedWriter writer) throws Exception;
}

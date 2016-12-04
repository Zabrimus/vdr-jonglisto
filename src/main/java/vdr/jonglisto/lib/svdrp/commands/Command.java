package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedWriter;
import java.net.Socket;

public interface Command {
    
    public void doTheWork(Socket client, BufferedWriter writer, String command, String subCommand) throws Exception;

    public void printHelp(String command, String subCommandName, BufferedWriter writer) throws Exception;
}

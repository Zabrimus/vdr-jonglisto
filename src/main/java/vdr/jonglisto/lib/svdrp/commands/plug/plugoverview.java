/**
 * This is only a dummy class, which exists only for show help info. 
 */
package vdr.jonglisto.lib.svdrp.commands.plug;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import vdr.jonglisto.lib.svdrp.commands.CommandBase;

public class plugoverview extends CommandBase {

    @Override
    public void doTheWork(Socket client, BufferedWriter writer, List<String> args) throws IOException {
    }
}



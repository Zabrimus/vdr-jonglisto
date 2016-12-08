package vdr.jonglisto.lib.svdrp.client;

import org.hampelratte.svdrp.Command;

public class GenericCommand extends Command {
    private static final long serialVersionUID = 1L;

    private String command;
    
    public GenericCommand(String command) {
        this.command = command;
    }
    
    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "Command: " + command;
    }
    
}

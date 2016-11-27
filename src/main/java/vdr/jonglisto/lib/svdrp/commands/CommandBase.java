package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class CommandBase implements Command {

    public void doNothing(String command, BufferedWriter writer, List<String> args) throws IOException {
        String response = createResponse(214, Arrays.asList("This is Jonglisto version 0.0.2", "End of " + command + " info: " + args));
        writer.write(response);
        writer.flush();
    }

    public void printHelp(String command, String subCommandName, BufferedWriter writer) throws Exception {
        String helpFile = "/vdr/jonglisto/lib/svdrp/commands/help/" + command + (subCommandName == null ? "" : "." + subCommandName) + ".txt";
        InputStream input = getClass().getResourceAsStream(helpFile);

        List<String> helpText = new ArrayList<>();
        helpText.add("This is Jonglisto version 0.0.2");
        
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            helpText.addAll(buffer.lines().collect(Collectors.toList()));
        }
        
        helpText.add("End of " + command + " info");
        send(writer, 214, helpText);
    }

    protected void sendOkay(BufferedWriter writer) throws Exception {
        send(writer, 250, "Angeforderte Aktion okay, beendet");
    }
    
    protected void send(BufferedWriter writer, int replyCode, String reply) throws Exception {
        writer.write(createResponse(replyCode, reply));
        writer.flush();
    }

    protected void send(BufferedWriter writer, int replyCode, List<String> replies) throws Exception {
        writer.write(createResponse(replyCode, replies));
        writer.flush();
    }

    protected String createResponse(int replyCode, String item) {
        return createResponse(replyCode, Collections.singletonList(item));
    }

    protected String createResponse(int replyCode, List<String> replies) {
        AtomicInteger idx = new AtomicInteger();
        String res = replies.stream() //                
                .map(s -> s.replaceAll("\n", "|").trim()) // Dies sollte doch nur bei Text notwendig sein, oder?
                .map(s2 -> new StringBuilder().append(replyCode).append(idx.getAndIncrement() == replies.size() - 1 ? " " : "-").append(s2).toString()) //
                .collect(Collectors.joining("\n"));
        
        res = res + "\n";

        return res;
    }
}

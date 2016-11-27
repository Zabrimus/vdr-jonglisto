package vdr.jonglisto.lib.svdrp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.svdrp.commands.Command;

public class Handler implements Runnable {

    private Logger log = LoggerFactory.getLogger(Handler.class);

    private final Socket client;

    public Handler(Socket client) {
        this.client = client;
    }

    public void run() {
        log.debug("New connection socket: " + client + ", " + client.getInetAddress().getHostAddress());

        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

            // send greeting
            writer.write("220 jonglisto SVDRP VideoDiskRecorder 2.4.0; Sun Dec 31 10:00:00 2016; UTF-8\n");
            writer.flush();

            String input;
            while ((input = in.readLine()) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Received command from " + Thread.currentThread().getName() + " : " + input);
                }

                List<String> args = Arrays.stream(input.split(" ")).collect(Collectors.toList());

                String commandName = args.remove(0).toUpperCase();
                String subCommandName = null;

                // special cases
                if ("QUIT".equals(commandName)) {
                    writer.write("221 jonglisto closing connection\n");
                    writer.flush();
                    writer.close();
                    in.close();
                    client.close();

                    return;
                }

                String packageName = "vdr.jonglisto.lib.svdrp.commands.";
                boolean callHelp = false;
                
                if ("PLUG".equals(commandName)) {
                    packageName = packageName + "plug.";
                    try {
                        commandName = args.remove(0).toUpperCase();
                    } catch (Exception e) {
                        commandName = "plugoverview";
                    }
                    
                    if (args.size() == 0) {
                        callHelp = true;
                    } else if ((args.size() >= 1) && "HELP".equals(args.get(0).toUpperCase())) {
                        if (args.size() >= 2) {
                            subCommandName = args.get(1).toUpperCase();
                        }
                        
                        callHelp = true;
                    }
                } else if ("HELP".equals(commandName)) {
                    if (args.size() > 0) {
                        commandName = args.get(0).toUpperCase();
                        callHelp = true;
                    }                    
                }

                Command cmd = null;

                try {
                    cmd = (Command) Class.forName(packageName + commandName).newInstance();
                } catch (Exception e) {
                    log.error("Exception:", e);
                    // class not found or not of type Command
                    // ignore command
                    if (callHelp) {
                        writer.write("504 HELP topic \"" + commandName + "\" unknown\n");
                    } else {
                        writer.write("502- Command " + commandName + " not implemented\n");
                    }

                    writer.flush();
                }

                if (cmd != null) {
                    try {
                        if (callHelp) {
                            cmd.printHelp(commandName, subCommandName, writer);
                        } else {
                            cmd.doTheWork(client, writer, args);
                        }
                    } catch (IOException ioException) {
                        // do nothing more
                        log.info("IOException", ioException);
                        client.close();
                        break;
                    }
                }
            }

            writer.write("221 jonglisto closing connection");
            client.close();
        } catch (Exception e) {
            log.error("Error", e);
        }
    }
}
package vdr.jonglisto.lib.svdrp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.svdrp.commands.Command;

public class Handler implements Runnable {

    private static Logger log = LoggerFactory.getLogger(Handler.class);

    // Group 1: Command
    // Group 4: Arguments
    private static Pattern cmdPattern = Pattern.compile("^(.*?)(( )+(.*?))?$");

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

                // List<String> args = Arrays.stream(input.split("
                // ")).collect(Collectors.toList());
                String[] args = splitCmd(input);
                String commandName = args[0].toUpperCase();
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
                        args = splitCmd(args[1]);
                        commandName = args[0].toUpperCase();
                    } catch (Exception e) {
                        commandName = "plugoverview";
                    }

                    if (args[0] == null) {
                        callHelp = true;
                    } else if ((args[1] != null) && "HELP".equals(args[0].toUpperCase())) {
                        subCommandName = args[1].toUpperCase();
                        callHelp = true;
                    }
                } else if ("HELP".equals(commandName)) {
                    if (args[1] != null) {
                        commandName = args[1].toUpperCase();
                    }
                    callHelp = true;
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
                            log.debug("Args: " + args[0] + " -> " + args[1]);
                            cmd.doTheWork(client, writer, args[0], args[1]);
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

    private String[] splitCmd(String cmd) {
        String[] result = new String[2];
        Matcher matcher = cmdPattern.matcher(cmd);

        if (matcher.matches()) {
            result[0] = matcher.group(1);
            result[1] = matcher.group(4);
        }

        return result;
    }
}
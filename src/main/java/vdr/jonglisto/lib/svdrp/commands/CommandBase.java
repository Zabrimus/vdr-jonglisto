package vdr.jonglisto.lib.svdrp.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.SvdrpNashornService;
import vdr.jonglisto.lib.VdrDataService;
import vdr.jonglisto.lib.impl.ConfigurationServiceImpl;
import vdr.jonglisto.lib.impl.SvdrpNashornServiceImpl;
import vdr.jonglisto.lib.impl.VdrDataServiceImpl;
import vdr.jonglisto.lib.model.Timer;
import vdr.jonglisto.lib.model.js.SvdrpInput;
import vdr.jonglisto.lib.model.js.SvdrpOutput;
import vdr.jonglisto.lib.util.Constants;

public abstract class CommandBase implements Command {

    private static Logger log = LoggerFactory.getLogger(CommandBase.class);

    // Group 1: Command
    // Group 4: Arguments
    protected static Pattern cmdPattern = Pattern.compile("^(.*?)(( )+(.*?))?$");

    protected SvdrpNashornService nashorn = new SvdrpNashornServiceImpl();
    protected VdrDataService vdrService = new VdrDataServiceImpl();
    protected ConfigurationService configurationService = new ConfigurationServiceImpl();

    public void doTheWork(Socket client, BufferedWriter writer, String command, String subCommand) throws IOException {
        doNothing(writer, command, subCommand);
    }

    public void doNothing(BufferedWriter writer, String command, String subCommand) throws IOException {
        String response = createResponse(214, Arrays.asList(
                "This is Jonglisto version " + Constants.version + ", End of " + command + " info: " + subCommand));

        writer.write(response);
        writer.flush();
    }

    public void printHelp(String command, String subCommandName, BufferedWriter writer) throws Exception {
        String helpFile = "/vdr/jonglisto/lib/svdrp/commands/help/" + command
                + (subCommandName == null ? "" : "." + subCommandName) + ".txt";
        InputStream input = getClass().getResourceAsStream(helpFile);

        List<String> helpText = new ArrayList<>();
        helpText.add("This is Jonglisto version " + Constants.version);

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            helpText.addAll(buffer.lines().collect(Collectors.toList()));
        }

        helpText.add("End of " + command + " info");
        send(writer, 214, helpText);
    }

    protected void sendOkay(BufferedWriter writer) throws Exception {
        send(writer, 250, "Angeforderte Aktion okay, beendet");
    }

    protected void send(BufferedWriter writer, int replyCode, String reply) {
        try {
            writer.write(createResponse(replyCode, reply));
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void send(BufferedWriter writer, int replyCode, List<String> replies) {
        try {
            writer.write(createResponse(replyCode, replies));
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String createResponse(int replyCode, String item) {
        return createResponse(replyCode, Collections.singletonList(item));
    }

    protected String createResponse(int replyCode, List<String> replies) {
        AtomicInteger idx = new AtomicInteger();
        String res = replies.stream() //
                .map(s -> s.replaceAll("\n", "|").trim())
                .map(s2 -> new StringBuilder().append(replyCode)
                        .append(idx.getAndIncrement() == replies.size() - 1 ? " " : "-").append(s2).toString()) //
                .collect(Collectors.joining("\n"));

        res = res + "\n";

        return res;
    }

    protected String getHostname(Socket client) {
        return client.getInetAddress().getCanonicalHostName();
    }

    protected SvdrpOutput updateTimerByScript(Socket client, Timer timer, String command) {
        // prepare input object
        SvdrpInput input = new SvdrpInput();
        input.setHost(getHostname(client));
        input.setSvdrp_command(command);
        input.setSvdrp_param(null);
        input.setTimer_aux(timer.getAux());
        input.setTimer_filename(timer.getFilename());

        // call script and get result
        SvdrpOutput output;
        try {
            output = nashorn.callTimerScript(input);
        } catch (NoSuchMethodException | ScriptException e) {
            // do nothing here
            log.error("script error", e);
            return null;
        }

        // replace input values with output
        if (StringUtils.isNotEmpty(output.getTimer_aux())) {
            timer.setAux(output.getTimer_aux());
        }

        if (StringUtils.isNotEmpty(output.getTimer_filename())) {
            timer.setFilename(output.getTimer_filename());
        }
        return output;
    }

    protected void sendTimerError(BufferedWriter writer, String message) {
        try {
            send(writer, 550, message);
        } catch (Exception ex) {
            // ignore this
            log.error("Unknown error: ", ex);
        }
    }

}

package vdr.jonglisto.web.components;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Submit;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.hampelratte.svdrp.Connection;
import org.hampelratte.svdrp.Response;
import org.slf4j.Logger;

import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.svdrp.client.GenericCommand;
import vdr.jonglisto.web.encoder.VDREncoder;

public class SvdrpConsole extends BaseComponent {

    @Inject
    Logger log;

    @Inject
    protected AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    protected JavaScriptSupport javaScriptSupport;

    @Inject
    private SelectModelFactory selectModelFactory;

    @InjectComponent
    private Zone consoleZone;

    @InjectComponent
    @Property
    private Form consoleForm;

    @InjectComponent
    @Property
    private Submit defaultSubmit;

    @InjectComponent
    @Property
    private TextField svdrpInput;

    @Persist
    @Property
    private SelectModel vdrModel;

    @Persist
    @Property
    private VDR selectedVdr;

    @Persist
    @Property
    private VDREncoder vdrEncoder;

    @Property
    private String svdrpCommand;

    void setupRender() {
        List<VDR> v = configuration.getSortedVdrList();
        vdrModel = selectModelFactory.create(v, "displayName");
        vdrEncoder = new VDREncoder(v);
    }

    void onSuccess() {
        List<String> result = executeCommand();

        if (request.isXHR()) {
            ajaxResponseRenderer.addCallback(appendToLog(result));
        }
    }

    public JavaScriptCallback appendToLog(List<String> text) {
        return new JavaScriptCallback() {

            public void run(JavaScriptSupport javascriptSupport) {
                javaScriptSupport.require("appendconsole").with(escape(svdrpCommand), "svdrpconsole", "svdrpinput",
                        JSONArray.from(text));
            }
        };
    }

    public List<String> executeCommand() {
        Connection con = null;
        List<String> result = new ArrayList<>();

        try {
            con = new Connection(selectedVdr.getIp(), selectedVdr.getSvdrpPort(), 1500);
            Response resp = con.send(new GenericCommand(svdrpCommand));
            Arrays.stream(resp.getMessage().split("\n")).forEach(s -> result.add(escape(s)));
            return result;
        } catch (ConnectException ce) {
            result.add("Unable to connect to " + selectedVdr.getIp() + ":" + selectedVdr.getSvdrpPort());
        } catch (IOException e) {
            result.add("Unknown error: " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (IOException e) {
                    // ignore this error
                }
            }
        }

        return result;
    }

    private String escape(String s) {
        return StringEscapeUtils.escapeHtml4(s).replaceAll(" ", "&nbsp;").replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
    }
}

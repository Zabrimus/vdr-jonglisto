package vdr.jonglisto.lib.impl;

import java.io.FileNotFoundException;

import javax.script.ScriptException;

import vdr.jonglisto.lib.SvdrpNashornService;
import vdr.jonglisto.lib.model.js.SvdrpInput;
import vdr.jonglisto.lib.model.js.SvdrpOutput;

public class SvdrpNashornServiceImpl extends NashornServiceImpl implements SvdrpNashornService {

    public SvdrpNashornServiceImpl() {
        try {
            initScript(configuration.getSvdrpScript());
        } catch (FileNotFoundException | ScriptException e) {
            throw new RuntimeException("unable to init SvdrpNashornService", e);
        }
    }

    public SvdrpOutput callTimerScript(SvdrpInput tInput) throws NoSuchMethodException, ScriptException {
        return mapToObject(callFunction("preprocessTimer", tInput), SvdrpOutput.class);
    }
    
    public SvdrpOutput callRouteScript(SvdrpInput tInput) throws NoSuchMethodException, ScriptException {
        return mapToObject(callFunction("route", tInput), SvdrpOutput.class);
    }
    
    public SvdrpOutput callFilterScript(SvdrpInput tInput) throws NoSuchMethodException, ScriptException {
        return mapToObject(callFunction("filter", tInput), SvdrpOutput.class);
    }
}

package vdr.jonglisto.lib;

import javax.script.ScriptException;

import vdr.jonglisto.lib.model.js.SvdrpInput;
import vdr.jonglisto.lib.model.js.SvdrpOutput;

public interface SvdrpNashornService {

    public SvdrpOutput callTimerScript(SvdrpInput tInput) throws NoSuchMethodException, ScriptException;

    public SvdrpOutput callRouteScript(SvdrpInput tInput) throws NoSuchMethodException, ScriptException;

    public SvdrpOutput callFilterScript(SvdrpInput tInput) throws NoSuchMethodException, ScriptException;
}

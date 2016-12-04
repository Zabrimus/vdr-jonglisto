package vdr.jonglisto.lib.impl;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import vdr.jonglisto.lib.Epg2VdrNashornService;

public class Epg2VdrNashornServiceImpl extends NashornServiceImpl implements Epg2VdrNashornService {
   
    public Epg2VdrNashornServiceImpl() {
        try {
            initScript(configuration.getEpg2VdrScript());
        } catch (FileNotFoundException | ScriptException e) {
            throw new RuntimeException("unable to init SvdrpNashornService", e);
        }
    }
    
    @Override
    public List<Map<String, Object>> callGetOsdList() throws NoSuchMethodException, ScriptException {
        return callNashornFunctionArray("getOsd", null);
    }
}

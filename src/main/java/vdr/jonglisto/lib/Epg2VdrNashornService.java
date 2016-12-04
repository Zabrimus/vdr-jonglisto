package vdr.jonglisto.lib;

import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

public interface Epg2VdrNashornService {

    public List<Map<String, Object>>  callGetOsdList() throws NoSuchMethodException, ScriptException;
    
}

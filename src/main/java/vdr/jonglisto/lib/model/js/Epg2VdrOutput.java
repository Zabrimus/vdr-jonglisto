package vdr.jonglisto.lib.model.js;

import java.util.List;
import java.util.Map;

public class Epg2VdrOutput {

    public List<Map<String, String>> result;
    
    public Epg2VdrOutput() {        
    }

    public List<Map<String, String>> getResult() {
        return result;
    }
    
    public void setResult(List<Map<String, String>> result) {
        this.result = result;
    }
}

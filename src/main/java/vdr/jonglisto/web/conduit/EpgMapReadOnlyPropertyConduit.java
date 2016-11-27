package vdr.jonglisto.web.conduit;

import java.util.Map;

public class EpgMapReadOnlyPropertyConduit extends MapPropertyConduit {

    public EpgMapReadOnlyPropertyConduit(String key, Class<?> type) {
        super(key, type);
    }

    public Object get(Object instance) {
        // return only values which are well known
        
        if (key.equals("time_from_to")) {
            return (((Map<?, ?>)instance).get("v_starttime") + " - " + ((Map<?, ?>)instance).get("v_endtime")); 
        }
        
        return super.get(instance);
    }

    public void set(Object instance, Object value) {
        // do nothing, read-only
    }
}
package vdr.jonglisto.lib.svdrp;

import vdr.jonglisto.lib.model.osd.TextOsd;

public interface OsdProvider {
       
    public TextOsd getOsd();
    public void processKey(String key);
}

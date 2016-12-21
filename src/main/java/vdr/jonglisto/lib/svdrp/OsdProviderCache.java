package vdr.jonglisto.lib.svdrp;

import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import vdr.jonglisto.lib.svdrp.osdimpl.JonglistoOsdProvider;

public class OsdProviderCache {

    // one OSD per IP
    private static Map<String, OsdProvider> osdCache = Collections.synchronizedMap(new HashMap<>());

    // Disable construction
    private OsdProviderCache() {
    }

    // get osd by client ip
    public static OsdProvider getOsdProvider(Socket client) {
        return osdCache.computeIfAbsent(client.getInetAddress().getHostAddress(),
                s -> new JonglistoOsdProvider(client));
    }

    public static void changeOsdProvider(Socket client, OsdProvider newProvider) {
        osdCache.put(client.getInetAddress().getHostAddress(), newProvider);
    }
}

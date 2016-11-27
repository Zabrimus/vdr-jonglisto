package vdr.jonglisto.lib.svdrp.osdimpl;

import java.net.Socket;

import vdr.jonglisto.lib.internal.Configuration;
import vdr.jonglisto.lib.model.osd.OsdItem;
import vdr.jonglisto.lib.model.osd.TextOsd;
import vdr.jonglisto.lib.svdrp.OsdProvider;
import vdr.jonglisto.lib.svdrp.OsdProviderCache;

public class JonglistoOsdProvider implements OsdProvider {

    private enum Type {
        DEFAULT, REMOTEOSD
    }
    
    private TextOsd osd;
    private Socket client;
    
    private Configuration configuration = Configuration.getInstance();
    
    private Type type;

    public JonglistoOsdProvider(Socket client) {
        this.client = client;
    }

    @Override
    public TextOsd getOsd() {
        return osd;
    }

    @Override
    public void processKey(String key) {
        switch (key) {
        case "MENU":
            if (osd == null) {
                createDefaultOsd();
            } else {
                // close OSD
                osd = null;
            }
            break;

        case "UP":
            if (osd != null) {
                osd.moveSelected(-1);
            }
            break;

        case "DOWN":
            if (osd != null) {
                osd.moveSelected(1);
            }
            break;

        case "RIGHT":
            if (osd != null) {
                osd.moveSelected(5);
            }
            break;

        case "LEFT":
            if (osd != null) {
                osd.moveSelected(-5);
            }
            break;

        case "OK":
            switch (getSelectedAction()) {
            case "RemoteOSD":
                createRemoteOsd();
                break;
                
            case "ShowRemote":
                // create and register new Provider
                OsdProvider osdProvider = new RemoteOsdProvider(client, getSelectedSubAction());
                OsdProviderCache.changeOsdProvider(client, osdProvider);
                break;
            }

            break;
            
        case "RED":
            if (type == Type.REMOTEOSD) {
                createDefaultOsd();
            }

        case "GREEN":
            break;
            
        case "YELLOW":
            break;
            
        case "BLUE":
            break;
        }
    }

    private String getSelectedAction() {
        return osd.getItems().stream().filter(s -> s.isSelected()).findFirst().get().getActionName();
    }

    private String getSelectedSubAction() {
        return osd.getItems().stream().filter(s -> s.isSelected()).findFirst().get().getSubActionName();
    }

    private void createDefaultOsd() {
        type = Type.DEFAULT;
        
        osd  = new TextOsd();
        osd.setTitle("Jonglisto");
        osd.getItems().add(new OsdItem("Remote OSD", true, "RemoteOSD", null));
        osd.getItems().get(0).setSelected(true);
    }

    private void createRemoteOsd() {
        type = Type.REMOTEOSD;
        
        osd = new TextOsd();
        osd.setTitle("Jonglisto");
        osd.setRed("Zurück");

        configuration.getConfiguredVdr().entrySet().stream().forEach(s -> {
            osd.getItems().add(new OsdItem("Osd von " + s.getValue().getDisplayName(), false, "ShowRemote", s.getValue().getUuid()));
        });

        osd.getItems().get(0).setSelected(true);
    }
}

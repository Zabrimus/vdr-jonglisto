package vdr.jonglisto.lib.svdrp.osdimpl;

import java.net.Socket;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vdr.jonglisto.lib.Epg2VdrNashornService;
import vdr.jonglisto.lib.EpgDataService;
import vdr.jonglisto.lib.impl.Epg2VdrNashornServiceImpl;
import vdr.jonglisto.lib.impl.EpgDataServiceImpl;
import vdr.jonglisto.lib.internal.Configuration;
import vdr.jonglisto.lib.model.osd.OsdItem;
import vdr.jonglisto.lib.model.osd.TextOsd;
import vdr.jonglisto.lib.svdrp.OsdProvider;
import vdr.jonglisto.lib.svdrp.OsdProviderCache;

public class JonglistoOsdProvider implements OsdProvider {

    private static Logger log = LoggerFactory.getLogger(JonglistoOsdProvider.class);

    private enum Type {
        DEFAULT, REMOTEOSD, EPG2VDR, EPG2VDR_RESULT
    }

    private TextOsd osd;
    private Socket client;

    private static Configuration configuration = Configuration.getInstance();
    private static Epg2VdrNashornService epg2vdrScript = new Epg2VdrNashornServiceImpl();
    private static EpgDataService epgDataService = new EpgDataServiceImpl();

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

            case "ShowEpg2Vdr":
                createEpg2VdrOsd();
                break;

            case "Epg2Vdr":
                createEpg2VdrResultOsd(getSelectedSubAction());
                break;
            }

            break;

        case "RED":
            if ((type == Type.REMOTEOSD) || (type == Type.EPG2VDR)) {
                createDefaultOsd();
            } else if (type == Type.EPG2VDR_RESULT) {
                createEpg2VdrOsd();
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

        osd = new TextOsd();
        osd.setTitle("Jonglisto");
        osd.getItems().add(new OsdItem("Remote OSD", false, "RemoteOSD", null));
        osd.getItems().add(new OsdItem("Epg2Vdr", false, "ShowEpg2Vdr", null));
        osd.getItems().get(0).setSelected(true);
    }

    private void createRemoteOsd() {
        type = Type.REMOTEOSD;

        osd = new TextOsd();
        osd.setTitle("Jonglisto");
        osd.setRed("Zurück");

        configuration.getConfiguredVdr().entrySet().stream().forEach(s -> {
            osd.getItems().add(new OsdItem("Osd von " + s.getValue().getDisplayName(), false, "ShowRemote",
                    s.getValue().getUuid()));
        });

        osd.getItems().get(0).setSelected(true);
    }

    private void createEpg2VdrOsd() {
        type = Type.EPG2VDR;

        osd = new TextOsd();
        osd.setTitle("Epg2Vdr");
        osd.setRed("Zurück");

        try {
            List<Map<String, Object>> osdList = epg2vdrScript.callGetOsdList();

            osdList.stream().forEach(s -> {
                osd.getItems().add(new OsdItem((String) s.get("display"), false, "Epg2Vdr", (String) s.get("sql")));
            });

            osd.getItems().get(0).setSelected(true);
        } catch (NoSuchMethodException | ScriptException e) {
            // do not generate any entry
            log.error("Script arror", e);
        }
    }

    private void createEpg2VdrResultOsd(String sql) {
        type = Type.EPG2VDR_RESULT;

        osd = new TextOsd();
        osd.setTitle("Epg2Vdr Egebnisse");
        osd.setRed("Zurück");

        List<Map<String, Object>> result = epgDataService.selectGeneric(sql);

        result.stream().forEach(s -> {
            StringBuilder value = new StringBuilder();

            for (int i = 1; i <= s.size(); ++i) {
                value.append(s.get(String.valueOf(i))).append("\t");
            }

            value.append("");

            osd.getItems().add(new OsdItem(value.toString(), false, "noaction", null));
        });

        osd.getItems().get(0).setSelected(true);
    }
}

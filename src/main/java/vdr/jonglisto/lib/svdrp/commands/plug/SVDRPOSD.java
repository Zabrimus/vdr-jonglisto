package vdr.jonglisto.lib.svdrp.commands.plug;

import java.io.BufferedWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import vdr.jonglisto.lib.model.osd.OsdItem;
import vdr.jonglisto.lib.model.osd.TextOsd;
import vdr.jonglisto.lib.svdrp.OsdProvider;
import vdr.jonglisto.lib.svdrp.OsdProviderCache;
import vdr.jonglisto.lib.svdrp.commands.CommandBase;

public class SVDRPOSD extends CommandBase {

    public void doTheWork(Socket client, BufferedWriter writer, List<String> args) throws Exception {
        String command = args.remove(0).toUpperCase();

        OsdProvider osd = OsdProviderCache.getOsdProvider(client);

        switch (command) {
        case "LSTO":
            send(writer, 920, createOsd(osd));
            break;

        case "OSDH":
            if ((osd.getOsd() == null) || (osd.getOsd().getTitle() == null)) {
                send(writer, 930, "empty title");
            } else {
                List<String> osdList = new ArrayList<String>();
                addColorKeys(osdList, osd.getOsd());
                send(writer, 920, osdList);
            }

            break;

        case "OSDI":
            if ((osd.getOsd() == null) || (osd.getOsd().getTitle() == null)) {
                send(writer, 930, "empty items");
            } else {
                List<String> osdList = new ArrayList<String>();
                addItems(osdList, osd.getOsd().getItems());
                send(writer, 920, osdList);
            }

            break;

        case "OSDM":
            if ((osd.getOsd() == null) || (osd.getOsd().getMessage() == null)) {
                send(writer, 930, "empty message");
            } else {
                send(writer, 920, osd.getOsd().getMessage());
            }

            break;

        case "OSDT":
            if ((osd.getOsd() == null) || (osd.getOsd().getTitle() == null)) {
                send(writer, 930, "empty title");
            } else {
                send(writer, 920, osd.getOsd().getTitle());
            }

            break;

        case "OSDX":
            if ((osd.getOsd() == null) || (osd.getOsd().getTextBlock() == null)) {
                send(writer, 930, "empty text");
            } else {
                send(writer, 920, osd.getOsd().getTextBlock());
            }

            break;
        }
    }

    private List<String> createOsd(OsdProvider osd) {
        TextOsd textOsd = osd.getOsd();

        List<String> osdList = new ArrayList<String>();
        osdList.add("T:" + textOsd.getTitle());

        addItems(osdList, textOsd.getItems());
        addColorKeys(osdList, textOsd);

        if (textOsd.getTextBlock() != null) {
            osdList.add("X:" + textOsd.getTextBlock());
        }

        if (textOsd.getMessage() != null) {
            osdList.add("M:" + textOsd.getMessage());
        }

        return osdList;
    }

    private void addColorKeys(List<String> osdList, TextOsd textOsd) {
        if (textOsd.getRed() != null) {
            osdList.add("R:" + textOsd.getRed());
        }

        if (textOsd.getGreen() != null) {
            osdList.add("G:" + textOsd.getGreen());
        }

        if (textOsd.getYellow() != null) {
            osdList.add("Y:" + textOsd.getYellow());
        }

        if (textOsd.getBlue() != null) {
            osdList.add("B:" + textOsd.getBlue());
        }
    }

    private void addItems(List<String> osdList, List<OsdItem> items) {
        addColumnLayout(osdList, items);
        
        // check, if this is a text block or a selectable list
        if ((items.size() == 1) && (items.get(0).getContent().contains("\n"))) {
            osdList.add("X:" + items.get(0).getContent().replaceAll("\n", "|"));            
        } else {
            items.stream().forEach(s -> osdList
                    .add(new StringBuilder().append(s.isSelected() ? "S:" : "I:").append(s.getContent()).toString()));
        }
    }

    private void addColumnLayout(List<String> osdList, List<OsdItem> items) {
        int tabs[] = new int[8];

        AtomicInteger i = new AtomicInteger();
        AtomicInteger idx = new AtomicInteger();

        items.stream().forEach(item -> {
            idx.set(0);
            i.set(0);

            item.getContent().chars().forEach(c -> {
                i.incrementAndGet();
                if ((char) c == '\t') {
                    tabs[idx.get()] = Math.max(tabs[idx.get()], i.get());
                    i.set(0);
                    idx.getAndIncrement();
                }
            });
        });
        
        Arrays.stream(tabs).filter(s -> s != 0).forEach(t -> osdList.add("C:" + t));
    }
}

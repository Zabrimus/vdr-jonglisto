package vdr.jonglisto.web.components;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;

import vdr.jonglisto.lib.util.EpgSorter;
import vdr.jonglisto.web.conduit.MapPropertyConduit;

public class ProgramChannelEpg extends ProgramBaseEpg {

    @Property
    private BeanModel<Object> epgModel;

    @Persist
    @Property
    private List<Map<String, Object>> epgData;

    private String lastLongDate;

    public ProgramChannelEpg() {
        epgModel = beanModelSource.createDisplayModel(Object.class, messages);
        epgModel.addEmpty("time");
        epgModel.addEmpty("title");
        epgModel.add("season", new MapPropertyConduit("season", String.class));
        epgModel.add("part", new MapPropertyConduit("part", String.class));
        epgModel.add("genre", new MapPropertyConduit("genre", String.class));
        epgModel.add("category", new MapPropertyConduit("category", String.class));
        epgModel.addEmpty("channelid");
        epgModel.addEmpty("action");
    }

    @OnEvent(value = "updateEpg")
    public void updateEpg() {
        epgData = epgDataService.getEpgChannelData(epgCriteria);

        Set<String> genres = new HashSet<>();
        epgData.stream().filter(s -> s.get("genre") != null).forEach(s -> genres.add((String) s.get("genre")));

        Set<String> categories = new HashSet<>();
        epgData.stream().filter(s -> s.get("category") != null)
                .forEach(s -> categories.add((String) s.get("category")));

        epglayout.updateData(genres.stream().sorted(String::compareTo).collect(Collectors.toList()),
                categories.stream().sorted(String::compareTo).collect(Collectors.toList()));

        epgSorter = new EpgSorter(epglayout.getChannels());

        ajaxResponseRenderer.addRender(epgListZone);
    }

    public String getChannelName() {
        if ((epgData != null) && (epgData.size() > 0)) {
            return findChannelName(epgData.get(0));
        } else {
            return "";
        }
    }

    public boolean isDateSwitch() {
        String currentLongDate = (String) epg.get("v_longstartdate");

        if ((lastLongDate == null) || !lastLongDate.equals(currentLongDate)) {
            lastLongDate = currentLongDate;
            return true;
        } else {
            return false;
        }
    }
}

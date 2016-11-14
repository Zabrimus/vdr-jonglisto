package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.util.EpgSorter;
import vdr.jonglisto.web.conduit.MapPropertyConduit;

public class ProgramDayEpg extends ProgramBaseEpg {

    @Property
    private BeanModel<Object> epgModel;

    @Persist
    @Property
    private List<List<Map<String, Object>>> epgData;

    @Property
    private List<Map<String, Object>> channelEpg;

    public ProgramDayEpg() {
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
        if (epglayout.getChannels() != null) {
            epgSorter = new EpgSorter(epglayout.getChannels());
        }

        Set<String> genres = new HashSet<>();
        Set<String> categories = new HashSet<>();

        epgData = new ArrayList<>();

        if (epgCriteria.getChannel() == null) {
            epglayout.getChannels().stream().forEach(s -> getEpgData(s, genres, categories));
            epgCriteria.setChannel(null);
        } else {
            getEpgData(epgCriteria.getChannel(), genres, categories);
        }

        epglayout.updateData(genres.stream().sorted(String::compareTo).collect(Collectors.toList()),
                categories.stream().sorted(String::compareTo).collect(Collectors.toList()));

        ajaxResponseRenderer.addRender(epgListZone);
    }

    public String getChannelName() {
        return findChannelName(channelEpg.get(0));
    }

    private void getEpgData(Channel channel, Set<String> genres, Set<String> categories) {
        epgCriteria.setChannel(channel);

        List<Map<String, Object>> data = epgDataService.getEpgDayData(epgCriteria);

        if (data.size() > 0) {
            epgData.add(data);

            data.stream().filter(d -> d.get("genre") != null).forEach(d -> genres.add((String) d.get("genre")));
            data.stream().filter(d -> d.get("category") != null)
                    .forEach(d -> categories.add((String) d.get("category")));
        }
    }
}

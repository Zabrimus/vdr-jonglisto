package vdr.jonglisto.web.components;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

import vdr.jonglisto.web.conduit.MapPropertyConduit;

public class SearchTimerResult { // extends ProgramBaseEpg {

    @Parameter
    @Property
    private List<Map<String, Object>> useIds;

    @Inject
    protected Messages messages;

    @Inject
    protected BeanModelSource beanModelSource;

    @Property
    private BeanModel<Object> epgModel;

    @Persist
    @Property
    protected Map<String, Object> epg;
        
    @Persist
    @Property
    private List<Map<String, Object>> epgData;

    public SearchTimerResult() {
        epgModel = beanModelSource.createDisplayModel(Object.class, messages);
        epgModel.addEmpty("channelid");
        epgModel.addEmpty("time");
        epgModel.addEmpty("title");
        epgModel.add("season", new MapPropertyConduit("season", String.class));
        epgModel.add("part", new MapPropertyConduit("part", String.class));
        epgModel.add("genre", new MapPropertyConduit("genre", String.class));
        epgModel.add("category", new MapPropertyConduit("category", String.class));
        epgModel.addEmpty("action");
    }

    public void beginRender() {
        epgData = Collections.emptyList();
        
        System.err.println("Size: " + useIds.size());
    }

    public Integer getStartTime() {
        return (Integer) epg.get("starttime");
    }

    public Long getEndTime() {
        return (Long) epg.get("endtime");
    }

    public String getChannelName() {
        return "Hallo";
    }

    public String getChannelId() {
        return epg != null ? (String) epg.get("channelid") : "";
    }

    public Long getUseId() {
        return (Long) epg.get("useid");
    }
}

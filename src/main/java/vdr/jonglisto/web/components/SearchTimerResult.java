package vdr.jonglisto.web.components;

import java.util.List;
import java.util.Map;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

import vdr.jonglisto.web.conduit.EpgMapReadOnlyPropertyConduit;
import vdr.jonglisto.web.conduit.MapPropertyConduit;

public class SearchTimerResult extends BaseComponent { 

    @Parameter
    @Property
    private List<Map<String, Object>> useIds;
    
    @InjectComponent
    protected Epg epg;    
    
    @Inject
    protected Messages messages;
    
    @Inject
    protected BeanModelSource beanModelSource;

    @Property
    private BeanModel<Object> epgModel;
    
    @Persist
    @Property
    protected Map<String, Object> epgMap;
    
    @Persist
    @Property
    private List<Map<String, Object>> epgData;
    
    @Property
    private Long epgDetailUseId;

    @Property
    private String epgDetailChannelName;
    
    public SearchTimerResult() {
        epgModel = beanModelSource.createDisplayModel(Object.class, messages);
        epgModel.add("channelId", new MapPropertyConduit("channelid", String.class));
        epgModel.add("date", new MapPropertyConduit("v_startdate", String.class));        
        epgModel.add("time_from_to", new EpgMapReadOnlyPropertyConduit("time_from_to", String.class));
        epgModel.addEmpty("title");
        epgModel.add("season", new MapPropertyConduit("season", String.class));
        epgModel.add("part", new MapPropertyConduit("part", String.class));
        epgModel.add("genre", new MapPropertyConduit("genre", String.class));
        epgModel.add("category", new MapPropertyConduit("category", String.class));
        //epgModel.addEmpty("action");
    }
    
    public void beginRender() {
        epgData = epgDataService.getEpgDataForUseIds(useIds); 
    }
    
    public String getChannelName() {
        return (String) useIds.stream().filter(s -> s.get("cnt_useid").equals(epgMap.get("useid"))).findFirst().get().get("channelname");
    }

    public String getChannelId() {
        return epgMap != null ? (String) epgMap.get("channelid") : "";
    }
    
    public Long getUseId() {
        return (Long) epgMap.get("useid");
    }
    
    public Integer getCount() {
        return useIds.size();
    }
    
    public void onShowEpg(Long useId, String channelName) {
        epgDetailUseId = useId;
        epgDetailChannelName = channelName;

        // function = Function.INFO;
        epg.showInfoZone();
    }
}

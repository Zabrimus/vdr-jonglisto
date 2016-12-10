package vdr.jonglisto.lib;

import java.util.List;
import java.util.Map;

import vdr.jonglisto.lib.model.channelmap.ChannelModel;
import vdr.jonglisto.lib.model.channelmap.IdModel;

public interface ChannelMapService {

    public void updateEpgIds(int providerId);

    public List<IdModel> getIds(int providerid);

    public List<ChannelModel> getChannelIds(String vdrUuid);

    public void addIncludeChannel(ChannelModel channel);

    public void removeIncludeChannel(String name);

    public Map<String, List<Object>> doAutoMapping(String vdrUuid);

    public Map<String, List<Object>> readMapping(String vdrUuid);

    public void saveMapping(Map<String, List<Object>> input);

    public String createEpgdMapping(String vdrUuid); 
    
    public void clearAll();
}

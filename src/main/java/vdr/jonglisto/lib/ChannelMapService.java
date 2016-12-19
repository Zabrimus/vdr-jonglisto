package vdr.jonglisto.lib;

import java.util.List;
import java.util.Map;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.channelmap.ChannelModel;
import vdr.jonglisto.lib.model.channelmap.IdModel;
import vdr.jonglisto.lib.model.channelmap.Provider;

public interface ChannelMapService {

    public void updateEpgIds(int providerId, String epgDataPin);

    public List<IdModel> getIds(int providerid);
    
    public List<String> getAllEpgIds();

    public List<ChannelModel> getChannelIds(String vdrUuid);

    public void addIncludeChannel(ChannelModel channel);

    public void removeIncludeChannel(String name);

    public void replaceIncludeChannel(List<String> channels);
    
    public List<ChannelModel> getIncludedChannels(String vdrUuid);
    
    public List<Channel> getIncludedVdrChannels(String vdrUuid);
    
    public Map<String, List<Object>> doAutoMapping(String vdrUuid);

    public Map<String, List<Object>> readMapping(String vdrUuid);

    public void saveMapping(Map<String, List<Object>> input);

    public String createEpgdMapping(String vdrUuid); 
    
    public Map<String, String> getNameMapping();
    
    public void clearAll();
    
    public void deleteNameMapping();

	public void saveProvider(Provider mainProv, Provider secProv);

	public Provider[] readProvider();
}

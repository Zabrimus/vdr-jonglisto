package vdr.jonglisto.lib;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.RecordingNamingMode;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria;

public interface EpgDataService {
		
	public Map<String, Object> getEpgDataForUseId(Long id);
	public Map<String, Object> getEpgDataForRecording(String recFilename);
	public Map<String, Object> getMediaIdsForUseId(Long id);
	
	public List<Map<String, Object>> getEpgData(Collection<Channel> channels, EpgSearchCriteria criteria);
	public List<Map<String, Object>> getEpgDayData(EpgSearchCriteria epgCriteria);	
	public List<Map<String, Object>> getEpgChannelData(EpgSearchCriteria epgCriteria);
	
	public String getVdrTimerName(Long id, RecordingNamingMode naming_mode);
}

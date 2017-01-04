package vdr.jonglisto.lib.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import vdr.jonglisto.lib.EpgDataService;
import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.RecordingNamingMode;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria;

public class HsqlDataServiceImpl extends ServiceBase implements EpgDataService {
    
    @Override
    public Map<String, Object> getEpgDataForUseId(Long id) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getEpgDataForUseIds(List<Map<String, Object>> useIds) {
        return null;
    }

    @Override
    public Map<String, Object> getEpgDataForRecording(String recFilename) {
        return null;
    }

    @Override
    public Map<String, Object> getMediaIdsForUseId(Long id) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getEpgData(Collection<Channel> channels, EpgSearchCriteria criteria) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getEpgDayData(EpgSearchCriteria epgCriteria) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getEpgChannelData(EpgSearchCriteria epgCriteria) {
        return null;
    }

    @Override
    public String getVdrTimerName(Long id, RecordingNamingMode naming_mode) {
        return null;
    }

    @Override
    public List<String> getGenres() {
        return null;
    }

    @Override
    public List<String> getCategories() {
        return null;
    }

    @Override
    public List<Map<String, Object>> selectGeneric(String sql) {
        return null;
    }
}

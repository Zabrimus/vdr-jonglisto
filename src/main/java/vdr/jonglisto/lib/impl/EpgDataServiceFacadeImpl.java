package vdr.jonglisto.lib.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import vdr.jonglisto.lib.EpgDataService;
import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.RecordingNamingMode;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria;

public class EpgDataServiceFacadeImpl extends ServiceBase implements EpgDataService {

    private EpgDataService service;
    
    public EpgDataServiceFacadeImpl() {
        if (configuration.isUseEpgd()) {
            service = new EpgdDataServiceImpl();
        } else {
            service = new HsqlDataServiceImpl();
        }
    }
    
    @Override
    public Map<String, Object> getEpgDataForUseId(Long id) {
        return service.getEpgDataForUseId(id);
    }

    @Override
    public List<Map<String, Object>> getEpgDataForUseIds(List<Map<String, Object>> useIds) {
        return service.getEpgDataForUseIds(useIds);
    }

    @Override
    public Map<String, Object> getEpgDataForRecording(String recFilename) {
        return service.getEpgDataForRecording(recFilename);
    }

    @Override
    public Map<String, Object> getMediaIdsForUseId(Long id) {
        return service.getMediaIdsForUseId(id);
    }

    @Override
    public List<Map<String, Object>> getEpgData(Collection<Channel> channels, EpgSearchCriteria criteria) {
        return service.getEpgData(channels, criteria);
    }

    @Override
    public List<Map<String, Object>> getEpgDayData(EpgSearchCriteria epgCriteria) {
        return service.getEpgDayData(epgCriteria);
    }

    @Override
    public List<Map<String, Object>> getEpgChannelData(EpgSearchCriteria epgCriteria) {
        return service.getEpgChannelData(epgCriteria);
    }

    @Override
    public String getVdrTimerName(Long id, RecordingNamingMode naming_mode) {
        return service.getVdrTimerName(id, naming_mode);
    }

    @Override
    public List<String> getGenres() {
        return service.getGenres();
    }

    @Override
    public List<String> getCategories() {
        return service.getCategories();
    }

    @Override
    public List<Map<String, Object>> selectGeneric(String sql) {
        return service.selectGeneric(sql);
    }

    @Override
    public void updateInternalEpgData(String vdrUuid) {
        service.updateInternalEpgData(vdrUuid);
    }
}

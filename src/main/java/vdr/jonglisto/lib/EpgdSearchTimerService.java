package vdr.jonglisto.lib;

import java.util.List;
import java.util.Map;

import vdr.jonglisto.lib.model.SearchTimer;
import vdr.jonglisto.lib.model.TimerEpg;

public interface EpgdSearchTimerService {

    public List<SearchTimer> getSearchTimers();

    public SearchTimer getSearchTimer(Long id);

    public void insertSearchTimer(SearchTimer timer);

    public void updateSearchTimer(SearchTimer timer);

    public void deleteSearchTimer(Long id);

    public void toggleActive(Long id);

    public List<Map<String, Object>> performSearch(SearchTimer timer);
    
    public TimerEpg getSearchTimerForEventId(Long id);
}

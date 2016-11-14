package vdr.jonglisto.web.components;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

import vdr.jonglisto.lib.model.search.EpgSearchCriteria;
import vdr.jonglisto.lib.util.EpgSorter;

public abstract class ProgramBaseEpg extends BaseComponent {

    @Persist
    @Property
    protected Map<String, Object> epg;

    @InjectComponent
    protected EpgLayout epglayout;

    @InjectComponent
    protected Zone epgListZone;

    @SessionAttribute("epgSearchCriteria")
    @Property
    protected EpgSearchCriteria epgCriteria;

    @Inject
    protected BeanModelSource beanModelSource;

    @Inject
    protected Messages messages;

    @Persist
    protected EpgSorter epgSorter;

    public String getChannelId() {
        return epg != null ? (String) epg.get("channelid") : "";
    }

    public BigInteger getEventId() {
        return epg != null ? (BigInteger) epg.get("eventid") : BigInteger.valueOf(0);
    }

    public BigInteger getImageId() {
        return epg != null ? (BigInteger) epg.get("imageid") : BigInteger.valueOf(0);
    }

    public Long getUseId() {
        return epg != null ? (Long) epg.get("useid") : 0;
    }

    public void onSelectGenre(String genre) {
        epglayout.onValueChangedFromGenre(genre);
    }

    public void onSelectCategory(String category) {
        epglayout.onValueChangedFromCategory(category);
    }

    public void onSwitchChannel(String channelId) {
        commandService.switchChannel(getHeadUuid(), channelId);
    }

    @OnEvent(value = "showEpg")
    public void showEpg(Long useId, String channelName) {
        epglayout.onShowEpg(useId, channelName);
    }

    public boolean isRunning() {
        if (epg != null) {
            BigDecimal b = (BigDecimal) epg.get("proz");
            if (b != null) {
                return b.intValue() > 0;
            }
        }

        return false;
    }

    public Integer getStartTime() {
        return (Integer) epg.get("starttime");
    }

    public Long getEndTime() {
        return (Long) epg.get("endtime");
    }

    protected String findChannelName(Map<String, Object> selectedEpg) {
        return epgSorter.getChannelName((String) selectedEpg.get("channelid"));
    }

    public abstract String getChannelName();
}

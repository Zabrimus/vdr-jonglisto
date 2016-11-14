package vdr.jonglisto.lib.model.search;

import vdr.jonglisto.lib.model.Channel;

public class EpgSearchCriteria {

    public enum What {
        DAY, TIME, CHANNEL
    }

    private boolean reset = true;
    private What what;

    private String channelGroup;
    private String genre;
    private String category;
    private Long time;
    private String searchText;
    private Channel channel;

    private boolean timeEnabled;
    private boolean channelEnabled;

    private boolean allChannelsEnabled;

    public EpgSearchCriteria(What what) {
        setWhat(what);
    }

    public void setWhat(What what) {
        this.what = what;

        switch (what) {
        case TIME:
            setTimeEnabled(true);
            setChannelEnabled(false);
            setAllChannelsEnabled(false);
            break;

        case CHANNEL:
            setTimeEnabled(false);
            setChannelEnabled(true);
            setAllChannelsEnabled(false);
            break;

        case DAY:
            setTimeEnabled(false);
            setChannelEnabled(true);
            setAllChannelsEnabled(true);
            break;

        default:
            break;
        }

        channelGroup = null;
        genre = null;
        category = null;
        time = null;
        searchText = null;
        channel = null;
    }

    public What getWhat() {
        return what;
    }

    public String getChannelGroup() {
        return channelGroup;
    }

    public void setChannelGroup(String channelGroup) {
        this.channelGroup = channelGroup;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isTimeEnabled() {
        return timeEnabled;
    }

    public void setTimeEnabled(boolean timeEnabled) {
        this.timeEnabled = timeEnabled;
    }

    public void setChannelEnabled(boolean channelEnabled) {
        this.channelEnabled = channelEnabled;
    }

    public boolean isChannelEnabled() {
        return channelEnabled;
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public boolean isAllChannelsEnabled() {
        return allChannelsEnabled;
    }

    public void setAllChannelsEnabled(boolean allChannelsEnabled) {
        this.allChannelsEnabled = allChannelsEnabled;
    }

    @Override
    public String toString() {
        return "EpgSearchCriteria [reset=" + reset + ", channelGroup=" + channelGroup + ", genre=" + genre
                + ", category=" + category + ", time=" + time + ", searchText=" + searchText + ", channel=" + channel
                + ", timeEnabled=" + timeEnabled + ", channelEnabled=" + channelEnabled + "]";
    }

}

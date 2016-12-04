package vdr.jonglisto.lib.model;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import vdr.jonglisto.lib.util.DateTimeUtil;
import vdr.jonglisto.lib.util.JonglistoUtil;

public class Timer {

    private final static String WD = "MTWTFSS";
    private static Pattern vdrTimerPattern = Pattern.compile("^(\\d+):(\\S+?):(\\S+?):(\\d+?):(\\d+?):(\\d+?):(\\d+?):(.*?)(:(.*))?$");
    private static Pattern weekdayPattern = Pattern.compile("((M|-)(T|-)(W|-)(T|-)(F|-)(S|-)(S|-))(@(.*?))?");
    
    @JsonProperty("id")
    private String id;

    @JsonProperty("flags")
    private Integer flags = 0;

    @JsonProperty("start_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @JsonProperty("stop_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stop;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("lifetime")
    private Integer lifetime;

    @JsonProperty("event_id")
    private Integer eventId;

    @JsonProperty("weekdays")
    private String weekdays;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("channel_name")
    private String channelName;

    @JsonProperty("is_pending")
    private Boolean isPending;

    @JsonProperty("is_recording")
    private Boolean isRecording;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("aux")
    private String aux;

    @JsonProperty("index")
    private int index;
    
    private String title;
    private String shortText;

    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    public Timer() {
    }

    public Timer(Timer source) {
        this.setId(source.getId());
        this.setFlags(source.getFlags());
        this.setStart(source.getStart());
        this.setStop(source.getStop());
        this.setPriority(source.getPriority());
        this.setLifetime(source.getLifetime());
        this.setEventId(source.getEventId());
        this.setWeekdays(source.getWeekdays());
        this.setChannel(source.getChannel());
        this.setFilename(source.getFilename());
        this.setChannelName(source.getChannelName());
        this.setIsPending(source.getIsPending());
        this.setIsRecording(source.getIsRecording());
        this.setIsActive(source.getIsActive());
        this.setAux(source.getAux());
        this.setIndex(source.getIndex());
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public void setStartTimestamp(LocalDateTime start) {
        this.start = start;
    }

    public void setStopTimestamp(LocalDateTime stop) {
        this.stop = stop;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getLifetime() {
        return lifetime;
    }

    public void setLifetime(Integer lifetime) {
        this.lifetime = lifetime;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(String weekdays) {
        this.weekdays = weekdays;

        setMonday(weekdays.charAt(0) == WD.charAt(0));
        setTuesday(weekdays.charAt(1) == WD.charAt(1));
        setWednesday(weekdays.charAt(2) == WD.charAt(2));
        setThursday(weekdays.charAt(3) == WD.charAt(3));
        setFriday(weekdays.charAt(4) == WD.charAt(4));
        setSaturday(weekdays.charAt(5) == WD.charAt(5));
        setSunday(weekdays.charAt(6) == WD.charAt(6));
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Boolean getIsPending() {
        return isPending;
    }

    public void setIsPending(Boolean isPending) {
        this.isPending = isPending;
    }

    public Boolean getIsRecording() {
        return isRecording;
    }

    public void setIsRecording(Boolean isRecording) {
        this.isRecording = isRecording;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        if (isActive) {
            flags = flags.intValue() | 1;
        } else {
            flags = flags & ~1;
        }

        this.isActive = isActive;
    }

    public String getAux() {
        return aux;
    }

    public void setAux(String aux) {
        this.aux = aux;
    }

    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
        setWeekdays(monday, 0);
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
        setWeekdays(tuesday, 1);
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
        setWeekdays(wednesday, 2);
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
        setWeekdays(thursday, 3);
    }

    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
        setWeekdays(friday, 4);
    }

    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
        setWeekdays(saturday, 5);
    }

    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
        setWeekdays(sunday, 6);
    }

    public boolean getVps() {
        return (flags.intValue() & 4) == 4;
    }

    public void setVps(boolean v) {
        if (v) {
            flags = flags.intValue() | 4;
        } else {
            flags = flags & ~4;
        }
    }

    private void setWeekdays(boolean day, int index) {
        StringBuilder b = new StringBuilder(weekdays);
        b.setCharAt(index, day ? WD.charAt(index) : '-');
        weekdays = b.toString();
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getStop() {
        return stop;
    }

    public void setStop(LocalDateTime stop) {
        this.stop = stop;
    }

    public String createTimerDiff(Timer newTimer) {
        if (!getId().equals(newTimer.getId())) {
            throw new IllegalArgumentException("Die TimerId ist unterschiedlich. Ein Update ist nicht möglich.");
        }

        if (this.equals(newTimer)) {
            return null;
        }

        String result = "timer_id=" + newTimer.getId();
        
        if (flags != newTimer.getFlags()) {
            result += "&flags=" + newTimer.getFlags();
        }

        if (!filename.equals(newTimer.getFilename())) {
            result += "&file=" + JonglistoUtil.encode(newTimer.getFilename());
        }

        if (!DateTimeUtil.toTime(stop).equals(DateTimeUtil.toTime(newTimer.getStop()))) {
            result += "&stop=" + DateTimeUtil.toRestfulTime(newTimer.getStop());
        }

        if (!DateTimeUtil.toTime(start).equals(DateTimeUtil.toTime(newTimer.getStart()))) {
            result += "&start=" + DateTimeUtil.toRestfulTime(newTimer.getStart());
        }

        if (!DateTimeUtil.toDate(start).equals(DateTimeUtil.toDate(newTimer.getStart()))) {
            result += "&day=" + DateTimeUtil.toRestfulDate(newTimer.getStart());
        }

        if (!channel.equals(newTimer.getChannel())) {
            result += "&channel=" + newTimer.getChannel();
        }

        if (!weekdays.equals(newTimer.getWeekdays())) {
            result += "&weekdays=" + newTimer.getWeekdays();
        }

        if (lifetime != newTimer.getLifetime()) {
            result += "&lifetime=" + newTimer.getLifetime();
        }

        if (priority != newTimer.getPriority()) {
            result += "&priority=" + newTimer.getPriority();
        }

        if (!aux.equals(newTimer.getAux())) {
            result += "&aux=" + JonglistoUtil.encode(newTimer.getAux());
        }

        return result;
    }

    public String createTimer() {
        StringBuilder result = new StringBuilder();
        return result.append("file=" + JonglistoUtil.encode(getFilename())) //
                .append("&flags=" + getFlags()) //
                .append("&start=" + DateTimeUtil.toRestfulTime(start)) //
                .append("&stop=" + DateTimeUtil.toRestfulTime(stop)) //
                .append("&day=" + DateTimeUtil.toRestfulDate(start)) //
                .append("&channel=" + getChannel()) //
                .append("&weekdays=" + getWeekdays()) //
                .append("&lifetime=" + getLifetime()) //
                .append("&priority=" + getPriority()) //

                // Info: Neither eventid nor useid is accepted. But why? It
                // seems i do something wrong
                // .append("&eventid=" + getEventId())

                .append("&aux=" + JonglistoUtil.encode(getAux())) //
                .toString();
    }

    /**
     * Conversion method: raw VDR timer <-> restfulapi timer
     */
    public void convertFromVdrTimer(String vdrTimer) {
        Matcher m = vdrTimerPattern.matcher(vdrTimer);
        if (m.matches()) {
            int fl = Integer.parseInt(m.group(1));
            setIsActive((fl & 1) != 0);
            setVps((fl & 4) != 0);
            
            setChannel(m.group(2));
            setPriority(Integer.parseInt(m.group(6)));
            setLifetime(Integer.parseInt(m.group(7)));
            setAux(m.group(10));
            setFilename(m.group(8));
            
            int tStart = Integer.parseInt(m.group(4));
            int tEnd = Integer.parseInt(m.group(5));
            String tDate = m.group(3);
            
            // check if weekdays are part of tDate
            Matcher dm = weekdayPattern.matcher(tDate);
            
            if (dm.matches()) {
                // weekdays are present
                setWeekdays(dm.group(1));
                
                // set new date
                tDate = dm.group(10);
            } else {
                setWeekdays("-------");
            }
            
            if (tDate == null) {
                // today
                setStartTimestamp(DateTimeUtil.toLocalDateTime(DateTimeUtil.toRestfulDate(LocalDateTime.now()), tStart));
                setStopTimestamp(DateTimeUtil.toLocalDateTime(DateTimeUtil.toRestfulDate(LocalDateTime.now()), tEnd));
            } else if (StringUtils.isNumeric(tDate)) {
                // day of month (future date)
                setStartTimestamp(LocalDateTime.now().withDayOfMonth(Integer.parseInt(tDate)).withHour(tStart/100).withMinute(tStart%100));
                
                if (getStart().isBefore(LocalDateTime.now())) {
                    // adjust start date by one month
                    setStartTimestamp(getStart().plusMonths(1));
                }
                
                setStopTimestamp(getStart().withHour(tEnd/100).withMinute(tEnd%100));
            } else {
                // assume we have a real date value
                setStartTimestamp(DateTimeUtil.toLocalDateTime(tDate, tStart));
                setStopTimestamp(DateTimeUtil.toLocalDateTime(tDate, tEnd));                
            }

            // if stop < start, then add 1 day to stop
            if (getStop().isBefore(getStart())) {
                setStop(getStop().plusDays(1));
            }
        } else {
            throw new RuntimeException("Not a valid VDR timer string: " + vdrTimer);
        }
    }
    
    public String convertToVdrTimer() {
        String result = new StringBuilder().append(getFlags()).append(":") //
                            .append(getChannel()).append(":") //
                            .append(!weekdays.equals("-------") ? weekdays + "@" : "") //
                            .append(DateTimeUtil.toRestfulDate(getStart())).append(":") //
                            .append(DateTimeUtil.toRestfulTime(getStart())).append(":") //
                            .append(DateTimeUtil.toRestfulTime(getStop())).append(":") //
                            .append(getPriority()).append(":") //
                            .append(getLifetime()).append(":") //
                            .append(getFilename()).append(":") //
                            .append(getAux()) //
                            .toString();
        
        return result;
    }
    
    /**
     * helper functions
     */

    public String getStartDate() {
        return DateTimeUtil.toDate(start);
    }

    public void setStartDate(String s) {
        start = DateTimeUtil.setDate(start, s);
    }

    public String getStartTime() {
        return DateTimeUtil.toTime(start);
    }

    public void setStartTime(String s) {
        start = DateTimeUtil.setTime(start, s);
    }

    public String getStopTime() {
        return DateTimeUtil.toTime(stop);
    }

    public void setStopTime(String s) {
        stop = DateTimeUtil.setTime(stop, s);
    }

    public String getDuration() {
        return DateTimeUtil.toDuration(start, stop);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "Timer [id=" + id + ", flags=" + flags + ", start=" + start + ", stop=" + stop + ", priority=" + priority
                + ", lifetime=" + lifetime + ", eventId=" + eventId + ", weekdays=" + weekdays + ", channel=" + channel
                + ", filename=" + filename + ", channelName=" + channelName + ", isPending=" + isPending
                + ", isRecording=" + isRecording + ", isActive=" + isActive + ", aux=" + aux + ", index=" + index
                + ", title=" + title + ", shortText=" + shortText + ", monday=" + monday + ", tuesday=" + tuesday
                + ", wednesday=" + wednesday + ", thursday=" + thursday + ", friday=" + friday + ", saturday="
                + saturday + ", sunday=" + sunday + "]";
    }
   
}
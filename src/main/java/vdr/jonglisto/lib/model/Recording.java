package vdr.jonglisto.lib.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Recording {

    @JsonProperty("number")
    private Integer number;

    @JsonProperty("name")
    private String name;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("relative_file_name")
    private String relativeFileName;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("frames_per_second")
    private Integer framesPerSecond;

    @JsonProperty("is_edited")
    private Boolean edited;

    @JsonProperty("filesize_mb")
    private Integer fileSize;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("marks")
    private List<String> marks = new ArrayList<String>();

    @JsonProperty("event_title")
    private String eventTitle;

    @JsonProperty("event_short_text")
    private String eventShortText;

    @JsonProperty("event_description")
    private String eventDescription;

    @JsonProperty("event_start_time")
    private Integer eventStartTime;

    @JsonProperty("event_duration")
    private Integer eventDuration;

    @JsonProperty("sync_action")
    private String syncAction;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("aux")
    private String aux;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRelativeFileName() {
        return relativeFileName;
    }

    public void setRelativeFileName(String relativeFileName) {
        this.relativeFileName = relativeFileName;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getFramesPerSecond() {
        return framesPerSecond;
    }

    public void setFramesPerSecond(Integer framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
    }

    public Boolean getEdited() {
        return edited;
    }

    public String getEditedStr() {
        return edited ? "Y" : "N";
    }

    public void setEditedStr(String v) {
        edited = "Y".equals(v);
    }

    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public List<String> getMarks() {
        return marks;
    }

    public void setMarks(List<String> marks) {
        this.marks = marks;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventShortText() {
        return eventShortText;
    }

    public void setEventShortText(String eventShortText) {
        this.eventShortText = eventShortText;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Integer getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(Integer eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public Integer getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(Integer eventDuration) {
        this.eventDuration = eventDuration;
    }

    public String getSyncAction() {
        return syncAction;
    }

    public void setSyncAction(String syncAction) {
        this.syncAction = syncAction;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getAux() {
        return aux;
    }

    public void setAux(String aux) {
        this.aux = aux;
    }
}
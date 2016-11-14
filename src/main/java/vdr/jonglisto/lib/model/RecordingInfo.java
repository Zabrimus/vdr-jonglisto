package vdr.jonglisto.lib.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import vdr.jonglisto.lib.util.DateTimeUtil;

public class RecordingInfo {

    private String recStartDate;
    private String recStartTime;
    private String duration;
    private long durationLong;
    private Integer fileSize;
    private String fileName;
    private String relativeFileName;
    private String name;

    private Integer framesPerSecond;
    private boolean edited;
    private String channelId;
    private String channelName;
    private String title;
    private String shortText;
    private String description;
    private String aux;

    private Integer recStart;

    public String getRecStartDate() {
        return recStartDate;
    }

    public String getRecStartTime() {
        return recStartTime;
    }

    public long getDurationLong() {
        return durationLong;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(Long d) {
        durationLong = d;
        duration = DateTimeUtil.toDuration(d);
    }

    public Integer getRecStart() {
        return recStart;
    }

    public void setRecStart(Integer recStart) {
        this.recStart = recStart;

        LocalDateTime d = DateTimeUtil.toDateTime(new Long(recStart));
        recStartDate = DateTimeUtil.toDate(d);
        recStartTime = DateTimeUtil.toTime(d);
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFramesPerSecond() {
        return framesPerSecond;
    }

    public void setFramesPerSecond(Integer framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
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

    public String getDescription() {
        return description;
    }

    public List<String> getDescription2() {
        return Arrays.asList(description.split("\n"));
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAux() {
        return aux;
    }

    public void setAux(String aux) {
        this.aux = aux;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getRelativeFileName() {
        return relativeFileName;
    }

    public void setRelativeFileName(String relativeFileName) {
        this.relativeFileName = relativeFileName;
    }

    @Override
    public String toString() {
        return "RecordingInfo [recStartDate=" + recStartDate + ", recStartTime=" + recStartTime + ", duration="
                + duration + ", durationLong=" + durationLong + ", fileSize=" + fileSize + ", fileName=" + fileName
                + ", relativeFileName=" + relativeFileName + ", name=" + name + ", framesPerSecond=" + framesPerSecond
                + ", edited=" + edited + ", channelId=" + channelId + ", channelName=" + channelName + ", title="
                + title + ", shortText=" + shortText + ", description=" + description + ", aux=" + aux + ", recStart="
                + recStart + "]";
    }
}

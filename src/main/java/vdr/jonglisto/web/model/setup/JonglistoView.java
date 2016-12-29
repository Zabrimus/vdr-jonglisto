package vdr.jonglisto.web.model.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JonglistoView {

    private String uuid;

    private String name;
    private List<String> head;
    private List<String> channels;
    private List<String> timer;
    private List<String> recordings;

    public JonglistoView() {
        uuid = UUID.randomUUID().toString();

        head = new ArrayList<>();
        channels = new ArrayList<>();
        timer = new ArrayList<>();
        recordings = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public List<String> getHead() {
        return head;
    }

    public void setHead(List<String> head) {
        this.head = head;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public List<String> getTimer() {
        return timer;
    }

    public void setTimer(List<String> timer) {
        this.timer = timer;
    }

    public List<String> getRecordings() {
        return recordings;
    }

    public void setRecordings(List<String> recordings) {
        this.recordings = recordings;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

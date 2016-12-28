package vdr.jonglisto.web.model.setup;

import java.util.ArrayList;
import java.util.List;

public class JonglistoView {

    private String name;
    private List<String> head;
    private List<String> channels;
    private List<String> timer;
    private List<String> recordings;

    public JonglistoView() {
        head = new ArrayList<>();
        channels = new ArrayList<>();
        timer = new ArrayList<>();
        recordings = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}

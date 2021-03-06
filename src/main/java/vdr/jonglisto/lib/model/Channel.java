package vdr.jonglisto.lib.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Channel {

    @JsonProperty("channel_id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("number")
    private Integer number;

    @JsonProperty("group")
    private String group;

    @JsonProperty("is_radio")
    private Boolean radio;
    
    public static Channel emptyChannel = new Channel("", "");

    public Channel() {
    }

    public Channel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Boolean getRadio() {
        return radio;
    }

    public void setRadio(Boolean radio) {
        this.radio = radio;
    }

    @Override
    public String toString() {
        return "Channel [id=" + id + ", name=" + name + ", number=" + number + ", group=" + group + ", radio=" + radio
                + "]";
    }
}

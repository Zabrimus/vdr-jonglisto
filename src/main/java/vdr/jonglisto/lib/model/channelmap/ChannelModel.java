package vdr.jonglisto.lib.model.channelmap;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import vdr.jonglisto.lib.util.JonglistoUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("channels")
public class ChannelModel {

    @JsonProperty(value = "number")
    private int number;

    @JsonProperty(value = "channel_id")
    private String channelId;

    @JsonProperty(value = "name")
    private String name;

    private String normalizedName;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.normalizedName = JonglistoUtil.channelNameNormalize(name);
    }

    /*
     * public String getNormalizedName() {
     * return normalizedName;
     * }
     */

    public String getNormalizedName(Map<String, String> channelNameMapping) {
        String norm = channelNameMapping.get(name);
        if (norm != null) {
            return norm;
        } else {
            return normalizedName;
        }
    }

    @Override
    public String toString() {
        return "ChannelModel [number=" + number + ", channelId=" + channelId + ", name=" + name + ", normalizedName="
                + normalizedName + "]";
    }
}

package vdr.jonglisto.lib.model.channelmap;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import vdr.jonglisto.lib.util.JonglistoUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdModel {

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "name")
    private String name;

    private int providerId;

    private String normalizedName;

    public IdModel() {
    }

    public IdModel(int providerId, String id, String name) {
        setProviderId(providerId);
        setId(id);
        setName(name);
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
        this.normalizedName = JonglistoUtil.channelNameNormalize(name);
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public String getNormalizedName(Map<String, String> channelNameMapping) {
        String norm = channelNameMapping.get(name);
        if (norm != null) {
            return norm;
        } else {
            return normalizedName;
        }
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    @Override
    public String toString() {
        return "IdModel [id=" + id + ", name=" + name + ", providerId=" + providerId + ", normalizedName="
                + normalizedName + "]";
    }
}

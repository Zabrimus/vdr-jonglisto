package vdr.jonglisto.lib.model.osd;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OsdItem {

    @JsonProperty("content")
    private String content;

    @JsonProperty("is_selected")
    private Boolean isSelected;

    private String actionName;
    private String subActionName;

    public OsdItem() {
    }

    public OsdItem(String content, boolean isSelected, String actionName, String subActionName) {
        this.content = content;
        this.isSelected = isSelected;
        this.actionName = actionName;
        this.subActionName = subActionName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getSubActionName() {
        return subActionName;
    }

    public void setSubActionName(String subActionName) {
        this.subActionName = subActionName;
    }

    public String toString() {
        return "OsdItem [content=" + content + ", isSelected=" + isSelected + ", actionName=" + actionName
                + ", subActionName=" + subActionName + "]";
    }
}

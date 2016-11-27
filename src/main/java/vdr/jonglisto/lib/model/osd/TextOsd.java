package vdr.jonglisto.lib.model.osd;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextOsd {

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("message")
    private String message;

    @JsonProperty("red")
    private String red;

    @JsonProperty("green")
    private String green;

    @JsonProperty("yellow")
    private String yellow;

    @JsonProperty("blue")
    private String blue;

    @JsonProperty("items")
    private List<OsdItem> items;

    private String textBlock;
    
    public TextOsd() {
        items = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRed() {
        return red;
    }

    public void setRed(String red) {
        this.red = red;
    }

    public String getGreen() {
        return green;
    }

    public void setGreen(String green) {
        this.green = green;
    }

    public String getYellow() {
        return yellow;
    }

    public void setYellow(String yellow) {
        this.yellow = yellow;
    }

    public String getBlue() {
        return blue;
    }

    public void setBlue(String blue) {
        this.blue = blue;
    }

    public String getTextBlock() {
        return textBlock;
    }
    
    public void setTextBlock(String textBlock) {
        this.textBlock = textBlock;
    }

    public List<OsdItem> getItems() {
        return items;
    }

    public void setItems(List<OsdItem> items) {
        this.items = items;
    }

    public void moveSelected(int offset) {
        // find selectedItem
        int oldIdx;
        for (oldIdx = 0; oldIdx < items.size(); ++oldIdx) {
            if (items.get(oldIdx).isSelected()) {
                break;
            }
        }

        // if no item is selected then preselect the first one
        if (oldIdx == items.size()) {
            oldIdx = 0;
        }
        
        // add offset and check range
        int newIdx = oldIdx + offset;
        if (newIdx < 0) {
            newIdx = 0;
        } else if (newIdx > items.size() - 1) {
            newIdx = items.size() - 1;
        }

        // switch selected
        items.get(oldIdx).setSelected(false);
        items.get(newIdx).setSelected(true);
    }

    @Override
    public String toString() {
        return "TextOsd [type=" + type + ", title=" + title + ", message=" + message + ", red=" + red + ", green="
                + green + ", yellow=" + yellow + ", blue=" + blue + ", items=" + items + ", textBlock=" + textBlock + "]";
    }
}

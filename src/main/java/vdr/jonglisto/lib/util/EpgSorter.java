package vdr.jonglisto.lib.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import vdr.jonglisto.lib.model.Channel;

public class EpgSorter {

    private Map<String, Channel> channels;

    public EpgSorter(List<Channel> ch) {
        channels = new TreeMap<String, Channel>();
        for (Channel s : ch) {
            channels.put(s.getId(), s);
        }
    }

    public String getChannelName(String channelId) {
        try {
            String result = channels.get(channelId).getName();
            return result != null ? result : "";
        } catch (Exception e) {
            return "";
        }
    }

    public Collection<Channel> getChannels() {
        return channels.values();
    }

    public List<Map<String, Object>> sort(List<Map<String, Object>> epgData) {
        return internalSort(epgData);
    }

    private List<Map<String, Object>> internalSort(List<Map<String, Object>> whole) {
        int center;

        if (whole.size() <= 1)
            return whole;
        else {
            center = whole.size() / 2;

            List<Map<String, Object>> left = new ArrayList<Map<String, Object>>(whole.subList(0, center));
            List<Map<String, Object>> right = new ArrayList<Map<String, Object>>(whole.subList(center, whole.size()));

            left = sort(left);
            right = sort(right);

            merge(left, right, whole);

        }
        return whole;
    }

    private void merge(List<Map<String, Object>> left, List<Map<String, Object>> right,
            List<Map<String, Object>> whole) {

        int leftIndex = 0;
        int rightIndex = 0;
        int wholeIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            Channel a = channels.get(left.get(leftIndex).get("channelid"));
            Channel b = channels.get(right.get(rightIndex).get("channelid"));

            if (a.getNumber().compareTo(b.getNumber()) < 0) {
                whole.set(wholeIndex, left.get(leftIndex));
                leftIndex++;
            } else {
                whole.set(wholeIndex, right.get(rightIndex));
                rightIndex++;
            }
            wholeIndex++;
        }

        List<Map<String, Object>> rest;
        int restIndex;
        if (leftIndex >= left.size()) {
            rest = right;
            restIndex = rightIndex;
        } else {
            rest = left;
            restIndex = leftIndex;
        }

        for (int i = restIndex; i < rest.size(); i++) {
            whole.set(wholeIndex, rest.get(i));
            wholeIndex++;
        }
    }

}

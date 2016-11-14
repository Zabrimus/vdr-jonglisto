package vdr.jonglisto.lib.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import vdr.jonglisto.lib.model.Channel;

public class JonglistoUtil {

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            // cannot happen, because UTF-8 is always valid
            return str;
        }
    }

    public static String encodePath(String str) {
        return Arrays.stream(str.split("/")).map(s -> encode(s)).collect(Collectors.joining("/"));
    }

    public static String decode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // cannot happen, because UTF-8 is always valid
            return str;
        }
    }

    public static String joinChannelId(Collection<Channel> list) {
        StringBuilder sb = new StringBuilder();

        Iterator<Channel> iter = list.iterator();
        while (iter.hasNext()) {
            sb.append("'").append(iter.next().getId()).append("'").append(iter.hasNext() ? "," : "");
        }

        return sb.toString();
    }
}

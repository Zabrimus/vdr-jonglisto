package vdr.jonglisto.lib.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.lang3.StringUtils;

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

    public static byte[] zip(String inputStr) {
        byte[] input = null;
        try {
            input = inputStr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // cannot happen, because UTF-8 is always valid
            return null;
        }

        byte[] output = new byte[64 * 1000];
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();
        int len = compresser.deflate(output);
        compresser.end();

        byte[] shortOut = new byte[len];
        System.arraycopy(output, 0, shortOut, 0, len);

        return shortOut;
    }

    public static String zipBase64(String inputStr) {
        byte[] input = null;
        try {
            input = inputStr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // cannot happen, because UTF-8 is always valid
            return null;
        }

        byte[] output = new byte[1000];
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();
        int len = compresser.deflate(output);
        compresser.end();

        byte[] shortOut = new byte[len];
        System.arraycopy(output, 0, shortOut, 0, len);

        String asB64 = Base64.getEncoder().encodeToString(shortOut);
        return asB64;
    }

    public static String unzipBase64(String inputStr) {
        byte[] asBytes = Base64.getDecoder().decode(inputStr);
        Inflater decompresser = new Inflater();
        decompresser.setInput(asBytes, 0, asBytes.length);

        try {
            byte[] result = new byte[500];
            int resultLength = decompresser.inflate(result);
            decompresser.end();
            String outputString = new String(result, 0, resultLength, "UTF-8");
            return outputString;
        } catch (UnsupportedEncodingException | DataFormatException e) {
            return null;
        }
    }

    public static String channelNameNormalize(String input) {
        return StringUtils.stripAccents(input) //
                .replaceAll("\\&", "and") //
                .replaceAll("\\+", "plus") //
                .replaceAll("\\*", "star") //
                .replaceAll("HD 1", "1") //
                .replaceAll("HD 2", "2") //
                .replaceAll("HD 3", "3") //
                .replaceAll("HD 4", "4") //
                .replaceAll("HD 5", "5") //
                .replaceAll("HD 6", "6") //
                .replaceAll("HD 7", "7") //
                .replaceAll("HD 8", "8") //
                .replaceAll("HD 9", "9") //
                .replaceAll("II", "2") //
                .replaceAll("III", "3") //
                .replaceAll("7", "sieben") //
                .replaceAll("\\s+\\(*HD\\)*$", "") //
                .replaceAll("\\s+\\(S\\)$", "") //
                .replaceAll("\\s+\\(*HD\\)*$", "") //
                .replaceAll("[^A-Za-z0-9]", "") //
                .trim() //
                .toLowerCase();
    }
}

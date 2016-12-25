package vdr.jonglisto.lib.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedChannel {

    private static Logger log = LoggerFactory.getLogger(ExtendedChannel.class);

    private Pattern pattern = Pattern.compile("^(\\d+) (.*?)(,.*?)?(;(.*?))?:(.*?):(.*?):(.*?):(.*?):(.*?):(.*?):(.*?):(.*?):(.*?):(.*?):(.*?):(.*?)$");
    private Pattern shortPattern = Pattern.compile("^(\\d+) (.*)$");

    private String channelLine;

    private String id;
    private Boolean radio;
    private Boolean encrypted;
    private String group;

    private String name;
    private Integer number;
    private String bouquet;
    private String frequency;
    private String parameter;
    private String source;
    private String symbolRate;
    private String vpid;
    private String apid;
    private String tpid;
    private String caid;
    private String sid;
    private String nid;
    private String tid;
    private String rid;

    public ExtendedChannel(String line) {
        Matcher sm = shortPattern.matcher(line);
        if (sm.matches()) {
            channelLine = sm.group(2);
        }

        // Parse the line
        Matcher m = pattern.matcher(line);
        if (m.matches()) {
            number = Integer.parseInt(m.group(1));
            name = m.group(2);
            bouquet = m.group(5);
            frequency = m.group(6);
            parameter = m.group(7);
            source = m.group(8);
            symbolRate = m.group(9);
            vpid = m.group(10);
            apid = m.group(11);
            tpid = m.group(12);
            caid = m.group(13);
            sid = m.group(14);
            nid = m.group(15);
            tid = m.group(16);
            rid = m.group(17);

            id = new StringBuilder().append(source).append("-").append(nid).append("-").append(tid).append("-")
                    .append(sid).toString();

            if ((rid != null) && !"0".equals(rid)) {
                id = id + "-" + rid;
            }
            
            // INFO: Ist vpid = 1 wirklich ein Kriterium für verschlüsselte
            // Radio-Kanäle?
            if ("0".equals(vpid)) {
                radio = true;
                encrypted = false;
            } else if ("1".equals(vpid)) {
                radio = true;
                encrypted = true;
            } else {
                radio = false;
            }

            if ("0".equals(caid)) {
                encrypted = false;
            } else {
                encrypted = true;
            }
        } else {
            // this must not happen
            log.error("Internal error: Channel line '" + line + "' cannot be parsed.");
        }
    }

    public String getChannelLine() {
        return channelLine;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getId() {
        return id;
    }

    public Boolean getRadio() {
        return radio;
    }

    public Boolean getEncrypted() {
        return encrypted;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public Integer getNumber() {
        return number;
    }

    public String getBouquet() {
        return bouquet;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getParameter() {
        return parameter;
    }

    public String getSource() {
        return source;
    }

    public String getSymbolRate() {
        return symbolRate;
    }

    public String getVpid() {
        return vpid;
    }

    public String getApid() {
        return apid;
    }

    public String getTpid() {
        return tpid;
    }

    public String getCaid() {
        return caid;
    }

    public String getSid() {
        return sid;
    }

    public String getNid() {
        return nid;
    }

    public String getTid() {
        return tid;
    }

    public String getRid() {
        return rid;
    }

    @Override
    public String toString() {
        return "ExtendedChannel [id=" + id + ", channelLine=" + channelLine + "]";
    }
}

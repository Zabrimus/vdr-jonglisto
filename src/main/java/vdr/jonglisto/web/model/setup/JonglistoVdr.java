package vdr.jonglisto.web.model.setup;

public class JonglistoVdr {

    private String uuid;
    private String hostname;
    private String displayName;
    private String alias;
    private String ip;
    private String svdrp;
    private String restful;

    public JonglistoVdr() {
        // set default values
        restful = "8002";
        svdrp = "6419";
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSvdrp() {
        return svdrp;
    }

    public void setSvdrp(String svdrp) {
        this.svdrp = svdrp;
    }

    public String getRestful() {
        return restful;
    }

    public void setRestful(String restful) {
        this.restful = restful;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}

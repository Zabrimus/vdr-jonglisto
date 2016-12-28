package vdr.jonglisto.web.model.setup;

public class JonglistoVdr {

    private String uuid;
    private String name;
    private String ip;
    private String svdrp;
    private String restful;

    public JonglistoVdr() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}

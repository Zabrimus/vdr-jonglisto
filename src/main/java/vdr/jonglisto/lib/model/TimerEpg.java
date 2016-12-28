package vdr.jonglisto.lib.model;

public class TimerEpg {

    private String name;
    private String method;
    private String remoteId;

    public TimerEpg(String name, String method, String remote_id) {
        this.name = name;
        this.method = method;
        this.remoteId = remote_id;
    }

    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    public String getRemoteId() {
        return remoteId;
    }

    @Override
    public String toString() {
        return "TimerEpg [name=" + name + ", method=" + method + ", remote_id=" + remoteId + "]";
    }
}

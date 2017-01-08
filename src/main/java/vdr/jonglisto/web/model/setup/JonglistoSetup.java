package vdr.jonglisto.web.model.setup;

import java.util.ArrayList;
import java.util.List;

public class JonglistoSetup {

    private String epgdDatabase;
    private String epgdHost;
    private String epgdPort;
    private String epgdUser;
    private String epgdPassword;

    private List<JonglistoVdr> availableVdr;
    private List<JonglistoView> availableViews;

    private boolean useEpgd;
    private String epgVdr;
    
    public JonglistoSetup() {
        // set some default values
        epgdDatabase = "epg2vdr";
        epgdHost = "server";
        epgdPort = "3306";
        epgdUser = "epg2vdr";
        epgdPassword = "epg";

        availableViews = new ArrayList<>();
        epgVdr = "<set desired alias>";
    }

    public String getEpgdHost() {
        return epgdHost;
    }

    public void setEpgdHost(String epgdHost) {
        this.epgdHost = epgdHost;
    }

    public String getEpgdPort() {
        return epgdPort;
    }

    public void setEpgdPort(String epgdPort) {
        this.epgdPort = epgdPort;
    }

    public String getEpgdUser() {
        return epgdUser;
    }

    public void setEpgdUser(String epgdUser) {
        this.epgdUser = epgdUser;
    }

    public String getEpgdPassword() {
        return epgdPassword;
    }

    public void setEpgdPassword(String epgdPassword) {
        this.epgdPassword = epgdPassword;
    }

    public String getEpgdDatabase() {
        return epgdDatabase;
    }

    public void setEpgdDatabase(String epgdDatabase) {
        this.epgdDatabase = epgdDatabase;
    }
    
    public boolean isUseEpgd() {
        return useEpgd;
    }

    public void setUseEpgd(boolean useEpgd) {
        this.useEpgd = useEpgd;
    }
    
    public String getEpgVdr() {
        return epgVdr;
    }
    
    public void setEpgVdr(String epgVdr) {
        this.epgVdr = epgVdr;
    }

    public List<JonglistoVdr> getAvailableVdr() {
        return availableVdr;
    }

    public void setAvailableVdr(List<JonglistoVdr> list) {
        // Make a copy of the list. Currently it is immutable.
        List<JonglistoVdr> result = new ArrayList<>();
        result.addAll(list);
        this.availableVdr = result;
    }

    public List<JonglistoView> getAvailableViews() {
        return availableViews;
    }

    public void setAvailableViews(List<JonglistoView> availableViews) {
        this.availableViews = availableViews;
    }
}

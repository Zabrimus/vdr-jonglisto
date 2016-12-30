package vdr.jonglisto.web.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.model.VDRView;

public class Timer {

    @Inject
    private ConfigurationService configuration;

    @SessionAttribute
    @Property
    private VDRView currentVdrView;

    @Property
    private String newTimerUseId;

    @Property
    private String channelName;

    Object onActivate(@RequestParameter(value = "reset", allowBlank = true) Boolean reset,
            @RequestParameter(value = "newTimerUseId", allowBlank = true) String useid,
            @RequestParameter(value = "channelName", allowBlank = true) String channelName) {

        if (!configuration.isSuccessfullyInitialized()) {
            return Setup.class;
        }

        if (currentVdrView == null) {
            // deep jump into this page?
            return Index.class;
        }

        newTimerUseId = useid;
        this.channelName = channelName;

        return null;
    }
}

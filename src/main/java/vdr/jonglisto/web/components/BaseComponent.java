package vdr.jonglisto.web.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.URLEncoder;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.tynamo.security.services.SecurityService;

import vdr.jonglisto.lib.CommandService;
import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.EpgDataService;
import vdr.jonglisto.lib.EpgImageService;
import vdr.jonglisto.lib.EpgdSearchTimerService;
import vdr.jonglisto.lib.VdrDataService;
import vdr.jonglisto.lib.model.VDRView;

public class BaseComponent {

    @SessionAttribute
    @Property
    protected VDRView currentVdrView;

    @Inject
    protected SecurityService securityService;

    @Inject
    protected AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    protected JavaScriptSupport javaScriptSupport;

    @Inject
    protected VdrDataService vdrDataService;

    @Inject
    protected ComponentResources componentResources;

    @Inject
    protected EpgDataService epgDataService;

    @Inject
    protected EpgImageService epgImageService;

    @Inject
    protected VdrDataService dataService;

    @Inject
    protected EpgdSearchTimerService searchTimerService;

    @Inject
    protected Request request;

    @Inject
    protected ComponentResources resources;

    @Inject
    protected ConfigurationService configuration;

    @Inject
    protected CommandService commandService;

    @Inject
    protected URLEncoder encoder;

    protected String getTimerUuid() {
        return currentVdrView.getTimerVdr().get();
    }

    protected String getChannelUuid() {
        return currentVdrView.getChannelVdr().get();
    }

    protected String getHeadUuid() {
        return currentVdrView.getHeadVdr().get();
    }

    protected String getRecordingUuid() {
        return currentVdrView.getRecordingVdr().get();
    }

}

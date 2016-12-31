package vdr.jonglisto.web.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.model.VDRView;

public class SearchTimer extends BasePage {

    @Inject
    private ConfigurationService configuration;

    @SessionAttribute
    @Property
    private VDRView currentVdrView;

    Object onActivate(@RequestParameter(value = "reset", allowBlank = true) Boolean reset) {
        if (!configuration.isSuccessfullyInitialized()) {
            return Setup.class;
        }

        if (currentVdrView == null) {
            // deep jump into this page?
            return Index.class;
        }

        if ((reset != null) && reset) {
            discardAllPagePersistent();
        }       

        return null;
    }
}

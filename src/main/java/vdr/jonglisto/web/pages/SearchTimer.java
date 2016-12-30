package vdr.jonglisto.web.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.model.VDRView;

public class SearchTimer {

    @Inject
    private ConfigurationService configuration;

    @SessionAttribute
    @Property
    private VDRView currentVdrView;

    Object onActivate() {
        if (!configuration.isSuccessfullyInitialized()) {
            return Setup.class;
        }

        if (currentVdrView == null) {
            // deep jump into this page?
            return Index.class;
        }

        return null;
    }
}

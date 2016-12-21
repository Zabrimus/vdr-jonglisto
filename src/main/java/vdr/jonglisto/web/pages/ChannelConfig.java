package vdr.jonglisto.web.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.SessionAttribute;

import vdr.jonglisto.lib.model.VDRView;

public class ChannelConfig {

    @SessionAttribute
    @Property
    private VDRView currentVdrView;

    Object onActivate(@RequestParameter(value = "reset", allowBlank = true) Boolean reset) {
        if (currentVdrView == null) {
            // deep jump into this page?
            return Index.class;
        }

        return null;
    }
}

package vdr.jonglisto.web.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.SessionAttribute;

import vdr.jonglisto.lib.model.VDRView;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria.What;

public class ProgramDay {

    @SessionAttribute("epgSearchCriteria")
    @Property
    private EpgSearchCriteria epgCriteria;

    @SessionAttribute
    @Property
    private VDRView currentVdrView;

    Object onActivate(@RequestParameter(value = "reset", allowBlank = true) Boolean reset) {
        if (currentVdrView == null) {
            // side jump into the page?
            return Index.class;
        }

        if (((reset != null) && reset) || (epgCriteria == null) || (epgCriteria.getWhat() != What.DAY)) {
            epgCriteria = new EpgSearchCriteria(What.DAY);
        }

        return null;
    }
}

package vdr.jonglisto.web.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

import vdr.jonglisto.lib.model.VDRView;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria.What;

@Import(stylesheet = "META-INF/assets/css/ProgramChannel.css")
public class ProgramChannel {

	@Inject
	private ComponentResources componentResources;

	@SessionAttribute("epgSearchCriteria")
	@Property
	private EpgSearchCriteria epgCriteria;

	@SessionAttribute
	@Property
	private VDRView currentVdrView;

	Object onActivate(@RequestParameter(value = "reset", allowBlank = true) Boolean reset) {
		
		if (currentVdrView == null) {
			// deep jump into this page?
			return Index.class;
		}
		
		if (((reset != null) && reset) || (epgCriteria == null) || (epgCriteria.getWhat() != What.CHANNEL)) {
			epgCriteria = new EpgSearchCriteria(What.CHANNEL);
			componentResources.triggerEvent("updateEpg", null, null);
		}

		return null;
	}
}

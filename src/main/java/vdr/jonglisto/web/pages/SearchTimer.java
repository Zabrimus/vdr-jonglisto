package vdr.jonglisto.web.pages;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Meta;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;

import vdr.jonglisto.lib.model.VDRView;

@Import(stylesheet = "META-INF/assets/css/SearchTimer.css")
public class SearchTimer {

    @SessionAttribute
	@Property
	private VDRView currentVdrView;
	
	Object onActivate() {
		if (currentVdrView == null) {
			// deep jump into this page?
			return Index.class;
		}	
		
		return null;
	}
}

package vdr.jonglisto.web.pages;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Meta;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.SessionAttribute;

import vdr.jonglisto.lib.model.VDRView;

@Import(stylesheet = "META-INF/assets/css/Timer.css")
public class Timer {

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

		if (currentVdrView == null) {
			// deep jump into this page?
			return Index.class;
		}		
		
		newTimerUseId = useid;
		this.channelName = channelName;
		
		return null;
	}
}

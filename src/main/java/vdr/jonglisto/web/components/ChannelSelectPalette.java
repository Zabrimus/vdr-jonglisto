package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.SelectModelFactory;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.web.encoder.ChannelEncoder;

public class ChannelSelectPalette extends BaseComponent {

	@Inject
	SelectModelFactory selectModelFactory;	

	@Property
	private ChannelEncoder encoder;
	
	@Property 
	private List<Channel> channels;
	
	@Property
	private SelectModel model;
	
	@Property
	@Persist
	private List<Channel> selectedValues;
	
	public void beginRender() {
		if (channels == null) {
			channels = vdrDataService.getChannelsMap(currentVdrView.getChannelVdr().get()).orElse(Collections.emptyList());
			encoder = new ChannelEncoder(channels);
			
			model = selectModelFactory.create(channels, "name");
		}
		
        if (selectedValues == null) {
            selectedValues = new ArrayList<>();
        }		
	}
}

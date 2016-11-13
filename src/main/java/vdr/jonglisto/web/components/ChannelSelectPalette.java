package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.SelectModelFactory;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.web.encoder.ChannelEncoder;

public class ChannelSelectPalette extends BaseComponent {

	@Inject
	SelectModelFactory selectModelFactory;

	@InjectComponent
	private Zone channelPaletteZone;

	@Property
	@Parameter
	private List<Channel> selectedChannels;

	@Persist
	@Property
	private ChannelEncoder encoder;

	@Property
	private List<Channel> channels;

	@Persist
	@Property
	private SelectModel model;

	@Persist
	@Property
	private List<String> groups;

	@Property
	@Persist
	private boolean sortAlpha;
	
	@Property
	@Persist
	private String group;

	public void beginRender() {
		if (channels == null) {
			channels = vdrDataService.getChannelsMap(currentVdrView.getChannelVdr().get())
					.orElse(Collections.emptyList());
			encoder = new ChannelEncoder(channels);

			model = selectModelFactory.create(channels, "name");
		}

		groups = dataService.getGroups(getChannelUuid()).orElse(Collections.emptyList());

		if (selectedChannels == null) {
			selectedChannels = new ArrayList<>();
		}
	}

	public void onPalSelectAlpha(@RequestParameter(value = "param", allowBlank = true) String param,
			@RequestParameter(value = "add", allowBlank = true) String add) {

		sortAlpha = !sortAlpha;
		setSelectValues(group, add);
	}

	public void onPalSelectGroup(@RequestParameter(value = "param", allowBlank = true) String param,
			@RequestParameter(value = "add", allowBlank = true) String add) {

		setSelectValues(param, add);
	}

	public void onValueChangedFromGroup(String selectedGroup) {
		channels = vdrDataService.getChannelsInGroup(currentVdrView.getChannelVdr().get(), selectedGroup)
				.orElse(Collections.emptyList());
		encoder = new ChannelEncoder(channels);
		model = selectModelFactory.create(channels, "name");

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(channelPaletteZone);
		}
	}

	private <T> void setSelectValues(String group, String preSelectedValues) {
		selectedChannels = new ArrayList<>();
		
		if (group != null) {
			// only channels in group
			channels = vdrDataService.getChannelsInGroup(currentVdrView.getChannelVdr().get(), group).orElse(Collections.emptyList());
		} else {
			// VDR, all channels, because group is null
			channels = vdrDataService.getChannels(currentVdrView.getChannelVdr().get()).orElse(Collections.emptyList());
		}
		
		// sort list
		if (sortAlpha) {			
			channels.sort(new Comparator<Channel>() {
				public int compare(Channel o1, Channel o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());				
				}
			});
		}

		// set preselected values
		if (preSelectedValues != null) {
			JSONArray array = new JSONArray(preSelectedValues);
			
			List<String> list = new ArrayList<String>();
			for (int i=0; i < array.length(); i++) {
			    list.add( array.getString(i) );
			}

			list.stream().forEach(s -> selectedChannels.add(vdrDataService.getChannel(currentVdrView.getChannelVdr().get(), s).get()));
			
			// the preselected values must be also in the whole list
			selectedChannels.stream().forEach(s -> {
				if (!channels.contains(s)) {
					channels.add(s);
				}
			});
			
			if (sortAlpha) {
				selectedChannels.sort(new Comparator<Channel>() {
					public int compare(Channel o1, Channel o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());				
					}
				});
			}
		}
		
		encoder = new ChannelEncoder(channels);
		model = selectModelFactory.create(channels, "name");
		
		this.group = group;
		
		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(channelPaletteZone);
		}
	}
	
}

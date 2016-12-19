package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;

import vdr.jonglisto.lib.model.Channel;

@Import(library = { "webjars:multiselect-two-sides:$version/js/multiselect.js" })
public class ChannelSelectPalette2 extends BaseComponent {
	@Inject
	SelectModelFactory selectModelFactory;

	@InjectComponent
	private Zone channelPaletteZone;

	@Property
	@Parameter
	private List<Channel> selectedChannels;

	@Property
	@Parameter
	private boolean includeRadio;

	@Persist
	@Property
	private Map<String, List<Channel>> channels;

	@Persist
	@Property
	private List<String> groups;

	@Property
	@Persist
	private boolean sortAlpha;

	@Property
	@Persist
	private String group;

	@Property
	private String lastGroup;

	@Persist
	private boolean includeRadioSaved;

	@Property
	private Channel currentChannel;

	public void beginRender() {
		includeRadioSaved = includeRadio;

		groups = dataService.getGroups(getChannelUuid()).orElse(Collections.emptyList());

		if (channels == null) {
			channels = new HashMap<>();

			groups.stream().forEach(s -> {
				channels.put(s,
						vdrDataService.getChannelsInGroup(currentVdrView.getChannelVdr().get(), s, includeRadioSaved)
								.orElse(Collections.emptyList()));
			});
		}

		if (selectedChannels == null) {
			selectedChannels = new ArrayList<>();
		}
	}

	public void afterRender() {
		javaScriptSupport.require("multiselect");
	}

	public List<Channel> getChannelsInGroup() {
		return channels.get(group);
	}

	public List<Channel> getSelectedChannelsInGroup() {
		return selectedChannels.stream().filter(s -> s.getGroup().equals(group)).collect(Collectors.toList());
	}

	public boolean hasChannelsInGroup() {
		return selectedChannels.stream().filter(s -> s.getGroup().equals(group)).findFirst().isPresent();
	}
	
	public void processSelectedChannels(Request request) {
		String[] channelIds = request.getParameters("to");
		selectedChannels = Arrays.stream(channelIds) //
				.map(s -> vdrDataService.getChannel(currentVdrView.getChannelVdr().get(), s).get()) //
				.collect(Collectors.toList());
	}
}

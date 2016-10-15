package vdr.jonglisto.web.components;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.SelectModelFactory;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria;
import vdr.jonglisto.web.encoder.ChannelEncoder;

@Import(stylesheet = "META-INF/assets/css/EpgLayout.css")
public class EpgLayout extends BaseComponent {

	public enum Function {
		LIST, INFO;
	}

	private static DateTimeFormatter localTimeformatter = DateTimeFormatter.ofPattern("HH:mm");

	private Function function;

	@InjectComponent
	protected Epg epg;

	@InjectComponent
	protected Zone epgCriteriaZone;

	@Inject
	@Property	
	private ChannelEncoder channelEncoder;

	@Inject
	private SelectModelFactory selectModelFactory;

	@SessionAttribute("epgSearchCriteria")
	@Property
	private EpgSearchCriteria epgCriteria;
	
	@Persist
	@Property
	private List<String> groups;

	@Persist
	@Property
	private SelectModel channelSelectModel;

	@Persist
	private List<Channel> channels;

	@Persist
	@Property
	private List<String> categories;

	@Persist
	@Property
	private List<String> genres;

	@Property
	private Long epgDetailUseId;
	
	@Property
	private String epgDetailChannelName;

	@SetupRender
	public void setupRender() {
		String channelId = request.getParameter("channelId");
		String searchTime = request.getParameter("searchTime");
		
		Optional<Channel> channel;

		if (channelId != null) {
			channel = dataService.getChannel(getChannelUuid(), channelId);
		} else {
			channel = Optional.empty();
		}
		
		if (epgCriteria.isReset() || channel.isPresent()) {
			epgCriteria.setReset(false);
			
			groups = dataService.getGroups(getChannelUuid()).orElse(Collections.emptyList());

			if (channel.isPresent()) {
				epgCriteria.setChannelGroup(channel.get().getGroup());
				epgCriteria.setChannel(channel.get());
				
				channels = dataService.getChannelsInGroup(getChannelUuid(), channel.get().getGroup()).get();
			} else {
				if (groups.size() > 0) {
					channels = dataService.getChannelsInGroup(getChannelUuid(), groups.get(0)).orElse(Collections.emptyList());
					epgCriteria.setChannelGroup(groups.get(0));
				} else {
					channels = dataService.getChannels(getChannelUuid()).orElse(Collections.emptyList());
					epgCriteria.setChannelGroup(null);
				}
	
				if (channels.size() > 0) {
					epgCriteria.setChannel(channels.get(0));
				}
			}
		}

		if (searchTime != null) {
			epgCriteria.setTime(Long.parseLong(searchTime));
		}
		
		updateData();
	}

	public void updateData(List<String> genres, List<String> categories) {
		this.genres = genres;
		this.categories = categories;
	}

	public void onValueChangedFromGroup(String selectedGroup) {
		epgCriteria.setChannelGroup(selectedGroup);

		// reset category and genre
		epgCriteria.setCategory(null);
		epgCriteria.setGenre(null);

		if (selectedGroup != null) {
			channels = dataService.getChannelsInGroup(getChannelUuid(), selectedGroup).orElse(Collections.emptyList());
		} else {
			channels = dataService.getChannels(getChannelUuid()).orElse(Collections.emptyList());
		}

		epgCriteria.setChannel(channels.get(0));

		updateData();
	}

	public void onValueChangedFromChannel1(Channel selectedChannel) {
		epgCriteria.setChannel(selectedChannel);
		updateData();
	}

	public void onValueChangedFromChannel2(Channel selectedChannel) {
		onValueChangedFromChannel1(selectedChannel);
	}

	public void onValueChangedFromGenre(String selectedGenre) {
		epgCriteria.setGenre(selectedGenre);
		updateData();
	}

	public void onValueChangedFromCategory(String selectedCategory) {
		epgCriteria.setCategory(selectedCategory);
		updateData();
	}

	public void onSelectedFromSearchText() {
		updateData();
	}

	public void onSelectedFromResetText() {
		epgCriteria.setSearchText(null);
		updateData();
	}

	public void onSelectedFromSearchTime() {
		updateData();
	}

	public void onSelectedFromResetTime() {
		epgCriteria.setTime(null);
		updateData();
	}

	public List<Channel> getChannels() {
		return channels;
	}

	public String getTimeStr() {
		if (epgCriteria.getTime() != null) {
			return LocalDateTime.ofInstant(Instant.ofEpochSecond(epgCriteria.getTime()), ZoneId.systemDefault())
					.toLocalTime().format(localTimeformatter);
		} else {
			return null;
		}
	}

	public void setTimeStr(String st) {
		int idx = st.indexOf(':');

		epgCriteria.setTime( //
				LocalDateTime.now() //
						.withHour(Integer.valueOf(st.substring(0, idx))) //
						.withMinute(Integer.valueOf(st.substring(idx + 1))) //
						.atZone(ZoneOffset.systemDefault()) //
						.toEpochSecond());
	}
	
	public boolean isFunction(Function function) {
		return function == this.function;
	}

	public void onShowEpg(Long useId, String channelName) {
		epgDetailUseId = useId;
		epgDetailChannelName = channelName;
		
		function = Function.INFO;		
		epg.showInfoZone();		
	}

	private void updateData() {
		function = Function.LIST;
		epg.hideInfoZone();
		
		genres = Collections.emptyList();
		categories = Collections.emptyList();

		channelEncoder.addChannels(channels);
		channelSelectModel = selectModelFactory.create(channels, "name");

		componentResources.triggerEvent("updateEpg", null, null);
		
		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(epgCriteriaZone);			
		}
	}
}

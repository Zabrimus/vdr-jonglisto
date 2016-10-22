package vdr.jonglisto.web.components;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.SearchTimer;
import vdr.jonglisto.web.conduit.SearchTimerConduit;

@Import(stylesheet = "META-INF/assets/css/SearchTimerView.css")
public class SearchTimerView extends BaseComponent {
	
	@Inject
	protected BeanModelSource beanModelSource;
	
	@Inject
	protected Messages messages;
	
	@InjectComponent
	private Zone searchTimerListZone;
	
	@Property
	private BeanModel<Object> searchTimerModel;
	
	@Property
	private List<SearchTimer> searchTimers;
	
	@Property
	private SearchTimer searchTimer;
	
	@Property
	private String currentChannel;
	
	public SearchTimerView() {
		searchTimerModel = beanModelSource.createDisplayModel(Object.class, messages);
		
		searchTimerModel.add("active", new SearchTimerConduit("active", Integer.class));
		searchTimerModel.add("name", new SearchTimerConduit("name", String.class));
		searchTimerModel.add("timerAction", new SearchTimerConduit("active", String.class));
		searchTimerModel.add("channels", new SearchTimerConduit("active", String.class));
		searchTimerModel.add("channelFormat", new SearchTimerConduit("active", String.class));
		searchTimerModel.add("searchText1", new SearchTimerConduit("active", String.class));
		searchTimerModel.add("searchText2", new SearchTimerConduit("active", String.class));
		searchTimerModel.add("directoy", new SearchTimerConduit("directory", String.class));
		searchTimerModel.addEmpty("action");
	}

	void setupRender() {
		searchTimers = searchTimerService.getSearchTimers();
		searchTimerService.performSearch(searchTimers.get(0));
	}
	
	public void onToggleSearchTimerActive(Long id) {
		searchTimerService.toggleActive(id);
		
		searchTimers = searchTimerService.getSearchTimers();		
		
		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(searchTimerListZone);
		}
	}
	
	public void onExecuteSearchTimer(Long id) {
		// TODO: implement this
		System.err.println("execute search timer is not yet implemented");
	}

	public void onEditSearchTimer(Long id) {
		// TODO: implement this
		System.err.println("edit search timer is not yet implemented");
	}

	public void onDeleteSearchTimer(Long id) {
		// TODO: implement this
		System.err.println("delete search timer is not yet implemented");
	}

	public void onNewSearchTimer(Long id) {
		// TODO: implement this
		System.err.println("new search timer is not yet implemented");
	}
	
	public boolean isActive() {
		return ((Long)searchTimer.get("active")) != 0L;
	}
	
	public String getTimerAction() {
		switch ((String)searchTimer.get("type")) {
		case "R": return "aufnehmen";
		case "V": return "umschalten";
		case "S": return "suchen";
		default: return "<unbekannt>";
		}
	}
	
	public List<String> getChannels() {
		String channelIds = searchTimer.getString("channelids");		
		return Arrays.stream(channelIds.split(",")) //
				.map(ch -> vdrDataService.getChannel(getTimerUuid(), ch)) // 
				.map(ch -> ch.orElse(Channel.emptyChannel).getName()) //
				.collect(Collectors.toList());
	}
}

package vdr.jonglisto.web.components;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.SearchTimer;

public class SearchTimerView extends BaseComponent {
	
	public enum Function {
		LIST, EDIT, EPG, NEWTIMER;
	}	
	
	@Inject
	protected BeanModelSource beanModelSource;
	
	@Inject
	protected Messages messages;
	
	@InjectComponent
	private Zone searchTimerListZone;
	
	@Inject
	private Block view;
	
	@Inject
	private Block edit;
	
	@Property
	private BeanModel<Object> searchTimerModel;
	
	@Property
	private List<SearchTimer> searchTimers;
	
	@Persist
	@Property
	private SearchTimer searchTimer;

	@Persist
	private Long searchTimerId;
	
	@Property
	private String currentChannel;
	
	@Property
	private Function function;

	public SearchTimerView() {
		function = Function.LIST;
	}

	void setupRender() {
		searchTimers = searchTimerService.getSearchTimers();
		function = Function.LIST;
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
		function = Function.EDIT;
		searchTimer = searchTimerService.getSearchTimer(id);
		searchTimerId = id;
				
		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(searchTimerListZone);
		}
	}

	public void onDeleteSearchTimer(Long id) {
		searchTimerService.deleteSearchTimer(id);
		
		searchTimers = searchTimerService.getSearchTimers();		
		
		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(searchTimerListZone);
		}
	}

	public void onNewSearchTimer() {
		// TODO: implement this
		System.err.println("new search timer is not yet implemented");
	}
	
	public void onPrepareForSubmit() {
		searchTimer = searchTimerService.getSearchTimer(searchTimerId);
	}
	
	void onSuccess() {
		if (searchTimerId == null) {
			return;
		}

		// Create or update timer
		if (searchTimer.getId() == null) {
			// create timer
			// dataService.createTimer(getTimerUuid(), timer);			
		} else {
			// update timer
			// Timer oldTimer = dataService.getTimerById(getTimerUuid(), timer.getId()).get();
			// dataService.updateTimer(getTimerUuid(), oldTimer, timer);
		}
		
		function = Function.LIST;
		searchTimer = null;
		
		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(searchTimerListZone);
		}
		
		searchTimers = searchTimerService.getSearchTimers();		
	}
	
    void onCancel() {
    	searchTimerId = null;
    }
	
	public boolean isListFunction() {
		return function == Function.LIST || function == Function.NEWTIMER;
	}
	
	public boolean isEditFunction() {
		return function == Function.EDIT || function == Function.NEWTIMER;
	}
	
	public boolean isNewSearchTimerFunction() {
		return function == Function.NEWTIMER;
	}
	
	public String getTimerAction() {
		switch (searchTimer.getType()) {
		case "R": return "aufnehmen";
		case "V": return "umschalten";
		case "S": return "suchen";
		default: return "<unbekannt>";
		}
	}
	
	public void setTimerAction(String s) {
		System.err.println("SetTimerAction: " + s);
	}
	
	public List<String> getChannels() {
		String channelIds = searchTimer.getChannels();		
		return Arrays.stream(channelIds.split(",")) //
				.map(ch -> vdrDataService.getChannel(getTimerUuid(), ch)) // 
				.map(ch -> ch.orElse(Channel.emptyChannel).getName()) //
				.collect(Collectors.toList());
	}
	
	public Object getActiveBlock()
    {
		switch(function) {
		case EDIT:
			return edit;
				
		case LIST:
			return view;
		
		default:
			return view;
		}
    }
}

package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.SelectModelFactory;

import vdr.jonglisto.lib.SearchTimerService;
import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.SearchTimer;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.web.encoder.VDREncoder;
import vdr.jonglisto.web.services.GlobalValues;

public class SearchTimerView extends BaseComponent {
	
	public enum Function {
		LIST, EDIT, EPG, NEWTIMER;
	}	
	
	@Inject
	protected BeanModelSource beanModelSource;
	
	@Inject
	protected Messages messages;
	
	@Inject
	SelectModelFactory selectModelFactory;
	
	@Inject
	private GlobalValues globalValues;
	
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

	@Property
	@Persist
	private List<Channel> selectedChannels;
	
	@Property
	@Persist
	private List<String> selectedCategories;
	
	@Persist
	@Property
	private StringValueEncoder categoryEncoder;
	
	@Persist
	@Property
	private SelectModel categoryModel;

	@Property
	@Persist
	private List<String> selectedGenres;
	
	@Persist
	@Property
	private StringValueEncoder genreEncoder;
	
	@Persist
	@Property
	private VDREncoder vdrEncoder;
	
	@Persist
	@Property
	private SelectModel genreModel;

	@Persist
	@Property
	private SelectModel vdrModel;
	
	@Persist
	@Property
	private VDR selectedVdr;
	
	void setupRender() {
		searchTimers = searchTimerService.getSearchTimers();
		function = Function.LIST;

		categoryModel = selectModelFactory.create(globalValues.getCategories());
		categoryEncoder = new StringValueEncoder();
		selectedCategories = new ArrayList<>();
		
		genreModel = selectModelFactory.create(globalValues.getGenres());
		genreEncoder = new StringValueEncoder();
		selectedGenres = new ArrayList<>();
		
		List<VDR> v = configuration.getSortedVdrList();
		vdrModel = selectModelFactory.create(v, "displayName");
		vdrEncoder = new VDREncoder(v);
	}
	
	public void afterRender() {
		// disabled, because i'm not sure if this is really useful
		// javaScriptSupport.require("hideme").with(".clickToHide");
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
		
		selectedChannels = new ArrayList<>();
		if (searchTimer.getChannelsList() != null) {
			searchTimer.getChannelsList().stream().forEach(s -> selectedChannels.add(vdrDataService.getChannel(currentVdrView.getChannelVdr().get(), s).get()));
		}

		selectedCategories = new ArrayList<>();
		if (searchTimer.getCategory() != null) {
			searchTimer.getCategory().stream().forEach(s -> selectedCategories.add(s));
		}
		
		selectedGenres = new ArrayList<>();
		if (searchTimer.getGenre() != null) {
			searchTimer.getGenre().stream().forEach(s -> selectedGenres.add(s));
		}
	
		selectedVdr = configuration.getVdr(currentVdrView.getRecordingVdr().get()); 
				
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
	
	void onFailure() {
	}
	
	void onSuccess() {
		if (searchTimerId == null) {
			return;
		}

		// set channels, genre and category
		searchTimer.setChannelsList(selectedChannels.stream().map(s -> s.getId()).collect(Collectors.toList()));
		searchTimer.setGenre(selectedGenres);
		searchTimer.setCategory(selectedCategories);
		
		// Alle Werte mal ausgeben:
		System.err.println("Timer1: " + searchTimer);
		
		// Create or update timer
		if (searchTimer.getId() == null) {			
			// create timer
			searchTimerService.insertSearchTimer(searchTimer);
		} else {
			// update timer
			searchTimerService.updateSearchTimer(searchTimer);
		}
		
		function = Function.LIST;
		searchTimer = null;
		
		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(searchTimerListZone);
		}
		
		searchTimers = searchTimerService.getSearchTimers();		
	}
	
    void onCancel() {
    	System.err.println("onCancel()");
    	
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
		return searchTimer.getChannelsList().stream() //
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
	
	public void onRenderZone() {
		// disabled, because i'm not sure if this is really useful
		// javaScriptSupport.require("hideme").with(".clickToHide");		
	}
}

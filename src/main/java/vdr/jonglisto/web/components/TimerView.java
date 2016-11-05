package vdr.jonglisto.web.components;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import vdr.jonglisto.lib.model.Timer;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.util.DateTimeUtil;

public class TimerView extends BaseComponent {

	public enum Function {
		LIST, EDIT, EPG, NEWTIMER;
	}	
	
	@Parameter
	@Property
	private String newTimerUseId;

	@Parameter
	@Property
	private String newTimerChannelName;

	@InjectComponent
	private Zone timerListZone;

	@InjectComponent
	private Zone timerEditZone;

	@InjectComponent
	protected Epg epg;
	
	@Property
	private Function function;

	@Property
	private List<Timer> timers;
	
	@Persist
	@Property
	private Timer timer;
	
	@Property
	private boolean selectAllTimer;

	private List<String> timerToChange;
	
	private String timerId;
	
	void setupRender() {
		timers = dataService.getTimer(getTimerUuid()).orElse(Collections.<Timer>emptyList());
		
		if (newTimerUseId != null) {
			function = Function.NEWTIMER;
		} else {
			function = Function.LIST;
		}
	}

	public boolean isListFunction() {
		return function == Function.LIST || function == Function.NEWTIMER;
	}
	
	public boolean isEditFunction() {
		return function == Function.EDIT || function == Function.NEWTIMER;
	}
	
	public boolean isNewTimerFunction() {
		return function == Function.NEWTIMER;
	}
	
	public boolean hasEventId() {
		return timer != null && timer.getEventId() != -1;
	}
	
	public boolean hasTimer() {
		return timers != null && timers.size() > 0;
	}

	public boolean isSelected() {
		return false;
	}

	public String getTimerId() {
		timerId = timer.getId();
		return timerId;
	}

	public void setTimerId(String id) {
		timerId = id;
	}
	
	public void setSelected(boolean checkbox) {
		if (checkbox) {
			timerToChange.add(timerId);
		}
	}
	
	void onPrepareForSubmit() {
		timerToChange = new ArrayList<String>();
	}
	
	public void onShowEpg(String tiId) {
		// timer neu laden
		timer = dataService.getTimerById(getTimerUuid(), tiId).get();

		epg.setTimer(timer);
		epg.showInfoZone();
	}
	
	void onToggleTimerActive(String id) {
		Timer currentTimer = dataService.getTimerById(getTimerUuid(), id).get();
		
		if (currentTimer.getIsActive()) {
			dataService.deactivateTimer(getTimerUuid(), id);
		} else {
			dataService.activateTimer(getTimerUuid(), id);
		}
	}

	void onDeleteTimer(String id) {
		dataService.deleteTimer(getTimerUuid(), id);
	}

	void onCreateTimer() {
		VDR vdr = configuration.getVdr(getTimerUuid());
		
		timer = new Timer();
		
		// read epg data for the useId
		Map<String, Object> epg = epgDataService.getEpgDataForUseId(Long.parseLong(newTimerUseId));
		
		// Copy some values from epg to timer
		timer.setId("__NEWTIMER__"); // indicates a new timer
		timer.setIsActive(true); // timer is active per default
		timer.setChannelName(encoder.decode(newTimerChannelName));
		
		timer.setTitle((String) epg.get("title"));
		timer.setShortText((String) epg.get("shorttext"));
		
		timer.setWeekdays("-------");
		timer.setAux(vdr.getTimerAux());
		
		// adjust start and stop time
		LocalDateTime start = DateTimeUtil.toDateTime(((Integer)epg.get("starttime")).longValue());
		LocalDateTime stop = DateTimeUtil.toDateTime((Long)epg.get("endtime"));
		
		start = start.minusMinutes(vdr.getTimerMinusMin());
		stop = stop.plusMinutes(vdr.getTimerPlusMin());
		
		timer.setStartTimestamp(start);
		timer.setStopTimestamp(stop);
				
		timer.setPriority(vdr.getTimerPrio()); 
		timer.setLifetime(vdr.getTimerLifetime());
		
		timer.setFilename(epgDataService.getVdrTimerName(Long.valueOf(newTimerUseId), vdr.getDefaultRecordingNamingMode())); 
		
		// Info: Neither eventid nor useid is accepted. But why?
		// It seems i do something wrong.
		// timer.setEventId(((BigInteger) epg.get("eventid")).intValue());
		
		timer.setChannel((String) epg.get("channelid"));
						
		javaScriptSupport.require("dialogmodal").invoke("activate").with("timerEdit", new JSONObject());
	}
	
	void onEditTimer(String id) {
		function = Function.EDIT;
		timer = dataService.getTimerById(getTimerUuid(), id).get();
		
		// read epg data for the useId
		Map<String, Object> epg = epgDataService.getEpgDataForUseId(new Long(timer.getEventId()));

		timer.setTitle((String) epg.get("title"));
		timer.setShortText((String) epg.get("shorttext"));
		
		showEditZone();
	}

	void onActivateTimers() {
		for (String id : timerToChange) {
			dataService.activateTimer(getTimerUuid(), id);
		}
	}

	void onDeactivateTimers() {
		for (String id : timerToChange) {
			dataService.deactivateTimer(getTimerUuid(), id);
		}
	}

	void onDeleteTimers() {
		dataService.bulkDeleteTimer(getTimerUuid(), timerToChange);
	}

	void onMoveTimers() {
		// TODO: Timer an anderen VDR verschieben
		// timerToChange
		
		/*
		if (selectedVdr != null) {
			log.info("Verschiebe Time nach " + selectedVdr.getName() + " von " + currentVdr.getName());
			for (String s : timerToChange) {
				log.info("  -> " + s);
			}
		} else {
			log.error("Keine Auswahl getroffen!");
		}
		*/
	}

	void onSuccess() {
		if (timer == null) {
			return;
		}

		// Create ot update timer
		if (timer.getId().equals("__NEWTIMER__")) {
			// create timer
			dataService.createTimer(getTimerUuid(), timer);			
		} else {
			// update timer
			Timer oldTimer = dataService.getTimerById(getTimerUuid(), timer.getId()).get();
			dataService.updateTimer(getTimerUuid(), oldTimer, timer);
		}
		
		function = Function.LIST;
		timer = null;
		showGridZone();
	
		timers = dataService.getTimer(getTimerUuid()).orElse(Collections.<Timer>emptyList());
	}
	
    void onCancel() {
    	timer = null;
    }

	
	private JavaScriptCallback makeScriptToShowEditModal() {
		return new JavaScriptCallback() {
			public void run(JavaScriptSupport javascriptSupport) {
				javaScriptSupport.require("dialogmodal").invoke("activate").with("timerEdit", new JSONObject());
			}
		};
	}

	private JavaScriptCallback makeScriptToShowGridModal() {
		return new JavaScriptCallback() {
			public void run(JavaScriptSupport javascriptSupport) {			
				javaScriptSupport.require("dialogmodal").invoke("hide").with("timerEdit");
			}
		};
	}

	private void showEditZone() {
		if (request.isXHR()) {
			ajaxResponseRenderer.addCallback(makeScriptToShowEditModal());
			ajaxResponseRenderer.addRender(timerEditZone);
		}
	}

	private void showGridZone() {
		if (request.isXHR()) {
			ajaxResponseRenderer.addCallback(makeScriptToShowGridModal());
			ajaxResponseRenderer.addRender(timerEditZone).addRender(timerListZone);
		}
	}

}

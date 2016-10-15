package vdr.jonglisto.web.components;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import vdr.jonglisto.lib.model.EPGMedia;
import vdr.jonglisto.lib.model.EPGMedia.MediaType;
import vdr.jonglisto.lib.model.Timer;

@Import(stylesheet = "META-INF/assets/css/Epg.css")
public class Epg extends BaseComponent {
	
	@Parameter
	private Long useId;
	
	@Property
	@Parameter(required = true)
	private String channelName;

	@Property
	@Parameter 
	private boolean isRecording;

	@Property
	@Parameter 
	private String recFilename;
	
	@PageActivationContext
	@Property
	private String epgTab;
	
	@InjectComponent
	protected Zone epgInfoZone;

	@Property
	private Map<String, Object> epg;

	@Persist
	private Long savedUseId;

	@Persist
	@Property
	private String filename;
	
	@Persist
	@Property
	private boolean recordingFlag; 

	@Property
	private List<EPGMedia> allMedia;
	
	@Property
	private EPGMedia epgMedia;
	
	@Persist
	private Set<MediaType> availableMediaTypes;

	@Property
    private String epgInfoModalId = "epgInfoModal";

	@Property
	private boolean visible;

	private Timer timer;
	
	// Trigger from Epg.tml to reload the epg data
	public void onLoadEpgData() {
		if (epg == null) {
			if (recordingFlag) {
				// special handling for recordings
				// epg = epgDataService.getEpgDataForRecording(recFilename);
				epg = epgDataService.getEpgDataForRecording(filename);
				channelName = (String) epg.get("channelname");
			} else {
				// normal processing
				epg = epgDataService.getEpgDataForUseId(savedUseId);
			}
		}
	}
	
	// nasty, but trigger does now allow parameters
	public void onReloadEpgImagesBanner() {
		reloadEpgImages(MediaType.Banner);
	}
	
	public void onReloadEpgImagesPoster() {
		reloadEpgImages(MediaType.Poster);
	}
	
	public void onReloadEpgImagesSeasonPoster() {
		reloadEpgImages(MediaType.SeasonPoster);
	}
	
	public void onReloadEpgImagesFanArt() {
		reloadEpgImages(MediaType.FanArt);
	}
	
	public void onReloadEpgImagesEpisodePic() {
		reloadEpgImages(MediaType.EpisodePic);
	}
	
	public void onReloadEpgImagesActor() {
		reloadEpgImages(MediaType.Actor);
	}
	
	public boolean onEditTimer(String id) {
		visible = false;
		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(epgInfoZone);
		}
		
		return false;
	}
	
	public List<String> getImageFilenames() {
		List<String> result;
		
		if (recordingFlag) {
			result = epgImageService.getImageFilenames(filename);
		} else {
			result = epgImageService.getImageFilenames(savedUseId);
		}
		
		return result;
	}
	
	public boolean isMediaTypeAvailable(MediaType type) {		
		return availableMediaTypes.contains(type);
	}
	
	private void reloadEpgImages(MediaType type) {		
		allMedia = epgImageService.getEpgMedia(savedUseId, filename, type);
	}
	
	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public void showInfoZone() {
		visible = true;
		
		savedUseId = useId;			
		filename = recFilename;
		recordingFlag = isRecording;
		availableMediaTypes = epgImageService.getAvailableMediaTypes(savedUseId, filename);
		
		onLoadEpgData();
		
		if (request.isXHR()) {
			ajaxResponseRenderer.addCallback(makeScriptToShowModal());
			ajaxResponseRenderer.addRender(epgInfoZone);
		}
	}

	public void hideInfoZone() {
		visible = false;
		
		if (request.isXHR()) {
			ajaxResponseRenderer.addCallback(makeScriptToHideModal());
			ajaxResponseRenderer.addRender(epgInfoZone);
		}
	}
	
	public JavaScriptCallback makeScriptToShowModal() {
        return new JavaScriptCallback() {
            public void run(JavaScriptSupport javascriptSupport) {
                javaScriptSupport.require("dialogmodal").invoke("activate").with(epgInfoModalId, new JSONObject());
            }
        };
    }

    public JavaScriptCallback makeScriptToHideModal() {
        return new JavaScriptCallback() {
            public void run(JavaScriptSupport javascriptSupport) {
                javaScriptSupport.require("dialogmodal").invoke("hide").with(epgInfoModalId);
            }
        };
    }

}

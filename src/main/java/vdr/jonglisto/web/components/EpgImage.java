package vdr.jonglisto.web.components;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.services.Response;

import vdr.jonglisto.lib.model.EPGMedia;

public class EpgImage extends BaseComponent {
	
	@Property
	@Parameter
	private EPGMedia imageKey;
	
	@Property
	@Parameter
	private String filename;
	
	@Property
	@Parameter(required = true)
	private Boolean scraper;
	
	@Property
	@Parameter
	private Boolean isActor;
	
	public Link getEpgImageLink() {
		if (scraper) {
			switch (imageKey.getType()) {
			case Movie: 
				return componentResources.createEventLink("movieMediaImage", imageKey.getMovieId(), imageKey.getActorId(), imageKey.getMediaType());

			case Series:
				return componentResources.createEventLink("seriesMediaImage", imageKey.getSeriesId(), imageKey.getSeasonNumber(), imageKey.getEpisodeId(), imageKey.getActorId(), imageKey.getMediaType());
				
			default:
				return null;
			}
		} else {
			return componentResources.createEventLink("epgImage", filename);
		}
	}
	
	@OnEvent(value = "epgImage")
	public StreamResponse createEpgImage(String key) {
		return new StreamResponse() {
			InputStream inputStream;

			@Override
			public void prepareResponse(Response response) {
				try {					
					byte[] file = epgImageService.getEpgImage(key);
					inputStream = new ByteArrayInputStream(file);
					response.setHeader("Content-Length", "" + inputStream.available());						
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public String getContentType() {
				return "text/png";
			}

			@Override
			public InputStream getStream() throws IOException {
				return inputStream;
			}
		};
	}
	
	@OnEvent(value = "movieMediaImage")
	public StreamResponse createMovieMediaImage(long movieId, long actorId, int mediaType) {
		return new StreamResponse() {
			InputStream inputStream;

			@Override
			public void prepareResponse(Response response) {
				try {					
					byte[] file = epgImageService.getMovieMediaImage(movieId, mediaType, actorId);					
					inputStream = new ByteArrayInputStream(file);
					response.setHeader("Content-Length", "" + inputStream.available());						
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public String getContentType() {
				return "text/png";
			}

			@Override
			public InputStream getStream() throws IOException {
				return inputStream;
			}
		};
	}

	@OnEvent(value = "seriesMediaImage")
	public StreamResponse createSeriesMediaImage(long seriesId, long seasonNumber, long episodeId, long actorId, int mediaType) {
		return new StreamResponse() {
			InputStream inputStream;

			@Override
			public void prepareResponse(Response response) {
				try {					
					byte[] file = epgImageService.getSeriesMediaImage(seriesId, seasonNumber, episodeId, mediaType, actorId);					
					inputStream = new ByteArrayInputStream(file);
					response.setHeader("Content-Length", "" + inputStream.available());						
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public String getContentType() {
				return "text/png";
			}

			@Override
			public InputStream getStream() throws IOException {
				return inputStream;
			}
		};
	}
	
	public String getCssClass() {
		if (filename != null) {
			return "epg-image";
		} else {
			switch (imageKey.getTabType()) {
			case Actor:
				return "epg_image_actor";
				
			case Banner:
				return "epg_image_banner";
				
			case EpisodePic:
				return "epg_image_epipic";
				
			case FanArt:
				return "epg_image_fanart";
				
			case Poster:
				return "epg_image_poster";
				
			case SeasonPoster:
				return "epg_image_season";
				
			default:
				return "";
			}
		}
	}
}

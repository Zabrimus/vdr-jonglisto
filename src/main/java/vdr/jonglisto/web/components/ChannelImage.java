package vdr.jonglisto.web.components;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;

import vdr.jonglisto.web.services.GlobalLogoFilename;

@Import(stylesheet = "META-INF/assets/css/ChannelImage.css")
public class ChannelImage extends BaseComponent {

	@Inject
	private GlobalLogoFilename logoCache;
	
	@Property
	@Parameter(required = true)
	private String channelName;

	@Inject
	@Path("META-INF/assets/empty_channel.png")
	protected Asset emptyChannel;

	public boolean isLogoExists() {
		if (channelName == null) {
			return false;
		}

		InputStream inputStream = null;
		try {
			// inputStream = getResourceAsStream(channelName);
			inputStream = logoCache.getResource(channelName);
			return inputStream != null;			
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public Link getChannelImageLink() {
		return componentResources.createEventLink("channelImage", channelName);
	}

	@OnEvent(value = "channelImage")
	public StreamResponse createChannelImage(String channel) {
		return new StreamResponse() {
			InputStream inputStream;

			@Override
			public void prepareResponse(Response response) {
				try {
					// inputStream = getResourceAsStream(channel);
					inputStream = logoCache.getResource(channel);
					
					if (inputStream == null) {
						inputStream = emptyChannel.getResource().openStream();
					}
					
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

	private InputStream getResourceAsStream(String channel) {
		return logoCache.getResource(channel);
		
		
		
		
		
		/*
		if ((channel != null) && (channel.length() > 0)) {
			// new version, try to find the logo in logo.jar
			String filename = StringUtils.stripAccents(channel)//					
					.replaceAll("\\&", "and")//
					.replaceAll("\\+", "plus")//
					.replaceAll("\\*", "star");
			
			String filename2 = filename.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
			
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("/logo/" + filename2 + ".png");
			
			if (inputStream == null) {
				// try to find similar channel images
				filename2 = filename.replaceAll("\\w*\\(S\\)$", "").replaceAll("\\w*HD$", "").replaceAll("[^A-Za-z0-9]", "").toLowerCase();
				inputStream = this.getClass().getClassLoader().getResourceAsStream("/logo/" + filename2 + ".png");
			}

			return inputStream;
		}
		
		return null;
		*/
	}
}

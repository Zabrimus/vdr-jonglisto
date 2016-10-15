package vdr.jonglisto.web.components;

import java.io.File;
import java.io.FileInputStream;
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

@Import(stylesheet = "META-INF/assets/css/ChannelImage.css")
public class ChannelImage extends BaseComponent {

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
		
		File file = new File(configuration.getChannelImagePath() + channelName.toLowerCase() + ".png");
		
		if (file.exists()) {
			return true;
		}
		
		return false;
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
					File file = null;

					if ((channel != null) && (channel.length() > 0)) {
						file = new File(
								configuration.getChannelImagePath() + channel.toLowerCase() + ".png");

						if (!file.exists()) {
							file = null;
						}
					}

					if (file != null) {
						inputStream = new FileInputStream(file);
					} else {
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
}

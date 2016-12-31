package vdr.jonglisto.web.pages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;

import vdr.jonglisto.lib.ConfigurationService;

/*
 * Hacky page to bypass problems with ajax requests and StreamResponse as answer
 */
public class StreamResponsePage extends BasePage {

    @Inject
    private ConfigurationService configuration;

    @Persist("flash")
    private String streamContent;
    
    @Persist("flash")
    private String streamFilename;
    
    @Persist("flash")
    private String streamType;
    
    public StreamResponse onActivate() {
        return new StreamResponse() {
    
            InputStream inputStream;
    
            @Override
            public void prepareResponse(Response response) {
                try {
                    response.setHeader("Content-Disposition", "attachment; filename=" + streamFilename);
                    inputStream = new ByteArrayInputStream(streamContent.getBytes("UTF-8"));    
                    response.setHeader("Content-Length", "" + inputStream.available());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    
            @Override
            public String getContentType() {
                return streamType;
            }
    
            @Override
            public InputStream getStream() throws IOException {
                return inputStream;
            }
        };
    }

    public void setStreamContent(String streamContent) {
        this.streamContent = streamContent;
    }

    public void setStreamFilename(String streamFilename) {
        this.streamFilename = streamFilename;
    }
    
    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }
}

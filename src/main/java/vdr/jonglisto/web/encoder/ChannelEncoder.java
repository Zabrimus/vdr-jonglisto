package vdr.jonglisto.web.encoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;

import vdr.jonglisto.lib.model.Channel;

public class ChannelEncoder implements ValueEncoder<Channel>, ValueEncoderFactory<Channel> { 

	private Map<String, Channel> channels;

	public ChannelEncoder() {
		channels = new HashMap<>();
	}

	public ChannelEncoder(List<Channel> ch) {
		channels = new HashMap<>();
		addChannels(ch);
	}
	
    @Override
    public String toClient(Channel value) {
        return value.getId(); 
    }

    @Override
    public Channel toValue(String id) {
    	return channels.get(id);
    }

    @Override
    public ValueEncoder<Channel> create(Class<Channel> type) {
        return this; 
    }

	public void addChannels(List<Channel> ch) {
		ch.stream().forEach(s -> channels.put(s.getId(), s));
	}    
}
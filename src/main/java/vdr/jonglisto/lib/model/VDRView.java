package vdr.jonglisto.lib.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vdr.jonglisto.lib.ConfigurationService;

public class VDRView implements Comparable<VDRView> {
	
	public enum Type {
		VDR, View
	}
	
	private ConfigurationService config;
	
	private String displayName;
	private List<VDR> channelVdr = new ArrayList<>();
	private VDR timerVDR;
	private VDR headVDR;
	private List<VDR> recordingVdr = new ArrayList<>();
	
	private Type type;
	
	public VDRView(ConfigurationService config) {
		this.config = config;
	}
	
	public Optional<String> getChannelVdr() {
		try {
			return Optional.of(channelVdr.stream().filter(s -> config.testRestfulApi(s.getIp(), s.getRestfulApiPort())).findFirst().get().getUuid());
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	public Optional<String> getRecordingVdr() {
		try {
			return Optional.of(recordingVdr.stream().filter(s -> config.testRestfulApi(s.getIp(), s.getRestfulApiPort())).findFirst().get().getUuid());
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	public Optional<String> getTimerVdr() {
		try {
			return Optional.of(timerVDR.getUuid());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public Optional<String> getHeadVdr() {
		try {
			return Optional.of(headVDR.getUuid());
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	public void addChannelVdr(VDR v) {
		channelVdr.add(v);
	}

	public void addRecordingVdr(VDR v) {
		recordingVdr.add(v);
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setChannelVdr(List<VDR> channelVdr) {
		this.channelVdr = channelVdr;
	}

	public void setTimerVDR(VDR timerVDR) {
		this.timerVDR = timerVDR;
	}

	public void setHeadVDR(VDR headVDR) {
		this.headVDR = headVDR;
	}

	public void setRecordingVdr(List<VDR> recordingVdr) {
		this.recordingVdr = recordingVdr;
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "VDRView [displayName=" + displayName + ", channelVdr=" + channelVdr + ", timerVDR=" + timerVDR
				+ ", recordingVdr=" + recordingVdr + "]";
	}

	public int compareTo(VDRView o) {
		return displayName.compareTo(o.getDisplayName());
	}
}

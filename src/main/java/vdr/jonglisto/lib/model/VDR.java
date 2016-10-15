package vdr.jonglisto.lib.model;

public class VDR {

	private String uuid;
	private String ip;
	private String displayName;
	private String mac;
	private int svdrpPort;
	private int restfulApiPort;

	private String timerAux = "";
	private int timerMinusMin = 5;
	private int timerPlusMin = 5;
	private int timerPrio = 50;
	private int timerLifetime = 14;
	private RecordingNamingMode defaultRecordingNamingMode = RecordingNamingMode.Auto; 
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getSvdrpPort() {
		return svdrpPort;
	}

	public void setSvdrpPort(int svdrPort) {
		this.svdrpPort = svdrPort;
	}

	public int getRestfulApiPort() {
		return restfulApiPort;
	}

	public void setRestfulApiPort(int restfulApiPort) {
		this.restfulApiPort = restfulApiPort;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}
	
	public String getTimerAux() {
		return timerAux;
	}

	public void setTimerAux(String timerAux) {
		this.timerAux = timerAux;
	}

	public int getTimerMinusMin() {
		return timerMinusMin;
	}

	public void setTimerMinusMin(int timerMinusMin) {
		this.timerMinusMin = timerMinusMin;
	}

	public int getTimerPlusMin() {
		return timerPlusMin;
	}

	public void setTimerPlusMin(int timerPlusMin) {
		this.timerPlusMin = timerPlusMin;
	}

	public int getTimerPrio() {
		return timerPrio;
	}

	public void setTimerPrio(int timerPrio) {
		this.timerPrio = timerPrio;
	}

	public int getTimerLifetime() {
		return timerLifetime;
	}

	public void setTimerLifetime(int timerLifetime) {
		this.timerLifetime = timerLifetime;
	}

	public RecordingNamingMode getDefaultRecordingNamingMode() {
		return defaultRecordingNamingMode;
	}

	public void setDefaultRecordingNamingMode(RecordingNamingMode defaultRecordingNamingMode) {
		this.defaultRecordingNamingMode = defaultRecordingNamingMode;
	}

	@Override
	public String toString() {
		return "VDR [uuid=" + uuid + ", ip=" + ip + ", displayName=" + displayName + ", mac=" + mac + ", svdrpPort="
				+ svdrpPort + ", restfulApiPort=" + restfulApiPort + ", timerAux=" + timerAux + ", timerMinusMin="
				+ timerMinusMin + ", timerPlusMin=" + timerPlusMin + ", timerPrio=" + timerPrio + ", timerLifetime="
				+ timerLifetime + "]";
	}
}

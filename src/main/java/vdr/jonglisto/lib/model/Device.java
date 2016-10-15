package vdr.jonglisto.lib.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Device {

	@JsonProperty("name")
	private String name;

	@JsonProperty("dvb_c")
	private Boolean dvbC;

	@JsonProperty("dvb_s")
	private Boolean dvbS;

	@JsonProperty("dvb_t")
	private Boolean dvbT;

	@JsonProperty("atsc")
	private Boolean atsc;

	@JsonProperty("primary")
	private Boolean primary;

	@JsonProperty("has_decoder")
	private Boolean hasDecoder;

	@JsonProperty("number")
	private Integer number;

	@JsonProperty("channel_id")
	private String channelId;

	@JsonProperty("channel_name")
	private String channelName;

	@JsonProperty("channel_nr")
	private Integer channelNr;

	@JsonProperty("live")
	private Boolean live;

	@JsonProperty("has_ci")
	private Boolean hasCi;

	@JsonProperty("signal_strength")
	private Integer signalStrength;

	@JsonProperty("signal_quality")
	private Integer signalQuality;

	@JsonProperty("adapter")
	private Integer adapter;

	@JsonProperty("frontend")
	private Integer frontend;

	@JsonProperty("type")
	private String type;

	public Device() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getDvbC() {
		return dvbC;
	}

	public void setDvbC(Boolean dvbC) {
		this.dvbC = dvbC;
	}

	public Boolean getDvbS() {
		return dvbS;
	}

	public void setDvbS(Boolean dvbS) {
		this.dvbS = dvbS;
	}

	public Boolean getDvbT() {
		return dvbT;
	}

	public void setDvbT(Boolean dvbT) {
		this.dvbT = dvbT;
	}

	public Boolean getAtsc() {
		return atsc;
	}

	public void setAtsc(Boolean atsc) {
		this.atsc = atsc;
	}

	public Boolean getPrimary() {
		return primary;
	}

	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}

	public Boolean getHasDecoder() {
		return hasDecoder;
	}

	public void setHasDecoder(Boolean hasDecoder) {
		this.hasDecoder = hasDecoder;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public Integer getChannelNr() {
		return channelNr;
	}

	public void setChannelNr(Integer channelNr) {
		this.channelNr = channelNr;
	}

	public Boolean getLive() {
		return live;
	}

	public void setLive(Boolean live) {
		this.live = live;
	}

	public Boolean getHasCi() {
		return hasCi;
	}

	public void setHasCi(Boolean hasCi) {
		this.hasCi = hasCi;
	}

	public Integer getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(Integer signalStrength) {
		this.signalStrength = signalStrength;
	}

	public Integer getSignalQuality() {
		return signalQuality;
	}

	public void setSignalQuality(Integer signalQuality) {
		this.signalQuality = signalQuality;
	}

	public Integer getAdapter() {
		return adapter;
	}

	public void setAdapter(Integer adapter) {
		this.adapter = adapter;
	}

	public Integer getFrontend() {
		return frontend;
	}

	public void setFrontend(Integer frontend) {
		this.frontend = frontend;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adapter == null) ? 0 : adapter.hashCode());
		result = prime * result + ((atsc == null) ? 0 : atsc.hashCode());
		result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
		result = prime * result + ((channelName == null) ? 0 : channelName.hashCode());
		result = prime * result + ((channelNr == null) ? 0 : channelNr.hashCode());
		result = prime * result + ((dvbC == null) ? 0 : dvbC.hashCode());
		result = prime * result + ((dvbS == null) ? 0 : dvbS.hashCode());
		result = prime * result + ((dvbT == null) ? 0 : dvbT.hashCode());
		result = prime * result + ((frontend == null) ? 0 : frontend.hashCode());
		result = prime * result + ((hasCi == null) ? 0 : hasCi.hashCode());
		result = prime * result + ((hasDecoder == null) ? 0 : hasDecoder.hashCode());
		result = prime * result + ((live == null) ? 0 : live.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((primary == null) ? 0 : primary.hashCode());
		result = prime * result + ((signalQuality == null) ? 0 : signalQuality.hashCode());
		result = prime * result + ((signalStrength == null) ? 0 : signalStrength.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Device other = (Device) obj;
		if (adapter == null) {
			if (other.adapter != null) {
				return false;
			}
		} else if (!adapter.equals(other.adapter)) {
			return false;
		}
		
		if (atsc == null) {
			if (other.atsc != null) {
				return false;
			}
		} else if (!atsc.equals(other.atsc)) {
			return false;
		}
		
		if (channelId == null) {
			if (other.channelId != null) {
				return false;
			}
		} else if (!channelId.equals(other.channelId)) {
			return false;
		}
		
		if (channelName == null) {
			if (other.channelName != null) {
				return false;
			}
		} else if (!channelName.equals(other.channelName)) {
			return false;
		}
		
		if (channelNr == null) {
			if (other.channelNr != null) {
				return false;
			}
		} else if (!channelNr.equals(other.channelNr)) {
			return false;
		}
		
		if (dvbC == null) {
			if (other.dvbC != null) {
				return false;
			}
		} else if (!dvbC.equals(other.dvbC)) {
			return false;
		}
		
		if (dvbS == null) {
			if (other.dvbS != null) {
				return false;
			}
		} else if (!dvbS.equals(other.dvbS)) {
			return false;
		}
		
		if (dvbT == null) {
			if (other.dvbT != null) {
				return false;
			}
		} else if (!dvbT.equals(other.dvbT)) {
			return false;
		}
		
		if (frontend == null) {
			if (other.frontend != null) {
				return false;
			}
		} else if (!frontend.equals(other.frontend)) {
			return false;
		}
		
		if (hasCi == null) {
			if (other.hasCi != null) {
				return false;
			}
		} else if (!hasCi.equals(other.hasCi)) {
			return false;
		}
		
		if (hasDecoder == null) {
			if (other.hasDecoder != null) {
				return false;
			}
		} else if (!hasDecoder.equals(other.hasDecoder)) {
			return false;
		}
		
		if (live == null) {
			if (other.live != null) {
				return false;
			}
		} else if (!live.equals(other.live)) {
			return false;
		}
		
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		
		if (number == null) {
			if (other.number != null) {
				return false;
			}
		} else if (!number.equals(other.number)) {
			return false;
		}
		
		if (primary == null) {
			if (other.primary != null) {
				return false;
			}
		} else if (!primary.equals(other.primary)) {
			return false;
		}
		
		if (signalQuality == null) {
			if (other.signalQuality != null) {
				return false;
			}
		} else if (!signalQuality.equals(other.signalQuality)) {
			return false;
		}
		
		if (signalStrength == null) {
			if (other.signalStrength != null) {
				return false;
			}
		} else if (!signalStrength.equals(other.signalStrength)) {
			return false;
		}
		
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		
		return true;
	}

}

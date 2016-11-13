package vdr.jonglisto.lib.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class SearchTimer {
	private Map<String, Object> dbData;
	
	// enriched
	private String vdrName;
	
	
	public SearchTimer() {
		dbData = new HashMap<>();
		dbData.put("state", "");
	}
	
	public SearchTimer(Map<String, Object> dbData) {
		this.dbData = dbData;
	}

	public Map<String, Object> getRawDbData() {
		return dbData;
	}

	public void setRawDbData(Map<String, Object> dbData) {
		this.dbData = dbData;
	}
	
	public Long getId() {
		return (Long)dbData.get("id");
	}
	
	public void setId(Long id) {
		dbData.put("id", id);
	}

	public String getVdrUuid() {
		return (String)dbData.get("vdruuid");
	}
	
	public void setVdrUuid(String id) {
		dbData.put("vdruuid", id);
	}

	public Integer getPriority() {
		return (Integer)dbData.get("priority");
	}
	
	public void setPriority(Integer id) {
		dbData.put("priority", id);
	}
	
	public Integer getLifetime() {
		return (Integer)dbData.get("lifetime");
	}
	
	public void setLifetime(Integer id) {
		dbData.put("lifetime", id);
	}
	
	public String getExpression() {
		return (String) dbData.get("expression");
	}

	public void setExpression(String expression) {
		dbData.put("expression", expression);
	}

	public String getDirectory() {
		return (String) dbData.get("directory");
	}

	public void setDirectory(String directory) {
		dbData.put("directory", directory);
	}

	public String getName() {
		return (String) dbData.get("name");
	}

	public void setName(String name) {
		dbData.put("name", name);
	}

	public String getExpression1() {
		return (String) dbData.get("expression1");
	}

	public void setExpression1(String expression1) {
		dbData.put("expression1", expression1);
	}

	public String getEpisodename() {
		return (String) dbData.get("episodename");
	}

	public void setEpisodename(String episodename) {
		dbData.put("episodename", episodename);
	}

	public String getSeason() {
		return (String) dbData.get("season");
	}

	public void setSeason(String season) {
		dbData.put("season", season);
	}

	public String getSeasonpart() {
		return (String) dbData.get("seasonpart");
	}

	public void setSeasonpart(String seasonpart) {
		dbData.put("seasonpart", seasonpart);
	}
	
	public List<String> getCategory() {
		String category = (String) dbData.get("category");		
		if (StringUtils.isNotEmpty(category)) {
			return Arrays.stream(category.split(",")).map(s -> s.replaceAll("^'", "").replaceAll("'$", "")).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	public void setCategory(List<String> category) {
		if (category.size() > 0) {
			dbData.put("category", category.stream().map(s -> "'" + s + "'").collect(Collectors.joining(",")));
		} else {
			dbData.put("category", null);
		}
	}

	public List<String> getGenre() {
		String genre = (String) dbData.get("genre");
		if (StringUtils.isNotEmpty(genre)) {
			return Arrays.stream(genre.split(",")).map(s -> s.replaceAll("^'", "").replaceAll("'$", "")).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	public void setGenre(List<String> genre) {
		if (genre.size() > 0) {
			dbData.put("genre", genre.stream().map(s -> "'" + s + "'").collect(Collectors.joining(",")));
		} else {
			dbData.put("genre", null);
		}
	}

	public String getTipp() {
		return (String)dbData.get("tipp");
	}

	public void setTipp(String tipp) {
		dbData.put("tipp", tipp);
	}

	public String getYear() {
		return (String)dbData.get("year");
	}

	public void setYear(String year) {
		dbData.put("year", year);
	}

	public String getChformat() {
		return (String)dbData.get("chformat");
	}

	public void setChformat(String chformat) {
		dbData.put("chformat", chformat);
	}
	
	public List<String> getChannelsList() {
		String channelIds = (String)dbData.get("channelids");
		if (!StringUtils.isEmpty(channelIds)) {
			return Arrays.stream(channelIds.split(",")).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
	
	public void setChannelsList(List<String> channels) {
		if (channels.size() > 0) {
			dbData.put("channelids", channels.stream().collect(Collectors.joining(",")));
		} else {
			dbData.put("channelids", null);
		}
	}
	
	public String getChannels() {
		return (String)dbData.get("channelids");
	}
	
	public Boolean getChannelExclude() {
		return !(0L == (Long)dbData.get("chexclude"));
	}

	public void setChannelExclude(Boolean channelExclude) {
		dbData.put("chexclude", channelExclude ? 1L : 0L);
	}

	public Boolean getVps() {
		if (dbData.get("vps") != null) {
			return !(0 == (Integer)dbData.get("vps"));
		} else {
			return false;
		}
	}

	public void setVps(Boolean vps) {
		dbData.put("vps", vps ? 1 : 0);
	}

	public Boolean getActive() {
		if (dbData.get("active") != null) {
			return !(0L == (Long)dbData.get("active"));
		} else {
			return false;
		}
	}

	public void setActive(Boolean active) {
		dbData.put("active", active ? 1L : 0L);
	}

	public String getStarttime() {
		Integer time = (Integer)dbData.get("starttime");
		if (time == null) {
			return null;
		} else {		
			return String.format("%02d:%02d", time/100, time%100);
		}
	}

	public void setStarttime(String starttime) {
		if (StringUtils.isEmpty(starttime)) {
			dbData.put("starttime", null);
		} else {
			dbData.put("starttime", Integer.valueOf(starttime.replace(":", "")));
		}
	}
	
	public Integer getRawStartTime() {
		return (Integer)dbData.get("starttime");
	}

	public String getEndtime() {
		Integer time = (Integer)dbData.get("endtime");
		if (time == null) {
			return null;
		} else {		
			return String.format("%02d:%02d", time/100, time%100);
		}
	}

	public void setEndtime(String endtime) {
		if (StringUtils.isEmpty(endtime)) {
			dbData.put("endtime", null);
		} else {
			dbData.put("endtime", Integer.valueOf(endtime.replace(":", "")));
		}
	}

	public Integer getRawEndTime() {
		return (Integer)dbData.get("endtime");
	}
	
	public Integer getNextDays() {
		return (Integer)dbData.get("nextdays");
	}

	public void setNextDays(Integer nextDays) {
		dbData.put("nextdays", nextDays);
	}

	public Boolean getNoepgmatch() {
		return !(0L == (Integer)dbData.get("noepgmatch"));
	}

	public void setNoepgmatch(Boolean noepgmatch) {
		dbData.put("noepgmatch", noepgmatch ? 1 : 0);
	}

	public Long getSearchmode() {
		return (Long)dbData.get("searchmode");
	}

	public void setSearchmode(Long searchmode) {
		dbData.put("searchmode", searchmode);
	}

	public Long getRawSearchFields() {		
		return (Long) dbData.get("searchfields");
	}

	public void setRawSearchFields(Long f) {		
		dbData.put("searchfields", f);
	}

	public Long getRawSearchFields1() {		
		return (Long) dbData.get("searchfields1");
	}

	public void setRawSearchFields1(Long f) {		
		dbData.put("searchfields1", f);
	}
	
	public Boolean getSearchFieldTitle() {
		return getLBitFlag(1, "searchfields");
	}
	
	public void setSearchFieldTitle(Boolean val) {
		setLBitFlag(val, 1, "searchfields");
	}
	
	public Boolean getSearchFieldFolge() {
		return getLBitFlag(2, "searchfields");
	}
	
	public void setSearchFieldFolge(Boolean val) {
		setLBitFlag(val, 2, "searchfields");
	}
	
	public Boolean getSearchFieldDescription() {
		return getLBitFlag(4, "searchfields");
	}
	
	public void setSearchFieldDescription(Boolean val) {
		setLBitFlag(val, 4, "searchfields");
	}

	public Boolean getSearchFieldTitle1() {
		return getLBitFlag(1, "searchfields1");
	}
	
	public void setSearchFieldTitle1(Boolean val) {
		setLBitFlag(val, 1, "searchfields1");
	}
	
	public Boolean getSearchFieldFolge1() {
		return getLBitFlag(2, "searchfields1");
	}
	
	public void setSearchFieldFolge1(Boolean val) {
		setLBitFlag(val, 2, "searchfields1");
	}
	
	public Boolean getSearchFieldDescription1() {
		return getLBitFlag(4, "searchfields1");
	}
	
	public void setSearchFieldDescription1(Boolean val) {
		setLBitFlag(val, 4, "searchfields1");
	}
	
	public Boolean getCasesensitiv() {
		return !(0L == (Long)dbData.get("casesensitiv"));
	}

	public void setCasesensitiv(Boolean casesensitiv) {
		dbData.put("casesensitiv", casesensitiv ? 1L : 0L);
	}

	public Integer getWeekdays() {
		return (Integer)dbData.get("weekdays");
	}

	public void setWeekdays(Integer weekdays) {
		dbData.put("weekdays", weekdays);
	}

	public String getType() {
		return (String)dbData.get("type");
	}
	
	public void setType(String type) {
		dbData.put("type", type);
	}

	public Integer getNamingMode() {
		return (Integer) dbData.get("namingmode");
	}
	
	public void setNamingMode(Integer mode) {
		dbData.put("namingmode", mode);
	}

	public Boolean getRepeatTitle() {
		return getLBitFlag(1, "repeatfields");
	}

	public void setRepeatTitle(Boolean repeat) {
		setLBitFlag(repeat, 1, "repeatfields");
	}
		
	public Boolean getRepeatShortText() {
		return getLBitFlag(2, "repeatfields");
	}

	public void setRepeatShortText(Boolean repeat) {
		setLBitFlag(repeat, 2, "repeatfields");
	}	

	public Boolean getRepeatDesc() {
		return getLBitFlag(4, "repeatfields");
	}

	public void setRepeatDesc(Boolean repeat) {
		setLBitFlag(repeat, 4, "repeatfields");
	}
	
	public Boolean getMonday() {		
		return getBitFlag(1, "weekdays");
	}
		
	public void setMonday(Boolean val) {
		setBitFlag(val, 1, "weekdays");
	}

	public Boolean getTuesday() {		
		return getBitFlag(2, "weekdays");
	}
	
	public void setTuesday(Boolean val) {
		setBitFlag(val, 2, "weekdays");
	}

	public Boolean getWednesday() {
		return getBitFlag(4, "weekdays");
	}
	
	public void setWednesday(Boolean val) {
		setBitFlag(val, 4, "weekdays");
	}

	public Boolean getThursday() {
		return getBitFlag(8, "weekdays");
	}
	
	public void setThursday(Boolean val) {
		setBitFlag(val, 8, "weekdays");
	}

	public Boolean getFriday() {
		return getBitFlag(16, "weekdays");
	}
	
	public void setFriday(Boolean val) {
		setBitFlag(val, 16, "weekdays");
	}

	public Boolean getSaturday() {
		return getBitFlag(32, "weekdays");
	}
	
	public void setSaturday(Boolean val) {
		setBitFlag(val, 32, "weekdays");
	}

	public Boolean getSunday() {
		return getBitFlag(64, "weekdays");
	}
	
	public void setSunday(Boolean val) {
		setBitFlag(val, 64, "weekdays");
	}
	
	public void setSource(String value) {
		dbData.put("source", value);
	}

	public String getSource() {
		return (String)dbData.get("source");
	}

	public Long getHits() {
		return (Long)dbData.get("hits");
	}
	
	public String getVdrName() {
		return vdrName;
	}
	
	public void setVdrName(String name) {
		this.vdrName = name;
	}
	
	private Boolean getBitFlag(int idx, String name) {		
		Integer data = (Integer)dbData.get(name);
		if (data == null) {
			return false;
		}
	
		return (data & idx) > 0;
	}
	
	private void setBitFlag(Boolean val, int idx, String name) {
		Integer data = (Integer)dbData.get(name);
		if (data == null) {
			data = 0;
		}
		
		if (val) {
			dbData.put(name, data |= idx);
		} else {
			dbData.put(name, data &= ~idx);
		}
	}

	private Boolean getLBitFlag(int idx, String name) {		
		Long data = (Long)dbData.get(name);
		if (data == null) {
			return false;
		}
	
		return (data & idx) > 0;
	}
	
	private void setLBitFlag(Boolean val, int idx, String name) {
		Long data = (Long)dbData.get(name);
		if (data == null) {
			data = 0L;
		}
		
		if (val) {
			dbData.put(name, data |= idx);
		} else {
			dbData.put(name, data &= ~idx);
		}
	}
	
	@Override
	public String toString() {
		return "SearchTimer [" + dbData.keySet().stream().sorted().map(s -> s + "->" + dbData.get(s)).collect(Collectors.joining(", ")) + "]";
	}
}

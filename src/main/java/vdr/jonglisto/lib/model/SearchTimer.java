package vdr.jonglisto.lib.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchTimer {
	private Map<String, Object> dbData;

	public SearchTimer() {
		dbData = new HashMap<>();
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
	
	public String getExpression() {
		return (String) dbData.get("expression");
	}

	public void setExpression(String expression) {
		dbData.put("expresion", expression);
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
		dbData.put("expresion1", expression1);
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

	public String getCategory() {
		return (String) dbData.get("category");
	}

	public void setCategory(String category) {
		dbData.put("category", category);
	}

	public String getGenre() {
		return (String)dbData.get("genre");
	}

	public void setGenre(String genre) {
		dbData.put("genre", genre);
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

	public String getChannels() {
		return (String)dbData.get("channelids");
	}

	public void setChannels(String channels) {
		dbData.put("channelids", channels);
	}

	public Boolean getChannelExclude() {
		return !(0L == (Long)dbData.get("chexclude"));
	}

	public void setChannelExclude(Boolean channelExclude) {
		dbData.put("chexclude", channelExclude ? 1L : 0L);
	}

	public Boolean getVps() {
		return !(0 == (Integer)dbData.get("vps"));
	}

	public void setVps(Boolean vps) {
		dbData.put("vps", vps ? 1 : 0);
	}

	public Boolean getActive() {
		return !(0L == (Long)dbData.get("active"));
	}

	public void setActive(Boolean active) {
		dbData.put("active", active ? 1L : 0L);
	}

	public Integer getStarttime() {
		return (Integer)dbData.get("starttime");
	}

	public void setStarttime(Integer starttime) {
		dbData.put("starttime", starttime);
	}

	public Integer getEndtime() {
		return (Integer)dbData.get("endtime");
	}

	public void setEndtime(Integer endtime) {
		dbData.put("endtime", endtime);
	}
	
	public Integer getNextDays() {
		return (Integer)dbData.get("nextdays");
	}

	public void setNextDays(Integer nextDays) {
		dbData.put("nextdays", nextDays);
	}

	public Boolean getNoepgmatch() {
		return !(0L == (Long)dbData.get("noepgmatch"));
	}

	public void setNoepgmatch(Boolean noepgmatch) {
		dbData.put("noepgmatch", noepgmatch ? 1L : 0L);
	}

	public Long getSearchmode() {
		return (Long)dbData.get("searchmode");
	}

	public void setSearchmode(Long searchmode) {
		dbData.put("searchmode", searchmode);
	}
	
	public Integer getSearchfields() {
		return dbData.get("searchfields") != null ? ((Long)dbData.get("searchfields")).intValue() : null;
	}

	public void setSearchfields(Integer searchfields) {
		dbData.put("searchfields", Long.valueOf(searchfields.intValue()));
	}

	public Integer getSearchfields1() {
		return dbData.get("searchfields1") != null ? ((Long)dbData.get("searchfields1")).intValue() : null;
	}

	public void setSearchfields1(Integer searchfields1) {
		dbData.put("searchfields1", Long.valueOf(searchfields1.intValue()));
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

	public Boolean getRepeatTitle() {
		return false;
	}

	public void setRepeatTitle(Boolean repeat) {
		
	}
	
	public Boolean getRepeatDesc() {
		return false;
	}

	public void setRepeatDesc(Boolean repeat) {
		
	}
	
	public Boolean getRepeatShortText() {
		return false;
	}

	public void setRepeatShortText(Boolean repeat) {
		
	}
	
	@Override
	public String toString() {
		return "SearchTimer [" + dbData.keySet().stream().sorted().map(s -> s + "->" + dbData.get(s)).collect(Collectors.joining(", ")) + "]";
	}
}

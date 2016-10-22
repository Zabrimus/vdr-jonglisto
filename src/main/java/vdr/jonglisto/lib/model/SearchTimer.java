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

	public Map<String, Object> getDbData() {
		return dbData;
	}

	public void setDbData(Map<String, Object> dbData) {
		this.dbData = dbData;
	}

	public Object get(String key) {
		return dbData.get(key);
	}
	
	public String getString(String key) {
		return (String)get(key);
	}

	public Integer getInteger(String key) {
		return (Integer)get(key);
	}

	public Long getLong(String key) {
		return (Long)get(key);
	}

	public void set(String key, Object value) {
		dbData.put(key, value);
	}

	@Override
	public String toString() {
		return "SearchTimer [" + dbData.keySet().stream().sorted().map(s -> s + "->" + dbData.get(s)).collect(Collectors.joining(", ")) + "]";
	}

}

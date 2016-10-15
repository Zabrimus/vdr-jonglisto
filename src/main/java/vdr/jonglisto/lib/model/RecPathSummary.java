package vdr.jonglisto.lib.model;

import vdr.jonglisto.lib.util.DateTimeUtil;

public class RecPathSummary {
	
	private String name;
	private long countRecordings;
	private long size;
	private long time;
	private String nodeId;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getCountRecordings() {
		return countRecordings;
	}
	
	public void setCountRecordings(long countRecordings) {
		this.countRecordings = countRecordings;
	}
	
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {		
		this.size = size;
	}
	
	public String getTime() {
		return DateTimeUtil.toDuration(time);
	}
	
	public void setTime(long time) {
		this.time = time;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
}

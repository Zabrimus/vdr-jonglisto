package vdr.jonglisto.lib.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecPathTree {
	public final String uuid = UUID.randomUUID().toString();
	public final String name;
	public RecPathTree parent;
	public List<RecPathTree> children;

	public RecPathTree(RecPathTree parent, String name) {
		this.name = name;
		this.parent = parent;
		this.children = new ArrayList<RecPathTree>();
	}
	
	public boolean isRoot() {
		return parent == null;
	}
	
	public boolean isLeaf() {
		return children.size() == 0;
	}
	
	public void addChild(String path) {
		int idx = path.indexOf("~");
		if (idx == -1) {
			children.add(new RecPathTree(this, path));			
		} else {			
			String nodeName = path.substring(0, idx);
			String lastPart = path.substring(idx+1, path.length());
			
			RecPathTree child;
			int cidx = searchChild(nodeName);
			if (cidx == -1) {
				child = new RecPathTree(this, nodeName);
				children.add(child);				
			} else {
				child = children.get(cidx);
			}
			
			child.addChild(lastPart);
		}
	}

	public RecPathTree searchSubTree(String uuid) {
		if (this.uuid.equals(uuid)) {
			return this;
		}

		for (RecPathTree child : children) {
			RecPathTree match = child.searchSubTree(uuid);

			if (match != null) {
				return match;
			}
		}

		return null;
	}
	
	public String getFullPath() {
		return getFullPath(this, name);
	}
	
	private String getFullPath(RecPathTree start, String path) {
		if ((start.parent == null) || (start.parent.parent == null)) {
			return path;
		} else {
			return getFullPath(start.parent, start.parent.name) + "~" + path;
		}
	}
	
	private int searchChild(String name) {
		for (int i = 0; i < children.size(); ++i) {
			RecPathTree c = children.get(i);
			if (c.name.equals(name)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public void output(RecPathTree tree, String prefix) {
		for (int i = 0; i < tree.children.size(); ++i) {
			RecPathTree c = tree.children.get(i);
			output(c, prefix + "  ");
		}
	}
}

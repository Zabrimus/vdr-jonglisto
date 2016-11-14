package vdr.jonglisto.web.model;

import java.util.List;

import org.apache.tapestry5.tree.TreeModelAdapter;

import vdr.jonglisto.lib.model.RecPathTree;

public class RecPathTreeAdapter implements TreeModelAdapter<RecPathTree> {

    private boolean withLeafs = true;

    public RecPathTreeAdapter(boolean withLeafs) {
        this.withLeafs = withLeafs;
    }

    public boolean isLeaf(RecPathTree node) {
        if (withLeafs) {
            return !hasChildren(node);
        } else {
            return false;
        }
    }

    public boolean hasChildren(RecPathTree node) {
        return node.children != null && !node.children.isEmpty();
    }

    public List<RecPathTree> getChildren(RecPathTree node) {
        return node.children;
    }

    public String getLabel(RecPathTree node) {
        return node.name;
    }
}

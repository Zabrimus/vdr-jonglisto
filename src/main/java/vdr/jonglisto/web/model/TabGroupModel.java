package vdr.jonglisto.web.model;

import org.apache.tapestry5.Block;

public interface TabGroupModel {

    public void addTab(String name, String label, Block body);
}
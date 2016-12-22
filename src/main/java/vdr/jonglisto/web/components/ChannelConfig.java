package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.util.FilterEncrypted;
import vdr.jonglisto.lib.util.FilterRadioTv;

@Import(library = { "webjars:jquery-ui:$version/jquery-ui.js" }, stylesheet = {
        "webjars:jquery-ui:$version/jquery-ui.css" })
public class ChannelConfig extends BaseComponent {

    @Inject
    Logger log;

    @Inject
    protected AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    protected JavaScriptSupport javaScriptSupport;

    @InjectComponent
    private Zone channelConfigZone;

    @InjectComponent
    private Zone groupRenameZone;

    @InjectComponent
    private Zone newGroupZone;

    @Inject
    protected Messages messages;

    @Property
    @Persist
    private List<String> channelGroups;

    @Persist
    @Property
    private String group;

    @Persist
    @Property
    private Channel channel;

    @Persist
    @Property
    private FilterRadioTv filterRadio;

    @Persist
    @Property
    private FilterEncrypted filterEncrypted;

    @Property
    @Persist
    private Map<String, List<Channel>> channelsInGroup;

    @Property
    @Persist
    private String parkingGroup;

    @Property
    private String newGroupName;
    
    void setupRender() {
        channelsInGroup = new HashMap<>();
        channelGroups = new ArrayList<>();
        parkingGroup = "-- " + messages.get("parking_group") + " --";

        Optional<List<String>> gr = vdrDataService.getGroups(currentVdrView.getChannelVdr().get());
        if (gr.isPresent()) {
            channelGroups.addAll(gr.get());
            gr.get().stream().forEach(g -> channelsInGroup.put(g,
                    vdrDataService.getChannelsInGroup(currentVdrView.getChannelVdr().get(), g, true).get()));
        } else {
            // create a default group
            channelGroups.add("-- Default --");
            channelsInGroup.put("-- Default --",
                    vdrDataService.getChannels(currentVdrView.getChannelVdr().get(), true).get());
        }

        channelGroups.add(parkingGroup);
        channelsInGroup.put(parkingGroup, new ArrayList<>());

        filterRadio = FilterRadioTv.RADIOTV;
        filterEncrypted = FilterEncrypted.ALL;
    }

    public void afterRender() {
        Link link = componentResources.createEventLink("draggedChannel");
        String baseURI = link.toAbsoluteURI();
        javaScriptSupport.require("portlet").with(baseURI);
    }

    public List<Channel> getChannels() {
        return channelsInGroup.get(group);
    }

    public void onDraggedChannel() {
        String id = request.getParameter("id");
        String to = request.getParameter("to");
        String from = request.getParameter("from");
        String fromidx = request.getParameter("fromidx");
        String toidx = request.getParameter("toidx");
        String group = request.getParameter("group");

        System.err.println("id: " + id + ", to: " + to + ", from: " + from + ", fromidx: " + fromidx + ", toidx: "
                + toidx + ", group: " + group);
    }

    public void onSaveChannelsConf() {
        log.info("Currently not implemented: ChannelConfig.onSaveChannelsConf");
    }

    public void onLoadChannelsConf() {
        log.info("Currently not implemented: ChannelConfig.onLoadChannelsConf");
    }

    public void onCreateChannelsConf() {
        log.info("Currently not implemented: ChannelConfig.onCreateChannelsConf");
    }

    public void onDeleteObsolete() {
        log.info("Currently not implemented: ChannelConfig.onDeleteObsolete");
    }

    public void onFixObsolete() {
        log.info("Currently not implemented: ChannelConfig.onFixObsolete");
    }

    public void onCreateChannelGroup() {
        if (request.isXHR()) {
            ajaxResponseRenderer.addCallback(new JavaScriptCallback() {
                public void run(JavaScriptSupport javascriptSupport) {
                    javaScriptSupport.require("dialogmodal").invoke("activate").with("newGroup");
                }
            });
            ajaxResponseRenderer.addRender(newGroupZone);
        }
    }

    public void onChangeFromFilterRadio() {
        log.info("Currently not implemented: ChannelConfig.onChangeFromFilterRadio");
    }

    public void onChangeFromFilterEncrypted() {
        log.info("Currently not implemented: ChannelConfig.onChangeFromFilterEncrypted");
    }

    public void onRenameGroup(String selectedGroup) {
        if (selectedGroup.startsWith("--")) {
            // internal group, do nothing
            return;
        }
        
        if (request.isXHR()) {
            group = selectedGroup;
            ajaxResponseRenderer.addCallback(new JavaScriptCallback() {
                public void run(JavaScriptSupport javascriptSupport) {
                    javaScriptSupport.require("dialogmodal").invoke("activate").with("groupRename");
                }
            });
            ajaxResponseRenderer.addRender(groupRenameZone);
        }
    }

    public void onDeleteGroup(String selectedGroup) {
        if (selectedGroup.startsWith("--")) {
            // internal group, do nothing
            return;
        }

        List<Channel> removed = channelsInGroup.get(selectedGroup);
        channelsInGroup.remove(selectedGroup);
        channelsInGroup.get("-- " + messages.get("parking_group") + " --").addAll(removed);

        channelGroups.remove(selectedGroup);

        updateZone();
    }

    public void onSortGroup(String selectedGroup) {
        channelsInGroup.put(selectedGroup,
                channelsInGroup.get(selectedGroup) //
                        .stream() //
                        .sorted((x, y) -> x.getName().toUpperCase().compareTo(y.getName().toUpperCase())) //
                        .collect(Collectors.toList()));

        updateZone();
    }

    public void onSuccessFromNewGroupForm() {
        channelGroups.add(newGroupName);
        channelsInGroup.put(newGroupName, new ArrayList<>());
        
        if (request.isXHR()) {
            ajaxResponseRenderer.addCallback(new JavaScriptCallback() {
                public void run(JavaScriptSupport javascriptSupport) {
                    javaScriptSupport.require("dialogmodal").invoke("hide").with("newGroup");
                }
            });
        }
        
        updateZone();
    }
    
    public void onSuccessFromRenameGroupForm() {
        int idx = channelGroups.indexOf(group);
        channelGroups.set(idx, newGroupName);
        
        List<Channel> ch = channelsInGroup.get(group);
        channelsInGroup.remove(group);
        channelsInGroup.put(newGroupName, ch);
        
        if (request.isXHR()) {
            ajaxResponseRenderer.addCallback(new JavaScriptCallback() {
                public void run(JavaScriptSupport javascriptSupport) {
                    javaScriptSupport.require("dialogmodal").invoke("hide").with("groupRename");
                }
            });
        }
        
        updateZone();
    }       
    
    public void onCancel() {
        if (request.isXHR()) {
            ajaxResponseRenderer.addCallback(new JavaScriptCallback() {
                public void run(JavaScriptSupport javascriptSupport) {
                    javaScriptSupport.require("dialogmodal").invoke("hide").with("newGroup");
                    javaScriptSupport.require("dialogmodal").invoke("hide").with("groupRename");
                }
            });
        }
    }
    
    private void updateZone() {
        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(channelConfigZone).addCallback(new JavaScriptCallback() {

                @Override
                public void run(JavaScriptSupport javascriptSupport) {
                    Link link = componentResources.createEventLink("draggedChannel");
                    String baseURI = link.toAbsoluteURI();
                    javaScriptSupport.require("portlet").with(baseURI);
                }
            });
        }
    }
}

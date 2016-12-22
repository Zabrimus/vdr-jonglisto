package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.util.FilterEncrypted;
import vdr.jonglisto.lib.util.FilterRadioTv;

@Import(library = { "webjars:jquery-ui:$version/jquery-ui.js" }, stylesheet = {"webjars:jquery-ui:$version/jquery-ui.css" })
public class ChannelConfig extends BaseComponent {

    @Inject
    Logger log;

    @Inject
    protected AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    protected JavaScriptSupport javaScriptSupport;

    @Inject
    protected Messages messages;

    @Property
    @Persist
    private List<String> channelGroups;

    @Property
    private String group;

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

    void setupRender() {
        channelsInGroup = new HashMap<>();
        channelGroups = new ArrayList<>();

        Optional<List<String>> gr = vdrDataService.getGroups(currentVdrView.getChannelVdr().get());
        if (gr.isPresent()) {
            channelGroups.addAll(gr.get());
            gr.get().stream().forEach(g -> channelsInGroup.put(g,
                    vdrDataService.getChannelsInGroup(currentVdrView.getChannelVdr().get(), g, true).get()));
        } else {
            // create a default group
            channelGroups.add("DEFAULT");
            channelsInGroup.put("DEFAULT",
                    vdrDataService.getChannels(currentVdrView.getChannelVdr().get(), true).get());
        }
        
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
        
        System.err.println("id: " + id + ", to: " + to + ", from: " + from + ", fromidx: " + fromidx + ", toidx: " + toidx + ", group: " + group);
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
        log.info("Currently not implemented: ChannelConfig.onCreateChannelGroup");
    }       

    public void onChangeFromFilterRadio() {
        log.info("Currently not implemented: ChannelConfig.onChangeFromFilterRadio");
    }
    
    public void onChangeFromFilterEncrypted() {
        log.info("Currently not implemented: ChannelConfig.onChangeFromFilterEncrypted");
    }

    public void onRenameGroup() {        
        log.info("Currently not implemented: ChannelConfig.onRenameGroup");
    }
    
    public void onDeleteGroup() {        
        log.info("Currently not implemented: ChannelConfig.onDeleteGroup");
    }
    
    public void onSortGroup() {       
        log.info("Currently not implemented: ChannelConfig.onSortGroup");
    }
}

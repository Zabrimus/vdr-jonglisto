package vdr.jonglisto.web.components;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import vdr.jonglisto.lib.model.ExtendedChannel;
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
    private ExtendedChannel channel;

    @Persist
    @Property
    private FilterRadioTv filterRadio;

    @Persist
    @Property
    private FilterEncrypted filterEncrypted;

    @Property
    private List<ExtendedChannel> filteredChannels;
    
    @Property
    @Persist
    private Map<String, List<ExtendedChannel>> channelsInGroup;

    @Property
    @Persist
    private String parkingGroup;

    @Property
    private String newGroupName;

    void setupRender() {
        channelGroups = new ArrayList<>();

        Optional<List<String>> gr = vdrDataService.getGroups(currentVdrView.getChannelVdr().get());
        if (gr.isPresent()) {
            channelGroups.addAll(gr.get());
        } else {
            // create a default group
            channelGroups.add("-- Default --");
        }

        parkingGroup = "-- " + messages.get("parking_group") + " --";
        
        channelsInGroup = vdrDataService.getExtendedChannelsInGroup(currentVdrView.getChannelVdr().get());

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

    public int getGroupCount() {
        return filteredChannels.size();
    }
    
    public List<ExtendedChannel> getChannels() {
        return filteredChannels;
    }

    public void onDraggedChannel() {
        String id = request.getParameter("id");
        String to = request.getParameter("to");
        String from = request.getParameter("from");
        String fromidx = request.getParameter("fromidx");
        String toidx = request.getParameter("toidx");
        String group = request.getParameter("group");

        System.err.println("id: " + id + ", to: " + to + ", from: " + from + ", fromidx: " + fromidx + ", toidx: " + toidx + ", group: " + group);
        
        if (Boolean.parseBoolean(group)) {
            // exchange groups
            String g = channelGroups.get(Integer.parseInt(to));
            channelGroups.set(Integer.parseInt(to), channelGroups.get(Integer.parseInt(from)));
            channelGroups.set(Integer.parseInt(from), g);
        } else {
            // 'from' group 'to' group, Zielindex toidx, Vonidx: fromidx
            ExtendedChannel c = channelsInGroup.get(from).remove(Integer.parseInt(fromidx));
            channelsInGroup.get(to).add(Integer.parseInt(toidx), c);
        }
    }

    public void onSaveChannelsConf() {
        log.info("Currently not implemented: ChannelConfig.onSaveChannelsConf");
    }

    public void onLoadChannelsConf() {
        log.info("Currently not implemented: ChannelConfig.onLoadChannelsConf");
    }

    public StreamResponse onCreateChannelsConf() {
        return new StreamResponse() {

            InputStream inputStream;

            @Override
            public void prepareResponse(Response response) {
                try {
                    response.setHeader("Content-Disposition", "attachment; filename=channelmap.conf");

                    // generate channels.conf
                    StringBuilder builder = new StringBuilder();
                    channelGroups.stream().forEach(g -> {
                        builder.append(":").append(g).append("\n");
                        channelsInGroup.get(g).stream().forEach(c -> builder.append(c.getChannelLine()).append("\n"));
                    });
                    
                    inputStream = new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));

                    response.setHeader("Content-Length", "" + inputStream.available());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public InputStream getStream() throws IOException {
                return inputStream;
            }
        };
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
        updateZone();
    }

    public void onChangeFromFilterEncrypted() {
        updateZone();
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

        List<ExtendedChannel> removed = channelsInGroup.get(selectedGroup);
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

        List<ExtendedChannel> ch = channelsInGroup.get(group);
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

    public void onFilterChannels() {
        filteredChannels = channelsInGroup.get(group)//
                .stream() //
                .filter(s -> {
                    // filter Radio/Tv
                    switch (filterRadio) {
                    case TV:
                        return !s.getRadio();

                    case RADIOTV:
                        return true;

                    case RADIO:
                        return s.getRadio();
                    }

                    return true;
                }) //
                .filter(s -> {
                    // filter encrypted/decrypted
                    switch (filterEncrypted) {
                    case ALL:
                        return true;

                    case FREE:
                        return !s.getEncrypted();

                    case ENCRYPTED:
                        return s.getEncrypted();
                    }

                    return false;
                }).collect(Collectors.toList());
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

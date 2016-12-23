package vdr.jonglisto.web.components;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import vdr.jonglisto.lib.ChannelMapService;
import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.channelmap.ChannelModel;
import vdr.jonglisto.lib.model.channelmap.IdModel;
import vdr.jonglisto.lib.model.channelmap.Provider;

@Import(library = { "webjars:jquery-ui:$version/jquery-ui.js" })
public class ChannelMap extends BaseComponent {

    @Inject
    Logger log;

    @Inject
    protected AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    protected JavaScriptSupport javaScriptSupport;

    @Inject
    private ChannelMapService channelMapService;

    @InjectComponent
    private Zone mappingZone;

    @InjectComponent
    private Zone mappingConfigZone;

    @InjectComponent
    private ChannelSelectPalette2 channelSelectPalette2;

    @Inject
    protected BeanModelSource beanModelSource;

    @Inject
    protected Messages messages;

    @Property
    private BeanModel<Object> mapModel;

    @Persist
    @Property
    private Provider mainProv;

    @Persist
    @Property
    private Provider secProv;

    @Persist
    @Property
    private String epgdataPin;

    @Property
    @Persist
    private List<Channel> selectedChannels;

    @Property
    @Persist
    private Map<String, List<Object>> mapData;

    @Property
    private ChannelModel channel;

    @Property
    private IdModel idModel;

    @Property
    private List<Object> data;

    @Property
    private Integer mapDataIndex;

    @Property
    private Integer idDataIndex;

    @Property
    @Persist
    Map<String, String> channelNameMapping;

    @Property
    @Persist
    private boolean showTvmIds;

    @Property
    @Persist
    private boolean showTvspIds;

    @Property
    @Persist
    private boolean showEpgDataIds;

    @Persist
    @Property
    private int idModulo;

    void setupRender() {
        selectedChannels = channelMapService.getIncludedVdrChannels(currentVdrView.getChannelVdr().get());

        if (mapData == null) {
            mapData = channelMapService.readMapping(currentVdrView.getChannelVdr().get());
        }

        channelNameMapping = channelMapService.getNameMapping();

        Provider[] provider = channelMapService.readProvider();
        mainProv = provider[0];
        secProv = provider[1];
    }

    public void afterRender() {
        Link link = componentResources.createEventLink("draggedChannel");
        String baseURI = link.toAbsoluteURI();
        javaScriptSupport.require("dragdrop").with(baseURI, ".dragme", ".dropme");
    }

    public void onSaveProviderConfig() {
        channelMapService.saveProvider(mainProv, secProv);
    }

    public void onDraggedChannel() {
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String name = request.getParameter("name");

        // get element and move it
        if (from.equals(to)) {
            // nothing to do
            return;
        }

        // search in list 'from' the channel 'name' and move it to 'to'
        List<ChannelModel> ch = mapData.get(from) //
                .stream() //
                .filter(s -> (s instanceof ChannelModel)) //
                .map(s -> (ChannelModel) s) //
                .filter(s -> s.getNormalizedName(channelNameMapping).equals(name)) //
                .collect(Collectors.toList());

        mapData.get(to).addAll(ch);

        // recreate the 'old' list
        List<Object> oldList = mapData.get(from) //
                .stream() //
                .filter(s -> {
                    if (s instanceof ChannelModel) {
                        return !((ChannelModel) s).getNormalizedName(channelNameMapping).equals(name);
                    } else {
                        return true;
                    }
                }).collect(Collectors.toList());

        mapData.put(from, oldList);
    }

    public void onDraggedid() {
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String name = request.getParameter("name");
        Integer providerId = Integer.valueOf(request.getParameter("providerid"));

        // get element and move it
        if (from.equals(to)) {
            // nothing to do
            return;
        }

        if (to.equals("delete")) {
            mapData.put(from,
                    mapData.get(from).stream() //
                            .filter(s -> s instanceof IdModel) //
                            .map(s -> (IdModel) s) //
                            .filter(s -> !s.getId().equals(name)) //
                            .collect(Collectors.toList()));
        } else {
            IdModel draggedIdModel = channelMapService.getIds(providerId).stream()
                    .filter(s -> s.getNormalizedName().equals(from)).findFirst().get();
            mapData.get(to).add(draggedIdModel);
        }
    }

    public void onAutoMapping() {
        mapData = channelMapService.doAutoMapping(currentVdrView.getChannelVdr().get());

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingZone).addCallback(new JavaScriptCallback() {

                @Override
                public void run(JavaScriptSupport javascriptSupport) {
                    Link link = componentResources.createEventLink("draggedChannel");
                    String baseURI = link.toAbsoluteURI();
                    javascriptSupport.require("dragdrop").with(baseURI, ".dragme", ".dropme");
                }
            });
        }
    }

    public void onResetMapping() {
        mapData = channelMapService.readMapping(currentVdrView.getChannelVdr().get());

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingZone).addCallback(new JavaScriptCallback() {

                @Override
                public void run(JavaScriptSupport javascriptSupport) {
                    Link link = componentResources.createEventLink("draggedChannel");
                    String baseURI = link.toAbsoluteURI();
                    javascriptSupport.require("dragdrop").with(baseURI, ".dragme", ".dropme");
                }
            });
        }
    }

    public void onSaveMapping() {
        // save
        channelMapService.saveMapping(mapData);

        // reload mapping
        mapData = channelMapService.readMapping(currentVdrView.getChannelVdr().get());
        channelNameMapping = channelMapService.getNameMapping();

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingZone).addCallback(new JavaScriptCallback() {

                @Override
                public void run(JavaScriptSupport javascriptSupport) {
                    Link link = componentResources.createEventLink("draggedChannel");
                    String baseURI = link.toAbsoluteURI();
                    javascriptSupport.require("dragdrop").with(baseURI, ".dragme", ".dropme");
                }
            });
        }
    }

    public void onReadProviderIds() {
        for (int i = 1; i < 3; ++i) {
            channelMapService.updateEpgIds(i, epgdataPin);
        }

        channelMapService.saveProvider(mainProv, secProv);
    }

    public void onUpdateSelectedChannels() {
        channelSelectPalette2.processSelectedChannels(request);

        channelMapService
                .replaceIncludeChannel(selectedChannels.stream().map(s -> s.getName()).collect(Collectors.toList()));
        channelMapService.doAutoMapping(currentVdrView.getChannelVdr().get());

        mapData = channelMapService.readMapping(currentVdrView.getChannelVdr().get());

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingConfigZone).addRender(mappingZone)
                    .addCallback(new JavaScriptCallback() {

                        @Override
                        public void run(JavaScriptSupport javascriptSupport) {
                            Link link = componentResources.createEventLink("draggedChannel");
                            String baseURI = link.toAbsoluteURI();
                            javascriptSupport.require("dragdrop").with(baseURI, ".dragme", ".dropme");
                        }
                    });
        }
    }

    public void onDeleteNameMapping() {
        // delete all custom mapping
        channelMapService.deleteNameMapping();

        // start auto mapping
        onAutoMapping();
    }

    public StreamResponse onDownload() {
        return new StreamResponse() {

            InputStream inputStream;

            @Override
            public void prepareResponse(Response response) {
                try {
                    response.setHeader("Content-Disposition", "attachment; filename=channelmap.conf");

                    String mapping = channelMapService.createEpgdMapping(currentVdrView.getChannelVdr().get());
                    inputStream = new ByteArrayInputStream(mapping.getBytes("UTF-8"));

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

    public List<Object> getSortedMapData() {
        return mapData.keySet().stream() //
                .sorted() //
                .map(s -> mapData.get(s)) //
                .collect(Collectors.toList());
    }

    public String getNormalizedChannel() {
        if (data.size() > 0) {
            if (data.get(0) instanceof ChannelModel) {
                return ((ChannelModel) data.get(0)).getNormalizedName(channelNameMapping);
            } else {
                return ((IdModel) data.get(0)).getNormalizedName(channelNameMapping);
            }
        } else {
            return null;
        }
    }

    public List<ChannelModel> getChannels() {
        return data.stream() //
                .filter(s -> s instanceof ChannelModel) //
                .map(s -> (ChannelModel) s) //
                .sorted(Comparator.comparing(ChannelModel::getName)) //
                .collect(Collectors.toList());
    }

    public List<IdModel> getEpgIds(final int providerId) {
        return data.stream() //
                .filter(s -> s instanceof IdModel) //
                .map(s -> (IdModel) s) //
                .filter(s -> s.getProviderId() == providerId) //
                .collect(Collectors.toList());
    }

    public List<IdModel> getAllEpgIds(final int providerId) {
        Set<String> usedIds = getAllUsedIds(providerId);

        List<IdModel> allIds = channelMapService.getIds(providerId) //
                .stream() //
                .filter(s -> !usedIds.contains(s.getId())) //
                .sorted((x, y) -> x.getName().compareTo(y.getName())) //
                .collect(Collectors.toList());

        if ((mapData.size() > 0) && (mapData.size() < allIds.size())) {
            idModulo = allIds.size() / mapData.size();
        } else {
            idModulo = 1;
        }

        return allIds;
    }

    public boolean getInsertId1Column() {
        return (mapDataIndex == 0) && showTvmIds;
    }

    public boolean getInsertId2Column() {
        return (mapDataIndex == 0) && showTvspIds;
    }

    public boolean getInsertId3Column() {
        return (mapDataIndex == 0) && showEpgDataIds;
    }

    public Integer getTvmColspan() {
        if (showTvmIds) {
            return 2;
        } else {
            return 1;
        }
    }

    public Integer getTvspColspan() {
        if (showTvspIds) {
            return 2;
        } else {
            return 1;
        }
    }

    public Integer getEpgdataColspan() {
        if (showEpgDataIds) {
            return 2;
        } else {
            return 1;
        }
    }

    public Integer getCountRows() {
        return mapData.size();
    }

    public void onShowTvmIds() {
        showTvmIds = true;

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingZone).addCallback(new JavaScriptCallback() {

                @Override
                public void run(JavaScriptSupport javascriptSupport) {
                    Link link = componentResources.createEventLink("draggedId");
                    String baseURI = link.toAbsoluteURI();
                    javascriptSupport.require("dragdropid").with(baseURI, 1, ".dragme1", ".dropme1");
                }
            });
        }
    }

    public void onShowTvspIds() {
        showTvspIds = true;

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingZone).addCallback(new JavaScriptCallback() {

                @Override
                public void run(JavaScriptSupport javascriptSupport) {
                    Link link = componentResources.createEventLink("draggedId");
                    String baseURI = link.toAbsoluteURI();
                    javascriptSupport.require("dragdropid").with(baseURI, 2, ".dragme2", ".dropme2");
                }
            });
        }
    }

    public void onShowEpgdataIds() {
        showEpgDataIds = true;

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingZone).addCallback(new JavaScriptCallback() {

                @Override
                public void run(JavaScriptSupport javascriptSupport) {
                    Link link = componentResources.createEventLink("draggedId");
                    String baseURI = link.toAbsoluteURI();
                    javascriptSupport.require("dragdropid").with(baseURI, 3, ".dragme3", ".dropme3");
                }
            });
        }
    }

    public void onHideTvmIds() {
        showTvmIds = false;

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingZone);
        }
    }

    public void onHideTvspIds() {
        showTvspIds = false;

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingZone);
        }
    }

    public void onHideEpgdataIds() {
        showEpgDataIds = false;

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(mappingZone);
        }
    }

    public boolean doInsertLineBreak() {
        if (idDataIndex % idModulo == 0) {
            return true;
        }

        return false;
    }

    private Set<String> getAllUsedIds(Integer providerId) {
        Set<String> result = new HashSet<>();

        mapData.values().stream().forEach(s -> {
            s.stream().forEach(ch -> {
                if ((ch instanceof IdModel) && ((IdModel) ch).getProviderId() == providerId) {
                    result.add(((IdModel) ch).getId());
                }
            });
        });

        return result;
    }
}

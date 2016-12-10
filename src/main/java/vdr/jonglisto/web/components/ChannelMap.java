package vdr.jonglisto.web.components;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import vdr.jonglisto.lib.ChannelMapService;

public class ChannelMap extends BaseComponent {

    @Inject
    Logger log;

    @Inject
    protected AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    protected JavaScriptSupport javaScriptSupport;

    @Inject ChannelMapService channelMapService;
    
    void setupRender() {
        
        /*
        channelMapService.clearAll();
        
        for (int i = 1; i < 3; ++i) {
            channelMapService.updateEpgIds(i);
        }
        
        channelMapService.doAutoMapping(currentVdrView.getChannelVdr().get());
        */
        
        /*
        Map<String, List<Object>> mapping = channelMapService.readMapping(currentVdrView.getChannelVdr().get());
        mapping.keySet().stream().forEach(s -> {
            System.err.print(s + " -->  ");
            System.err.println(mapping.get(s));
        });
        */
        
        String result = channelMapService.createEpgdMapping(currentVdrView.getChannelVdr().get());
        System.err.println("\n\nMapping:\n" + result);
    }
}

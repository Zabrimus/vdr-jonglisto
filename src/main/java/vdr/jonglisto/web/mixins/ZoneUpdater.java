/**
 * A simple mixin for attaching javascript that updates a zone on any
 * client-side event.
 * Based on http://tinybits.blogspot.com/2010/03/new-and-better-zoneupdater.html
 *
 * copied from tapestry jumpstart
 * http://jumpstart.doublenegative.com.au/jumpstart7/examples/ajax/onevent
 */
package vdr.jonglisto.web.mixins;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONLiteral;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.json.JSONObject;

public class ZoneUpdater {

    // Parameters

    /**
     * The event to listen for on the client. If not specified, zone update can
     * only be triggered manually through
     * calling updateZone on the JS object.
     */
    @Parameter(name = "clientEvent", defaultPrefix = BindingConstants.LITERAL)
    private String clientEvent;

    /**
     * The event to listen for in your component class
     */
    @Parameter(name = "event", defaultPrefix = BindingConstants.LITERAL, required = true)
    private String event;

    @Parameter(name = "prefix", defaultPrefix = BindingConstants.LITERAL, value = "default")
    private String prefix;

    @Parameter(name = "context")
    private Object[] context;

    /**
     * The zone to be updated by us.
     */
    @Parameter(name = "zone", defaultPrefix = BindingConstants.LITERAL, required = true)
    private String zone;

    /**
     * Set secure to true if https is being used, else set to false.
     */
    @Parameter(name = "secure", defaultPrefix = BindingConstants.LITERAL, value = "false")
    private boolean secure;

    /**
     * Set the additional value
     */
    @Parameter(name = "addValue", defaultPrefix = BindingConstants.LITERAL)
    private String addValue;

    // Useful bits and pieces

    @Inject
    private ComponentResources componentResources;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    /**
     * The element we attach ourselves to
     */
    @InjectContainer
    private ClientElement attachedTo;

    // The code

    void afterRender() {
        String listenerURI = componentResources.createEventLink(event, context).toAbsoluteURI(secure);
        javaScriptSupport.require("zone-updater").with(attachedTo.getClientId(), clientEvent, listenerURI, zone,
                addValue != null ? addValue : new JSONLiteral(null));
    }
}
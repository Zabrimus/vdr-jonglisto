package vdr.jonglisto.web.components;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.PersistentLocale;

import vdr.jonglisto.lib.model.VDRView;
import vdr.jonglisto.lib.model.VDRView.Type;
import vdr.jonglisto.web.pages.Index;

@Import(module = "bootstrap/collapse", stylesheet = { "META-INF/assets/css/jonglisto.less" })
public class Layout extends BaseComponent {

    enum Mode {
        VIEW, VDR;
    }

    @Inject
    private PersistentLocale persistentLocaleService;
    
    @Inject
    protected ComponentResources componentResources;

    @Inject
    @Property
    private Locale currentLocale;
    
    @Inject
    private LocalizationSetter localizationSetter;
    
    @Property
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String title;

    @Property
    private String pageName;

    @Property
    private VDRView view;

    @Property
    @Inject
    @Symbol(SymbolConstants.APPLICATION_VERSION)
    private String appVersion;

    @SessionAttribute
    @Property
    private Type type;

    @Property
    private String language;
    
    public void setupRender() {
        if (currentLocale.getLanguage().equals(new Locale("de").getLanguage())) {
            language="de";
        } else  if (currentLocale.getLanguage().equals(new Locale("en").getLanguage())) {
            language="en";
        }
    }

    public String getClassForView() {
        if (currentVdrView == null) {
            // could happen in rare case. e.g. after a server restart
            return null;
        }
        
        return currentVdrView.getDisplayName().equals(view.getDisplayName()) ? "active" : null;
    }

    public List<VDRView> getViews() {
        if (configuration.isSuccessfullyInitialized()) {
            List<VDRView> permViews = Collections.emptyList();
            
            if (type == null) {
                // set default type and view
                type = Type.View;
                permViews = getPermittedViews(true);

                if (permViews.isEmpty()) {
                    type = Type.VDR;
                    permViews = getPermittedViews(true);
                }

                if (!permViews.isEmpty()) {
                    permViews.get(0);
                }
            } else {
                permViews = getPermittedViews(true);
            }

            return permViews;
        } else {
            return Collections.emptyList();
        }
    }

    public void onSelectView(String displayName) {
        if (securityService.hasPermission("view:vdr:" + displayName)) {
            currentVdrView = getPermittedViews(false) //
                                .stream() //
                                .filter(s -> s.getDisplayName().equals(displayName)) //
                                .findFirst() //
                                .get();
            
            type = currentVdrView.getType();
        }
    }

    public Object onToggleViewType() {
        if (!configuration.isSuccessfullyInitialized()) {
            return null;
        }
        
        List<VDRView> v;
        
        if (type == Type.VDR) {
            type = Type.View;
            v = getPermittedViews(true);
            if (v.size() == 0) {
                // don't toggle the view, because there is nothing to view
                type = Type.VDR;                
            } else {
                currentVdrView = v.get(0);
            }
        } else {
            type = Type.VDR;
            v = getPermittedViews(true);
            if (v.size() == 0) {
                // don't toggle the view, because there is nothing to view
                type = Type.View;                
            } else {
                currentVdrView = v.get(0);
            }
        }
        
        // currentVdrView = getPermittedViews(true).get(0);
        
        return Index.class;
    }

    public boolean showVdrList() {
        return type == Type.VDR;
    }
    
    public Object onValueChangedFromLanguage(String selectedLanguage) {
        localizationSetter.setLocaleFromLocaleName(selectedLanguage);
        return componentResources.getPage();
    }
    
    private List<VDRView> getPermittedViews(boolean withType) {
        return configuration.getConfiguredViews() //
            .values() //
            .stream() //
            .filter(s -> securityService.hasPermission("view:vdr:" + s.getDisplayName())) //
            .filter(s -> (withType && s.getType() == type) || !withType) //
            .sorted() //
            .collect(Collectors.toList());
    }
}

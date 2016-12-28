package vdr.jonglisto.web.pages;

import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import vdr.jonglisto.web.model.setup.JonglistoSetup;
import vdr.jonglisto.web.model.setup.JonglistoVdr;
import vdr.jonglisto.web.model.setup.JonglistoView;

/**
 * Start page of application VDR Jonglisto app.
 */
@Import(library = { "webjars:jquery-ui:$version/jquery-ui.js" }, stylesheet = {"webjars:jquery-ui:$version/jquery-ui.css" })
public class Setup {

    @Inject
    private Logger log;

    @Inject
    protected JavaScriptSupport javaScriptSupport;

    @Inject
    protected AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    private AlertManager alertManager;
    
    @InjectComponent 
    protected Form setupform;

    @InjectComponent
    private Zone setupZone;
    
    @Persist
    @Property
    private JonglistoSetup setup;

    @Persist
    @Property
    private Sql2o sql2o;

    @Property
    private JonglistoVdr vdr;

    @Persist
    @Property
    private JonglistoView view;
    
    @Property
    private int viewIndex;
    
    public void onActivate() {
        if (setup == null) {
            setup = new JonglistoSetup();
        }
    }

    public void afterRender() {
    }

    public Object onSelectedFromTestDatabase() {
        try {
            String url = "jdbc:mysql://" + setup.getEpgdHost() + ":" + setup.getEpgdPort() + "/" + setup.getEpgdDatabase();
            
            BasicDataSource epgDb = new BasicDataSource();
            epgDb.setDriverClassName("com.mysql.jdbc.Driver");        
            epgDb.setUrl(url);
            epgDb.setUsername(setup.getEpgdUser());
            epgDb.setPassword(setup.getEpgdPassword());
    
            sql2o = new Sql2o(epgDb);
            
            // read vdr data
            try (Connection con = sql2o.open()) {
                setup.setAvailableVdr(con.createQuery("select uuid, name, ip, svdrp from vdrs where name <> 'epgd'").executeAndFetch(JonglistoVdr.class));
            }
                        
            this.alertManager.info("Database connection is sucessfully configured.");
        } catch (Exception e) {            
            sql2o = null;
            setupform.recordError("Unable to connect to database. Please adjust the values.");
        }
        
        return setupZone;
    }
    
    public Object onSelectedFromNewVdr() {
        JonglistoVdr newVdr = new JonglistoVdr();
        newVdr.setRestful("8002");
        newVdr.setSvdrp("6419");
        newVdr.setUuid(UUID.randomUUID().toString());
        
        setup.getAvailableVdr().add(newVdr);
        
        return setupZone;
    }
    
    public Object onSelectedFromNewView() {
        JonglistoView newView = new JonglistoView();
        setup.getAvailableViews().add(newView);        
        
        return setupZone;
    }

    public Object getVdrNameModel() {
        return setup.getAvailableVdr().stream().map(s -> s.getName()).collect(Collectors.toList());
    }

    public Object onDeleteVdr(String uuid) {
        setup.setAvailableVdr(setup.getAvailableVdr().stream().filter(s -> !s.getUuid().equals(uuid)).collect(Collectors.toList()));
        return setupZone;
    }
    
    public Object onDeleteView(int idx) {
        setup.getAvailableViews().remove(idx);
        return setupZone;
    }
}

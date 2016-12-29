package vdr.jonglisto.web.pages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Inject
    private Request request;
    
    @InjectPage
    private StreamResponsePage streamResponse;
    
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
    
    @Property
    private boolean genConfig;

    @Property
    private int vdrIndex;
    
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
        newView.setName("HALLO?");
        setup.getAvailableViews().add(newView);        
        
        return setupZone;
    }
    
    public void onSelectedFromGenerateConfig() {
        // FIXME: Prüfe, ob die Aliasnamen eindeutig sind!      
        genConfig = true;
    }
    
    public Object onSuccess() {
        if (genConfig) {
            JSONObject cfg = new JSONObject();
            
            // all default values            
            cfg.put("useRecordingSyncMap", "false");
            cfg.put("developer_mode", "false");
            cfg.put("svdrpPort", "5000");
            cfg.put("remoteOsdSleepTime", "200");
            cfg.put("remoteOsdIncSleepTime", "200");
     
            // hsqldb configuration
            JSONObject hsql = new JSONObject();
            hsql.put("path", "/var/cache/jonglisto-db");
            hsql.put("remote", "false");            
            cfg.put("hsqldb", hsql);

            // nashorn scripts
            JSONObject nashorn = new JSONObject();
            nashorn.put("svdrp", "/etc/jonglisto/svdrp.js");
            nashorn.put("epg2vdr", "/etc/jonglisto/epg2vdr.js");
            cfg.put("NashornScripts", nashorn);

            // mysql configuration
            JSONObject mysql = new JSONObject();
            mysql.put("url", "jdbc:mysql://" + setup.getEpgdHost() + ":" + setup.getEpgdPort() + "/" + setup.getEpgdDatabase());
            mysql.put("username", setup.getEpgdUser());
            mysql.put("password", setup.getEpgdPassword());
            cfg.put("epg2vdr", mysql);

            // aliases            
            JSONObject aliases = new JSONObject();
            setup.getAvailableVdr().stream() //
                .forEach(s -> {
                    aliases.put(s.getName(), s.getUuid());
                });
            cfg.put("aliases", aliases);
            
            // VDR
            setup.getAvailableVdr().stream() //
                .forEach(s -> {
                    JSONObject vdrcfg = new JSONObject();
                    vdrcfg.put("uuid", s.getName());
                    vdrcfg.put("displayName", s.getName());
                    
                    JSONObject vdrval = new JSONObject();
                    vdrval.put("TIMER_AUX", "");
                    vdrval.put("TIMER_MINUS_MINUTES", 10);
                    vdrval.put("TIMER_PLUS_MINUTES", 10);
                    vdrval.put("TIMER_PRIORITY", 50);
                    vdrval.put("TIMER_LIFETIME", 99);
                    vdrval.put("RECORDING_NAMING_MODE", 1);
                    vdrcfg.put("config", vdrval);
                    
                    cfg.append("VDR", vdrcfg);
                });
            
            // Sichten
            setup.getAvailableViews().stream() //
                .forEach(s -> {
                    System.err.println("View.name: " + s.getName());
                    
                    JSONObject sicht = new JSONObject();
                    sicht.put("displayName", s.getName());
                    sicht.put("head", s.getHead().get(0));
                    sicht.put("timers", s.getTimer().get(0));
                    s.getChannels().stream().forEach(c -> sicht.append("channels", c));
                    s.getRecordings().stream().forEach(r -> sicht.append("recordings", r));
                    
                    cfg.append("Sichten", sicht);
                });
            
            streamResponse.setStreamContent(cfg.toString(false));
            streamResponse.setStreamFilename("jonglisto.json");
            streamResponse.setStreamType("text/plain");
            
            return streamResponse;
        } else {
            return null;
        }        
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
    
    void onAliasNameChanged(@RequestParameter(value = "param", allowBlank = true) String name, @RequestParameter(value = "constant", allowBlank = true) String oldName) {
        Optional<JonglistoVdr> myv = setup.getAvailableVdr().stream().filter(s -> s.getName().equals(oldName)).findFirst();
        if (myv.isPresent()) {
            myv.get().setName(name);
        }
        
        setup.getAvailableViews().stream().forEach(v -> {
            if (v.getChannels().remove(oldName)) {
                v.getChannels().add(name);
            }
            
            if (v.getHead().remove(oldName)) {
                v.getHead().add(name);
            }
            
            if (v.getRecordings().remove(oldName)) {
                v.getRecordings().add(name);
            }
            
            if (v.getTimer().remove(oldName)) {
                v.getTimer().add(name);
            }                       
        });

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(setupZone);
        }
    }
    
    public ValueEncoder<JonglistoVdr> getVdrEncoder() {
        return new ValueEncoder<JonglistoVdr>() {
            @Override
            public String toClient(JonglistoVdr value) {
                return value.getUuid();
            }

            @Override
            public JonglistoVdr toValue(String clientValue) {
                return setup.getAvailableVdr().stream().filter(s -> s.getUuid().equals(clientValue)).findFirst().get();
            }
        };
    }
    
    public ValueEncoder<JonglistoView> getViewEncoder() {
        return new ValueEncoder<JonglistoView>() {
            @Override
            public String toClient(JonglistoView value) {
                return value.getUuid();
            }

            @Override
            public JonglistoView toValue(String clientValue) {
                return setup.getAvailableViews().stream().filter(s -> s.getUuid().equals(clientValue)).findFirst().get();
            }
        };
    }

    private JonglistoSetup readExistingSetup() {
        Pattern pattern = Pattern.compile("jdbc:mysql://(.*?):(.*?)/(.*?)");
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        JonglistoSetup s = new JonglistoSetup();
        
        try {
            Map<String, Object> config = mapper.readValue(new File("/etc/jonglisto/jonglisto.json"), Map.class);

            // Database configuration
            Map<String, Object> dbConfig = (Map<String, Object>) config.get("epg2vdr");

            Matcher matcher = pattern.matcher((String) dbConfig.get("url"));
            if (matcher.matches()) {                
                s.setEpgdPassword((String) dbConfig.get("password"));
                s.setEpgdUser((String) dbConfig.get("username"));
                s.setEpgdHost(matcher.group(1));            
                s.setEpgdPort(matcher.group(2));
                s.setEpgdDatabase(matcher.group(3));
                
                // Connect to database
                onSelectedFromTestDatabase();
                if (sql2o == null) {
                    // at least the database configuration must be correct...
                    return s;
                }
            }            
            
            // Aliases configuration
            Map<String, String> aliases = (Map<String, String>) config.get("aliases");
            aliases.keySet().stream().forEach(a -> {
                Optional<JonglistoVdr> myv = s.getAvailableVdr().stream().filter(av -> av.getUuid().equals(aliases.get(a))).findFirst();
                if (myv.isPresent()) {
                    myv.get().setName(a);
                } else {
                    JonglistoVdr newv = new JonglistoVdr();
                    newv.setName(a);
                    newv.setUuid(aliases.get(a));
                    s.getAvailableVdr().add(newv);
                }
            });

            // VDR configuration
            ArrayList<Map<String, Object>> vdrConfig = (ArrayList<Map<String, Object>>) config.get("VDR");
            vdrConfig.stream().forEach(v -> {
                // search the vdr
                Optional<JonglistoVdr> x = s.getAvailableVdr().stream().filter(f -> f.getName().equals(v.get("displayName"))).findFirst();
                if (x.isPresent()) {
                    x.get().setIp(StringUtils.defaultIfBlank((String) v.get("ip"), vdr.getIp()));
                    x.get().setRestful(StringUtils.defaultIfBlank((String) v.get("restfulApiPort"), vdr.getRestful()));
                    x.get().setSvdrp(StringUtils.defaultIfBlank((String) v.get("svdrpPort"), vdr.getSvdrp()));
                }
            });
            
            // View configuration
            
            // FIXME: Finish the implementation
        } catch (IOException e) {
            // setup does not exists => use the empty one            
        }        
        
        return s;
    }
    
}

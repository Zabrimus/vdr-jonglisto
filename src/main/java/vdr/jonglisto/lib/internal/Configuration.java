package vdr.jonglisto.lib.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.hsqldb.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.impl.ConfigurationServiceImpl;
import vdr.jonglisto.lib.model.RecordingNamingMode;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.model.VDRView;
import vdr.jonglisto.lib.model.VDRView.Type;
import vdr.jonglisto.lib.svdrp.SvdrpServer;

public class Configuration {

    class LastCheck {

        public String key = null;
        public Long time = 0L;
        public Boolean result = false;

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
            result = prime * result + ((time == null) ? 0 : time.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            LastCheck other = (LastCheck) obj;

            if (key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!key.equals(other.key)) {
                return false;
            }

            if (result == null) {
                if (other.result != null) {
                    return false;
                }
            } else if (!result.equals(other.result)) {
                return false;
            }

            if (time == null) {
                if (other.time != null) {
                    return false;
                }
            } else if (!time.equals(other.time)) {
                return false;
            }

            return true;
        }
    }

    private static Configuration instance = new Configuration("/etc/jonglisto/jonglisto.json");
    
    private int prodVersion = 1;
    private String version = "0.0.6-snapshot";
    private boolean successfullyConfigured = true;

    private Logger log = LoggerFactory.getLogger(Configuration.class);

    private Server dbServer;

    private Sql2o sql2oEpg2vdr;
    private Sql2o sql2oHsqldb;

    private Map<String, VDR> configuredVdr;
    private Map<String, VDRView> configuredVdrView;

    private String channelImagePath;

    private Map<String, LastCheck> lastCheck = new ConcurrentHashMap<>();

    private boolean useSyncMap = false;
    private boolean developerMode = false;

    private Integer svdrpPort;
    private SvdrpServer svdrpServer;

    private long remoteOsdSleepTime = 200L;
    private long remoteOsdIncSleepTime = 200L;

    private String svdrpScript;
    private String epg2vdrScript;

    private boolean useEpgd;
    
    private Configuration(String pathname) {
        initConfiguration(pathname);
    }

    public static Configuration getInstance() {
        return instance;
    }

    public String getVersion() {
        return version;
    }
    
    public boolean isSuccessfullyConfigured() {
        return successfullyConfigured;
    }

    public VDR getVdr(String uuid) {
        return getConfiguredVdr().get(uuid);
    }

    public VDRView getVdrView(String name) {
        return configuredVdrView.get(name);
    }

    public Sql2o getSql2oEpg2vdr() {
        return sql2oEpg2vdr;
    }

    public Sql2o getSql2oHsqldb() {
        return sql2oHsqldb;
    }

    public Map<String, VDR> getConfiguredVdr() {
        return configuredVdr;
    }

    public Map<String, VDRView> getConfiguredViews() {
        return configuredVdrView;
    }

    public boolean isUseSyncMap() {
        return useSyncMap;
    }

    public void setUseSyncMap(boolean useSyncMap) {
        this.useSyncMap = useSyncMap;
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    public List<VDR> getSortedVdrList() {
        return configuredVdr.values().stream() //
                .sorted((v1, v2) -> v1.getDisplayName().compareTo(v2.getDisplayName())) //
                .collect(Collectors.toList());
    }

    public String getChannelImagePath() {
        return channelImagePath;
    }

    public long getRemoteOsdSleepTime() {
        return remoteOsdSleepTime;
    }

    public long getRemoteOsdIncSleepTime() {
        return remoteOsdIncSleepTime;
    }

    public String getSvdrpScript() {
        return svdrpScript;
    }

    public String getEpg2VdrScript() {
        return epg2vdrScript;
    }

    public boolean isUseEpgd() {
        return useEpgd;
    }

    public void sendWol(String uuid) {
        VDR v = getVdr(uuid);

        if ((v == null) || (v.getMac() == null)) {
            log.error("MAC for VDR with uuid [} is not available", uuid);
        }

        // check MAC and extract parts
        byte[] macBytes = new byte[6];
        String[] hex = v.getMac().split("(\\:|\\-)");
        if (hex.length != 6) {
            log.error("Invalid MAC address: {}", v.getMac());
            return;
        }

        try {
            for (int i = 0; i < 6; i++) {
                macBytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid MAC address: {}", v.getMac());
            return;
        }

        // create the magic packet
        byte[] bytes = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        // try to find the broadcast addresses and send magic packet
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback())
                    continue;

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null)
                        continue;

                    // send magic packet to broadcast address
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcast, 9);
                    DatagramSocket socket = new DatagramSocket();
                    socket.send(packet);
                    socket.close();
                }
            }
        } catch (IOException e) {
            // i'm unsure what to do
            log.error("Unable to send WOL packet to broadcase address", e);
        }
    }

    public boolean pingHost(String ip) {
        List<String> command = new ArrayList<>();
        command.add("ping");
        command.add("-c");
        command.add("1");
        command.add(ip);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            BufferedReader standardOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String outputLine;

            while ((outputLine = standardOutput.readLine()) != null) {
                if (outputLine.toLowerCase().contains("destination host unreachable")) {
                    return false;
                }
            }
        } catch (Exception e) {
            // log.error("Ping to " + ip + " failed", e);
            return false;
        }

        return true;
    }

    public boolean testSvdrp(String ip, int svdrpPort) {
        String key = "S:" + ip + ":" + svdrpPort;

        LastCheck ch = lastCheck.get(key);
        if (ch == null) {
            ch = getInstance().new LastCheck();
        }

        if ((System.currentTimeMillis() - ch.time) < 5 * 60 * 1000) {
            return ch.result;
        }

        ch.key = key;
        ch.time = System.currentTimeMillis();

        try {
            log.debug("TestSvdrp: " + ip + ":" + svdrpPort);

            Socket clientSocket = null;

            try {
                clientSocket = new Socket(ip, svdrpPort);
                clientSocket.setSoTimeout(5000);

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine = in.readLine();
                String[] splitted = inputLine.split(" ");

                if ((splitted != null) && (splitted.length > 1) && "220".equals(splitted[0])) {
                    ch.result = true;
                    lastCheck.put(key, ch);
                } else {
                    ch.result = false;
                    lastCheck.put(key, ch);
                }

                out.append("QUIT\n");
                out.flush();

                return ch.result;
            } finally {
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        // fail silently
                    }
                }
            }
        } catch (IOException e) {
            ch.result = false;
            lastCheck.put(key, ch);

            // log.error("SVDRP Connection failed", e);
            return false;
        }
    }

    public boolean testRestfulApi(String ip, int restfulApiPort) {
        String key = "S:" + ip + ":" + restfulApiPort;

        LastCheck ch = lastCheck.get(key);
        if (ch == null) {
            ch = getInstance().new LastCheck();
        }

        if ((System.currentTimeMillis() - ch.time) < 5 * 60 * 1000) {
            return ch.result;
        }

        ch.key = key;
        ch.time = System.currentTimeMillis();

        try {
            log.debug("TestRestfulApi: " + ip + ":" + restfulApiPort);

            String restUrl = "http://" + ip + ":" + restfulApiPort + "/channels/.json?start=1&limit=1";

            @SuppressWarnings("unused")
            HttpResponse<String> jsonResponse = Unirest.get(restUrl).asString();
        } catch (UnirestException e) {
            ch.result = false;
            lastCheck.put(key, ch);

            // log.error("Restfulapi Connection failed", e);
            return false;
        }

        ch.result = true;
        lastCheck.put(key, ch);

        return true;
    }

    public void shutdown() {
        log.info("SVDRP Server shutdown started ...");
        if (svdrpServer != null) {
            svdrpServer.initStop();
        }

        log.info("HSQLDB shutdown started ...");

        if (dbServer != null) {
            dbServer.stop();
            dbServer.shutdown();
        }
    }

    @SuppressWarnings("unchecked")
    private void initConfiguration(String pathname) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);

        try {
            Map<String, Object> config = mapper.readValue(new File(pathname), Map.class);

            channelImagePath = (String) config.get("channelImagePath");

            useSyncMap = Boolean.valueOf((String) config.get("useRecordingSyncMap"));

            // INFO:
            // Es gibt immer noch seltsame Probleme, nachdem umfangreiche
            // Änderungen
            // an den Aufnahmen vorgenommen wurden.
            // Die interne Datenbank wurde nicht korrekt aktualisiert. Bis zur
            // genaueren
            // Analyse und einem Fix, ist es auf jeden Fall besser, erstmal auf
            // die
            // RecordingSyncMap zu verzichten. Nur zur Sicherheit.
            useSyncMap = false;

            developerMode = Boolean.valueOf((String) config.get("developer_mode"));

            svdrpPort = Integer.valueOf((String) config.get("svdrpPort"));

            useEpgd = Boolean.valueOf((String) config.get("useEpgd"));
            
            Map<String, Object> scripts = (Map<String, Object>) config.get("NashornScripts");
            svdrpScript = (String) scripts.get("svdrp");
            epg2vdrScript = (String) scripts.get("epg2vdr");

            if (config.get("remoteOsdSleepTime") != null) {
                remoteOsdSleepTime = Long.parseLong((String) config.get("remoteOsdSleepTime"));
            }

            if (config.get("remoteOsdIncSleepTime") != null) {
                remoteOsdIncSleepTime = Long.parseLong((String) config.get("remoteOsdIncSleepTime"));
            }

            Map<String, Object> dbConfig;
            // create DataSource for epg2vdr
            dbConfig = (Map<String, Object>) config.get("epg2vdr");

            BasicDataSource epgDb = new BasicDataSource();
            epgDb.setDriverClassName("com.mysql.jdbc.Driver");
            epgDb.setUrl((String) dbConfig.get("url"));
            epgDb.setUsername((String) dbConfig.get("username"));
            epgDb.setPassword((String) dbConfig.get("password"));

            sql2oEpg2vdr = new Sql2o(epgDb);

            // hsqldb configuration
            dbConfig = (Map<String, Object>) config.get("hsqldb");

            startHsqlDatabase(dbConfig);

            BasicDataSource hsqlDb = new BasicDataSource();
            hsqlDb.setDriverClassName("org.hsqldb.jdbcDriver");
            hsqlDb.setUsername("SA");
            hsqlDb.setPassword("");

            if (Boolean.parseBoolean((String) dbConfig.get("remote"))) {
                hsqlDb.setUrl("jdbc:hsqldb:hsql://localhost/" + dbConfig.get("path") + "/jonglisto");
            } else {
                hsqlDb.setUrl("jdbc:hsqldb:file:" + dbConfig.get("path"));
            }

            sql2oHsqldb = new Sql2o(hsqlDb);

            checkDatabases();

            // read aliases
            Map<String, String> aliases = (Map<String, String>) config.get("aliases");

            // create vdr entries
            ArrayList<Map<String, Object>> vdrConfig = (ArrayList<Map<String, Object>>) config.get("VDR");
            try (Connection con = sql2oEpg2vdr.open()) {
                configuredVdr = con.createQuery("select uuid, name displayName, ip, svdrp svdrpPort, mac from vdrs") //
                        .executeAndFetch(VDR.class) //
                        .stream() //
                        .map(s -> addinfo(s, vdrConfig, aliases)) //
                        .filter(s -> s.getRestfulApiPort() > 0) //
                        .collect(Collectors.toMap(VDR::getUuid, c -> c));
            }

            configuredVdr.values().stream().forEach(System.out::println);

            // create views entries
            ConfigurationService service = new ConfigurationServiceImpl();

            configuredVdrView = new HashMap<>();
            ArrayList<Map<String, Object>> vdrViews = (ArrayList<Map<String, Object>>) config.get("Sichten");

            vdrViews.stream().forEach(s -> {
                VDRView view = new VDRView(service);
                view.setDisplayName((String) s.get("displayName"));
                view.setType(Type.View);

                // add channels
                ArrayList<String> channels = (ArrayList<String>) s.get("channels");
                channels.stream().forEach(ch -> view.addChannelVdr(configuredVdr.get(aliases.get(ch))));

                // add recordings
                ArrayList<String> recordings = (ArrayList<String>) s.get("recordings");
                recordings.stream().forEach(ch -> view.addRecordingVdr(configuredVdr.get(aliases.get(ch))));

                // add timer
                String timers = (String) s.get("timers");
                view.setTimerVDR(configuredVdr.get(aliases.get(timers)));

                // add head
                String head = (String) s.get("head");
                view.setHeadVDR(configuredVdr.get(aliases.get(head)));

                // add epg
                String epg = (String) s.get("epg");
                view.setEpgVDR(configuredVdr.get(aliases.get(epg)));

                // add result
                configuredVdrView.put(view.getDisplayName(), view);
            });

            configuredVdrView.values().stream().forEach(System.out::println);

            // add every VDR as it's own view
            configuredVdr.values().stream().forEach(s -> {
                VDRView view = new VDRView(service);
                view.setDisplayName(s.getDisplayName());
                view.setType(Type.VDR);

                // add channels
                view.addChannelVdr(s);

                // add recordings
                view.addRecordingVdr(s);

                // add timer
                view.setTimerVDR(s);

                // add head
                view.setHeadVDR(s);
                
                // add epg
                view.setEpgVDR(s);

                // add result
                configuredVdrView.put(view.getDisplayName(), view);
            });

            // start SVDRP server
            startSvdrpServer();
        } catch (IOException e) {
            System.err.println("Error while reading " + pathname + ": " + e.getMessage());
            successfullyConfigured = false;
        }
    }

    private VDR addinfo(VDR vdr, ArrayList<Map<String, Object>> vdrConfig, Map<String, String> aliases) {
        // Uuid suchen
        Optional<Map<String, Object>> cfg = vdrConfig.stream()
                .filter(s -> vdr.getUuid().equals(aliases.get(s.get("uuid")))).findFirst();

        if (cfg.isPresent()) {
            Map<String, Object> c = cfg.get();
            vdr.setDisplayName(StringUtils.defaultIfBlank((String) c.get("displayName"), vdr.getDisplayName()));
            vdr.setIp(StringUtils.defaultIfBlank((String) c.get("ip"), vdr.getIp()));
            vdr.setMac(StringUtils.defaultIfBlank((String) c.get("mac"), vdr.getMac()));
            vdr.setSvdrpPort(vdr.getSvdrpPort() == 0 ? (Integer) c.get("svdrpPort") : vdr.getSvdrpPort());
            vdr.setRestfulApiPort(c.get("restfulApiPort") != null ? (Integer) c.get("restfulApiPort") : 8002);

            // find the alias name
            vdr.setAlias(aliases.entrySet().stream().filter(s -> s.getValue().equals(vdr.getUuid())).findFirst().get()
                    .getKey());

            @SuppressWarnings("unchecked")
            Map<String, Object> vc = (Map<String, Object>) c.get("config");
            vc.keySet().stream().forEach(x -> {
                switch (x) {
                case "TIMER_AUX":
                    vdr.setTimerAux((String) vc.get(x));
                    break;

                case "TIMER_MINUS_MINUTES":
                    vdr.setTimerMinusMin((int) vc.get(x));
                    break;

                case "TIMER_PLUS_MINUTES":
                    vdr.setTimerPlusMin((int) vc.get(x));
                    break;

                case "TIMER_PRIORITY":
                    vdr.setTimerPrio((int) vc.get(x));
                    break;

                case "TIMER_LIFETIME":
                    vdr.setTimerLifetime((int) vc.get(x));
                    break;

                case "RECORDING_NAMING_MODE":
                    vdr.setDefaultRecordingNamingMode(RecordingNamingMode.fromId((int) vc.get(x)));
                    break;
                }
            });
        }

        return vdr;
    }

    private void checkDatabases() {
        // internal HSQLDB

        Integer currentVersion = isDatabaseVersion(prodVersion);

        // always call createDatabase
        createDatabase(prodVersion);

        if (currentVersion < prodVersion) {
            upgradeDatabase(currentVersion, prodVersion);
        }

        // MySQL, epg2vdr
        executeMysqlScript("/database/01-mysql_delete_timer_filename.sql");
        executeMysqlScript("/database/02-mysql_create_timer_filename.sql");
    }

    private void executeMysqlScript(String resource) {
        String sql = null;

        try {
            sql = Files.readAllLines(Paths.get(this.getClass().getResource(resource).toURI()), Charset.defaultCharset())
                    .stream().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("Unable to read mysql init script " + resource + "!", e);
            System.exit(-1);
        }

        try (Connection con = sql2oEpg2vdr.beginTransaction()) {
            con.createQuery(sql).executeUpdate();
            con.commit();
        } catch (Exception e) {
            log.error("Mysql database init failed!", e);
            System.exit(-1);
        }

    }

    private void createDatabase(int version) {
        log.info("Start internal database creation...");

        List<String> sql = null;

        try {
            sql = Files.readAllLines(Paths.get(this.getClass().getResource("/database/init_db.sql").toURI()),
                    Charset.defaultCharset());
        } catch (Exception e) {
            log.error("Unable to read database init script!", e);
            System.exit(-1);
        }

        List<String> statements = new ArrayList<String>();
        String stmt = "";
        for (String s : sql) {
            s = s.trim();
            if (s.length() > 0 && !s.startsWith("--")) {
                stmt = stmt + " " + s;
                if (s.indexOf(";;") >= 0) {
                    statements.add(stmt.replace(";;", ""));
                    stmt = "";
                }
            }
        }

        try (Connection con = sql2oHsqldb.beginTransaction()) {
            for (String s : statements) {
                log.info("Execute in internal database: " + s);
                con.createQuery(s).executeUpdate();
            }

            con.commit();
        } catch (Exception e) {
            log.error("Database init failed!", e);
            System.exit(-1);
        }
    }

    private void upgradeDatabase(int currentVersion, int prodVersion) {
        log.info("Start internal database upgrade...");
        // noch nix zu tun
    }

    private Integer isDatabaseVersion(int version) {
        try (Connection con = sql2oHsqldb.open()) {
            String val = con.createQuery("select val from configuration where name = 'version'")
                    .executeScalar(String.class);
            return Integer.valueOf(val);
        } catch (Exception e) {
            return null;
        }
    }

    private void startHsqlDatabase(Map<String, Object> config) {
        // log.info("Starte HSQLDB...");

        dbServer = new Server();

        if (Boolean.parseBoolean((String) config.get("remote"))) {
            dbServer.setDaemon(true);
            dbServer.setAddress("127.0.0.1");
            dbServer.setPort(9001);
        } else {
            dbServer.setDaemon(false);
        }

        dbServer.setDatabasePath(0, (String) config.get("path") + "/jonglisto");
        dbServer.setDatabaseName(0, "jonglisto");
        dbServer.setSilent(true);
        dbServer.start();
    }

    private void startSvdrpServer() {
        if ((svdrpPort == null) || (svdrpPort == 0)) {
            log.error("SVDRP Port is not configured. Server will not be started.");
            return;
        }

        svdrpServer = new SvdrpServer(svdrpPort, 10);
        Thread thread = new Thread(svdrpServer);
        thread.start();

        log.info("SVDRP Server started at port " + svdrpPort);
    }
}

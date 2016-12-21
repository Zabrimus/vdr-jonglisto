package vdr.jonglisto.lib.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import vdr.jonglisto.lib.VdrDataService;
import vdr.jonglisto.lib.exception.Http404Exception;
import vdr.jonglisto.lib.exception.NetworkException;
import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.Device;
import vdr.jonglisto.lib.model.Plugin;
import vdr.jonglisto.lib.model.RecPathSummary;
import vdr.jonglisto.lib.model.Recording;
import vdr.jonglisto.lib.model.RecordingInfo;
import vdr.jonglisto.lib.model.Timer;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.model.osd.TextOsd;
import vdr.jonglisto.lib.util.JonglistoUtil;

public class VdrDataServiceImpl extends ServiceBase implements VdrDataService {

    Logger log = LoggerFactory.getLogger(VdrDataService.class);

    protected ObjectMapper mapper;
    protected Pattern eventIdPattern = Pattern.compile("<eventid>(.*)<\\/eventid>");

    public VdrDataServiceImpl() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new AfterburnerModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
    }

    /*
     * Information
     */
    public List<Plugin> getPlugins(String vdrUuid) {
        try {
            JSONObject object = (JSONObject) getJsonData(vdrUuid, "info.json").get("vdr");
            return convertJSONArrayToList(object.getJSONArray("plugins"), Plugin.class);
        } catch (NetworkException e) {
            // could happen, if VDR is down
            log.debug("NetworkException for getPlugins, " + vdrUuid);
            return Collections.emptyList();
        }
    }

    public List<Device> getDevices(String vdrUuid) {
        try {
            JSONObject object = (JSONObject) getJsonData(vdrUuid, "info.json").get("vdr");
            return convertJSONArrayToList(object.getJSONArray("devices"), Device.class);
        } catch (NetworkException e) {
            // could happen, if VDR is down
            log.debug("NetworkException for getDevices, " + vdrUuid);
            return Collections.emptyList();
        }
    }

    /*
     * Channels
     */

    public Optional<Channel> getChannel(String vdrUuid, String channelId) {
        List<Channel> list = getJsonList(vdrUuid, "channels/" + JonglistoUtil.encode(channelId) + ".json", "channels",
                Channel.class).orElse(Collections.emptyList());
        return list.size() > 0 ? Optional.of(list.get(0)) : Optional.empty();
    }

    public Optional<List<Channel>> getChannels(String vdrUuid, boolean includeRadio) {
        Optional<List<Channel>> ch = getJsonList(vdrUuid, "channels/.json", "channels", Channel.class);

        if (!includeRadio && ch.isPresent()) {
            return Optional.of(ch.get().stream().filter(s -> !s.getRadio()).collect(Collectors.toList()));
        }

        return ch;
    }

    public Optional<List<String>> getGroups(String vdrUuid) {
        return getJsonList(vdrUuid, "channels/groups.json", "groups", String.class);
    }

    public Optional<List<Channel>> getChannelsInGroup(String vdrUuid, String group, boolean includeRadio) {
        if (group != null) {
            Optional<List<Channel>> ch = getJsonList(vdrUuid, "channels.json?group=" + JonglistoUtil.encode(group),
                    "channels", "name", Channel.class);

            if (!includeRadio && ch.isPresent()) {
                return Optional.of(ch.get().stream().filter(s -> !s.getRadio()).collect(Collectors.toList()));
            }

            return ch;
        } else {
            return getChannels(vdrUuid, includeRadio);
        }
    }

    public Optional<List<Channel>> getChannelsMap(String vdrUuid, boolean includeRadio) {
        // get List of channels in VDR
        return filterChannels(getChannels(vdrUuid, includeRadio));
    }

    public Optional<List<Channel>> getChannelsInGroupMap(String vdrUuid, String group, boolean includeRadio) {
        // get List of channels in VDR
        return filterChannels(getChannelsInGroup(vdrUuid, group, includeRadio));
    }

    /*
     * Timer
     */

    public Optional<List<Timer>> getTimer(String vdrUuid) {
        // get timer list
        Optional<List<Timer>> list = getJsonList(vdrUuid, "timers.json", "timers", Timer.class);
        List<Timer> timer = list.orElse(Collections.emptyList());

        // check if all timers has an eventid
        timer.stream().forEach(s -> enrichEventId(s));
        return Optional.of(timer);
    }

    public Optional<Timer> getTimerById(String vdrUuid, String timerId) {
        Optional<List<Timer>> list = getJsonList(vdrUuid, "timers/" + timerId + ".json", "timers", Timer.class);

        if (list.isPresent()) {
            return Optional.of(enrichEventId(list.get().get(0)));
        } else {
            return null;
        }
    }

    public void createTimer(String vdrUuid, Timer timer) {
        post(vdrUuid, "timers", timer.createTimer());
    }

    public void updateTimer(String vdrUuid, Timer oldTimer, Timer newTimer) {
        String request = oldTimer.createTimerDiff(newTimer);
        put(vdrUuid, "timers", request);
    }

    public void deleteTimer(String vdrUuid, Timer timer) {
        deleteTimer(vdrUuid, timer.getId());
    }

    public void deleteTimer(String vdrUuid, String timerId) {
        delete(vdrUuid, "timers/" + JonglistoUtil.encode(timerId), null);
    }

    public void bulkDeleteTimer(String vdrUuid, List<String> timerIds) {
        JSONArray ja = new JSONArray();
        timerIds.stream().forEach(s -> ja.put(s));

        JSONObject obj = new JSONObject();
        obj.put("timers", ja);

        delete(vdrUuid, "timers/bulkdelete.json", obj.toString());
    }

    public void activateTimer(String vdrUuid, String timerId) {
        put(vdrUuid, "timers", "timer_id=" + timerId + "&flags=1");
    }

    public void deactivateTimer(String vdrUuid, String timerId) {
        put(vdrUuid, "timers", "timer_id=" + timerId + "&flags=0");
    }

    /*
     * Recordings
     */

    public void fullRecSync(String vdrUuid) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.beginTransaction()) {
            // delete existing recordings
            con.createQuery("delete from recording where vdr_uuid = :vdrUuid") //
                    .addParameter("vdrUuid", vdrUuid) //
                    .executeUpdate();

            String syncIdStr = "";

            if (configuration.isUseSyncMap()) {
                // create new sync id if an existing syncid cannot be found
                Integer syncId = getRecSyncId(vdrUuid, con);

                if (syncId == null) {
                    con.createQuery(
                            "MERGE INTO configuration USING (VALUES(:name, concat((next value for seq_recording_sync),''))) AS vals(x,y) ON configuration.name = vals.x WHEN MATCHED THEN UPDATE SET configuration.val = vals.y WHEN NOT MATCHED THEN INSERT VALUES vals.x, vals.y") //
                            .addParameter("name", "rec_" + vdrUuid) //
                            .executeUpdate();

                    syncId = con.createQuery("select current value for seq_recording_sync from (VALUES(0))") //
                            .executeScalar(Integer.class);
                }

                syncIdStr = "?" + createSyncStr(vdrUuid, syncId);
            } else {
                syncIdStr = "";
            }

            // get recordings
            List<Recording> list = getJsonList(vdrUuid, "recordings/.json" + syncIdStr, "recordings", Recording.class) //
                    .orElse(Collections.<Recording>emptyList());

            // insert all recordings
            list.stream().forEach(s -> insertRecording(con, s, vdrUuid));

            con.commit();
        }
    }

    public void recSync(String vdrUuid) {
        if (!configuration.isUseSyncMap()) {
            fullRecSync(vdrUuid);
            return;
        }

        Sql2o sql2o = configuration.getSql2oHsqldb();

        // sync
        try (Connection con = sql2o.beginTransaction()) {
            // get sync id
            Integer syncId = getRecSyncId(vdrUuid, con);
            if (syncId == null) {
                // do a full sync
                fullRecSync(vdrUuid);
                return;
            }

            // Create parameter body
            List<Map<String, Object>> recList = con
                    .createQuery("select file_name, hash from recording where vdr_uuid = :vdruuid") //
                    .addParameter("vdruuid", vdrUuid) //
                    .executeAndFetchTable() //
                    .asList();

            String body = "";
            for (Map<String, Object> s : recList) {
                body += "recordings[]=" + JonglistoUtil.encodePath((String) s.get("file_name")) + "," + s.get("hash")
                        + "\n";
            }

            List<Recording> list;
            list = postAndGetList(vdrUuid, "recordings/sync.json?" + createSyncStr(vdrUuid, syncId), body, "recordings",
                    Recording.class);
            processRecordingList(vdrUuid, con, list);

            con.commit();
        }
    }

    public void recUpdate(String vdrUuid) {
        if (!configuration.isUseSyncMap()) {
            fullRecSync(vdrUuid);
            return;
        }

        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.beginTransaction()) {
            // get sync id
            Integer syncId = getRecSyncId(vdrUuid, con);
            if (syncId == null) {
                // do a full sync
                fullRecSync(vdrUuid);
                return;
            }

            List<Recording> list = getJsonList(vdrUuid, "recordings/updates.json?" + createSyncStr(vdrUuid, syncId),
                    "recordings", Recording.class).orElse(Collections.<Recording>emptyList());

            processRecordingList(vdrUuid, con, list);

            con.commit();
        }
    }

    public List<String> getRecDirectories(String vdrUuid) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        List<String> tmpResult;
        try (Connection con = sql2o.beginTransaction()) {
            tmpResult = con.createQuery("select name from recording where vdr_uuid = :vdruuid order by upper(name)") //
                    .addParameter("vdruuid", vdrUuid).executeScalarList(String.class);
        }

        List<String> result = new ArrayList<String>();

        for (String node : tmpResult) {
            int idx = node.lastIndexOf("~");
            if (idx != -1) {
                String path = node.substring(0, idx);

                // check if path(element) already exists
                if (!result.contains(path)) {
                    if (!result.stream().filter(s -> s.startsWith(path + "~")).findFirst().isPresent()) {
                        result.add(path);
                    }
                }
            }
        }

        return result;
    }

    public List<RecordingInfo> getRecordingsInPath(String vdrUuid, String path) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.open()) {
            return con.createQuery(
                    "SELECT NAME, regexp_substring(NAME, '[^~]*$') NAME, file_name fileName, relative_file_name relativeFileName, duration, filesize fileSize, event_start_time recStart FROM recording WHERE (( POSITION(CONCAT(:path, '~') IN NAME) = 1 AND POSITION('~' IN RIGHT(NAME,LENGTH(NAME) - LENGTH(CONCAT(:path, '~')))) = 0 ) OR ( (nvl(:path, '') = '') AND (POSITION('~' IN NAME) = 0) )) AND vdr_uuid = :vdruuid") //
                    .addParameter("vdruuid", vdrUuid) //
                    .addParameter("path", path) //
                    .executeAndFetch(RecordingInfo.class);
        }
    }

    public RecordingInfo getRecording(String vdruuid, String file) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.open()) {
            return con.createQuery(
                    "select regexp_substring(name, '[^~]*$') name, file_name fileName, relative_file_name relativeFileName, duration, filesize fileSize, event_start_time recStart, frames_per_second framesPerSecond, edited, channel_id channelId, event_title title, event_short_text shortText, event_description description, aux from recording where relative_file_name = :fileName and vdr_uuid = :vdruuid") //
                    .addParameter("fileName", file) //
                    .addParameter("vdruuid", vdruuid) //
                    .executeAndFetchFirst(RecordingInfo.class);
        }
    }

    public RecPathSummary getRecSummary(String vdrUuid, String path) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.open()) {
            return con.createQuery(
                    "select count(*) countRecordings, sum(duration) time, sum(filesize) size from recording where vdr_uuid = :vdruuid and name like concat(:path, '~%')") //
                    .addParameter("path", path) //
                    .addParameter("vdruuid", vdrUuid) //
                    .executeAndFetchFirst(RecPathSummary.class);
        }
    }

    public void deleteRecording(String vdrUuid, String fileName) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.beginTransaction()) {
            con.createQuery("delete from recording where vdr_uuid = :vdruuid and file_name=:fileName") //
                    .addParameter("vdruuid", vdrUuid) //
                    .addParameter("fileName", fileName) //
                    .executeUpdate();

            if (configuration.isUseSyncMap()) {
                delete(vdrUuid, "recordings" + JonglistoUtil.encodePath(fileName) + "?"
                        + createSyncStr(vdrUuid, getRecSyncId(vdrUuid, con)), null);
            } else {
                delete(vdrUuid, "recordings" + JonglistoUtil.encodePath(fileName), null);
            }

            con.commit();
        }

        if (configuration.isUseSyncMap()) {
            recUpdate(vdrUuid);
        } else {
            fullRecSync(vdrUuid);
        }
    }

    public void deleteRecordings(String vdrUuid, List<String> recordingsToChange) {
        if (recordingsToChange != null) {
            recordingsToChange.stream().forEach(s -> deleteRecording(vdrUuid, s));
        }
    }

    public void renameRecording(String vdrUuid, String fileName, String newName) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.beginTransaction()) {
            String oldName = con
                    .createQuery("select name from recording where vdr_uuid = :vdruuid and file_name = :fileName") //
                    .addParameter("fileName", fileName) //
                    .addParameter("vdruuid", vdrUuid) //
                    .executeScalar(String.class);

            // build new name
            String name = null;

            int idx = oldName.lastIndexOf("~");

            if (idx == -1) {
                name = newName;
            } else {
                name = oldName.substring(0, idx + 1) + newName;
            }

            name = name.replaceAll("/", "~");
            moveRecording(con, vdrUuid, fileName, name);

            con.commit();
        }

        if (configuration.isUseSyncMap()) {
            recUpdate(vdrUuid);
        } else {
            fullRecSync(vdrUuid);
        }
    }

    public void moveRecordings(String vdrUuid, List<String> recordingsToChange, String destination) {
        if (recordingsToChange != null) {
            recordingsToChange.stream().forEach(s -> moveRecording(vdrUuid, s, destination));
        }
    }

    public void moveRecording(String vdrUuid, String source, String destination) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.beginTransaction()) {
            String oldName = con
                    .createQuery("select name from recording where vdr_uuid = :vdruuid and file_name = :fileName") //
                    .addParameter("fileName", source) //
                    .addParameter("vdruuid", vdrUuid) //
                    .executeScalar(String.class);

            // build new name
            // Directories have to be splitted by '~' not '/'
            String name = null;

            destination = destination.replaceAll("/", "~");

            int idx = oldName.lastIndexOf("~");
            if (idx == -1) {
                name = destination + "~" + name;
            } else {
                name = destination + "~" + oldName.substring(idx + 1, oldName.length());
            }

            moveRecording(con, vdrUuid, source, name);
        }
    }

    /*
     * OSD
     */
    public TextOsd getOsd(String vdrUuid) {
        try {
            JSONObject json = getJsonData(vdrUuid, "osd.json");
            if (json.get("TextOsd") != null) {
                return mapper.readValue(json.get("TextOsd").toString(), TextOsd.class);
            } else {
                return null;
            }
        } catch (Http404Exception h404) {
            return null;
        } catch (Exception e) {
            // could happen, if VDR is down
            log.debug("NetworkException for getOsd, " + vdrUuid, e);
            return null;
        }
    }

    public void processKey(String vdrUuid, String key) {
        String kbd = StringUtils.capitalize(key.toLowerCase());
        post(vdrUuid, "remote/" + kbd, null);
    }

    public void processString(String vdrUuid, String string) {
        String kbd = StringUtils.capitalize(string.toLowerCase());
        post(vdrUuid, "remote/kbd", "{'kbd':'" + kbd.replaceAll("'", "\"") + "'}");
    }

    // TODO: OSD keyboard sequence

    /*
     * private helper functions
     */

    private <T> Optional<List<T>> getJsonList(String vdrUuid, String urlPart, String arrayName, String key,
            Class<T> clazz) {
        try {
            JSONArray array = getJsonData(vdrUuid, urlPart).getJSONArray(arrayName);

            if (key != null) {
                return Optional.of(convertJSONArrayToList(array, key, clazz));
            } else {
                return Optional.of(convertJSONArrayToList(array, clazz));
            }
        } catch (Exception e) {
            log.error("Fehler in getJsonList1: ", e);
            return Optional.empty();
        }
    }

    private <T> Optional<List<T>> getJsonList(String vdrUuid, String urlPart, String arrayName, Class<T> clazz) {
        try {
            JSONArray array = getJsonData(vdrUuid, urlPart).getJSONArray(arrayName);
            return Optional.of(convertJSONArrayToList(array, clazz));
        } catch (Exception e) {
            log.error("Fehler in getJsonList2: ", e);
            return Optional.empty();
        }
    }

    protected Timer enrichEventId(Timer timer) {
        if (timer.getEventId() == -1) {
            // try to find an eventid in epgsearch aux
            Matcher m = eventIdPattern.matcher(timer.getAux());
            if (m.find()) {
                timer.setEventId(Integer.valueOf(m.group(1)));
            } else {
                // try to find an event id in epg2vdr database

                // TODO: implement this.
                // perhaps this could work:
                // select * from eventsviewplain
                // where cnt_channelid = 'C-133-6-1124' (timer.getChannelId())
                // and cnt_starttime >= 1476064080 (timer.getStart())
                // and cnt_starttime + cnt_duration <= 1476067800;
                // (timer.getStop())

            }
        }

        return timer;
    }

    private String createSyncStr(String name, Integer syncId) {
        if (configuration.isUseSyncMap()) {
            return "syncId=" + JonglistoUtil.encode(name + ":" + syncId);
        } else {
            return "";
        }
    }

    private Integer getRecSyncId(String vdrUuid, Connection con) {
        return con.createQuery("select cast(val as integer) from configuration where name = concat('rec_', :vdruuid)") //
                .addParameter("vdruuid", vdrUuid) //
                .executeScalar(Integer.class);
    }

    private void moveRecording(Connection con, String vdrUuid, String source, String destination) {
        con.createQuery("delete from recording where vdr_uuid = :vdruuid and file_name = :fileName") //
                .addParameter("vdruuid", vdrUuid) //
                .addParameter("fileName", source) //
                .executeUpdate();

        post(vdrUuid, "recordings/move.json",
                "source=" + JonglistoUtil.encodePath(source) + "&target=" + JonglistoUtil.encodePath(destination));
    }

    private void insertRecording(Connection con, Recording r, String vdrUuid) {
        /*
         * if (USE_SYNC_ID) { // in esoteric cases (2 identical VDR
         * configurations), it is possible, // that a recording already exists
         * with the same hash but with different // file names. con.
         * createQuery("delete from recording where hash = :hash and vdr_uuid = :vdruuid"
         * ) // .addParameter("hash", r.getHash()) // .addParameter("vdruuid",
         * vdrUuid) // .executeUpdate(); }
         */

        con.createQuery(
                "insert into recording (id, vdr_uuid, number, name, file_name, relative_file_name, duration, frames_per_second, edited, filesize, channel_id, event_title, event_short_text, event_description, event_start_time, event_duration, hash, aux) values ((next value for seq_recording), :vdruuid, :number, :name, :file_name, :relative_file_name, :duration, :frames_per_second, :edited, :filesize, :channel_id, :event_title, :event_short_text, :event_description, :event_start, :event_duration, :hash, :aux)") //
                .addParameter("vdruuid", vdrUuid) //
                .addParameter("number", r.getNumber()) //
                .addParameter("name", r.getName()) //
                .addParameter("file_name", r.getFileName()) //
                .addParameter("relative_file_name", r.getRelativeFileName()) //
                .addParameter("edited", r.getEditedStr()) //
                .addParameter("filesize", r.getFileSize()) //
                .addParameter("channel_id", r.getChannelId()) //
                .addParameter("duration", r.getDuration()) //
                .addParameter("frames_per_second", r.getFramesPerSecond()) //
                .addParameter("event_title", r.getEventTitle()) //
                .addParameter("event_short_text", r.getEventShortText()) //
                .addParameter("event_description", r.getEventDescription()) //
                .addParameter("event_start", r.getEventStartTime()) //
                .addParameter("event_duration", r.getEventDuration()) //
                .addParameter("hash", r.getHash()) //
                .addParameter("aux", r.getAux()) //
                .executeUpdate();
    }

    private void deleteRecording(Connection con, Recording r, String vdrUuid) {
        con.createQuery("delete from recording where file_name = :fileName and vdr_uuid = :vdruuid") //
                .addParameter("fileName", r.getFileName()) //
                .addParameter("vdruuid", vdrUuid) //
                .executeUpdate();
    }

    private void processRecordingList(String vdrUuid, Connection con, List<Recording> list) {
        for (Recording r : list) {
            // add, delete or update.
            if ("add".equals(r.getSyncAction())) {
                insertRecording(con, r, vdrUuid);
            } else if ("delete".equals(r.getSyncAction())) {
                deleteRecording(con, r, vdrUuid);
            } else if ("update".equals(r.getSyncAction())) {
                deleteRecording(con, r, vdrUuid);
                insertRecording(con, r, vdrUuid);
            }
        }
    }

    public List<String> getDirectoriesWithLeafs(String vdrUuid, Connection con) {
        return con.createQuery("select name from recording where vdr_uuid = :vdruuid order by upper(name)") //
                .addParameter("vdruuid", vdrUuid).executeScalarList(String.class);
    }

    private Optional<List<Channel>> filterChannels(Optional<List<Channel>> vdrChannels) {
        // get List of channels in epg2vdr
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            final List<String> channelMap = con.createQuery("select distinct channelid from channelmap")
                    .executeAndFetch(String.class);

            // find all VDR channels, which are also in channelMap
            return Optional.of(vdrChannels.orElse(Collections.emptyList()).stream()
                    .filter(c -> channelMap.contains(c.getId())).collect(Collectors.toList()));
        }
    }

    /*
     * conversion methods
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> convertJSONArrayToList(JSONArray array, Class<T> clazz) {
        List<T> result = new ArrayList<>();

        array.forEach(s -> {
            try {
                if (clazz == String.class) {
                    result.add((T) s);
                } else {
                    result.add(mapper.readValue(s.toString(), clazz));
                }
            } catch (Exception e) {
                throw new NetworkException("unknown error; " + e.getMessage());
            }
        });

        return result;
    }

    protected <T> List<T> convertJSONArrayToList(JSONArray array, String key, Class<T> clazz) {
        List<T> result = new ArrayList<>();

        array.forEach(s -> {
            try {
                result.add(mapper.readValue(s.toString(), clazz));
            } catch (Exception e) {
                throw new NetworkException("unknown error; " + e.getMessage());
            }
        });

        return result;
    }

    /*
     * low level rest api methods
     */
    private String getVdrRestUrl(String vdrUuid) {
        VDR v = configuration.getVdr(vdrUuid);
        return "http://" + v.getIp() + ":" + v.getRestfulApiPort() + "/";
    }

    protected JSONObject getJsonData(String vdrUuid, String path) {
        JSONObject result;
        HttpResponse<String> jsonResponse;

        try {
            String restUrl = getVdrRestUrl(vdrUuid) + path;

            if (log.isDebugEnabled()) {
                log.debug("GET: " + restUrl);
            }

            jsonResponse = Unirest.get(restUrl).asString();
        } catch (UnirestException e) {
            throw new NetworkException(e);
        }

        if ((jsonResponse != null) && (jsonResponse.getStatus() == 200)) {
            result = new JSONObject(jsonResponse.getBody());
        } else {
            if (jsonResponse == null) {
                throw new NetworkException("unknown error: Keine Antwort erhalten");
            } else {
                if (jsonResponse.getStatus() == 404) {
                    throw new Http404Exception("unknown error, jsonResponse: " + jsonResponse.getHeaders() + ", "
                            + jsonResponse.getBody() + ", Code: " + jsonResponse.getStatus());
                } else {
                    throw new NetworkException("unknown error, jsonResponse: " + jsonResponse.getHeaders() + ", "
                            + jsonResponse.getBody() + ", Code: " + jsonResponse.getStatus());
                }
            }
        }

        return result;
    }

    protected String put(String vdrUuid, String path, String body) {
        try {
            String restUrl = getVdrRestUrl(vdrUuid) + path;

            if (log.isDebugEnabled()) {
                log.debug("PUT: " + restUrl + "\n" + body);
            }

            HttpResponse<String> result = Unirest.put(restUrl).body(body).asString();
            if (result.getStatus() != 200) {
                throw new NetworkException(
                        "Put failed with code " + result.getStatus() + ", " + result.getStatusText());
            }

            return result.getBody();
        } catch (UnirestException e) {
            throw new NetworkException(e);
        }
    }

    protected String delete(String vdrUuid, String path, String body) {
        try {
            String restUrl = getVdrRestUrl(vdrUuid) + path;

            HttpResponse<String> result;

            if (log.isDebugEnabled()) {
                log.debug("DELETE: " + restUrl + "\n" + body);
            }

            if (body != null) {
                result = Unirest.delete(restUrl).body(body).asString();
            } else {
                result = Unirest.delete(restUrl).asString();
            }

            if (result.getStatus() == 404) {
                // recording not found, but we want to delete this
                return result.getBody();
            } else if (result.getStatus() != 200) {
                throw new NetworkException(
                        "Delete failed with code " + result.getStatus() + ", " + result.getStatusText());
            }

            return result.getBody();
        } catch (UnirestException e) {
            throw new NetworkException(e);
        }
    }

    protected String post(String vdrUuid, String path, String body) {
        String restUrl = getVdrRestUrl(vdrUuid) + path;

        try {
            HttpResponse<String> result;

            if (log.isDebugEnabled()) {
                log.debug("POST: " + restUrl + "\n" + body);
            }

            if (body != null) {
                result = Unirest.post(restUrl).body(body).asString();
            } else {
                result = Unirest.post(restUrl).asString();
            }

            if (result.getStatus() != 200) {
                throw new NetworkException(
                        "Post failed with code " + result.getStatus() + ", " + result.getStatusText());
            }

            return result.getBody();
        } catch (UnirestException e) {
            throw new NetworkException(e);
        }
    }

    protected <T> List<T> postAndGetList(String vdrUuid, String urlPart, String body, String name, Class<T> clazz) {
        JSONArray array = new JSONObject(post(vdrUuid, urlPart, body)).getJSONArray(name);
        return convertJSONArrayToList(array, clazz);
    }

}

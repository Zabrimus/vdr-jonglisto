package vdr.jonglisto.lib.impl;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import vdr.jonglisto.lib.EpgDataService;
import vdr.jonglisto.lib.exception.NotImplementedException;
import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.RecordingNamingMode;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria;
import vdr.jonglisto.lib.util.JonglistoUtil;

public class HsqlDataServiceImpl extends ServiceBase implements EpgDataService {

    private Logger log = LoggerFactory.getLogger(ChannelMapServiceImpl.class);
    private Map<String, List<Pattern>> epgInfoConfig;

    private static String selectFromView = //
            "SELECT " + //
                    "cast(greatest(0, round(CAST((:unixtime - start_time) AS float) / casewhen(duration <> 0, duration, 1) * 100)) as BIGINT) AS proz , "
                    + //
                    "to_char(cast(timestamp_with_zone(start_time) AS time), 'HH24:MI') AS v_starttime, " + //
                    "to_char(cast(timestamp_with_zone(start_time + duration) AS time), 'HH24:MI') AS v_endtime, " + //
                    "to_char(cast(timestamp_with_zone(start_time) AS date), 'DAY DD.MM.YYYY') AS v_longstartdate, " + //
                    "to_char(cast(timestamp_with_zone(start_time) AS date), 'DD.MM.YYYY') AS v_startdate, " + //
                    "to_char(cast(timestamp_with_zone(start_time + duration) AS date), 'DD.MM.YYYY') AS v_enddate, " + //
                    "to_char(cast(timestamp(duration) AS time), 'HH24:MI') AS v_duration, " + //
                    "cast(e.useid as BIGINT) AS useid, " + //
                    "e.EPGID AS eventid, " + //
                    "e.CHANNEL AS channelid, " + //
                    "NULL AS imageid, " + //
                    "e.TITLE AS title, " + //
                    "e.SHORT_TEXT AS shorttext, " + //
                    "e.START_TIME AS starttime, " + //
                    "e.START_TIME + e.DURATION AS endtime, " + //
                    "e.DURATION AS duration, " + //
                    "e.PARENTAL_RATING AS parentalrating, " + //
                    "e.IMAGES AS imagecount, " + //
                    "e.GENRE AS genre, " + //
                    "e.CATEGORY AS category, " + //
                    "e.COUNTRY AS country, " + //
                    "e.\"YEAR\" AS YEAR, " + //
                    "e.SHORT_TEXT AS shortdescription, " + //
                    "NULL AS shortreview, " + //
                    "NULL AS tipp, " + //
                    "NULL AS rating, " + //
                    "NULL AS topic, " + //
                    "e.DESCRIPTION AS longdescription, " + //
                    "NULL AS moderator, " + //
                    "NULL AS guest, " + //
                    "e.ACTORS AS actor, " + //
                    "e.PRODUCER AS producer, " + //
                    "NULL AS other, " + //
                    "e.DIRECTOR AS director, " + //
                    "e.SCREENPLAY AS screenplay, " + //
                    "e.CAMERA AS camera, " + //
                    "NULL AS music, " + //
                    "NULL AS audio, " + //
                    "e.FLAGS AS flags, " + //
                    "e.EPISODE AS episodename, " + //
                    "e.SHORTNAME AS shortname, " + //
                    "NULL AS partname, " + //
                    "NULL AS extracol1, " + //
                    "NULL AS extracol2, " + //
                    "NULL AS extracol3, " + //
                    "e.SEASON AS season, " + //
                    "e.PART AS part, " + //
                    "e.PARTS AS parts, " + //
                    "NULL AS number, " + //
                    "NULL AS sub_scrseriesid, " + //
                    "NULL AS sub_scrseriesepisode, " + //
                    "NULL AS sub_scrmovieid, " + //
                    "SOURCE AS MERGE, " + //
                    "NULL AS series_overview, " + //
                    "NULL AS series_firstaired, " + //
                    "NULL AS series_network, " + //
                    "NULL AS series_rating, " + //
                    "NULL AS series_status, " + //
                    "NULL AS episode_overview, " + //
                    "NULL AS episode_rating, " + //
                    "NULL AS movie_tagline, " + //
                    "NULL AS movie_overview, " + //
                    "NULL AS movie_release_date, " + //
                    "NULL AS movie_vote_average " + //
                    "FROM epg e";

    private static String selectFromRecordingList = //
            "SELECT " + //
                    "0 AS proz, " + //
                    "to_char(cast(timestamp_with_zone(event_start_time) AS time), 'HH24:MI') AS v_starttime, " + //
                    "to_char(cast(timestamp_with_zone(event_start_time + event_duration) AS time), 'HH24:MI') AS v_starttime, " + //
                    "to_char(cast(timestamp_with_zone(event_start_time) AS date), 'DAY DD.MM.YYYY') AS v_longstartdate, " + //
                    "to_char(cast(timestamp_with_zone(event_start_time) AS date), 'DD.MM.YYYY') AS v_startdate, " + //
                    "to_char(cast(timestamp_with_zone(event_start_time + event_duration) AS date), 'DD.MM.YYYY') AS v_enddate, " + //
                    "to_char(cast(timestamp(event_duration) AS time), 'HH24:MI') AS v_duration, " + //
                    "0 AS useid, " + //
                    "0 AS eventid, " + //
                    "e.CHANNEL_id AS channelid, " + //
                    "e.event_TITLE AS title, " + //
                    "e.event_SHORT_TEXT AS shorttext, " + //
                    "e.event_START_TIME AS starttime, " + //
                    "e.event_START_TIME + e.DURATION AS endtime, " + //
                    "e.event_DURATION AS duration, " + //
                    "0 AS imagecount, " + //
                    "e.event_SHORT_TEXT AS shortdescription, " + //
                    "e.event_DESCRIPTION AS longdescription " + //
                    "FROM recording e " + //
                    "WHERE relative_FILE_NAME = :filename";

    public HsqlDataServiceImpl() {
        epgInfoConfig = new HashMap<>();

        try {
            Files.readAllLines(Paths.get(this.getClass().getResource("/vdr/jonglisto/lib/conf/epginfo.cat").toURI()),
                    Charset.defaultCharset()).stream() //
                    .forEach(line -> {
                        if (!line.startsWith("#")) {
                            try {
                                int equalIdx = line.indexOf("=");
                                if (equalIdx != -1) {
                                    epgInfoConfig.put(line.substring(0, equalIdx).trim(),
                                            Arrays.stream(line.substring(equalIdx + 1).trim().split(",")) //
                                                    .map(s -> Pattern.compile("(?m)(^" + s.trim() + ":(.*?))$")) //
                                                    .collect(Collectors.toList()));
                                }
                            } catch (Exception e) {
                                log.error("Unable to split line '" + line + "' in epginfo.cat: " + e.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            // no configuration available
            log.error("Unable to load epginfo.cat. Extended EPG infos were not extracted.");
        }
    }

    @Override
    public Map<String, Object> getEpgDataForUseId(Long id) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.open()) {
            List<Map<String, Object>> result;
            
            result = con.createQuery(selectFromView + " WHERE useid = :useid") //
                    .addParameter("useid", id) //
                    .addParameter("unixtime", System.currentTimeMillis() / 1000L) //
                    .executeAndFetchTable() //
                    .asList();

            if ((result != null) && !result.isEmpty()) {
                return result.get(0);
            }
            
            // Fallback. It is possible, that information are available with another criteria
            result = con.createQuery(selectFromView + " WHERE epgid = :useid") //
                    .addParameter("useid", id) //
                    .addParameter("unixtime", System.currentTimeMillis() / 1000L) //
                    .executeAndFetchTable() //
                    .asList();

            if ((result != null) && !result.isEmpty()) {
                return result.get(0);
            }

            // no data available
            return Collections.<String, Object>emptyMap();            
        }
    }

    @Override
    public List<Map<String, Object>> getEpgDataForUseIds(List<Map<String, Object>> useIds) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.open()) {
            // filter useids
            String ids = useIds.stream().map(s -> ((Long) s.get("cnt_useid")).toString())
                    .collect(Collectors.joining(","));

            if (StringUtils.isEmpty(ids)) {
                // no result found
                return Collections.emptyList();
            }

            List<Map<String, Object>> epg = con.createQuery(selectFromView + " WHERE useid in (" + ids + ")")
                    .addParameter("unixtime", System.currentTimeMillis() / 1000L) //
                    .executeAndFetchTable() //
                    .asList();

            // Make a copy of the list. Currently it is immutable.
            List<Map<String, Object>> result = new ArrayList<>();
            result.addAll(epg);

            return result;
        }
    }

    @Override
    public Map<String, Object> getEpgDataForRecording(String recFilename) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.open()) {
            List<Map<String, Object>> r = con.createQuery(selectFromRecordingList) //
                    .addParameter("filename", recFilename) //
                    .executeAndFetchTable() //
                    .asList();

            if ((r != null) && (r.size() > 0)) {
                // make a deep copy of the result to be able to enrich data
                Map<String, Object> newResult = new HashMap<>();
                r.get(0).keySet().stream().forEach(s -> newResult.put(s, r.get(0).get(s)));

                newResult.putAll(extractDescription((String) newResult.get("longdescription")));
                return newResult;
            } else {
                return Collections.emptyMap();
            }
        }
    }

    @Override
    public Map<String, Object> getMediaIdsForUseId(Long id) {
        return Collections.<String, Object>emptyMap();
    }

    @Override
    public List<Map<String, Object>> getEpgData(Collection<Channel> channels, EpgSearchCriteria criteria) {
        String select = selectFromView + " WHERE channel IN (" + JonglistoUtil.joinChannelId(channels) + ")";
        return getDataWithCriteria(select, criteria);
    }

    @Override
    public List<Map<String, Object>> getEpgDayData(EpgSearchCriteria epgCriteria) {
        String select = selectFromView
                + " WHERE e.start_time + e.duration >= UNIX_TIMESTAMP() AND timestamp_with_zone(start_time) <= timestamp(UNIX_TIMESTAMP( CURDATE() + INTERVAL 1 DAY - INTERVAL 1 SECOND))";
        return getDataWithCriteria(select, epgCriteria);
    }

    @Override
    public List<Map<String, Object>> getEpgChannelData(EpgSearchCriteria epgCriteria) {
        String select = selectFromView + " WHERE e.start_time + e.duration >= UNIX_TIMESTAMP()";
        return getDataWithCriteria(select, epgCriteria);
    }

    @Override
    public String getVdrTimerName(Long id, RecordingNamingMode naming_mode) {
        // i don't know how to handle this
        
        return null;
    }

    @Override
    public List<String> getGenres() {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.open()) {
            return con.createQuery("select distinct genre from epg where genre is not null order by genre") //
                    .executeAndFetch(String.class);
        }
    }

    @Override
    public List<String> getCategories() {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.open()) {
            return con.createQuery("select distinct category from epg where category is not null order by category") //
                    .executeAndFetch(String.class);
        }
    }

    @Override
    public List<Map<String, Object>> selectGeneric(String sql) {
        throw new NotImplementedException("Call of selectGeneric is not implemented. The call shall be prevented!");
    }

    @Override
    public synchronized void updateInternalEpgData(String vdrUuid) {
        log.info("Start to update EPG database using VDR with uuid " + vdrUuid);

        // read all epg data
        JSONArray events = getJsonData(vdrUuid, "events.json").getJSONArray("events");

        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.beginTransaction()) {
            // delete all existing data
            con.createQuery("delete from epg").executeUpdate();

            // insert new data
            events.forEach(s -> {
                JSONObject singleEvent = (JSONObject) s;

                Query query = con.createQuery(
                        "insert into epg (useid, epgid, title, short_text, description, start_time, channel, channel_name, duration, images, timer_exists, timer_active, timer_id, parental_rating, genre, category, country, year, actors, flags, source, producer, camera, director, season, part, parts, epi_number, episode, shortname, screenplay) values ((next value for seq_epg), :epgid, :title, :short_text, :description, :start_time, :channel, :channel_name, :duration, :images, :timer_exists, :timer_active, :timer_id, :parental_rating, :genre, :category, :country, :year, :actors, :flags, :source, :producer, :camera, :director, :season, :part, :parts, :epi_number, :episode, :shortname, :screenplay)") //
                        .addParameter("epgid", singleEvent.get("id")) //
                        .addParameter("title", singleEvent.get("title")) //
                        .addParameter("short_text", singleEvent.get("short_text")) //
                        .addParameter("start_time", singleEvent.get("start_time")) //
                        .addParameter("channel", singleEvent.get("channel")) //
                        .addParameter("channel_name", singleEvent.get("channel_name")) //
                        .addParameter("duration", singleEvent.get("duration")) //
                        .addParameter("images", singleEvent.get("images")) //
                        .addParameter("timer_exists", singleEvent.get("timer_exists")) //
                        .addParameter("timer_active", singleEvent.get("timer_active")) //
                        .addParameter("timer_id", singleEvent.get("timer_id")) //
                        .addParameter("parental_rating", singleEvent.get("parental_rating"));

                addDescriptionParameter(query, (String) singleEvent.get("description")) //
                        .executeUpdate();
            });

            con.commit();
        }

        log.info("EPG Database update finished");
    }

    private Map<String, String> extractDescription(String text) {
        // collect all parameters and values
        Map<String, String> parameters = new HashMap<>();

        String[] mytext = new String[1];
        mytext[0] = text;

        epgInfoConfig.keySet().stream().forEach(key -> {
            List<Pattern> values = epgInfoConfig.get(key);

            values.stream().forEach(v -> {
                // Search info, extract value and save it into the database
                Matcher m = v.matcher(mytext[0]);
                if (m.find()) {
                    // instead collect all values
                    parameters.put(key, m.group(2).trim());

                    // remove match in description
                    mytext[0] = mytext[0].replace(m.group(1), "");
                } else if (parameters.get(key) == null) {
                    // get() == null could mean 1. key exists but value is null,
                    // or 2. key does not exists.
                    parameters.putIfAbsent(key, (String) null);
                }
            });
        });

        mytext[0] = mytext[0].replaceAll("\n{3,}", "\n\n").trim();
        parameters.put("description", mytext[0]);
        parameters.put("longdescription", mytext[0]);

        return parameters;
    }

    private Query addDescriptionParameter(Query query, String text) {
        Map<String, String> parameters = extractDescription(text);
        parameters.keySet().stream().forEach(s -> query.addParameter(s, parameters.get(s)));
        return query;
    }

    private List<Map<String, Object>> getDataWithCriteria(String baseSelect, EpgSearchCriteria criteria) {
        Sql2o sql2o = configuration.getSql2oHsqldb();

        try (Connection con = sql2o.open()) {
            if (criteria.isTimeEnabled()) {
                baseSelect += " AND :unixtime >= start_time AND :unixtime < (start_time + duration)";
            }

            if (criteria.getGenre() != null) {
                baseSelect += " AND genre = :genre";
            }

            if (criteria.getCategory() != null) {
                baseSelect += " AND category = :category";
            }

            if (criteria.getSearchText() != null) {
                baseSelect += " AND (upper(title) like upper(:text) OR upper(shorttext) like upper(:text))";
            }

            if (criteria.isChannelEnabled() && (criteria.getChannel() != null)) {
                baseSelect += " AND channel = :channelid";
            }

            baseSelect += "  ORDER BY starttime ASC";

            Query query = con.createQuery(baseSelect);

            if (criteria.isTimeEnabled() && criteria.getTime() != null) {
                query = query.addParameter("unixtime", criteria.getTime());
            } else {
                query = query.addParameter("unixtime", System.currentTimeMillis() / 1000L);
            }

            if (criteria.getGenre() != null) {
                query = query.addParameter("genre", criteria.getGenre());
            }

            if (criteria.getCategory() != null) {
                query = query.addParameter("category", criteria.getCategory());
            }

            if (criteria.getSearchText() != null) {
                query.addParameter("text", "%" + criteria.getSearchText() + "%");
            }

            if (criteria.isChannelEnabled() && (criteria.getChannel() != null)) {
                query.addParameter("channelid", criteria.getChannel().getId());
            }

            // Make a copy of the list. Currently it is immutable.
            List<Map<String, Object>> result = new ArrayList<>();
            result.addAll(query.executeAndFetchTable().asList());

            return result;
        }
    }
}

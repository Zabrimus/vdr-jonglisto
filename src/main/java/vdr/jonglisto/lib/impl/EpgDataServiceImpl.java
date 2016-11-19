package vdr.jonglisto.lib.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import vdr.jonglisto.lib.EpgDataService;
import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.RecordingNamingMode;
import vdr.jonglisto.lib.model.search.EpgSearchCriteria;
import vdr.jonglisto.lib.util.JonglistoUtil;

public class EpgDataServiceImpl extends ServiceBase implements EpgDataService {

    private static String selectFromView = //
            "SELECT " + //
                    "greatest( " + //
                    "   0, " + //
                    "   ROUND(( :unixtime - cnt_starttime ) / cnt_duration * 100 ) " + //
                    ") AS proz, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( cnt_starttime ), '%H:%i' ) AS v_starttime, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( cnt_starttime + cnt_duration ), '%H:%i' ) AS v_endtime, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( cnt_starttime ), '%W %d.%m.%Y' ) AS v_longstartdate, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( cnt_starttime ), '%d.%m.%Y' ) AS v_startdate, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( cnt_starttime + cnt_duration ), '%d.%m.%Y' ) AS v_enddate, " + //
                    "TIME_FORMAT( timediff(FROM_UNIXTIME(cnt_duration), FROM_UNIXTIME(0)), '%H:%i') as v_duration, " + //
                    "cnt_useid AS useid, " + //
                    "cnt_eventid AS eventid, " + //
                    "cnt_channelid AS channelid, " + //
                    "imageid AS imageid, " + //
                    "sub_title AS title, " + //
                    "sub_shorttext AS shorttext, " + //
                    "cnt_starttime AS starttime, " + //
                    "cnt_starttime + cnt_duration AS endtime, " + //
                    "cnt_duration AS duration, " + //
                    "cnt_parentalrating AS parentalrating, " + //
                    "sub_imagecount AS imagecount, " + //
                    "sub_genre AS genre, " + //
                    "sub_category AS category, " + //
                    "sub_country AS country, " + //
                    "sub_year AS YEAR, " + //
                    "sub_shortdescription AS shortdescription, " + //
                    "sub_shortreview AS shortreview, " + //
                    "sub_tipp AS tipp, " + //
                    "sub_rating AS rating, " + //
                    "sub_topic AS topic, " + //
                    "sub_longdescription AS longdescription, " + //
                    "sub_moderator AS moderator, " + //
                    "sub_guest AS guest, " + //
                    "sub_actor AS actor, " + //
                    "sub_producer AS producer, " + //
                    "sub_other AS other, " + //
                    "sub_director AS director, " + //
                    "sub_screenplay AS screenplay, " + //
                    "sub_camera AS camera, " + //
                    "sub_music AS music, " + //
                    "sub_audio AS audio, " + //
                    "sub_flags AS flags, " + //
                    "epi_episodename AS episodename, " + //
                    "epi_shortname AS shortname, " + //
                    "epi_partname AS partname, " + //
                    "epi_extracol1 AS extracol1, " + //
                    "epi_extracol2 AS extracol2, " + //
                    "epi_extracol3 AS extracol3, " + //
                    "epi_season AS season, " + //
                    "epi_part AS part, " + //
                    "epi_parts AS parts, " + //
                    "epi_number AS NUMBER, " + //
                    "sub_scrseriesid, " + //
                    "sub_scrseriesepisode, " + //
                    "sub_scrmovieid, " + //
                    "MERGE, " + //
                    "ser.series_overview, " + //
                    "ser.series_firstaired, " + //
                    "ser.series_network, " + //
                    "ser.series_rating, " + //
                    "ser.series_status, " + //
                    "serep.episode_overview, " + //
                    "serep.episode_rating, " + //
                    "mov.movie_tagline, " + //
                    "mov.movie_overview, " + //
                    "mov.movie_release_date, " + //
                    "mov.movie_vote_average " + //
                    "FROM " + //
                    "eventsviewplain " + //
                    "left join series ser on ser.series_id = sub_scrseriesid " + //
                    "left join series_episode serep on serep.episode_id = sub_scrseriesepisode " + //
                    "left join movie mov on mov.movie_id = sub_scrmovieid ";

    private static String selectFromRecordingList = //
            "SELECT " + //
                    "100 as proz, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( starttime ), '%H:%i' ) AS v_starttime, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( starttime + duration ), '%H:%i' ) AS v_endtime, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( starttime ), '%W %d.%m.%Y' ) AS v_longstartdate, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( starttime ), '%d.%m.%Y' ) AS v_startdate, " + //
                    "DATE_FORMAT( FROM_UNIXTIME( starttime + duration ), '%d.%m.%Y' ) AS v_enddate, " + //
                    "TIME_FORMAT( timediff(FROM_UNIXTIME(duration), FROM_UNIXTIME(0)), '%H:%i') AS v_duration, " + //
                    "0 AS useid, " + //
                    "0 AS eventid, " + //
                    "channelid AS channelid, " + //
                    "channelname AS channelName, " + //
                    "title AS title, " + //
                    "shorttext AS shorttext, " + //
                    "starttime AS starttime, " + //
                    "starttime + duration AS endtime, " + //
                    "duration AS duration, " + //
                    "fsk AS parentalrating, " + //
                    "0 AS imagecount, " + //
                    "genre AS genre, " + //
                    "category AS category, " + //
                    "country AS country, " + //
                    "year AS YEAR, " + //
                    "shorttext AS shortdescription, " + //
                    "shortreview AS shortreview, " + //
                    "tipp AS tipp, " + //
                    "rating AS rating, " + //
                    "topic AS topic, " + //
                    "longdescription AS longdescription, " + //
                    "moderator AS moderator, " + //
                    "guest AS guest, " + //
                    "actor AS actor, " + //
                    "producer AS producer, " + //
                    "other AS other, " + //
                    "director AS director, " + //
                    "screenplay AS screenplay, " + //
                    "camera AS camera, " + //
                    "music AS music, " + //
                    "audio AS audio, " + //
                    "flags AS flags, " + //
                    "scrseriesid, " + //
                    "scrseriesepisode, " + //
                    "scrinfomovieid, " + //
                    "ser.series_overview, " + //
                    "ser.series_firstaired, " + //
                    "ser.series_network, " + //
                    "ser.series_rating, " + //
                    "ser.series_status, " + //
                    "serep.episode_overview, " + //
                    "serep.episode_rating, " + //
                    "mov.movie_tagline, " + //
                    "mov.movie_overview, " + //
                    "mov.movie_release_date, " + //
                    "mov.movie_vote_average " + //
                    "FROM " + //
                    "recordinglist " + //
                    "LEFT JOIN series ser  " + //
                    "ON ser.series_id = scrseriesid LEFT JOIN series_episode serep " + //
                    "ON serep.episode_id = scrseriesepisode LEFT JOIN movie mov " + //
                    "ON mov.movie_id = scrinfomovieid " + //
                    "where concat('/', path) = :path";

    private static String selectMediaIdsFromView = //
            "SELECT " + //
                    "cnt_useid AS useid, " + //
                    "cnt_eventid AS eventid, " + //
                    "imageid AS imageid, " + //
                    "sub_scrseriesid as scrseriedid, " + //
                    "sub_scrseriesepisode as scrseriesepisode, " + //
                    "sub_scrmovieid as scrmovieid" + //
                    "FROM " + //
                    "eventsviewplain ";

    private static Map<String, String> recInfos;

    static {
        recInfos = new HashMap<>();

        recInfos.put("Genre:", "genre");
        recInfos.put("Kategorie:", "category");
        recInfos.put("Land:", "country");
        recInfos.put("Regie:", "director");
        recInfos.put("Jahr:", "YEAR");
        recInfos.put("Thema:", "topic");
        recInfos.put("Moderator:", "moderator");
        recInfos.put("Gäste:", "guest");
        recInfos.put("Altersempfehlung: ab", "parentalrating");
        recInfos.put("Darsteller:", "actor");
        recInfos.put("Produzent:", "producer");
        recInfos.put("Sonstige:", "other");
        recInfos.put("Drehbuch:", "screenplay");
        recInfos.put("Kamera:", "camera");
        recInfos.put("Musik:", "music");
        recInfos.put("Audio:", "audio");
        recInfos.put("Flags:", "flags");
        recInfos.put("Serie:", "episodename");
        recInfos.put("Kurzname:", "partname");
        recInfos.put("Episode:", "episodename");
        recInfos.put("Staffel:", "season");
        recInfos.put("Staffelfolge:", "part");
        recInfos.put("Staffelfolgen:", "parts");
        recInfos.put("Folge:", "epi_number");
        recInfos.put("Quelle:", "MERGE");
    }

    public Map<String, Object> getEpgDataForUseId(Long id) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            List<Map<String, Object>> result = con.createQuery(selectFromView + " WHERE cnt_useid = :useid") //
                    .addParameter("useid", id) //
                    .addParameter("unixtime", System.currentTimeMillis() / 1000L) //
                    .executeAndFetchTable() //
                    .asList();

            if ((result != null) && !result.isEmpty()) {
                return result.get(0);
            } else {
                return Collections.<String, Object>emptyMap();
            }
        }
    }

    public List<Map<String, Object>> getEpgDataForUseIds(List<Map<String, Object>> useIds) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            // filter useids
            String ids = useIds.stream().map(s -> ((Long)s.get("cnt_useid")).toString()).collect(Collectors.joining(","));
            
            if (StringUtils.isEmpty(ids)) {
                // no result found
                return Collections.emptyList();
            }
            
            List<Map<String, Object>> epg = con.createQuery(selectFromView + " WHERE cnt_useid in (" + ids + ")")
                    .addParameter("unixtime", System.currentTimeMillis() / 1000L) //
                    .executeAndFetchTable() //
                    .asList();

            // Make a copy of the list. Currently it is immutable.
            List<Map<String, Object>> result = new ArrayList<>();
            result.addAll(epg);

            return result;
        }
    }
    
    public Map<String, Object> getEpgDataForRecording(String recFilename) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            List<Map<String, Object>> result = con.createQuery(selectFromRecordingList) //
                    .addParameter("path", recFilename) //
                    .executeAndFetchTable() //
                    .asList();

            if ((result != null) && !result.isEmpty()) {
                // Make a copy of the map, because not all 'normal' fields are
                // present
                // and then we always get a NullPointerException from the map
                // implementation of sql2o
                Map<String, Object> newResult = new HashMap<>();
                result.get(0).keySet().stream().forEach(s -> newResult.put(s, result.get(0).get(s)));

                // longdescription filtern
                String desc = (String) newResult.get("longdescription");
                for (String s : recInfos.keySet()) {
                    Pattern p = Pattern.compile("(?m)^" + s + "(.*?)$");
                    Matcher m = p.matcher(desc);

                    if (m.find()) {
                        String r = m.group(1);
                        newResult.put(recInfos.get(s), r.trim());
                        desc = m.replaceFirst("");
                    }
                }

                newResult.put("longdescription", desc);

                return newResult;
            } else {
                return Collections.<String, Object>emptyMap();
            }
        }
    }

    public Map<String, Object> getMediaIdsForUseId(Long id) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            List<Map<String, Object>> result = con.createQuery(selectMediaIdsFromView + " WHERE cnt_useid = :eventId;") //
                    .addParameter("eventId", id) //
                    .executeAndFetchTable() //
                    .asList();

            if ((result != null) && !result.isEmpty()) {
                return result.get(0);
            } else {
                return Collections.<String, Object>emptyMap();
            }
        }
    }

    public List<Map<String, Object>> getEpgData(Collection<Channel> channels, EpgSearchCriteria criteria) {
        String select = selectFromView + " where cnt_channelid IN (" + JonglistoUtil.joinChannelId(channels) + ")";
        return getDataWithCriteria(select, criteria);
    }

    public List<Map<String, Object>> getEpgDayData(EpgSearchCriteria epgCriteria) {
        String select = selectFromView
                + " WHERE cnt_starttime + cnt_duration >= UNIX_TIMESTAMP() AND cnt_starttime <= UNIX_TIMESTAMP( CURDATE() + INTERVAL 1 DAY - INTERVAL 1 SECOND )";
        return getDataWithCriteria(select, epgCriteria);
    }

    public List<Map<String, Object>> getEpgChannelData(EpgSearchCriteria epgCriteria) {
        String select = selectFromView + " WHERE cnt_starttime + cnt_duration >= UNIX_TIMESTAMP()";
        return getDataWithCriteria(select, epgCriteria);
    }

    private List<Map<String, Object>> getDataWithCriteria(String baseSelect, EpgSearchCriteria criteria) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            Query localeQuery = con.createQuery("SET lc_time_names = 'de_DE'");
            localeQuery.executeUpdate();

            if (criteria.isTimeEnabled()) {
                baseSelect += " AND :unixtime >= cnt_starttime AND :unixtime < (cnt_starttime + cnt_duration)";
            }

            if (criteria.getGenre() != null) {
                baseSelect += " AND sub_genre = :genre";
            }

            if (criteria.getCategory() != null) {
                baseSelect += " AND sub_category = :category";
            }

            if (criteria.getSearchText() != null) {
                baseSelect += " AND (upper(sub_title) like upper(:text) OR upper(sub_shorttext) like upper(:text))";
            }

            if (criteria.isChannelEnabled() && (criteria.getChannel() != null)) {
                baseSelect += " AND cnt_channelid = :channelid";
            }

            baseSelect += "  ORDER BY cnt_starttime ASC";

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

    public String getVdrTimerName(Long id, RecordingNamingMode namingMode) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        int modeInt = namingMode.getId();

        try (Connection con = sql2o.open()) {
            return con.createQuery("select create_timer_filename(:useid, :namingmode) as filename") //
                    .addParameter("useid", id) //
                    .addParameter("namingmode", modeInt).executeScalar(String.class);
        }
    }

    public List<String> getGenres() {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            return con.createQuery(
                    "select distinct sub_genre from eventsviewplain where sub_genre is not null order by sub_genre") //
                    .executeAndFetch(String.class);
        }
    }

    public List<String> getCategories() {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            return con.createQuery(
                    "select distinct sub_category from eventsviewplain where sub_category is not null order by sub_category") //
                    .executeAndFetch(String.class);
        }
    }
}

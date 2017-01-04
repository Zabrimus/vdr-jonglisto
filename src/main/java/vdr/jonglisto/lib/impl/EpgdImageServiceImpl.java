package vdr.jonglisto.lib.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import vdr.jonglisto.lib.EpgImageService;
import vdr.jonglisto.lib.model.EPGMedia;
import vdr.jonglisto.lib.model.EPGMedia.MediaType;

public class EpgdImageServiceImpl extends ServiceBase implements EpgImageService {

    public List<String> getImageFilenames(Long useId) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            List<String> result = con.createQuery(
                    "select distinct ir.imagename from imagerefs ir, images i where ir.eventid = (select imageid from eventsviewplain where cnt_useid = :useid) and ir.imagename = i.imagename and i.image is not null") //
                    .addParameter("useid", useId) //
                    .executeAndFetch(String.class);

            if ((result != null) && !result.isEmpty()) {
                return result;
            } else {
                return Collections.<String>emptyList();
            }
        }
    }

    public List<String> getImageFilenames(String filename) {
        // TODO: implement this
        // but it is unsure if recordings have an EPG image
        return Collections.<String>emptyList();
    }

    public byte[] getEpgImage(String filename) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            return con.createQuery("select image from images where imagename = :imagename") //
                    .addParameter("imagename", filename) //
                    .executeAndFetchFirst(byte[].class);
        }
    }

    public byte[] getSeriesMediaImage(long seriesId, long seasonNumber, long episodeId, int mediaType, long actorId) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            byte[] result;

            result = con.createQuery(
                    "select media_content from series_media where series_id = :seriesId and season_number = :seasonNumber and episode_id = :episodeId and media_type = :mediaType and actor_id = :actorId ") //
                    .addParameter("seriesId", seriesId) //
                    .addParameter("seasonNumber", seasonNumber) //
                    .addParameter("episodeId", episodeId) //
                    .addParameter("mediaType", mediaType) //
                    .addParameter("actorId", actorId) //
                    .executeAndFetchFirst(byte[].class);

            return result;
        }
    }

    public byte[] getMovieMediaImage(long movieId, int mediaType, long actorId) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        try (Connection con = sql2o.open()) {
            byte[] result;

            result = con.createQuery(
                    "select media_content from movie_media where movie_id = :movieId and media_type = :mediaType and actor_id = :actorId ") //
                    .addParameter("movieId", movieId) //
                    .addParameter("mediaType", mediaType) //
                    .addParameter("actorId", actorId) //
                    .executeAndFetchFirst(byte[].class);

            return result;
        }
    }

    public Set<MediaType> getAvailableMediaTypes(Long useId, String filename) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        Set<EPGMedia.MediaType> result = new HashSet<>();

        try (Connection con = sql2o.open()) {
            Map<String, Object> ids;

            if (useId != null) {
                ids = getMediaIds(useId, con);
            } else {
                ids = getRecMediaIds(filename, con);
            }

            String query = "select  m.media_type " + //
                    "from series_media m " + //
                    "left join series_actor a on a.actor_id = m.actor_id " + //
                    "where m.series_id = :seriesid " + //
                    "and   m.episode_id in (0, :episodeid) " + //
                    "and   m.season_number in (0, :season) " + //
                    "union " + //
                    "select  m.media_type " + //
                    "from movie_media m " + //
                    "left join series_actor a on a.actor_id = m.actor_id " + //
                    "where m.movie_id = :movieid";

            List<Integer> types = con.createQuery(query) //
                    .addParameter("seriesid", ids.get("scrseriedid")) //
                    .addParameter("episodeid", ids.get("scrseriesepisode")) //
                    .addParameter("season", ids.get("season")) //
                    .addParameter("movieid", ids.get("scrmovieid")) //
                    .executeAndFetch(Integer.class);

            types.stream().forEach(s -> {
                switch (s) {
                case 0:
                case 1:
                case 2:
                    result.add(MediaType.Banner);
                    break;

                case 3:
                case 4:
                case 5:
                    result.add(MediaType.Poster);
                    break;

                case 6:
                    result.add(MediaType.SeasonPoster);
                    break;

                case 7:
                case 8:
                case 9:
                    result.add(MediaType.FanArt);
                    break;

                case 10:
                    result.add(MediaType.EpisodePic);
                    break;

                case 11:
                    result.add(MediaType.Actor);
                    break;
                }
            });

            return result;
        }
    }

    public List<EPGMedia> getEpgMedia(Long useId, String filename, MediaType type) {
        Sql2o sql2o = configuration.getSql2oEpg2vdr();

        List<EPGMedia> result = new ArrayList<>();

        try (Connection con = sql2o.open()) {
            Map<String, Object> ids;

            if (useId != null) {
                ids = getMediaIds(useId, con);
            } else {
                ids = getRecMediaIds(filename, con);
            }

            if ((Long) ids.get("scrseriedid") > 0) {
                // Select series media
                String querySeriesStr = "select :tabType as tabType, 'Series' as type, m.media_type as mediaType, m.series_id as seriesId, m.episode_id as episodeId, m.season_number as seasonNumber, m.actor_id as actorId, a.actor_name as actorName, a.actor_role as actorRole, a.actor_sortorder as actorSortOrder "
                        + //
                        "from series_media m " + //
                        "left join series_actor a on a.actor_id = m.actor_id " + //
                        "where m.series_id = :seriesid " + //
                        "and   m.episode_id in (0, :episodeid) " + //
                        "and   m.season_number in (0, :season) " + //
                        createMediaTypeQuery(type) + //
                        "order by a.actor_sortorder";

                Query querySeries = con.createQuery(querySeriesStr) //
                        .addParameter("tabType", type) //
                        .addParameter("seriesid", ids.get("scrseriedid")) //
                        .addParameter("episodeid", ids.get("scrseriesepisode")) //
                        .addParameter("season", ids.get("season"));

                result.addAll(querySeries.executeAndFetch(EPGMedia.class));
            }

            if ((Long) ids.get("scrmovieid") > 0) {
                // Select movies media
                String queryMoviesStr = "select :tabType as tabType, 'Movie' as type, m.media_type as mediaType, m.movie_id as movieId, m.actor_id as actorId, a.actor_name as actorName, a.actor_role as actorRole, a.actor_sortorder as actorSortOrder "
                        + //
                        "from movie_media m " + //
                        "left join series_actor a on a.actor_id = m.actor_id " + //
                        "where m.movie_id = :movieid " + //
                        createMediaTypeQuery(type) + //
                        "order by a.actor_sortorder";

                Query queryMovies = con.createQuery(queryMoviesStr) //
                        .addParameter("tabType", type) //
                        .addParameter("movieid", ids.get("scrmovieid"));

                result.addAll(queryMovies.executeAndFetch(EPGMedia.class));
            }

            return result;
        }
    }

    private Map<String, Object> getRecMediaIds(String filename, Connection con) {
        // get all necessary ids
        return con
                .createQuery("SELECT " + //
                        "0 AS useid, " + //
                        "0 AS eventid, " + //
                        "0 AS imageid, " + //
                        "ifnull(scrseriesid, 0) AS scrseriedid, " + //
                        "ifnull(scrseriesepisode, 0) AS scrseriesepisode, " + //
                        "ifnull(scrinfomovieid, 0) AS scrmovieid, " + //
                        "0 AS season " + //
                        "from recordinglist " + //
                        "where concat('/', path) = :path")
                .addParameter("path", filename) //
                .executeAndFetchTable() //
                .asList() //
                .get(0);
    }

    private Map<String, Object> getMediaIds(Long useId, Connection con) {
        // get all necessary ids
        return con
                .createQuery("SELECT " + //
                        "cnt_useid AS useid, " + //
                        "cnt_eventid AS eventid, " + //
                        "imageid AS imageid, " + //
                        "ifnull(sub_scrseriesid, 0) AS scrseriedid, " + //
                        "ifnull(sub_scrseriesepisode, 0) AS scrseriesepisode, " + //
                        "ifnull(sub_scrmovieid, 0) AS scrmovieid, " + //
                        "ifnull(epi_season, 0) AS season " + //
                        "FROM eventsviewplain " + //
                        "WHERE cnt_useid = :useid")
                .addParameter("useid", useId) //
                .executeAndFetchTable() //
                .asList() //
                .get(0);
    }

    private String createMediaTypeQuery(MediaType type) {
        switch (type) {
        case Actor:
            return " and m.media_type = 11 ";

        case Banner:
            return " and m.media_type in (0,1,2) ";

        case EpisodePic:
            return " and m.media_type = 10 ";

        case FanArt:
            return " and m.media_type in (7,8,9) ";

        case Poster:
            return " and m.media_type in (3,4,5) ";

        case SeasonPoster:
            return " and m.media_type = 6 ";

        default:
            return "";
        }
    }
}

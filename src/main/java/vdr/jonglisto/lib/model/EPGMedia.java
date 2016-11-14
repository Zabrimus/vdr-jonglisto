package vdr.jonglisto.lib.model;

public class EPGMedia {

    public enum MediaType {
        All, Banner, Poster, SeasonPoster, FanArt, EpisodePic, Actor
    }

    public enum Type {
        Series, Movie
    }

    private MediaType tabType;
    private Type type;

    // primary key for series_media
    private long seriesId;
    private long seasonNumber;
    private long episodeId;

    // primary key for movie_media
    private long movieId;

    // common key for series_media and movie_media
    private int mediaType;
    private long actorId;

    // additional information
    private String actorName;
    private String actorRole;
    private Integer actorSortOrder;

    public EPGMedia() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(long seriesId) {
        this.seriesId = seriesId;
    }

    public long getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(long seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public long getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(long episodeId) {
        this.episodeId = episodeId;
    }

    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public long getActorId() {
        return actorId;
    }

    public void setActorId(long actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public Integer getActorSortOrder() {
        return actorSortOrder;
    }

    public void setActorSortOrder(Integer actorSortOrder) {
        this.actorSortOrder = actorSortOrder;
    }

    public MediaType getTabType() {
        return tabType;
    }

    public void setTabType(MediaType tabType) {
        this.tabType = tabType;
    }

    @Override
    public String toString() {
        return "EPGMedia [tabType=" + tabType + ", type=" + type + ", seriesId=" + seriesId + ", seasonNumber="
                + seasonNumber + ", episodeId=" + episodeId + ", movieId=" + movieId + ", mediaType=" + mediaType
                + ", actorId=" + actorId + ", actorName=" + actorName + ", actorRole=" + actorRole + ", actorSortOrder="
                + actorSortOrder + "]";
    }
}

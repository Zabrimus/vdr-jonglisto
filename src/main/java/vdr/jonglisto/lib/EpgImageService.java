package vdr.jonglisto.lib;

import java.util.List;
import java.util.Set;

import vdr.jonglisto.lib.model.EPGMedia;
import vdr.jonglisto.lib.model.EPGMedia.MediaType;

public interface EpgImageService {

    public List<String> getImageFilenames(Long useId);

    public List<String> getImageFilenames(String filename);

    public byte[] getEpgImage(String filename);

    public byte[] getSeriesMediaImage(long seriesId, long seasonNumber, long episodeId, int mediaType, long actorId);

    public byte[] getMovieMediaImage(long movieId, int mediaType, long actorId);

    public Set<EPGMedia.MediaType> getAvailableMediaTypes(Long useid, String filename);

    public List<EPGMedia> getEpgMedia(Long useid, String filename, MediaType type);
}

package vdr.jonglisto.lib.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import vdr.jonglisto.lib.EpgImageService;
import vdr.jonglisto.lib.model.EPGMedia;
import vdr.jonglisto.lib.model.EPGMedia.MediaType;

public class HsqlImageServiceImpl implements EpgImageService {

    @Override
    public List<String> getImageFilenames(Long useId) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getImageFilenames(String filename) {
        return Collections.emptyList();
    }

    @Override
    public byte[] getEpgImage(String filename) {
        return null;
    }

    @Override
    public byte[] getSeriesMediaImage(long seriesId, long seasonNumber, long episodeId, int mediaType, long actorId) {
        return null;
    }

    @Override
    public byte[] getMovieMediaImage(long movieId, int mediaType, long actorId) {
        return null;
    }

    @Override
    public Set<MediaType> getAvailableMediaTypes(Long useid, String filename) {
        return Collections.emptySet();
    }

    @Override
    public List<EPGMedia> getEpgMedia(Long useid, String filename, MediaType type) {
        return Collections.emptyList();
    }
}

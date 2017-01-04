package vdr.jonglisto.lib.impl;

import java.util.List;
import java.util.Set;

import vdr.jonglisto.lib.EpgImageService;
import vdr.jonglisto.lib.model.EPGMedia;
import vdr.jonglisto.lib.model.EPGMedia.MediaType;

public class EpgImageServiceFacadeImpl extends ServiceBase implements EpgImageService {

    private EpgImageService service;
    
    public EpgImageServiceFacadeImpl() {
        if (configuration.isUseEpgd()) {
            service = new EpgdImageServiceImpl();
        } else {
            service = new HsqlImageServiceImpl();
        }
    }

    @Override
    public List<String> getImageFilenames(Long useId) {
        return service.getImageFilenames(useId);
    }

    @Override
    public List<String> getImageFilenames(String filename) {
        return service.getImageFilenames(filename);
    }

    @Override
    public byte[] getEpgImage(String filename) {
        return service.getEpgImage(filename);
    }

    @Override
    public byte[] getSeriesMediaImage(long seriesId, long seasonNumber, long episodeId, int mediaType, long actorId) {
        return service.getSeriesMediaImage(seriesId, seasonNumber, episodeId, mediaType, actorId);
    }

    @Override
    public byte[] getMovieMediaImage(long movieId, int mediaType, long actorId) {
        return service.getMovieMediaImage(movieId, mediaType, actorId);
    }

    @Override
    public Set<MediaType> getAvailableMediaTypes(Long useid, String filename) {
        return service.getAvailableMediaTypes(useid, filename);
    }

    @Override
    public List<EPGMedia> getEpgMedia(Long useid, String filename, MediaType type) {
        return service.getEpgMedia(useid, filename, type);
    }

}

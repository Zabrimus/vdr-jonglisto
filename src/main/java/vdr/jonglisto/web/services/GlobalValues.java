package vdr.jonglisto.web.services;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.tapestry5.ioc.annotations.Inject;

import vdr.jonglisto.lib.EpgDataService;

/**
 * Caches the genre and categories of events, because the select runtime is too
 * long
 */
public class GlobalValues {

    @Inject
    protected EpgDataService epgService;

    private Set<String> genres = Collections.synchronizedSet(new TreeSet<>());
    private Set<String> categories = Collections.synchronizedSet(new TreeSet<>());

    private long lastGenreFetchTime = 0;
    private long lastCategoriesFetchTime = 0;

    public synchronized List<String> getGenres() {
        if ((genres.size() == 0) || ((System.currentTimeMillis() - lastGenreFetchTime > 4 * 60 * 60 * 1000))) { // 4
                                                                                                                // hours
            genres.addAll(epgService.getGenres());
        }

        return genres.stream().collect(Collectors.toList());
    }

    public synchronized List<String> getCategories() {
        if ((categories.size() == 0) || ((System.currentTimeMillis() - lastCategoriesFetchTime > 4 * 60 * 60 * 1000))) { // 4
                                                                                                                         // hours
            categories.addAll(epgService.getCategories());
        }

        return categories.stream().collect(Collectors.toList());
    }

}

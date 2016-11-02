package vdr.jonglisto.web.services;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class GlobalLogoFilename {

	private Map<String, String> filenames = Collections.synchronizedMap(new HashMap<>());

	public InputStream getResource(String channelName) {
		if ((channelName != null) && (channelName.length() > 0)) {
			// first check, if there already exists an entry in the cache
			if (filenames.containsKey(channelName)) {
				String fn = filenames.get(channelName);
				if (fn == null) {
					return null;
				} else {
					return this.getClass().getClassLoader().getResourceAsStream(fn);
				}
			}

			// create a new normalized filename and search the resource
			String filename = StringUtils.stripAccents(channelName)//
					.replaceAll("\\&", "and")//
					.replaceAll("\\+", "plus")//
					.replaceAll("\\*", "star");

			String filename2 = filename.replaceAll("[^A-Za-z0-9]", "").toLowerCase();

			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("/logo/" + filename2 + ".png");

			if (inputStream == null) {
				// try to find similar channel images
				filename2 = filename.replaceAll("\\w*\\(S\\)$", "").replaceAll("\\w*HD$", "").replaceAll("[^A-Za-z0-9]", "").toLowerCase();
				inputStream = this.getClass().getClassLoader().getResourceAsStream("/logo/" + filename2 + ".png");
			}

			// put data into cache
			if (inputStream != null) {
				filenames.put("channelName", "/logo/" + filename2 + ".png");
			} else {
				filenames.put("channelName", null);
			}

			return inputStream;
		}

		return null;
	}

}

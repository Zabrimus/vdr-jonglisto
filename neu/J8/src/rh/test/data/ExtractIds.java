package rh.test.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ExtractIds {

	public static void main(String argv[]) {
		// set proxy configuration
		System.setProperty("proxySet", "true");
		System.setProperty("http.proxyHost", "mmoproxy.mdv.mmo.de");
		System.setProperty("https.proxyHost", "mmoproxy.mdv.mmo.de");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("https.proxyPort", "8080");

		// TVSP
		try {
			List<IdModel> tvsp = readTvsp();
			writeToFile(tvsp, "etc/tvsp.id");
		} catch (Exception e) {
			System.err.println("Extracting TVSP ids failed: " + e.getMessage());
		}
		
		// TVM
		try {
			List<IdModel> tvm = readTvm();
			writeToFile(tvm, "etc/tvm.id");
		} catch (Exception e) {
			System.err.println("Extracting TVM ids failed: " + e.getMessage());
		}
		
		// EPGDATA (unknown format)
	}

	public static void writeToFile(List<IdModel> input, String filename) {
		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(filename)))) {
			input.stream().forEach(s -> pw.print(s.getId() + "~" + s.getName() + "\n"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<IdModel> readTvsp() throws JsonProcessingException, IOException {
		// read data
		String myUrl = "https://live.tvspielfilm.de/static/content/channel-list/livetv";
		List<String> result = doGet(myUrl, true);

		// List<IdModel> tvsp = readJsonStr(result.get(0));
		List<IdModel> tvsp = readJsonStr(result.get(0));
		return tvsp;
	}

	public static List<IdModel> readTvm() {
		List<IdModel> tvm = new ArrayList<>();

		// read data
		String myUrl = "http://wwwa.tvmovie.de/static/tvghost/html/onlinedata/cftv520/datainfo.txt";
		List<String> result = doGet(myUrl, false);

		// extract information
		for (int i = 3; i < result.size(); i += 2) {
			tvm.add(new IdModel(result.get(i + 1), result.get(i)));
		}

		return tvm;
	}

	private static List<String> doGet(String getFrom, boolean isZipped) {
		URL url = null;
		BufferedReader reader = null;

		try {
			url = new URL(getFrom);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setReadTimeout(15 * 1000);
			connection.connect();

			if (!isZipped) {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} else {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream())));
			}

			List<String> result = new ArrayList<>();

			String line = null;
			while ((line = reader.readLine()) != null) {
				result.add(line);
			}

			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		}
	}

	private static List<IdModel> readJsonStr(String json) throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		final JsonNode response = mapper.readTree(json);
		final CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(List.class, IdModel.class);
		List<Object> object = mapper.readerFor(collectionType).readValues(response.traverse()).readAll();

		@SuppressWarnings("unchecked")
		List<IdModel> result = (List<IdModel>) object.get(0);
		
		return result;
	}
}

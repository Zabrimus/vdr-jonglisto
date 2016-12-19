package vdr.jonglisto.lib.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import vdr.jonglisto.lib.ChannelMapService;
import vdr.jonglisto.lib.VdrDataService;
import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.channelmap.ChannelModel;
import vdr.jonglisto.lib.model.channelmap.IdModel;
import vdr.jonglisto.lib.model.channelmap.Provider;
import vdr.jonglisto.lib.util.JonglistoUtil;

public class ChannelMapServiceImpl extends ServiceBase implements ChannelMapService {

	private Logger log = LoggerFactory.getLogger(ChannelMapServiceImpl.class);
	private VdrDataService vdrDataService = new VdrDataServiceImpl();

	@Override
	public void saveProvider(Provider mainProv, Provider secProv) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery("delete from configuration where name in ('mainprov', 'secprov')").executeUpdate();
			
			con.createQuery("insert into configuration (name, val) values (:name, :prov)") //
					.addParameter("name", "mainprov") //
					.addParameter("prov", mainProv.toString()) //
					.executeUpdate();

			con.createQuery("insert into configuration (name, val) values (:name, :prov)") //
					.addParameter("name", "secprov") //
					.addParameter("prov", secProv.toString()) //
					.executeUpdate();

			con.commit();
		}
	}

	@Override
	public Provider[] readProvider() {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.open()) {
			Provider[] result = new Provider[2];

			try {
				result[0] = Provider.valueOf(con.createQuery("select val from configuration where name = 'mainprov'")
						.executeScalar(String.class));
			} catch (Exception e) {
				// could happen, if there is currently no configuration =>
				// ignore
			}

			try {
				result[1] = Provider.valueOf(con.createQuery("select val from configuration where name = 'secprov'")
						.executeScalar(String.class));
			} catch (Exception e) {
				// could happen, if there is currently no configuration =>
				// ignore
			}

			return result;
		}
	}

	@Override
	public void updateEpgIds(int providerId, String epgDataPin) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.beginTransaction()) {
			// URL bestimmen
			String zu = con.createQuery("select url from epg_provider where id = :id and enabled = true") //
					.addParameter("id", providerId) //
					.executeScalar(String.class);

			if (zu != null) {
				List<IdModel> result = null;

				String url = JonglistoUtil.unzipBase64(zu);
				switch (providerId) {
				case 1:
					result = new ArrayList<>();
					List<String> provids1 = doGet(url, false);

					// extract information
					for (int i = 3; i < provids1.size(); i += 2) {
						result.add(new IdModel(providerId, provids1.get(i + 1), provids1.get(i)));
					}

					break;

				case 2:
					List<String> provids2 = doGet(url, true);

					try {
						result = readJsonStr(provids2.get(0));
					} catch (IOException e) {
						log.error("Failed to load epg provider " + providerId, e);
						result = null;
					}
					break;

				case 3:
					// this provider is currently not implemented
					if (epgDataPin != null) {
						url.replaceAll("##PIN##", epgDataPin);
					}
					break;
				}

				// save data into the database
				if (result != null) {
					// delete old data
					con.createQuery("delete from epg_ids where ref_provider_id = :id") //
							.addParameter("id", providerId) //
							.executeUpdate();

					// insert new data
					result.stream().forEach(s -> {
						con.createQuery(
								"insert into epg_ids (ref_provider_id, epgid, name) values (:id, :epgid, :name)") //
								.addParameter("id", providerId) //
								.addParameter("epgid", s.getId()) //
								.addParameter("name", s.getName()) //
								.executeUpdate();
					});
				}

				con.commit();
			}
		}
	}

	@Override
	public List<IdModel> getIds(int providerId) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.open()) {
			return con.createQuery(
					"select epgid as id, name, ref_provider_id as providerId from epg_ids where ref_provider_id = :id") //
					.addParameter("id", providerId) //
					.executeAndFetch(IdModel.class);
		}
	}

	@Override
	public List<String> getAllEpgIds() {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.open()) {
			return con.createQuery("select distinct name from epg_ids order by name asc") //
					.executeAndFetch(String.class);
		}
	}

	@Override
	public List<ChannelModel> getChannelIds(String vdrUuid) {
		final List<ChannelModel> result;

		Optional<List<Channel>> ch = vdrDataService.getChannels(vdrUuid, false);
		if (ch.isPresent()) {
			result = new ArrayList<>();

			ch.get().stream().forEach(s -> {
				if (!s.getRadio()) {
					ChannelModel model = new ChannelModel();
					model.setChannelId(s.getId());
					model.setName(s.getName());
					model.setNumber(s.getNumber());
					result.add(model);
				}
			});

			if (result.size() == 0) {
				return null;
			}
		} else {
			return null;
		}

		return result;
	}

	@Override
	public void addIncludeChannel(ChannelModel channel) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery("delete from channel_include where channel_name = :name").executeUpdate();

			con.createQuery("insert into channel_include values (:name)") //
					.addParameter("name", channel.getName()) //
					.executeUpdate();
			con.commit();
		}
	}

	@Override
	public void removeIncludeChannel(String name) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery("delete from channel_include where channel_name = :name").executeUpdate();
			con.commit();
		}
	}

	@Override
	public void replaceIncludeChannel(List<String> channels) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery("delete from channel_include").executeUpdate();

			channels.stream().forEach(s -> {
				con.createQuery("insert into channel_include (channel_name) values (:ch)") //
						.addParameter("ch", s) //
						.executeUpdate();
			});

			con.commit();
		}
	}

	@Override
	public List<ChannelModel> getIncludedChannels(String vdrUuid) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		List<ChannelModel> result = new ArrayList<>();

		try (Connection con = sql2o.open()) {
			List<ChannelModel> channels = getChannelIds(vdrUuid);
			List<String> includeList = con.createQuery("select channel_name from channel_include")
					.executeAndFetch(String.class);

			includeList.stream().forEach(s -> {
				result.add(channels.stream().filter(c -> c.getName().equals(s)).findFirst().get());
			});
		}

		return result;
	}

	public List<Channel> getIncludedVdrChannels(String vdrUuid) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		List<Channel> result = new ArrayList<>();

		try (Connection con = sql2o.open()) {
			List<Channel> channels = vdrDataService.getChannels(vdrUuid, false).get();
			List<String> includeList = con.createQuery("select channel_name from channel_include")
					.executeAndFetch(String.class);

			includeList.stream().forEach(s -> {
				result.add(channels.stream().filter(c -> c.getName().equals(s)).findFirst().get());
			});
		}

		return result;
	}

	@Override
	public Map<String, List<Object>> doAutoMapping(String vdrUuid) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		Map<String, List<Object>> result = new HashMap<>();

		try (Connection con = sql2o.open()) {
			// read the channel name mapping
			Map<String, String> channelNameMapping = getNameMapping();

			// test if there exists a channel_include, otherwise don't filter
			// channels
			boolean doFiltering = con.createQuery("select count(*) from channel_include")
					.executeScalar(Integer.class) > 0;

			// read all available channels and filter them
			List<ChannelModel> channels = getChannelIds(vdrUuid);

			if (doFiltering) {
				channels = channels.stream().filter(s -> {
					int count = con.createQuery("select count(*) from channel_include where channel_name = :name") //
							.addParameter("name", s.getName()) //
							.executeScalar(Integer.class);

					return count > 0;
				}).collect(Collectors.toList());
			}

			// prepare result
			channels.stream().forEach(s -> {
				if (result.containsKey(s.getNormalizedName(channelNameMapping))) {
					result.get(s.getNormalizedName(channelNameMapping)).add(s);
				} else {
					List<Object> list = new ArrayList<>();
					list.add(s);
					result.put(s.getNormalizedName(channelNameMapping), list);
				}
			});

			// iterate over all providers
			for (int i = 1; i <= 3; ++i) {
				getIds(i).stream().forEach(s -> {
					if (result.containsKey(s.getNormalizedName(channelNameMapping))) {
						result.get(s.getNormalizedName(channelNameMapping)).add(s);
					}
				});
			}
		}

		// save and return
		saveMapping(result);
		return result;
	}

	@Override
	public Map<String, List<Object>> readMapping(String vdrUuid) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		Map<String, List<Object>> result = new HashMap<>();

		// read all available channels
		List<ChannelModel> channels = getChannelIds(vdrUuid);

		try (Connection con = sql2o.open()) {
			// read all mappings
			con.createQuery("select channel_name, mapping from channel_map").executeAndFetchTable().asList().stream()
					.forEach(s -> {
						String key = (String) s.get("channel_name");
						JSONArray array = new JSONArray((String) s.get("mapping"));

						final List<Object> list = new ArrayList<>();

						array.forEach(ja -> {
							JSONObject obj = (JSONObject) ja;

							obj.keySet().stream().forEach(k -> {
								switch ((int) obj.get(k)) {
								case 0:
									ChannelModel ch = findChannelByName(k, channels);
									if (ch != null) {
										list.add(ch);
									}
									break;

								case 1:
								case 2:
								case 3:
									list.add(getIds((int) obj.get(k)).stream().filter(ids -> ids.getId().equals(k))
											.findFirst().get());
									break;
								}
							});
						});

						result.put(key, list);
					});
		}

		return result;
	}

	@Override
	public void saveMapping(Map<String, List<Object>> input) {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery("delete from channel_map").executeUpdate();

			input.keySet().stream().forEach(s -> {
				JSONArray array = new JSONArray();

				List<Object> list = input.get(s);

				if (list.size() > 0) {
					list.stream().forEach(k -> {
						String name = null;

						if (k instanceof ChannelModel) {
							ChannelModel idmod = (ChannelModel) k;
							JSONObject jo = new JSONObject();
							jo.put(idmod.getName(), 0);
							array.put(jo);

							name = idmod.getName();
						} else {
							IdModel idmod = (IdModel) k;
							JSONObject jo = new JSONObject();
							jo.put(idmod.getId(), idmod.getProviderId());
							array.put(jo);

							name = idmod.getName();
						}

						if (!JonglistoUtil.channelNameNormalize(name).equals(s)) {
							int count = con
									.createQuery(
											"select count(*) from channel_name_mapping where channel_name_a = :name") //
									.addParameter("name", name) //
									.executeScalar(Integer.class);

							if (count > 0) {
								con.createQuery(
										"update channel_name_mapping set channel_name_b = :norm where channel_name_a = :name") //
										.addParameter("norm", s) //
										.addParameter("name", name) //
										.executeUpdate();
							} else {
								con.createQuery(
										"insert into channel_name_mapping (channel_name_a, channel_name_b) values (:name, :norm)") //
										.addParameter("norm", s) //
										.addParameter("name", name) //
										.executeUpdate();
							}
						}
					});

					con.createQuery("insert into channel_map (channel_name, mapping) values (:name, :mapping)") //
							.addParameter("name", s) //
							.addParameter("mapping", array.toString()) //
							.executeUpdate();
				}
			});

			con.commit();
		}
	}

	public Map<String, String> getNameMapping() {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.open()) {
			// read the channel name mapping
			Map<String, String> channelNameMapping = new Hashtable<>();
			List<Map<String, Object>> cnm = con.createQuery("select * from channel_name_mapping").executeAndFetchTable()
					.asList();
			cnm.stream().forEach(s -> {
				channelNameMapping.put((String) s.get("channel_name_a"), (String) s.get("channel_name_b"));
			});

			return channelNameMapping;
		}
	}

	@Override
	public void clearAll() {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery("delete from channel_map").executeUpdate();
			con.createQuery("delete from channel_include").executeUpdate();
			con.createQuery("delete from epg_ids").executeUpdate();

			con.commit();
		}
	}

	public void deleteNameMapping() {
		Sql2o sql2o = configuration.getSql2oHsqldb();

		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery("delete from channel_name_mapping").executeUpdate();
			con.commit();
		}
	}

	public String createEpgdMapping(String vdrUuid) {
		Map<String, List<Object>> mapping = readMapping(vdrUuid);

		final Map<Integer, String> result = new HashMap<>();

		final Map<String, String> provMappingId = new HashMap<>();
		Provider[] provider = readProvider();

		for (int i = 0; i < 2; ++i) {
			switch (provider[i]) {
			case EPGDATA:
				provMappingId.put("epgdata", ":" + (i+1));
				break;

			case TVM:
				provMappingId.put("tvm", ":" + (i+1));
				break;

			case TVSP:
				provMappingId.put("tvsp", ":" + (i+1));
				break;
			}
		}

		String header = new StringBuilder().append("//\n").append("// channelmap.conf for the epg2vdr daemon\n")
				.append("// created by vdr jonglisto (https://github.com/Zabrimus/vdr-jonglisto)\n").append("//\n\n")
				.toString();

		mapping.values().stream().forEach(m -> {
			StringBuilder builder = new StringBuilder();

			// ----------
			// add header
			// ----------

			// get all channels
			List<ChannelModel> channels = m.stream().filter(c -> c instanceof ChannelModel).map(c -> (ChannelModel) c)
					.collect(Collectors.toList());

			// add VDR header
			builder.append("// vdr:     ")
					.append(channels.stream().map(d -> d.getName()).collect(Collectors.joining(", "))).append("\n");

			// add header for the several EPG providers
			for (int i = 1; i <= 3; ++i) {
				AtomicInteger ai = new AtomicInteger(i);
				String str = m.stream().filter(c -> c instanceof IdModel)//
						.map(c -> (IdModel) c) //
						.filter(d -> d.getProviderId() == ai.get()) //
						.map(id -> id.getName()).collect(Collectors.joining(", "));

				switch (i) {
				case 1:
					builder.append("// tvm:     ").append(str).append("\n");
					break;

				case 2:
					builder.append("// tvsp:    ").append(str).append("\n");
					break;

				case 3:
					builder.append("// epgdata: ").append(str).append("\n");
					break;
				}
			}

			final String channelIds = channels.stream().map(s -> s.getChannelId()).collect(Collectors.joining(", "));

			// ----------------
			// add VDR channels
			// ----------------
			builder.append("vdr:000:0:0").append("  = ").append(channelIds).append("\n");

			// -------------------------
			// add EPG provider channels
			// -------------------------
			m.stream().filter(c -> c instanceof IdModel).map(c -> (IdModel) c).forEach(s -> {
				switch (s.getProviderId()) {
				case 1:
					builder.append("tvm:").append(s.getId()).append(provMappingId.get("tvm")).append("  = ").append(channelIds)
							.append("\n");
					break;

				case 2:
					builder.append("tvsp:").append(s.getId()).append(provMappingId.get("tvsp")).append("  = ").append(channelIds)
							.append("\n");
					break;

				case 3:
					builder.append("epgdata:").append(s.getId()).append(provMappingId.get("epgdata")).append("  = ").append(channelIds)
							.append("\n");
					break;
				}
			});

			builder.append("\n");

			int minChannelNumber = channels.stream().map(n -> n.getNumber()).min(Integer::compare).get();
			result.put(minChannelNumber, builder.toString());
		});

		return header + result.keySet().stream().sorted().map(s -> result.get(s)).collect(Collectors.joining());
	}

	private List<String> doGet(String getFrom, boolean isZipped) {
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

	private List<IdModel> readJsonStr(String json) throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		final JsonNode response = mapper.readTree(json);
		final CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(List.class,
				IdModel.class);
		List<Object> object = mapper.readerFor(collectionType).readValues(response.traverse()).readAll();

		@SuppressWarnings("unchecked")
		List<IdModel> result = (List<IdModel>) object.get(0);

		return result;
	}

	private ChannelModel findChannelByName(String name, List<ChannelModel> channels) {
		try {
			return channels.stream().filter(s -> s.getName().equals(name)).findFirst().get();
		} catch (Exception e) {
			return null;
		}
	}
}

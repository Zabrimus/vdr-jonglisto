package vdr.jonglisto.lib.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import vdr.jonglisto.lib.SearchTimerService;
import vdr.jonglisto.lib.model.SearchTimer;

public class SearchTimerServiceImpl extends ServiceBase implements SearchTimerService {

	private Logger log = LoggerFactory.getLogger(SearchTimerServiceImpl.class);
	
	public List<SearchTimer> getSearchTimers() {
		Sql2o sql2o = configuration.getSql2oEpg2vdr();

		try (Connection con = sql2o.open()) {
			List<SearchTimer> result = new ArrayList<>();
			
			List<Map<String, Object>> dbResult = con.createQuery("SELECT * FROM searchtimers") //
					.executeAndFetchTable().asList();
			
			dbResult.stream().forEach(s -> {
				// Make a copy of the map, because sql2o implementation is immutable and throws a NullPointerException, if the key is not found
				Map<String, Object> newResult = new HashMap<>();
				s.keySet().stream().forEach(resultKey -> newResult.put(resultKey, s.get(resultKey)));
				
				result.add(new SearchTimer(newResult));
			});
						
			return result;
		}
	}

	public SearchTimer getSearchTimer(Long id) {
		Sql2o sql2o = configuration.getSql2oEpg2vdr();

		try (Connection con = sql2o.open()) {
			Map<String, Object> dbResult = con.createQuery("SELECT * FROM searchtimers where id = :id") //
					.addParameter("id", id)  //
					.executeAndFetchTable().asList().get(0);
			
			// Make a copy of the map, because sql2o implementation is immutable and throws a NullPointerException, if the key is not found
			Map<String, Object> newResult = new HashMap<>();
			dbResult.keySet().stream().forEach(resultKey -> newResult.put(resultKey, dbResult.get(resultKey)));
			
			return new SearchTimer(newResult);
		}
	}
	
	public void insertSearchTimer(SearchTimer timer) {
		Sql2o sql2o = configuration.getSql2oEpg2vdr();

		try (Connection con = sql2o.beginTransaction()) {
			String sql = "INSERT INTO epg2vdr.searchtimers " + //
				 "(inssp, channelids, chexclude, chformat, name, expression, expression1, searchmode, searchfields, searchfields1, casesensitiv, repeatfields, " + //"
				 "episodename, season, seasonpart, category, genre, year, tipp, noepgmatch, type, state, namingmode, active, source, hits, vdruuid, weekdays, " + //
				 "nextdays, starttime, endtime, directory, priority, lifetime, vps, childlock) " + //
				 "VALUES(UNIX_TIMESTAMP(), :channelids, :chexclude, :chformat, :name, :expression, :expression1, :searchmode, :searchfields, :searchfields1, :casesensitiv, :repeatfields, " + //
				 ":episodename, :season, :seasonpart, :category, :genre, :year, :tipp, :noepgmatch, :type, :state, :namingmode, :active, :source, :hits, :vdruuid, :weekdays, " + //
				 ":nextdays, :starttime, :endtime, :directory, :priority, :lifetime, :vps, :childlock)";

			Query query = con.createQuery(sql);			
			query.getParamNameToIdxMap().keySet().stream().forEach(key -> query.addParameter(key, timer.getRawDbData().get(key)));
			query.executeUpdate();
			
			con.commit();
		}
	}
	
	public void updateSearchTimer(SearchTimer timer) {
		Sql2o sql2o = configuration.getSql2oEpg2vdr();

		try (Connection con = sql2o.beginTransaction()) {
			String sql = "update searchtimers " + //
					"set updsp = UNIX_TIMESTAMP(), channelids = :channelids, chexclude = :chexclude, chformat = :chformat, " + //
					"name = :name, expression = :expression, expression1 = :expression1, searchmode = :searchmode , searchfields = :searchfields, " + //
					"searchfields1 = :searchfields1, casesensitiv = :casesensitiv, repeatfields = :repeatfields, episodename = :episodename, " + //
					"season = :season, seasonpart = :seasonpart, category = :category, genre = :genre, year = :year, tipp = :tipp, " + //
					"noepgmatch = :noepgmatch, type = :type, namingmode = :namingmode, active = :active, source = :source, vdruuid = :vdruuid, " + //
					"weekdays = :weekdays, nextdays = :nextdays, starttime = :starttime, endtime = :endtime, directory = :directory, " + //
					"priority = :priority, lifetime = :lifetime, vps = :vps, childlock = :childlock " + //
					"where id = :id";
			
			Query query = con.createQuery(sql);			
			query.getParamNameToIdxMap().keySet().stream().forEach(key -> query.addParameter(key, timer.getRawDbData().get(key)));
			query.executeUpdate();
			
			con.commit();
		}
	}

	public void deleteSearchTimer(Long id) {
		Sql2o sql2o = configuration.getSql2oEpg2vdr();
		
		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery("update searchtimers set state = 'D' where id = :ID")
			.addParameter("ID", id)
			.executeUpdate();
			
			con.commit();
		}
	}

	public void toggleActive(Long id) {
		Sql2o sql2o = configuration.getSql2oEpg2vdr();
		
		try (Connection con = sql2o.beginTransaction()) {
			con.createQuery("update searchtimers set active = not active where id = :ID")
			.addParameter("ID", id)
			.executeUpdate();
			
			con.commit();
		}
	}	
	
	public List<Map<String, Object>> performSearch(SearchTimer timer) {
		Sql2o sql2o = configuration.getSql2oEpg2vdr();

		try (Connection con = sql2o.open()) {
			String queryString = createSelectStatement(timer);

			Query query = con.createQuery(queryString);
			
			// fill all parameters
			query.getParamNameToIdxMap().keySet().stream().forEach(key -> {
				if ("EXPRESSION".equals(key)) {
					query.addParameter("EXPRESSION", createLike(timer.getExpression(), timer.getSearchmode()));
				} else if ("EXPRESSION1".equals(key)) {
					query.addParameter("EXPRESSION1", createLike(timer.getExpression1(), timer.getSearchmode()));
				} else if ("EPISODENAME".equals(key)) {
					query.addParameter("EPISODENAME", timer.getEpisodename());
				} else if ("FORMAT1".equals(key)) {
					// add all FORMATx parameters
					AtomicInteger ai = new AtomicInteger(1);
					Arrays.stream(timer.getChformat().split(",")).forEach(ch -> query.addParameter("FORMAT" + ai.getAndIncrement(), StringUtils.strip(ch, "'")));
				} else if ("CATEGORY1".equals(key)) {
					// add all CATEGORYx parameters					
					AtomicInteger ai = new AtomicInteger(1);
					timer.getCategory().stream().forEach(ch -> query.addParameter("CATEGORY" + ai.getAndIncrement(), StringUtils.strip(ch, "'")));
				} else if ("GENRE1".equals(key)) {
					// add all GENREx parameters
					AtomicInteger ai = new AtomicInteger(1);
					timer.getGenre().stream().forEach(ch -> query.addParameter("GENRE" + ai.getAndIncrement(), StringUtils.strip(ch, "'")));
				} else if ("TIPP1".equals(key)) {
					/// add aöö TIPPx parameters
					AtomicInteger ai = new AtomicInteger(1);
					Arrays.stream(timer.getTipp().split(",")).forEach(ch -> query.addParameter("TIPP" + ai.getAndIncrement(), StringUtils.strip(ch, "'")));
				} else if ("STARTTIME".equals(key)) {
					query.addParameter("STARTTIME", timer.getStarttime());
				} else if ("ENDTIME".equals(key)) {
					query.addParameter("ENDTIME", timer.getEndtime());
				} else if ("WEEKDAYS".equals(key)) {
					query.addParameter("WEEKDAYS", timer.getWeekdays());
				} else if ("NEXTDAYS".equals(key)) {
					query.addParameter("NEXTDAYS", timer.getNextDays());
				}
			});
			
			return query.executeAndFetchTable().asList();
		}
	}
	
	private String createSelectStatement(SearchTimer timer) {
		String searchOp = "=";
				
		String expression = timer.getExpression();
		String expression1 = timer.getExpression1();
	    String episodename = timer.getEpisodename();
	    String season = timer.getSeason();
	    String seasonpart = timer.getSeasonpart();
	    List<String> category = timer.getCategory();
	    List<String> genre = timer.getGenre();
	    String tipp = timer.getTipp();
	    String year = timer.getYear();
	    String chformat = timer.getChformat();
	    
	    String channels = timer.getChannels();	    
	    Boolean channelExclude = timer.getChannelExclude();
	    Integer starttime = timer.getRawStartTime();
	    Integer endtime = timer.getRawEndTime();
	    Integer nextDays = timer.getNextDays();
		       
		Boolean noepgmatch = timer.getNoepgmatch();
		Long searchmode = timer.getSearchmode();
		Long searchfields = timer.getRawSearchFields() != null ? timer.getRawSearchFields().longValue() : null;
		Long searchfields1 = timer.getRawSearchFields1() != null ? timer.getRawSearchFields1().longValue() : null;
		Boolean casesensitiv = timer.getCasesensitiv();
		Integer weekdays = timer.getWeekdays(); 
		
		StringBuilder sb = new StringBuilder();
		
		switch (searchmode.intValue()) {
			// smExact
			case 1: searchOp = "="; break;
			
			// smRegexp
			case 2: searchOp = "regexp"; break;
			
			// smLike
			case 3: searchOp = "like"; break;

			// smContained
			case 4: searchOp = "like"; break;
		}

		if (casesensitiv) {
			searchOp = searchOp + " BINARY";
		}
	
		sb.append("select c.channelname, e.cnt_useid " + //
		           "from eventsviewplain e, (select distinct channelid, channelname, format, ord, visible from channelmap) c " + //
				   "where " + //
		           "e.CNT_CHANNELID = c.CHANNELID and e.updflg in ('A','L','P') " + //
				   "and e.cnt_starttime >= unix_timestamp() - 120 ");
		
		// channels
		if (StringUtils.isNotEmpty(channels)) {
			sb.append(" and ").append(channelExclude ? "not" : "").append("(");
			sb.append("e.CNT_CHANNELID in (").append(Arrays.stream(channels.split(",")).map(s -> String.format("'%s'", s)).collect(Collectors.joining(","))).append("))");
		}

		// starttime
		if (starttime != null) {
			sb.append(" and CAST(DATE_FORMAT(FROM_UNIXTIME( e.cnt_starttime), '%H%i') AS UNSIGNED) >= :STARTTIME ");
		}
		
		if (endtime != null) {
			sb.append(" and CAST(DATE_FORMAT(FROM_UNIXTIME( e.cnt_starttime), '%H%i') AS UNSIGNED) <= :ENDTIME ");
		}
		
		if (nextDays != null) {
			sb.append(" and e.cnt_starttime <= UNIX_TIMESTAMP(CURRENT_TIMESTAMP) + 86400 * :NEXTDAYS ");
		}
		
		// expression
		if (StringUtils.isNotEmpty(expression) && !"%".equals(expression) && !"%%".equals(expression) && searchfields > 0) {
			sb.append(" and (");
			
			if ((searchfields & 1) > 0) {
				sb.append("(e.SUB_TITLE " + searchOp + " :EXPRESSION)").append(" or ");
			}

			if ((searchfields & 2) > 0) {
				sb.append("(e.SUB_SHORTTEXT " + searchOp + " :EXPRESSION)").append(" or ");
			}

			if ((searchfields & 4) > 0) {
				sb.append("(e.SUB_COMPLONGDESCRIPTION " + searchOp + " :EXPRESSION)").append(" or ");
			}
			
			sb.append("false)");
		}		
	
		// expression1
		if (StringUtils.isNotEmpty(expression1) && !"%".equals(expression1) && !"%%".equals(expression1) && searchfields1 > 0) {
			sb.append(" and (");
			
			if ((searchfields1 & 1) > 0) {
				sb.append("(e.SUB_TITLE " + searchOp + " :EXPRESSION1)").append(" or ");
			}

			if ((searchfields1 & 2) > 0) {
				sb.append("(e.SUB_SHORTTEXT " + searchOp + " :EXPRESSION1)").append(" or ");
			}

			if ((searchfields1 & 4) > 0) {
				sb.append("(e.SUB_COMPLONGDESCRIPTION " + searchOp + " :EXPRESSION1)").append(" or ");
			}
			
			sb.append("false)");
		}		
		
		// Channel Format (CHFORMAT)
		if (StringUtils.isNotEmpty(chformat)) {
			AtomicInteger ai = new AtomicInteger(1);
			
			sb.append(" and (");		
			sb.append(Arrays.stream(chformat.split(",")).map(s -> "(c.FORMAT = :FORMAT" + ai.getAndIncrement() + ")").collect(Collectors.joining(" or ")));		
			sb.append(")");
		}
		
		// Kategorie 'Spielfilm','Serie' (CATEGORY)
		if (category.size() > 0) {
			AtomicInteger ai = new AtomicInteger(1);
			
			sb.append(" and (");

			if (noepgmatch) {
				sb.append("e.SUB_CATEGORY is null or ");
			}

			sb.append(category.stream().map(s -> "(e.SUB_CATEGORY = :CATEGORY" + ai.getAndIncrement() + ")").collect(Collectors.joining(" or ")));
			sb.append(")");
		}

		// Genre 'Krimi','Action' (GENRE)
		if (genre.size() > 0) {
			AtomicInteger ai = new AtomicInteger(1);
			
			sb.append(" and (");
			
			if (noepgmatch) {
				sb.append("e.SUB_GENRE is null or ");
			}
			
			sb.append(genre.stream().map(s -> "(e.SUB_GENRE = :GENRE" + ai.getAndIncrement() + ")").collect(Collectors.joining(" or ")));
			sb.append(")");
		}

		// Tipp (TIPP)
		if (StringUtils.isNotEmpty(tipp)) {
			AtomicInteger ai = new AtomicInteger(1);
			
			sb.append(" and (");
			
			if (noepgmatch) {
				sb.append("e.SUB_TIPP is null or ");
			}
			
			sb.append(Arrays.stream(tipp.split(",")).map(s -> "(e.SUB_TIPP = :TIPP" + ai.getAndIncrement() + ")").collect(Collectors.joining(" or ")));
			sb.append(")");
		}
		
		// Serien Titel (EPISODENAME)
		if (StringUtils.isNotEmpty(episodename)) {
			sb.append(" and (e.EPI_EPISODENAME = :EPISODENAME or (e.EPI_EPISODENAME is null and e.SUB_TITLE = :EPISODENAME))");
		}
		
		// Staffel like 3-5 (SEASON)
		if (StringUtils.isNotEmpty(season)) {
			sb.append(" and (");
			
			if (noepgmatch) {
				sb.append("e.EPI_SEASON is null or ");
			}

			sb.append(String.format("e.EPI_SEASON between %d and %d", rangeFrom(season), rangeTo(season))).append(")");
		}
		
		// Staffelfolge (SEASONPART)
		if (StringUtils.isNotEmpty(seasonpart)) {
			sb.append(" and (");
			
			if (noepgmatch) {
				sb.append("e.EPI_PART is null or ");
			}

			sb.append(String.format("e.EPI_PART between %d and %d", rangeFrom(seasonpart), rangeTo(seasonpart))).append(")");
		}
		
		// Jahr (YEAR)
		if (StringUtils.isNotEmpty(year)) {
			sb.append(" and (");
			
			if (noepgmatch) {
				sb.append("e.SUB_YEAR is null or ");
			}

			sb.append(String.format("e.SUB_YEAR between %d and %d", rangeFrom(year), rangeTo(year))).append(")");
		}
		
		// Wochentage (WEEKDAYS)
		if (weekdays > 0) {
			sb.append(" and (:WEEKDAYS & (1 << weekday(from_unixtime(e.CNT_STARTTIME)))) <> 0");
		}
	
		sb.append(" order by e.cnt_starttime, c.ord");

		return sb.toString();
	}

	private int rangeFrom(String s) {
		try {
			return Integer.parseInt((s.split("-"))[0]);
		} catch (Exception e) {
			return Integer.MIN_VALUE;
		}
	}

	private int rangeTo(String s) {
		try {
			return Integer.parseInt((s.split("-"))[1]);
		} catch (Exception e) {
			return Integer.MAX_VALUE;
		}
	}

	private String createLike(String param, Long searchMode) {
		StringBuffer sb = new StringBuffer();
		
		if (searchMode == 4 || searchMode == 3) {
			return sb.append("%").append(param).append("%").toString();
		} else {
			return param;
		}
	}
}

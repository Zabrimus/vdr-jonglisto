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

	public void updateSearchTimer(SearchTimer timer) {
		// TODO Auto-generated method stub
	}

	public void deleteSearchTimer(SearchTimer timer) {
		// TODO Auto-generated method stub
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
					query.addParameter("EXPRESSION", createLike(timer.getString("expression"), timer.getLong("searchmode")));
				} else if ("EXPRESSION1".equals(key)) {
					query.addParameter("EXPRESSION1", createLike(timer.getString("expression1"), timer.getLong("searchmode")));
				} else if ("EPISODENAME".equals(key)) {
					query.addParameter("EPISODENAME", timer.getString("episodename"));
				} else if ("FORMAT1".equals(key)) {
					// add all FORMATx parameters
					AtomicInteger ai = new AtomicInteger(1);
					Arrays.stream(timer.getString("chformat").split(",")).forEach(ch -> query.addParameter("FORMAT" + ai.getAndIncrement(), StringUtils.strip(ch, "'")));
				} else if ("CATEGORY1".equals(key)) {
					// add all CATEGORYx parameters
					AtomicInteger ai = new AtomicInteger(1);
					Arrays.stream(timer.getString("category").split(",")).forEach(ch -> query.addParameter("CATEGORY" + ai.getAndIncrement(), StringUtils.strip(ch, "'")));
				} else if ("GENRE1".equals(key)) {
					// add all GENREx parameters
					AtomicInteger ai = new AtomicInteger(1);
					Arrays.stream(timer.getString("genre").split(",")).forEach(ch -> query.addParameter("GENRE" + ai.getAndIncrement(), StringUtils.strip(ch, "'")));
				} else if ("TIPP1".equals(key)) {
					/// add aöö TIPPx parameters
					AtomicInteger ai = new AtomicInteger(1);
					Arrays.stream(timer.getString("tipp").split(",")).forEach(ch -> query.addParameter("TIPP" + ai.getAndIncrement(), StringUtils.strip(ch, "'")));
				} else if ("STARTTIME".equals(key)) {
					query.addParameter("STARTTIME", timer.getInteger("starttime"));
				} else if ("ENDTIME".equals(key)) {
					query.addParameter("ENDTIME", timer.getInteger("endtime"));
				} else if ("WEEKDAYS".equals(key)) {
					query.addParameter("WEEKDAYS", timer.getInteger("weekdays"));
				} else if ("NEXTDAYS".equals(key)) {
					query.addParameter("NEXTDAYS", timer.getInteger("nextdays"));
				}
			});
			
			return query.executeAndFetchTable().asList();
		}
	}
	
	private String createSelectStatement(SearchTimer timer) {
		String searchOp = "=";
				
		String expression = timer.getString("expression");
		String expression1 = timer.getString("expression1");
	    String episodename = timer.getString("episodename");
	    String season = timer.getString("season");
	    String seasonpart = timer.getString("seasonpart");
	    String category = timer.getString("category");
	    String genre = timer.getString("genre");
	    String tipp = timer.getString("tipp");
	    String year = timer.getString("year");
	    String chformat = timer.getString("chformat");
	    
	    String channels = timer.getString("channelids");	    
	    Boolean channelExclude = !(0L == timer.getLong("chexclude"));
	    Integer starttime = timer.getInteger("starttime");
	    Integer endtime = timer.getInteger("endtime");
	    Integer nextDays = timer.getInteger("nextdays");
		       
		Boolean noepgmatch = !(0 == timer.getInteger("noepgmatch"));
		Long searchmode = timer.getLong("searchmode");
		Integer searchfields = timer.getLong("searchfields") != null ? timer.getLong("searchfields").intValue() : null;
		Integer searchfields1 = timer.getLong("searchfields1") != null ? timer.getLong("searchfields1").intValue() : null;
		Boolean casesensitiv = !(0L == timer.getLong("casesensitiv"));
		Integer weekdays = timer.getInteger("weekdays"); 
		
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
		if (StringUtils.isNotEmpty(category)) {
			AtomicInteger ai = new AtomicInteger(1);
			
			sb.append(" and (");

			if (noepgmatch) {
				sb.append("e.SUB_CATEGORY is null or ");
			}

			sb.append(Arrays.stream(chformat.split(",")).map(s -> "(e.SUB_CATEGORY = :CATEGORY" + ai.getAndIncrement() + ")").collect(Collectors.joining(" or ")));		
			sb.append(")");
		}

		// Genre 'Krimi','Action' (GENRE)
		if (StringUtils.isNotEmpty(genre)) {
			AtomicInteger ai = new AtomicInteger(1);
			
			sb.append(" and (");
			
			if (noepgmatch) {
				sb.append("e.SUB_GENRE is null or ");
			}
			
			sb.append(Arrays.stream(category.split(",")).map(s -> "(e.SUB_GENRE = :GENRE" + ai.getAndIncrement() + ")").collect(Collectors.joining(" or ")));
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

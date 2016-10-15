CREATE FUNCTION `create_timer_filename`(useid INT unsigned, naming_mode INT)
	RETURNS VARCHAR(256)
	
BEGIN
	DECLARE dateStr   	VARCHAR(64);
	DECLARE title	  	VARCHAR(200);
	DECLARE shorttext 	VARCHAR(300);
	DECLARE category  	VARCHAR(50);
	DECLARE epname    	VARCHAR(100);
	DECLARE shortname 	VARCHAR(100);
	DECLARE season	  	INT(11);
	DECLARE part	  	INT(11);
	DECLARE num		  	INT(11);
	DECLARE col1      	VARCHAR(250);
	DECLARE col2      	VARCHAR(250);

	DECLARE l_title   	VARCHAR(100);
	DECLARE l_part    	VARCHAR(2);
	DECLARE l_number  	VARCHAR(3);
	DECLARE l_ermittler VARCHAR(250);
	DECLARE l_season    VARCHAR(4);
	DECLARE l_ort		VARCHAR(250);
	
	SELECT DATE_FORMAT(FROM_UNIXTIME( cnt_starttime ), '%W %d.%m.%Y %H:%i'), 
		  sub_title, sub_shorttext, sub_category, 
		  epi_episodename, epi_shortname, epi_season, epi_part, epi_number, epi_extracol1, epi_extracol2
	INTO  dateStr, title, shorttext, category, epname, shortname, season, part, num, col1, col2
	FROM  eventsviewplain 
    WHERE cnt_useid = useid;


    IF naming_mode = 1 THEN
		IF TRIM(COALESCE(epname, '')) <> '' THEN
			SET l_title = COALESCE(shortname, epname);
			SET l_part = LPAD(part, 2, 0);
			SET l_number = LPAD(num, 3, 0);
		
			IF title = 'Tatort' THEN
				SET l_ermittler = TRIM(substr(col1, 11));
				SET l_ort = TRIM(substr(col2, 5));
				SET l_season = season + 1969;
					
				RETURN concat('Tatort~', l_ort, '~', l_ermittler, '~', l_season, 'x', l_part, ' - ', l_number, '. ', shorttext);
			ELSE
				IF title = 'Polizeiruf 110' THEN
					SET l_season = season + 1971;
				ELSE
					SET l_season = LPAD(season, 2, 0);
				END IF;

                RETURN concat(l_title, '~', l_season, 'x', l_part, ' - ', l_number, '. ', shorttext);
			END IF;
			
		ELSEIF COALESCE(category, '') = 'Serie' THEN
			IF (TRIM(COALESCE(shorttext, '')) <> '') AND (shorttext <> title) THEN
				RETURN concat(title, '~', shorttext);
			ELSE
				RETURN concat(title, '~', dateStr);
			END IF;
			
		ELSE
			RETURN title;
		END IF;

	ELSEIF naming_mode = 2 THEN
  		IF TRIM(COALESCE(epname, '')) <> ''  THEN
			SET l_title = COALESCE(shortname, epname);
			SET l_part = LPAD(part, 2, 0);
			SET l_number = LPAD(num, 3, 0);
			
			IF title = 'Tatort' THEN
				SET l_ermittler = substr(col1, 11);
				SET l_ort = substr(col2, 5);
				SET l_season = season + 1969;
				
				RETURN concat('Tatort~', l_ort, '~', l_ermittler, '~', l_season, 'x', l_part, ' - ', l_number, '. ', shorttext);
			ELSE
				IF title = 'Polizeiruf 110' THEN
					SET l_season = season + 1971;
				ELSE
					SET l_season = LPAD(season, 2, 0);
				END IF;

                RETURN concat(l_title, '~', l_season, 'x', l_part, ' - ', l_number, '. ', shorttext);
			END IF;
			
		ELSEIF TRIM(COALESCE(shorttext, '')) <> '' AND shorttext <> title THEN
			RETURN concat(title, '~?x? - ?. ', shorttext);
		ELSE
			RETURN concat(title, '~', dateStr);
		END IF;
      		
	ELSEIF naming_mode = 3 THEN			
		IF TRIM(COALESCE(shorttext, '')) <> '' AND shorttext <> title THEN
			RETURN concat(title, '~', shorttext);
		ELSE
			RETURN title;
		END IF;

	ELSEIF naming_mode = 4 THEN			
  		IF TRIM(COALESCE(category, '')) <> '' THEN
			RETURN concat(category, '~', title);
  		ELSE
  			RETURN title;
  		END IF;
	ELSE
        BEGIN
	        IF TRIM(COALESCE(shorttext, '')) <> '' THEN
	        	RETURN concat(title, '~', shorttext);
			ELSE
				RETURN concat(title, '~', dateStr);
			END IF;
        END;
    END IF;
END;

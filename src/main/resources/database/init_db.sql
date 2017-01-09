-- Tables ---------------------------------------------------------------------------------

create table if not exists 
     configuration (name   varchar(256),
                    val    varchar(1024),
                    primary key(name)
                   );;

create table if not exists 
	recording ( id Integer,
                vdr_uuid varchar(200),
				number Integer,
				name Varchar(255),
				file_name Varchar(2048),
				relative_file_name Varchar(2048),
				duration Integer,
				frames_per_second Integer,
				edited Varchar(1),
				filesize Integer,
				channel_id Varchar(255),
				event_title Varchar(255),
				event_short_text Varchar(1024),
				event_description Varchar(4096),
				event_start_time Integer,
				event_duration Integer,
				hash Varchar(64),
				aux Varchar(1024),
				primary key(id)
			 );;

				       
create table if not exists
	recording_mark (id Integer,
					ref_recording_id Integer,
					mark Varchar(12),
					primary key(id),
					foreign key (ref_recording_id) references recording(id) on delete cascade
				   );;
						
				   
create table if not exists
    epg_provider (id Integer,
                  name Varchar(20),
                  url Varchar(500),
                  enabled BOOLEAN,
                  primary key(id)
                  );;
                  
create table if not exists
    epg_ids (ref_provider_id Integer,
             epgid Varchar(20),
             name  Varchar(200),
             foreign key (ref_provider_id) references epg_provider(id)
            );;

create table if not exists
    channel_include ( channel_name Varchar(200)
                    );;
            
create table if not exists
    channel_name_mapping (channel_name_a Varchar(200),
                     	  channel_name_b Varchar(200)
                         );;
                         
create table if not exists
    channel_map (channel_name Varchar(200),
    			 mapping Varchar(2000)
                 );;
                 
create table if not exists
    channel_conf_group (id Integer,
    			 		groupname Varchar(200)
                 	   );;
                 
create table if not exists
    channel_conf_channel (id Integer,
    			 		 name Varchar(200),
    			 		 group_id Integer
                 	     );;
                 	     
create table if not exists 
	users (	
			id Integer, 
			username varchar(20), 
			password varchar(50), 
			salt varchar(128), 
			primary key(id), 
			constraint c_username unique(username)
		  );;                 	     
          
create table if not exists 
	permissions 
		(	
			id Integer, 
		   	permission varchar(100), 
		   	message_key varchar(50), 
			primary key(id), 
			constraint c_permission unique(permission)			
		);;                 	     

create table if not exists 
	user_permissions 
		(	
			id Integer,
			ref_user_id Integer,
		   	ref_permission_id Integer,
		   	permission_add varchar(50),
			primary key(id), 
			constraint c_user_permission unique(ref_user_id, ref_permission_id),
			foreign key (ref_user_id) references users(id) on delete cascade,
			foreign key (ref_permission_id) references permissions(id) on delete cascade,
		);;                 	     
		
create table if not exists 
	roles 
		(	
			id Integer,
			role varchar(50),
			primary key(id), 
			constraint c_role unique(role)
		);;                 	     

create table if not exists 
	roles_permissions 
		(	
			id Integer,
			ref_role_id Integer,
			ref_permission_id Integer,
			primary key(id), 
			foreign key (ref_role_id) references roles(id) on delete cascade,
			foreign key (ref_permission_id) references permissions(id) on delete cascade,
		);;                 	     
		
create table if not exists 
	user_roles 
		(	
			id Integer,
			ref_role_id Integer,
			ref_user_id Integer,
			primary key(id), 
			constraint c_user_role unique(ref_user_id, ref_role_id),
			foreign key (ref_user_id) references users(id) on delete cascade,
			foreign key (ref_role_id) references roles(id) on delete cascade,
		);;                 	     
		
create table if not exists
    epg
       (
       	  useid Integer,
		  epgid BigInt,
	      title Varchar(200), 
	 	  short_text Varchar(400),
	 	  description Varchar(35000),
		  start_time Integer,
		  channel Varchar(50),
		  channel_name Varchar(100),
		  duration Integer,
		  images Integer,
		  timer_exists boolean,
		  timer_active boolean,
		  timer_id Varchar(100),
	 	  parental_rating Integer,
	 	  genre Varchar(100),
	 	  category Varchar(100),
	 	  country Varchar(50),
	 	  year Varchar(10),
	 	  actors Varchar(1000),
	 	  flags Varchar(100),
	 	  source Varchar(20),	 	  
	 	  producer Varchar(500),
	 	  camera Varchar(200),
		  director Varchar(200),
		  season Varchar(10),
		  part Varchar(10),
		  parts Varchar(10),
		  epi_number Varchar(10),
		  episode Varchar(500),
		  shortname Varchar(200),
		  screenplay Varchar(500)		  
       );;

-- Sequences --------------------------------------------------------------------------------- 
					  
create sequence if not exists seq_recording as Integer start with 1 increment by 1;;
create sequence if not exists seq_recording_mark as Integer start with 1 increment by 1;;
create sequence if not exists seq_recording_sync as Integer start with 1 increment by 1;;
create sequence if not exists seq_users as Integer start with 1 increment by 1;;
create sequence if not exists seq_permissions as Integer start with 1 increment by 1;;
create sequence if not exists seq_user_permissions as Integer start with 1 increment by 1;;
create sequence if not exists seq_roles as Integer start with 1 increment by 1;;
create sequence if not exists seq_user_roles as Integer start with 1 increment by 1;;
create sequence if not exists seq_roles_permissions as Integer start with 1 increment by 1;;
create sequence if not exists seq_epg as Integer start with 1 increment by 1;;


-- Procedure / Functions --------------------------------------------------------------------- 


-- roles and permissions

merge into permissions as c 
   using(values('**', 'admin_permission')) 
   as vals(permission, message_key) on c.permission = vals.permission
when not matched then insert values (next value for seq_permissions), vals.permission, vals.message_key;;


-- First data --------------------------------------------------------------------------------

merge into configuration as c using(values('version', '1')) as vals(name, val) on c.name = vals.name
when not matched then insert values vals.name, vals.val;;

merge into epg_provider as c 
   using(values(0, 'vdr', '', true)) 
   as vals(id, name, url, enabled) on c.name = vals.name
when not matched then insert values vals.id, vals.name, vals.url, vals.enabled
when matched then update set  url = vals.url, enabled = vals.enabled;;

merge into epg_provider as c 
   using(values(1, 'tvm', 'eJwVx0sKwCAMBcAb+Uqhm94m+KkBNYKP2OOXzm4qOW9g7y2B3s01h5SxKNQI+lNtEZW9wUbTkZNQEAv9Og/80VEs8OUHHCgb8Q==', true)) 
   as vals(id, name, url, enabled) on c.name = vals.name
when not matched then insert values vals.id, vals.name, vals.url, vals.enabled
when matched then update set  url = vals.url, enabled = vals.enabled;;

merge into epg_provider as c 
   using(values(2, 'tvsp', 'eJwdykEKgDAMBMAX2dz9TYkrDayxkCXvF5zzLGnXacZoDHXtAO/gMy5YaSrc/E0hZb5mJngwSv9Xf+npF+s=', true)) 
   as vals(id, name, url, enabled) on c.name = vals.name
when not matched then insert values vals.id, vals.name, vals.url, vals.enabled
when matched then update set url = vals.url, enabled = vals.enabled;;

merge into epg_provider as c 
   using(values(3, 'epgdata', 'eJzLKCkpsNLXLy8v10stSE9JLEnUS87P1c/MS0mt0CvIKLBPTC7JzM+zLU7NS/HMS84pTUlVy/R39bUtSylSK8jMs1VWDvD0U1ZWA2kNqSxIta3IzQEANyUeWg==', false)) 
   as vals(id, name, url, enabled) on c.name = vals.name
when not matched then insert values vals.id, vals.name, vals.url, enabled
when matched then update set url = vals.url, enabled = vals.enabled;;



-- channel name mapping
merge into CHANNEL_NAME_MAPPING as c 
   using(values('NDR', 'ndrfshh')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('NDR HH', 'ndrfshh')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('sportdigital.tv', 'sportdigital')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('BBC Entertainment', 'bbc')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('H3', 'hrfernsehen')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('EURONEWS', 'euronewsd')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('euronews', 'euronewsd')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('WDR', 'wdrdusseldorf')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('AutoMotorSportChannel', 'automotorundsportchannel')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Auto Motor Sport', 'automotorundsportchannel')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Handshake 2 Deutschland', 'h2dhandshake2deutschland')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('RTL Television', 'rtl')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Glitz*', 'tntglitz')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('SWR', 'swrfernsehenbw')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('SWR BW', 'swrfernsehenbw')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('SKY Action', 'skycinemaaction')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Sky Action', 'skycinemaaction')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('National Geographic Wild', 'natgeowild')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('NDR NI', 'ndrfsnds')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('EUROSPORT', 'eurosport1')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('BloombergTV', 'bloomberg')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('BR Fernsehen Süd', 'brfernsehennord')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('B3', 'brfernsehennord')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('BR', 'brfernsehennord')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('NDR MV', 'ndrfsmv')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('SWR RP', 'swrfernsehenrp')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Fox Serie', 'fox')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Fox Serie (S)', 'fox')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('FOX Channel', 'fox')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Kabel', 'kabeleins')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('QVC Beauty & Style', 'qvcbeauty')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Disney SD', 'disney')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Disney Channel', 'disney')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Comedy Central', 'vivacomedycentral')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('VIVA', 'vivacomedycentral')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('SKY Comedy', 'skycinemacomedy')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Sky Comedy', 'skycinemacomedy')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('E! Entertainment Television', 'eentertainment')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('AXN Action', 'axn')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Silverline', 'silverlinemoviechannel')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('SKY Cinema Hits', 'skyhits')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('S RTL', 'superrtl')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('NDR SH', 'ndrfssh')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Nickelodeon/Nicknight', 'nickelodeon')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Nick', 'nickelodeon')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('CNN International', 'cnn')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Sony Entertainment', 'sonytv')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Sony Entertainment Television', 'sonytv')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Discovery Channel (S)', 'discovery')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Discovery Channel', 'discovery')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('MDR', 'mdrsanhalt')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('MDR Thüringen', 'mdrsanhalt')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('MDR Sachsen-Anhalt', 'mdrsanhalt')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('MDR Sachsen', 'mdrsanhalt')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('SCI FI', 'syfy')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('13th Street Universal', '13thstreet')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Home Shopping Europe', 'hse24')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('History Channel', 'history')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('ARD', 'daserste')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('SKY Emotion', 'skycinemaemotion')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Sky Emotion', 'skycinemaemotion')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Wetter Fernsehen', 'deutscheswetterfernsehen')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('DW (Europe)', 'deutscheswetterfernsehen')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('SKY Nostalgie', 'skycinemanostalgie')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Sky Nostalgie', 'skycinemanostalgie')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('National Geographic (S)', 'natgeo')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('National Geographic', 'natgeo')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('Blue Movie', 'bluemovie1')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('DELUXE MUSIC TV', 'deluxemusic')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

merge into CHANNEL_NAME_MAPPING as c 
   using(values('BR Alpha', 'ardalpha')) 
   as vals(channel_name_a, channel_name_b) on c.channel_name_a = vals.channel_name_a
when not matched then insert values vals.channel_name_a, vals.channel_name_b;;

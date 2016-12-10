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
            
-- Sequences --------------------------------------------------------------------------------- 
					  
create sequence if not exists seq_recording as Integer start with 1 increment by 1;;
create sequence if not exists seq_recording_mark as Integer start with 1 increment by 1;;
create sequence if not exists seq_recording_sync as Integer start with 1 increment by 1;;


-- Procedure / Functions --------------------------------------------------------------------- 


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



-- Tables ---------------------------------------------------------------------------------

create table configuration (name   varchar(256),
                            val    varchar(1024),
                            primary key(name)
                           );;

create table recording ( id Integer,
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

				       
create table recording_mark (id Integer,
							 ref_recording_id Integer,
							 mark Varchar(12),
							 primary key(id),
							 foreign key (ref_recording_id) references recording(id) on delete cascade
							);;
								
-- Sequences --------------------------------------------------------------------------------- 
					  
create sequence seq_recording as Integer start with 1 increment by 1;;
create sequence seq_recording_mark as Integer start with 1 increment by 1;;
create sequence seq_recording_sync as Integer start with 1 increment by 1;;


-- Procedure / Functions --------------------------------------------------------------------- 


-- First data --------------------------------------------------------------------------------

insert into configuration (name, val) values ('version', '1');;


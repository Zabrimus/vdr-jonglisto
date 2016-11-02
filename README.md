# Welcome to vdr-jonglisto. A web frontend for multiple VDR instances.

My plan is to replace vdradmin-am with this new application. Currently you can see and search EPG data, see and edit timers and see, rename and move recordings.
As a special feature used in my environment it is possible to define views of all configured VDR instances.

if you have e.g. 4 running VDR instances: 2 headless which do nothing more than recordings and handling timers) and 2 instances which are connected to a display (TV) and which do not have any DVB device installed. Then it is possible to define a view and the application use the configured VDR instance to do the desired work.

* head: points to the VDR instance which is connected to a display. Mainly used for channel switches.
* channels: The application needs to be able to read the channel list and channel groups. In my environment, all VDR channel lists are synchronized, so i define the order of VDR instances where to get the channel list and channel groups. The first reachable VDR is then used.
* timers: only the headless VDR instances have defined timers and are responsible for all timers. The head VDR do not have any timer.
* recordings: where to get the recordings and do the desired work

### Requirements
* a runnning vdr-epg-daemon, especially the mysql database
* vdr restfulapi plugin, at least version 0.2.6.5
* java 8

### Channel logos
The application now uses channel logos from <https://github.com/picons/picons-source>. To build the necessary logo.jar ju,p into directory tools and start ./build-logo-jar.sh.
This jar have to created only at the very first build or if there exists some new and interesting logos. The logo.jar has a size of only 8MB.

### Theme
The application currently use a theme from <https://bootswatch.com/cerulean/>. If you want to change the default theme, change build.less in directory tools/build-bootstrap and start build.sh in the same directory.

### Build instructions 

There exist multiple possibilities to build and start the application. 

1.  Use the docker repository <https://github.com/Zabrimus/vdr-jonglisto-docker>.

2.  Build a war file which can be deployed in a running servlet container like jetty or tomcat.
	> cd tools; ./build-logo-jar.sh
	
	> ./build.sh war
	
3.  Build a runnable (hybrid) war file which either can be deployed  (see 2.) or can be started as normal application.
	> cd tools; ./build-logo-jar.sh
	  
	> ./build.sh standaloneWar
	
	> Start with java -jar vdr-jonglisto-0.0.1.war
	
	> Default port is 8080
    
### Configuration
The main configuration file jonglisto.json must be copied to /etc/jonglisto. There exists a sample in the samples directory.

 
* **useRecordingSyncMap:** use a feature in vdr restfulapi plugin to speed up the recording list. But be aware: This needs the most recent version! It is safe to set this value to "false", otherwise your recordings could be deleted/corrupted/diced. You are warned.
```json
	"useRecordingSyncMap" : "false"
```
	
* **epg2vdr:** configuration of the mysql connector to epg2vdr database.
 ```json
	"epg2vdr": {
    	"url": "jdbc:mysql://server:3307/epg2vdr",
    	"username": "epg2vdr",
    	"password": "epg"
    }
```
  
* **hsqldb:** configuration of the internal hsqldb database used mainly for recording information. 
```json
	"hsqldb": {
		"path": "/var/cache/jonglisto-db",
		"remote": "true"
	}
```
	
* **aliases:** define a shortcut and the correspondig UUID (as in epg2vdr) for the other configuration.
```json
	"aliases" : {
		"pivdr1"   : "39F75BA3-E7AE-4DEE-876D-203FEC6F3CD2",
		"pivdr2"   : "4048B55F-E3DA-4654-AB36-6503F09FCD3F",
		"stream1"  : "68CE1D58-BF0E-446F-BCE7-4B1F21EFFEEF",
		"stream2"  : "96AB80F7-84F6-46DE-89DA-1D1D02595853"
	}
```

* **VDR:** this is array where we define all desired VDR instances.

	> **uuid:** is the value defined in "aliases"
	
	> **displayName:** Value which will be shown in the Web Application
	
	> **TIMER_AUX:** default string for timer aux information. In this example the remotetimers id is set.
	
	> **TIMER_MINUS_MINUTES:** time in minutes
	
	> **TIMER_PLUS_MINUTES:** time in minutes
	
	> **TIMER_PRIORITY:** default timer priority
		
	> **TIMER_LIFETIME:** default timer lifetime
	
	> **RECORDING_NAMING_MODE:** the same value as used in epg2vdr plugin
	
```json
  "VDR": [
    {
      "uuid": "pivdr1",
      "displayName": "Pi Vdr 1",
      "config" : {
      	  "TIMER_AUX" : "<remotetimers>1</remotetimers>",
		  "TIMER_MINUS_MINUTES" : 10,
		  "TIMER_PLUS_MINUTES" : 10,
		  "TIMER_PRIORITY" : 50,
		  "TIMER_LIFETIME" : 99,
		  "RECORDING_NAMING_MODE": 1		
      }
    },

    {
      "uuid": "pivdr2",
      "displayName": "Pi Vdr 2",
      "config" : {
      	  "TIMER_AUX" : "<remotetimers>2</remotetimers>",
		  "TIMER_MINUS_MINUTES" : 10,
		  "TIMER_PLUS_MINUTES" : 10,
		  "TIMER_PRIORITY" : 50,
		  "TIMER_LIFETIME" : 99,
		  "RECORDING_NAMING_MODE": 1	
      }
    },

    {
      "uuid": "stream1",
      "displayName": "Stream 1",
      "restfulApiPort" : 9004,     
      "config" : {
      	  "TIMER_AUX" : "<remotetimers>1</remotetimers>",
		  "TIMER_MINUS_MINUTES" : 10,
		  "TIMER_PLUS_MINUTES" : 10,
		  "TIMER_PRIORITY" : 50,
		  "TIMER_LIFETIME" : 99,
		  "RECORDING_NAMING_MODE": 1	
      }
    },

    {
      "uuid": "stream2",
      "displayName": "Stream 2",
      "restfulApiPort" : 9006,
      "config" : {
      	  "TIMER_AUX" : "<remotetimers>2</remotetimers>",
		  "TIMER_MINUS_MINUTES" : 10,
		  "TIMER_PLUS_MINUTES" : 10,
		  "TIMER_PRIORITY" : 50,
		  "TIMER_LIFETIME" : 99,
		  "RECORDING_NAMING_MODE": 1	
      }
    }
  ]	
```		
 
* **Sichten:** This is a view to all configured VDR instances

	> **displayName:** Value which will be shown in the Web Application
	
	> **head:** the value defined in "aliases". This VDR instance has normally a display connected. Channel change and such operations works on this VDR.
	
	> **channels:** ordered list of VDR instances where to get a working channel list. Used in the EPG view to order the channels and to define the channel groups.
	 	
	> **timers:** the VDR instance on which a timer shall be generated
	
	> **recordings:** ordered list of VDR instances where to get the recording lists and do some operations.
	
```json
	"Sichten": [
		{
			"displayName": "Oben",
			"head" : "pivdr1",
			"channels": [ "pivdr1", "stream1", "stream2", "pivdr2" ],
			"timers" : "stream1",
			"recordings" : [ "stream1", "pivdr1" ]
		},

		{
			"displayName": "Unten",
			"head" : "pivdr2",
			"channels": [ "pivdr2", "stream2", "stream1", "pivdr1" ],
			"timers" : "stream2",
			"recordings" : [ "stream2", "pivdr2" ]
		}
	]  
```

## **Screenshots:** Everyone loves screenshots...

**start page showing the configured views:**
![Start page for views](https://github.com/Zabrimus/page/blob/master/startseite-sichten.png)

**start page showing the configured VDR:**
![Start page for VDRs](https://github.com/Zabrimus/page/blob/master/startseite-vdr.png)

**currently running program:**
![EPG now](https://github.com/Zabrimus/page/blob/master/epg-now.png)

**what's running today:**
![EPG day](https://github.com/Zabrimus/page/blob/master/epg-day.png)

**all EPG data for a channel:**
![EPG channel](https://github.com/Zabrimus/page/blob/master/epg-channel.png)

**configured timers:**
![EPG details](https://github.com/Zabrimus/page/blob/master/timer.png)

**available recordings:**
![EPG details](https://github.com/Zabrimus/page/blob/master/recordings.png)







# Welcome to vdr-jonglisto. A web frontend for multiple VDR instances.

My plan is to replace vdradmin-am with this new application. Currently you can see and search EPG data, see and edit timers and see, rename and move recordings.
As a special feature used in my environment it is possible to define views of all configured VDR instances.

if you have e.g. 4 running VDR instances: 2 headless which do nothing more than recordings and handling timers) and 2 instances which are connected to a display (TV) and which do not have any DVB device installed. Then it is possible to define a view and the application use the configured VDR instance to do the desired work.

* head: points to the VDR instance which is connected to a display. Mainly used for channel switches.
* channels: The application needs to be able to read the channel list and channel groups. In my environment, all VDR channel lists are synchronized, so i define the order of VDR instances where to get the channel list and channel groups. The first reachable VDR is then used.
* timers: only the headless VDR instances have defined timers and are responsible for all timers. The head VDR do not have any timer.
* recordings: where to get the recordings and do the desired work

### Requirements
* optional: a runnning vdr-epg-daemon, especially the mysql database  
* vdr restfulapi plugin, at least version 0.2.6.5
* optional: remoteosd/svdrpservice plugin (See <http://vdr.schmirler.de/>) 
* java 8

### Setup
If you first start the application without an valid configuration, you will be redirected to a setup page to create your first minimal configuration. The configuration file jonglisto.json can then be *downloaded*. There is no automatically installation. But this downloaded file is a good starting point for further configuration.

### Login / Security
While starting with a fresh internal database (e.g. the first start), an admin user (Login: admin, Password: jonglisto) is created. All pages and services are secured. 
**Currently only the admin user is usable. Configuration pages are currently in the implementation phase.**  

### i18n
It is possible to switch the application language. Currently english and german are supported. But the english version could be a little bit bumby.
The language files could be found in directory src/main/webapp/WEB-INF. app.properties or app_de.properties.
Any help in translatating to other languages or correcting some flaws are very appreciated.

### Channel logos
The application now uses channel logos from <https://github.com/picons/picons-source>. To build the necessary logo.jar ju,p into directory tools and start ./build-logo-jar.sh.
This jar have to created only at the very first build or if there exists some new and interesting logos. The logo.jar has a size of only 8MB.

### Theme
The application currently use a theme from <https://bootswatch.com/cerulean/>. If you want to change the default theme, change build.less in directory tools/build-bootstrap and start build.sh in the same directory.

### SVDRP Server and OSD
The application start an own svdrp server and therefore an OSD. The OSD implements the protocol from the svdrposd/remoteosd plugin. 
At this moment the internal server handles requests for OSDs of the other configured VDR instances. The svdrposd plugin is not neccessary, because the proxy gets the remote osd via restfulapi-plugin.
A highly configurable menu entry for epg2vdr is also possible (see script samples/epg2vdr). Currently some status information from epg2vdr database could be selected.

### Remote Timers proxy
The SVDRP server is now able to route every remote timers request to another VDR instance. In this case all SVDRP commands are translated to restfulapi calls. The timer configuration can be changed by the new internal script engine which uses the Java Nashorn implementation (javascript). Samples could be found in folder samples. The remote timers plugin have to be configured accordingly to point to jonglisto server and svdrp port.
Currently the commands LSTR and EDIT are not implemented.

### Nashorn integration
For determining the routing of the SVDRP commands and modifying remote timers timer requests, the Java Nashorn library (javascript) is integrated. Samples could be found in folder samples. 

### SVDRP console
It is possible to send a SVDRP command to all configured VDR instances and see the result.

### channelmap.conf editor (epg2vdr daemon)
The configuration of channelmap.conf for the epg2vdr daemon can be done via Jonglisto. It supports currently different epg provider (all german). The mapping could be done via drag and drop. The configuration itself is saved into the internal database. If the configuration is done well, the included auto-mapping tool will create a reasonable channelmap.conf.
Included is a configuration which is reasonable for my cable provider. 
It is also possible to sort the channels in the epg2vdr database according to your personal sorting.

### channel sorting
It is possible to create channel groups and sort channels via drag and drop. The final channels.conf can then be downloaded.
The current configuration (based on channel names and groups) can be saved into the internal database. If the internal configuration is then loaded after some while or after a new channel scan, the channel list will be then sorted via the saved configuration. If some channel ids are changed, loading the configuration should resort the channel list.
 

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

* **useEpgd:** if "true" then use the configured epgd database, otherwise get all epg data via restfulapi from a configured VDR (see epgVdr)

* **epgVdr:** If you don't want to use jonglisto with the epgd database, this value is needed. Here the alias VDR have to be configured, where EPG data will be read.
 
* **useRecordingSyncMap:** use a feature in vdr restfulapi plugin to speed up the recording list. It is safe to set this value to "false". This feature really needs at least 0.2.6.5 of restfulapi plugin. 
```json
	"useRecordingSyncMap" : "false"
```

* **developer_mode:** enable this feature to see some more developer pages, which are only useful while developing the application. 
```json
	"developer_mode" : "false"
```

* **svdrpPort:** enable the internal svdrp server. At this moment the server is a remoteosd/svdrposd proxy. More to come... 
```json
	"svdrpPort" : "5000"
```

* **remoteOsdSleepTime:** Sleep time in ms before the remote osd will be loaded. This highly depends on your system configuration. 
```json
	"remoteOsdSleepTime" : "200"
```

* **remoteOsdIncSleepTime:** Sleep time in ms in next retry before the remote osd will be loaded. This highly depends on your system configuration. 
```json
	"remoteOsdIncSleepTime" : "false"
```

* **NashornScripts:** Configuration of the existing javascripts, which are used internally. Currently SVDRP and epg2vdr script is necessary.
```json
	"svdrp" : "/etc/jonglisto/svdrp.js"
	"epg2vdr": "/etc/jonglisto/epg2vdr.js"
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







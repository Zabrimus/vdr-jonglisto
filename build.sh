#!/bin/sh


usage() {
   echo "Start with:"
   echo "  $0 war          to create a war file which can be deployed in jetty or tomcat or something similar."
   echo "  $0 standalone   to create a runnable war file which can be started with java -jar jonglisto.war"
   echo "  $0 clean        to clean"
   
   exit 1
}

if [ "$#" -ne 1 ]; then
	usage
fi

case "$1" in
        "war") 
        	echo "create war..."
        	./gradlew war
            ;;
            
        "standalone") 
        	echo "create runnable war...";
        	./gradlew standaloneWar
            ;;
            
        "clean") 
        	echo "clean...";
        	./gradlew clean
            ;;
            
        *) 
        	usage
            ;;
esac
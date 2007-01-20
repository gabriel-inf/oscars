#!/bin/sh
. setclasspath.sh
java -Daxis2.xml=repo/axis2.xml QueryReservationClient repo https://oscars-dev.es.net:9090/axis2/services/OSCARS

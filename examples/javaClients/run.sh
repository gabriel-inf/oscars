#!/bin/sh

# update classpath
OSCARS_CLASSPATH="."
for f in "$AXIS2_HOME"/lib/*.jar
do
 OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done
OSCARS_CLASSPATH="$OSCARS_CLASSPATH":../OSCARS-client-api.jar:OSCARS-client-examples.jar

url=$2

if [  $# -lt 2  ]
 then
    echo "run.sh createReservation|signal url [request-specific-params]"
elif [ $1 == "createReservation" ] && [ $# -eq 2 ] && [ $2 != "-help" ]
 then
    echo $2
    java -cp $OSCARS_CLASSPATH CreateReservationClient repo $url
elif [  $1 == "createReservation"  ]
 then
    java -cp $OSCARS_CLASSPATH CreateReservationCLI $*
elif [ $1 == "signal"  ]
 then    
    java -cp $OSCARS_CLASSPATH SignalClient repo $url $3 $4
else
    echo "Please specify 'createReservation' or 'signal'"
fi

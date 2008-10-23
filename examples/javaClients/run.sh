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
    echo "run.sh createReservation|signal|list|query|cancel|subscribe|renew|pause|ressume|unsubscribe|regpublisher|destroyreg|notifylistener [request-specific-params]"
elif [ $1 == "createReservation" ] && [ $2 == "-pf" ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true CreateReservationClient $*
elif [  $1 == "createReservation"  ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true CreateReservationCLI $*
elif [ $1 == "signal"  ]
 then    
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true SignalClient repo $url $3 $4 $5 $6
elif [ $1 == "query"  ]
 then    
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true QueryReservationCLI $*
elif [ $1 == "list"  ]
 then    
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true ListReservationCLI $*
elif [ $1 == "cancel"  ]
 then    
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true CancelReservationCLI $*
elif [ $1 == "subscribe"  ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true SubscribeClient $*
elif [ $1 == "renew"  ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true RenewClient $*
elif [ $1 == "pause"  ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true PauseSubscriptionClient $*
elif [ $1 == "resume"  ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true ResumeSubscriptionClient $*
elif [ $1 == "unsubscribe"  ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true UnsubscribeClient $*
elif [ $1 == "regpublisher"  ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true RegisterPublisherClient $*
elif [ $1 == "destroyreg"  ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true DestroyRegistrationClient $*
elif [ $1 == "notifylistener"  ]
 then
    java -cp $OSCARS_CLASSPATH -Djava.net.preferIPv4Stack=true -Djavax.net.ssl.keyStore=repo/OSCARS.jks -Djavax.net.ssl.keyStorePassword=password NotifyEchoHandler $*
else
    echo "Invalid operation specified. Usage: "
    echo "run.sh createReservation|signal|list|query|cancel|subscribe|renew|pause|ressume|unsubscribe|regpublisher|dstroyreg|notifylistener [request-specific-params]"
fi


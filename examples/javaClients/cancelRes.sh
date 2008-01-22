#!/bin/sh
. setclasspath.sh
url="https://oscars.es.net/axis2/services/OSCARS"
if [  $# -eq 1  ]
 then
case $1 in
oscars-devext) url="https://oscars-devext.es.net/axis2/services/OSCARS";;
oscars-devint) url="https://oscars-devint.es.net:9090/axis2/services/OSCARS";;
esac
fi
java -Daxis2.xml=repo/axis2.xml -Djava.net.preferIPv4Stack=true CancelReservationClient repo $url

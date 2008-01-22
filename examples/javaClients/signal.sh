#!/bin/sh
. ./setclasspath.sh
#url="https://hopishib.internet2.edu:8443/axis2/services/OSCARS"
url=$1
if [  $# -eq 1  ]
 then
case $1 in
oscars-devint) url="https://oscars-devint.es.net:9090/axis2/services/OSCARS";;
oscars-devext) url="https://oscars-devext.es.net/axis2/services/OSCARS";;
esac
fi
echo $url
java -Daxis2.xml=repo/axis2.xml -Djava.net.preferIPv4Stack=true SignalClient repo $url $2 $3 $4 $5 $6

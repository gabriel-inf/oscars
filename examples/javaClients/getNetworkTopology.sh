#!/bin/sh
. ./setclasspath.sh
url="https://hopiuclp1.internet2.edu:8443/axis2/services/OSCARS"
if [  $# -eq 1  ]
 then
case $1 in
hopi) url="https://hopibruw.internet2.edu:8443/axis2/services/OSCARS";;
anna) url="https://hopiuclp1.internet2.edu:8443/axis2/services/OSCARS";;
oscars-dev) url="https://oscars-dev.es.net:8443/axis2/services/OSCARS";;
oscars-devint) url="https://oscars-devint.es.net:9090/axis2/services/OSCARS";;
oscars) url="https://oscars.es.net/axis2/services/OSCARS";;
jra3) url="https://srv2.lon.uk.geant2.net:8443/Interdomain/services/OSCARS";;
esac
fi
java -Daxis2.xml=repo/axis2.xml -Djava.net.preferIPv4Stack=true GetNetworkTopologyClient repo $url

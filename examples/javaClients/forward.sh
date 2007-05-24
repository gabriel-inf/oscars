#!/bin/sh
. setclasspath.sh
url="https://oscars.es.net/axis2/services/OSCARS"
if [  $# -eq 1  ]
 then
case $1 in
oscars-dev) url="https://oscars-dev.es.net:9090/axis2/services/OSCARS";;
oscars-test) url="https://oscars-test.es.net/axis2/services/OSCARS";;
esac
fi
java -Daxis2.xml=repo/axis2.xml ForwardClient repo $url false


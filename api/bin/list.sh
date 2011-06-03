#!/bin/sh 
#usage list.sh -n <number of reservations requested> -o <offest>
test   ! -d target/tmp  -o \( target/tmp -ot target/api-0.0.1-SNAPSHOT.one-jar.jar \) && . bin/expandOneJar.sh
. bin/setclasspath.sh
#  -Djavax.net.debug=all will dump the ssl messages
java net.es.oscars.api.test.IDCTest \
-v 0.6 -a x509 -c list $*


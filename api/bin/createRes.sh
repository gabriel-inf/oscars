#!/bin/sh   
#usage createRes.sh [-gri <gri>] [-pf <paramFile>]
test   ! -d target/tmp  -o \( target/tmp -ot target/api-0.0.1-SNAPSHOT.one-jar.jar \) && . bin/expandOneJar.sh
. bin/setclasspath.sh
cp src/test/resources/*.yaml target/test-classes
# -Djavax.net.debug=all dumps all ssl messages
java net.es.oscars.api.test.IDCTest  \
-v 0.6 -a x509 -c createReservation $*


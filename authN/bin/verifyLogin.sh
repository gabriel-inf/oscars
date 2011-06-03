#!/bin/sh 
#usage verifyLogin -u <loginName> -p <password> -C <context>
#  -Djavax.net.debug=all will dump the ssl messages
test -d target/tmp || . bin/expandOneJar.sh
. bin/setclasspath.sh
java net.es.oscars.authN.test.AuthNTest -c verifyLogin $* 

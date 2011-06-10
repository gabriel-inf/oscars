#!/bin/bash
vers=`cat $OSCARS_DIST/VERSION`
java -Djava.net.preferIPv4Stack=true -jar target/lookup-0.0.1-$vers.one-jar.jar  $*
#!/bin/sh
rm -rf $OSCARS_DIST/api/target/tmp
mkdir $OSCARS_DIST/api/target/tmp
cp $OSCARS_DIST/api/config/log4j*properties $OSCARS_DIST/api/target/classes
(cd $OSCARS_DIST/api/target/tmp; jar -xf ../api-0.0.1-SNAPSHOT.one-jar.jar )

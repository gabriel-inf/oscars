#!/bin/sh
rm -rf target/tmp
mkdir target/tmp
cp $OSCARS_DIST/resourceManager/config/log4j*properties $OSCARS_DIST/resourceManager/target/classes
cp $OSCARS_DIST/resourceManager/src/test/resources/store.yaml $OSCARS_DIST/resourceManager/target/classes
(cd target/tmp; jar -xf ../resourceManager-0.0.1-SNAPSHOT.one-jar.jar )

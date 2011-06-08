#!/bin/sh
vers=`cat $OSCARS_DIST/VERSION`
rm -rf $OSCARS_DIST/wsnbroker/target/tmp
mkdir $OSCARS_DIST/wsnbroker/target/tmp
cp $OSCARS_DIST/wsnbroker/config/log4j.*.properties $OSCARS_DIST/wsnbroker/target/classes
(cd $OSCARS_DIST/wsnbroker/target/tmp; jar -xf ../wsnbroker-$vers.one-jar.jar )

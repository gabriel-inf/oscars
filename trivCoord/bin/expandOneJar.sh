#!/bin/sh
rm -rf target/tmp
mkdir target/tmp
(cd target/tmp; jar -xf ../trivCoord-0.0.1-SNAPSHOT.one-jar.jar )

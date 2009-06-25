#!/bin/bash

DOJO_VERSION="1.3.1";
BUILD_PROFILE="../../../oscars-stats.profile.js";

if [ -f dojo-release-$DOJO_VERSION-src.tar.gz ] && [ ! -d dojo-release-$DOJO_VERSION-src ]; then
    echo "-- Unpacking dojo-release-$DOJO_VERSION-src.tar.gz";
    gunzip dojo-release-$DOJO_VERSION-src.tar.gz;
    if [ $? != 0 ]; then
        echo "-- Error occurred unzipping dojo.";
        exit 1;
    fi
fi

if [ -f dojo-release-$DOJO_VERSION-src.tar ] && [ ! -d dojo-release-$DOJO_VERSION-src ]; then
    echo "-- Unpacking dojo-release-$DOJO_VERSION-src.tar";
    tar -xvf dojo-release-$DOJO_VERSION-src.tar;
    if [ $? != 0 ]; then
        echo "-- Error occurred untarring dojo.";
        `rm dojo-release-$DOJO_VERSION-src.tar`;
        exit 1 ;
    fi
    `rm dojo-release-$DOJO_VERSION-src.tar`;
fi

echo "-- Building custom dojo package...";
if [ ! -d ./dojo-release-$DOJO_VERSION-src/util/buildscripts/ ]; then
    echo "Could not find directory dojo-release-$DOJO_VERSION-src/util/buildscripts/";
    exit 1;
fi
cd ./dojo-release-$DOJO_VERSION-src/util/buildscripts/;

./build.sh profileFile="$BUILD_PROFILE" action=release
if [ $? != 0 ]; then
    echo "-- Error building dojo package.";
    exit 1;
fi
echo "-- Custom dojo package built.";
echo "-- Installing new dojo files";
cd ../../
if [ -d ../../../html/stats/lib/dojo/ ]; then
    echo "--- Removing old dojo files";
    rm -rf ../../../html/stats/lib/dojo/;
fi

cp -r release/dojo ../../../html/stats/lib/dojo
if [ $? != 0 ]; then
    echo "-- Error copying dojo package.";
    exit 1;
fi
echo "-- New dojo files installed.";

echo "";
echo "##############################################################################";
echo "Dojo successfully built!";
echo "##############################################################################";

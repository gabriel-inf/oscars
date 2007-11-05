#!/bin/sh
. ./setclasspath.sh
java -Daxis2.xml=repo/axis2.xml CreateReservationClient $1 $2 $3

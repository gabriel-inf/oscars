#!/bin/sh
. ./setclasspath.sh
java -Daxis2.xml=repo/axis2.xml -Djava.net.preferIPv4Stack=true OSCARSRandExerciser $*

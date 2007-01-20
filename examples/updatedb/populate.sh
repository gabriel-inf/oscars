#!/bin/sh
. setclasspath.sh
export CLASSPATH=$CLASSPATH:../../api.build/WEB-INF/classes/net/es/oscars/bss/topology/
java PopulateDB $*


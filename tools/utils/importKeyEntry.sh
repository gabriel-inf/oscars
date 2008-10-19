#!/bin/sh
# update classpath
OSCARS_CLASSPATH=".:classes"
for f in lib/*.jar
do
    OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

java -cp $OSCARS_CLASSPATH KeyStoreImport $*

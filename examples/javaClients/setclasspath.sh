# Assume in trunk/examples/javaClients if OSCARS_HOME not set
OSCARS_LIB="${OSCARS_HOME-../..}/lib"
echo OSCARS_LIB is $OSCARS_LIB
# update classpath
OSCARS_CLASSPATH=""
for f in "$OSCARS_LIB"/*.jar
do
 OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done
for f in "$OSCARS_LIB"/axis2/*.jar
do
 OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

CLASSPATH=.:../../build/examples/classes:$OSCARS_CLASSPATH
export OSCARS_LIB=$OSCARS_LIB
export CLASSPATH=$CLASSPATH

#echo CLASSPATH is $CLASSPATH

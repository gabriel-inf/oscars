# Only set OSCARS_HOME if not already set
[ -z "$OSCARS_HOME" ] && OSCARS_HOME=../..
echo OSCARS_HOME is $OSCARS_HOME
# update classpath
OSCARS_CLASSPATH=""
for f in "$OSCARS_HOME"/lib/*.jar
do
 OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done
for f in "$OSCARS_HOME"/lib/axis2/*.jar
do
 OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

CLASSPATH=.:../../build/examples/classes:$OSCARS_CLASSPATH
export OSCARS_HOME=$OSCARS_HOME
export CLASSPATH=$CLASSPATH

#echo CLASSPATH is $CLASSPATH

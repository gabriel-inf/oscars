# Only set OSCARS_HOME if not already set
if ( ! $?OSCARS_HOME ) then
  set OSCARS_HOME="../.."
endif
echo OSCARS_HOME is $OSCARS_HOME
# update classpath
set OSCARS_CLASSPATH=""
foreach f  (`ls $OSCARS_HOME/lib/*.jar`)
 set OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
end
foreach f (`ls -1 $OSCARS_HOME/lib/axis2/*.jar`)
 set OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
end

setenv CLASSPATH .:../../build/WEB-INF/classes:$OSCARS_CLASSPATH
echo CLASSPATH is $CLASSPATH

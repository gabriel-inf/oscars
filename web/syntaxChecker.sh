#!/bin/sh

filename="$1"
CLASSPATH=../lib/js.jar
export CLASSPATH=$CLASSPATH

echo $filename
java org.mozilla.javascript.tools.shell.Main lib/jslint.js $filename
exit 1

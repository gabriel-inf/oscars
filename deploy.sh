#!/bin/sh
echo "  ";
echo "--- Checking prerequisites...";
if [ -d src/net/es/oscars ]; then
	echo "    We seem to be in the correct directory";
else
	echo "Error. Run this script from its directory. ";
	exit 1;
fi

# Find Tomcat
if [ -n "$CATALINA_HOME" ]; then
	echo "    CATALINA_HOME is set to $CATALINA_HOME";
else
	echo "Required environment variable CATALINA_HOME is not set. Please run install.sh .";
	exit 1;
fi
echo "  ";
echo "  ";

echo "--- Stopping Tomcat...";
$CATALINA_HOME/bin/shutdown.sh;
echo "  ";
echo "  ";
STATUS2="";
echo "Do you wish to copy key files and oscars configuration files to Tomcat now? [y/n] " 
ans=0;
while [ $ans == 0 ]; do
   read ans;
   if [ "$ans" != "y" ] && [ "$ans" != "Y" ] && [ "$ans" != "n" ] && [ "$ans" != "N" ]; then
		ans=0;
   fi
done
if [ "$ans" == "y" ] || [ "$ans" == "Y" ]; then
	echo "  OK, will setup the server";
	ant setupServer;
	STATUS2=", server configured"
fi
echo "--- Deploying IDC...";
ant deployall;
echo "  ";
echo "  ";
echo "--- Restarting Tomcat...";
$CATALINA_HOME/bin/startup.sh;
echo "  ";
echo "  ";
echo "IDC deployed$STATUS2";

exit 0;

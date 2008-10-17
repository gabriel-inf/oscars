#!/bin/bash
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
	echo "Required environment variable CATALINA_HOME is not set. Please run do_build.sh .";
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

#Update services.xml
CATALINA_HOME_LEN=`expr length "$CATALINA_HOME"`;
if [ `echo $CATALINA_HOME | egrep "/$"` ]; then
    CATALINA_HOME_LEN=`expr $CATALINA_HOME_LEN - 1`;
fi
CATALINA_HOME_ESC=`expr substr "$CATALINA_HOME" 1 $CATALINA_HOME_LEN`;
CATALINA_HOME_ESC=`echo $CATALINA_HOME_ESC | sed -e 's/\//\\\\\//g'`;
sed -i -e "s/<\!ENTITY rampConfig SYSTEM \".*\">/<\!ENTITY rampConfig SYSTEM \"$CATALINA_HOME_ESC\/shared\/classes\/repo\/rampConfig.xml\">/" conf/server/services.xml;
if [ $? != 0 ]; then
    echo "Error modifying conf/server/services.xml"
    exit 1;
fi
sed -i -e "s/<\!ENTITY rampConfig SYSTEM \".*\">/<\!ENTITY rampConfig SYSTEM \"$CATALINA_HOME_ESC\/shared\/classes\/repo\/rampConfig.xml\">/" conf/notify-server/services.xml;
if [ $? != 0 ]; then
    echo "Error modifying conf/notify-server/services.xml"
    exit 1;
fi

#Deploy IDC
echo "--- Deploying IDC...";
ant deployall;
echo "  ";
echo "  ";
echo "--- Restarting Tomcat...";
$CATALINA_HOME/bin/startup.sh;

echo "  ";
echo "  ";
echo "IDC deployed$STATUS2";
echo "  ";
echo "  ";

#Build the OSCARS tools
READ_BUILDTOOLS=0;
while [ $READ_BUILDTOOLS == 0 ]; do
    echo "";
    echo -n "Should I build the OSCARS tools for you y/n?";
    read READ_BUILDTOOLS;
    if [ "$READ_BUILDTOOLS" != "y" ] && [ "$READ_BUILDTOOLS" != "Y" ] && [ "$READ_BUILDTOOLS" != "n" ] && [ "$READ_BUILDTOOLS" != "N" ]; then
        READ_BUILDTOOLS=0;
    fi
done
if [ "$READ_BUILDTOOLS" == "y" ] || [ "$READ_BUILDTOOLS" == "Y" ]; then
    echo "";
    echo "";
    echo "--- Building tools...";
    cd ./tools/utils
    ant
    echo "";
    echo "";
    echo "--- Tools built.";
fi


exit 0;

#!/bin/bash
# Script to upgrade from DCN release 0.4/0.5.0 to 0.5.1
# if you do not have a DCN installation run do_build.sh
# Changes private password to keystore password
# Updates bss and aaa database tables (no changes to notify tables)
# Asks if youw ant to auto-update your local topology

# Upgrade 0.5.0 -> 0.5.1
#Check OSCARS_HOME for 0.5.1
if [ -n "$OSCARS_HOME" ] && [ "$OSCARS_HOME" != `pwd` ] && [ ! -d ./conf-0-5-1.default ]; then
    #stop oscars
    $OSCARS_HOME/oscars.sh stop ;
    
    `mv ./conf ./conf-0-5-1.default`;
    if [ $? != 0 ]; then
        echo "Unable to move default properties from ./conf' to ./conf-0-5-1.default";
        exit;
    fi
    `cp -r $OSCARS_HOME/conf ./conf`;
    if [ $? != 0 ]; then
        echo "Unable to copy properties from '$OSCARS_HOME/conf' to ./conf";
        rm -rf ./conf-0-5-1.default
        exit;
    fi
    
    CUR_DIR=`pwd`;
    echo "-- Your configuration files and keystores have been copied to this directory.";
    echo "";
    echo "##############################################################################";
    echo "IMPORTANT: YOU MUST CHANGE \$OSCARS_HOME to $CUR_DIR BEFORE RUNNING DO_INSTALL.SH"
    echo "";
    echo "Upgrade completed successfully. Change \$OSCARS_HOME and run do_install.sh";
    echo "##############################################################################";
    exit 0;
elif [ -n "$OSCARS_HOME" ] && [ "$OSCARS_HOME" != `pwd` ]; then
    echo "-- Your configuration files and keystores were copied by a previous upgrade";
    echo "-- If you think this is a mistake then run 'rm -rf ./conf-0-5-1.default' and try this script again";
    exit 0;
elif [ -n "$OSCARS_HOME" ]; then
    echo "-- We are in \$OSCARS_HOME so nothing to do.";
    exit 0;
fi


# Upgrade 0.4.0 -> 0.5.1
#Check current directory
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
	echo "Required environment variable CATALINA_HOME is not set. Please run do_build.sh.";
	exit 1;
fi
echo "  ";
echo "  ";

#Change keystore password from "password" to keystore password
REPO_PATH="$CATALINA_HOME/shared/classes/repo";
INSTALL_HOME=`pwd`;
KS_PASS=`grep "org.apache.ws.security.crypto.merlin.keystore.password" $REPO_PATH/rampConfig.xml | sed -e 's/\s*<ramp:property name="org\.apache\.ws\.security\.crypto\.merlin\.keystore\.password">\(.*\)<\/ramp:property>/\1/'`;
KS_PASS=`echo "$KS_PASS" | sed 's/^[ \t]*//;s/[ \t]*$//'`; # delete leading and trailing whitespace
KEY_ALIAS=`grep "ramp:user" $REPO_PATH/rampConfig.xml | sed -e 's/\s*<ramp:user>\(.*\)<\/ramp:user>/\1/'`;
KEY_ALIAS=`echo "$KEY_ALIAS" | sed 's/^[ \t]*//;s/[ \t]*$//'`; # delete leading and trailing whitespace
if [ "$KS_PASS" != "password" ]; then
    keytool -keypasswd -keystore $CATALINA_HOME/shared/classes/repo/OSCARS.jks -keypass password -new $KS_PASS -storepass $KS_PASS -alias $KEY_ALIAS
fi

#Upgrade mysql tables
MYSQL_ANS=0;
while [ $MYSQL_ANS == 0 ]; do
	echo -n "Would you like to upgrade your aaa database y/n? ";
    read MYSQL_ANS;
    if [ "$MYSQL_ANS" != "y" ] && [ "$MYSQL_ANS" != "Y" ] && [ "$MYSQL_ANS" != "n" ] && [ "$MYSQL_ANS" != "N" ]; then
        MYSQL_ANS=0;
    fi
done
if [ "$MYSQL_ANS" = "y" ] || [ "$MYSQL_ANS" = "Y" ]; then
    echo -n "Please enter your mysql user name: ";
    read MYSQL_USER;
    mysql -u $MYSQL_USER -p aaa < sql/aaa/upgradeTables0.4-0.5.sql
    echo "--- mysql tables upgraded";
fi
MYSQL_ANS2=0;
while [ $MYSQL_ANS2 == 0 ]; do
	echo -n "Would you like to upgrade your bss MySQL tables y/n? ";
    read MYSQL_ANS2;
    if [ "$MYSQL_ANS2" != "y" ] && [ "$MYSQL_ANS2" != "Y" ] && [ "$MYSQL_ANS2" != "n" ] && [ "$MYSQL_ANS2" != "N" ]; then
        MYSQL_ANS2=0;
    fi
done
if [ "$MYSQL_ANS2" = "y" ] || [ "$MYSQL_ANS2" = "Y" ]; then
    echo -n "Please enter your mysql user name: ";
    read MYSQL_USER;
    mysql -u $MYSQL_USER -p bss < sql/bss/upgradeTables0.4-0.5.sql
    echo "--- mysql tables upgraded";
fi

PROP_ANS=0;
while [ $PROP_ANS == 0 ]; do
	echo -n "Would you like to automatically update the OSCARS database when the XML topology changes instead of running \"updateTopology.sh\"  y/n? ";
    read PROP_ANS;
    if [ "$PROP_ANS" != "y" ] && [ "$PROP_ANS" != "Y" ] && [ "$PROP_ANS" != "n" ] && [ "$PROP_ANS" != "N" ]; then
        PROP_ANS=0;
    fi
done
if [ "$PROP_ANS" = "y" ] || [ "$PROP_ANS" = "Y" ]; then
    echo "external.service.topology.updateLocal=1" >> $CATALINA_HOME/shared/classes/server/oscars.properties
fi

echo "--- Upgrade changes complete";
echo "";
echo "##############################################################################";
echo "";
echo "UPGRADE NOTES";
echo "";
echo "Please run ./do_install.sh to complete your upgrade.";
echo "##############################################################################";

exit 0;


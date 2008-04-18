#!/bin/sh

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
	echo "Required environment variable CATALINA_HOME is not set. Please run do_build.sh .";
	exit 1;
fi
echo "  ";
echo "  ";

#Upgrade mysql tables
MYSQL_ANS=0;
while [ $MYSQL_ANS == 0 ]; do
	echo -n "Would you like to upgrade your bss MySQL tables y/n? ";
    read MYSQL_ANS;
    if [ "$MYSQL_ANS" != "y" ] && [ "$MYSQL_ANS" != "Y" ] && [ "$MYSQL_ANS" != "n" ] && [ "$MYSQL_ANS" != "N" ]; then
        MYSQL_ANS=0;
    fi
done
if [ "$MYSQL_ANS" = "y" ] || [ "$MYSQL_ANS" = "Y" ]; then
    echo -n "Please enter your mysql user name: ";
    read MYSQL_USER;
    mysql -u $MYSQL_USER -p < sql/bss/upgradeTables.sql
    echo "--- mysql tables upgraded";
fi
MYSQL_ANS2=0;
while [ $MYSQL_ANS2 == 0 ]; do
	echo -n "Would you like to add the OSCARS-service and OSCARS-operator attributes to your aaa database y/n? ";
    read MYSQL_ANS2;
    if [ "$MYSQL_ANS2" != "y" ] && [ "$MYSQL_ANS2" != "Y" ] && [ "$MYSQL_ANS2" != "n" ] && [ "$MYSQL_ANS2" != "N" ]; then
        MYSQL_ANS2=0;
    fi
done
if [ "$MYSQL_ANS2" = "y" ] || [ "$MYSQL_ANS2" = "Y" ]; then
    echo -n "Please enter your mysql user name: ";
    read MYSQL_USER;
    mysql -u $MYSQL_USER -p < sql/bss/upgradeTables.sql
    echo "--- mysql tables upgraded";
fi

#check for sec-server.jks
if [ -f $CATALINA_HOME/shared/classes/server/sec-server.jks ]; then
    echo "--- sec-server.jks looks good. no upgrade needed for file.";
else
    JKS_ANS=0;
    while [ $JKS_ANS == 0 ]; do
        echo -n "May I copy sec-server.jks to $CATALINA_HOME/shared/classes/server y/n? ";
        read JKS_ANS;
        if [ "$JKS_ANS" != "y" ] && [ "$JKS_ANS" != "Y" ] && [ "$JKS_ANS" != "n" ] && [ "$JKS_ANS" != "N" ]; then
            JKS_ANS=0;
        fi
    done
    if [ "$JKS_ANS" = "y" ] || [ "$JKS_ANS" = "Y" ]; then
        if [ -n "$DOMAIN_HOME" ]; then
            cp $DOMAIN_HOME/server/sec-server.jks $CATALINA_HOME/shared/classes/server/sec-server.jks
        else
            cp conf/examples/server/sec-server.jks $CATALINA_HOME/shared/classes/server/sec-server.jks
        fi
        
        echo "--- sec-server.jks moved to $CATALINA_HOME/shared/classes/server/";
    fi
fi

#Check for sec-server.properties
if [ -f $CATALINA_HOME/shared/classes/server/sec-server.properties ]; then 
    PROPS_ANS=0;
    while [ $PROPS_ANS == 0 ]; do
        echo -n "May I update sec-server.properties to point to $CATALINA_HOME/shared/classes/server/sec-server.jks y/n? ";
        read PROPS_ANS;
        if [ "$PROPS_ANS" != "y" ] && [ "$PROPS_ANS" != "Y" ] && [ "$PROPS_ANS" != "n" ] && [ "$PROPS_ANS" != "N" ]; then
            PROPS_ANS=0;
        fi
    done
    if [ "$PROPS_ANS" = "y" ] || [ "$PROPS_ANS" = "Y" ]; then
        sed -e 's/org\.apache\.ws\.security\.crypto\.merlin\.file=.*/org.apache.ws.security.crypto.merlin.file=server\/sec-server.jks/g' $CATALINA_HOME/shared/classes/server/sec-server.properties > conf/server/sec-server.properties
        cp conf/server/sec-server.properties $CATALINA_HOME/shared/classes/server/sec-server.properties
        echo "--- Upgraded sec-server.properties";
    fi
else
    PROPS_ANS=0;
    while [ $PROPS_ANS == 0 ]; do
        echo -n "May I copy sec-server.properties to $CATALINA_HOME/shared/classes/server y/n? ";
        read PROPS_ANS;
        if [ "$PROPS_ANS" != "y" ] && [ "$PROPS_ANS" != "Y" ] && [ "$PROPS_ANS" != "n" ] && [ "$PROPS_ANS" != "N" ]; then
            PROPS_ANS=0;
        fi
    done
    if [ "$PROPS_ANS" = "y" ] || [ "$PROPS_ANS" = "Y" ]; then
        cp conf/examples/server/sec-server.properties $CATALINA_HOME/shared/classes/server/sec-server.properties
        echo "--- sec-server.properties moved to $CATALINA_HOME/shared/classes/server/";
    fi
fi

#Fix oscars.properties
NEED_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | grep aaa.secureCookie`;
if [ "$NEED_PROP" == "" ]; then
    echo "" >> $CATALINA_HOME/shared/classes/server/oscars.properties;
    echo "aaa.secureCookie=0" >> $CATALINA_HOME/shared/classes/server/oscars.properties;
    echo "--- oscars.properties upgraded.";
else
    echo "--- oscars.properties looks good. no upgrade needed for file.";
fi

echo "--- Upgrade changes complete";
echo "";
echo "##############################################################################";
echo "";
echo "UPGRADE NOTES";
echo "";
echo "You may now run ./do_install.sh to complete your upgrade.";
echo "";
echo "##############################################################################";



exit 0;


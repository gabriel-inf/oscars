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
	echo "Required environment variable CATALINA_HOME is not set. Please run do_build.sh.";
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
    mysql -u $MYSQL_USER -p bss < sql/bss/upgradeTables0.3-0.4.sql
    echo "--- mysql tables upgraded";
fi
MYSQL_ANS2=0;
while [ $MYSQL_ANS2 == 0 ]; do
	echo -n "Would you like to upgrade your aaa database y/n? ";
    read MYSQL_ANS2;
    if [ "$MYSQL_ANS2" != "y" ] && [ "$MYSQL_ANS2" != "Y" ] && [ "$MYSQL_ANS2" != "n" ] && [ "$MYSQL_ANS2" != "N" ]; then
        MYSQL_ANS2=0;
    fi
done
if [ "$MYSQL_ANS2" = "y" ] || [ "$MYSQL_ANS2" = "Y" ]; then
    echo -n "Please enter your mysql user name: ";
    read MYSQL_USER;
    mysql -u $MYSQL_USER -p aaa < sql/aaa/upgradeTables0.3-0.4.sql
    echo "--- mysql tables upgraded";
fi
MYSQL_ANS3=0;
while [ $MYSQL_ANS3 == 0 ]; do
	echo -n "Would you like to create the notify database y/n? ";
    read MYSQL_ANS3;
    if [ "$MYSQL_ANS3" != "y" ] && [ "$MYSQL_ANS3" != "Y" ] && [ "$MYSQL_ANS3" != "n" ] && [ "$MYSQL_ANS3" != "N" ]; then
        MYSQL_ANS3=0;
    fi
done
if [ "$MYSQL_ANS3" = "y" ] || [ "$MYSQL_ANS3" = "Y" ]; then
    echo "NOTE: This action requires you to specifiy a privileged MySQL user account such as 'root'";
    echo -n "Please input a privileged mysql user (i.e. root): ";
    read MYSQL_USER;
    mysql -u $MYSQL_USER -p < sql/notify/createTables.sql
    echo "--- mysql tables upgraded";
fi

#upgrade topology - TODO: Read terce properties
TOPO_ANS=0;
while [ $TOPO_ANS == 0 ]; do
	echo -n "Would you like to update your topology description to match the latest schema version? ";
    read TOPO_ANS;
    if [ "$TOPO_ANS" != "y" ] && [ "$TOPO_ANS" != "Y" ] && [ "$TOPO_ANS" != "n" ] && [ "$TOPO_ANS" != "N" ]; then
        TOPO_ANS=0;
    fi
done
if [ "$TOPO_ANS" = "y" ] || [ "$TOPO_ANS" = "Y" ]; then
    INTER_FILE=`cat $CATALINA_HOME/shared/classes/terce.conf/terce-ws.properties | grep tedb.static.db.interdomain | sed -e 's/tedb.static.db.interdomain=//g'`;
    INTRA_FILE=`cat $CATALINA_HOME/shared/classes/terce.conf/terce-ws.properties | grep tedb.static.db.intradomain | sed -e 's/tedb.static.db.intradomain=//g'`;
    sed -i -e 's/20071023/20080828\//g' $INTER_FILE
    sed -i -e 's/20071023/20080828\//g' $INTRA_FILE
    sed -i -e 's/Specfic/Specific/g' $INTER_FILE
    sed -i -e 's/Specfic/Specific/g' $INTRA_FILE
fi

echo "- Updating oscars.properties...";
OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | grep "lookup.url"`;
if [ -z "$OLD_PROP" ]; then
    echo "-- Changing lookup.url to lookup.hints...";
    sed -i"" -e 's/lookup\.url\=.+/lookup\.hints=http:\/\/www.perfsonar.net\/gls.root.hints/g' $CATALINA_HOME/shared/classes/server/oscars.properties
fi

OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | grep "idc.url"`;
if [ -z "$OLD_PROP" ]; then
    echo "Enter your IDC's URL: ";
    read IDC_URL;
    echo "idc.url=$IDC_URL" >> $CATALINA_HOME/shared/classes/server/oscars.properties
fi

OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | grep "notify.ws.broker.url"`;
if [ -z "$OLD_PROP" ]; then
    if [ -z "$IDC_URL" ]; then
        echo "Enter your IDC's URL: ";
         read IDC_URL;
    fi
    echo "notify.ws.broker.url=$IDC_URL""Notify" >> $CATALINA_HOME/shared/classes/server/oscars.properties
fi

OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | grep "perfsonar.topology_url"`;
if [ -z "$OLD_PROP" ]; then
    echo "-- Adding default topology service URL";
    echo "perfsonar.topology_url=http://packrat.internet2.edu:8012/perfSONAR_PS/services/topology" >> $CATALINA_HOME/shared/classes/server/oscars.properties
fi

OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | grep "notifybroker.pep.1=net.es.oscars.notify.ws.policy.IDCEventPEP"`;
if [ -z "$OLD_PROP" ]; then
    echo "-- Adding IDC PEP to notification broker";
    echo "notifybroker.pep.1=net.es.oscars.notify.ws.policy.IDCEventPEP" >> $CATALINA_HOME/shared/classes/server/oscars.properties
fi

OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | grep "pathfinder.pathMethod=terce"`;
if [ -n "$OLD_PROP" ]; then
    PS_ANS=0;
    while [ "$PS_ANS" == "0" ]; do
        echo -n "Would you like to use the new perfsonar pathfinder? [y/n]  ";
        read PS_ANS;
        if [ "$PS_ANS" != "y" ] && [ "$PS_ANS" != "Y" ] && [ "$PS_ANS" != "n" ] && [ "$PS_ANS" != "N" ]; then
            PS_ANS=0;
        fi
        if [ "$PS_ANS" = "y" ] || [ "$PS_ANS" = "Y" ]; then
            sed -i"" -e 's/pathfinder\.pathMethod=terce/pathfinder\.pathMethod=perfsonar,terce/g' $CATALINA_HOME/shared/classes/server/oscars.properties
        fi
    done
fi

WS_ANS=0;
OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | egrep "notify\.observer\.[0-9]+=net.es.oscars.notify.WSObserver"`;
if [ -z "$OLD_PROP" ]; then
    while [ "$WS_ANS" == "0" ]; do
        echo -n "Would you like to activate WS-Notifcations? [y/n]  ";
        read WS_ANS;
        if [ "$WS_ANS" != "y" ] && [ "$WS_ANS" != "Y" ] && [ "$WS_ANS" != "n" ] && [ "$WS_ANS" != "N" ]; then
            WS_ANS=0;
        fi
        if [ "$WS_ANS" = "y" ] || [ "$WS_ANS" = "Y" ]; then
            LAST_FOUND=0;
            COUNT=1;
            while [ $LAST_FOUND == 0 ]; do
                OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | egrep "^notify\.observer\.$COUNT=.+"`;
                if [ -z "$OLD_PROP" ]; then
                    LAST_FOUND=1;
                else
                    COUNT=`expr $COUNT + 1`;
                fi
            done
            echo "notify.observer.$COUNT=net.es.oscars.notify.WSObserver" >> $CATALINA_HOME/shared/classes/server/oscars.properties
        fi
    done    
fi

SERV_ANS=0;
OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | egrep "external\.service\.[0-9]+=subscribe"`;
if [ -z "$OLD_PROP" ]; then
    while [ "$SERV_ANS" == "0" ]; do
        echo -n "Would you like to subscribe to other IDCs notifications? [y/n]  ";
        read SERV_ANS;
        if [ "$SERV_ANS" != "y" ] && [ "$SERV_ANS" != "Y" ] && [ "$SERV_ANS" != "n" ] && [ "$SERV_ANS" != "N" ]; then
            SERV_ANS=0;
        fi
        if [ "$SERV_ANS" = "y" ] || [ "$SERV_ANS" = "Y" ]; then
            LAST_FOUND=0;
            COUNT=1;
            while [ $LAST_FOUND == 0 ]; do
                OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | egrep "^external\.service\.$COUNT=.+"`;
                if [ -z "$OLD_PROP" ]; then
                    LAST_FOUND=1;
                else
                    COUNT=`expr $COUNT + 1`;
                fi
            done
            echo "external.service.$COUNT=subscribe" >> $CATALINA_HOME/shared/classes/server/oscars.properties
        fi
    done    
fi

SERV_ANS=0;
OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | egrep "external\.service\.[0-9]+=topology"`;
if [ -z "$OLD_PROP" ]; then
    while [ "$SERV_ANS" == "0" ]; do
        echo -n "Would you like to register the local topology with the topology service? [y/n]  ";
        read SERV_ANS;
        if [ "$SERV_ANS" != "y" ] && [ "$SERV_ANS" != "Y" ] && [ "$SERV_ANS" != "n" ] && [ "$SERV_ANS" != "N" ]; then
            SERV_ANS=0;
        fi
        if [ "$SERV_ANS" = "y" ] || [ "$SERV_ANS" = "Y" ]; then
            LAST_FOUND=0;
            COUNT=1;
            while [ $LAST_FOUND == 0 ]; do
                OLD_PROP=`cat $CATALINA_HOME/shared/classes/server/oscars.properties | egrep "^external\.service\.$COUNT=.+"`;
                if [ -z "$OLD_PROP" ]; then
                    LAST_FOUND=1;
                else
                    COUNT=`expr $COUNT + 1`;
                fi
            done
            echo "external.service.$COUNT=topology" >> $CATALINA_HOME/shared/classes/server/oscars.properties
        fi
    done    
fi

if [ -f "$CATALINA_HOME/webapps/axis2/WEB-INF/lib/jdom.jar" ]; then
    echo "-- Found JDOM";
else
    JDOM_ANS=0;
    while [ "$JDOM_ANS" == "0" ]; do
        echo -n "May this script copy jdom.jar to $CATALINA_HOME/webapps/axis2/WEB-INF/lib/? [y/n]  ";
        read JDOM_ANS;
        if [ "$JDOM_ANS" != "y" ] && [ "$JDOM_ANS" != "Y" ] && [ "$JDOM_ANS" != "n" ] && [ "$JDOM_ANS" != "N" ]; then
            JDOM_ANS=0;
        fi
        if [ "$JDOM_ANS" = "y" ] || [ "$JDOM_ANS" = "Y" ]; then
            cp lib/jdom.jar $CATALINA_HOME/webapps/axis2/WEB-INF/lib/jdom.jar
        fi
    done
fi

# add neighbor brokers

echo "--- Upgrade changes complete";

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

echo "";
echo "##############################################################################";
echo "";
echo "UPGRADE NOTES";
echo "";
echo "You may now run ./do_install.sh to complete your upgrade.";
echo "";
echo "You also need to complete the following steps:";
echo "";
echo "   1. Create a user account for the local domain as described in section '7.3 Activation Notifications' of the DCNSS install document";
echo "";
echo "   2. Add the URL of each neighboring domain's NotificationBroker by running tools/utils/idc-serviceadd as described at the end of section '7.2 Making your IDC Aware of Other Domains' of the DCNSS install document";
echo "";
echo "   3. Associate each neighboring domain with an Institution by running tools/utils/idc-siteadd";
echo "";
echo "##############################################################################";



exit 0;


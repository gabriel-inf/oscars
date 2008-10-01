#!/bin/bash
echo "  ";
echo "--- Checking prerequisites...";
if [ -d src/net/es/oscars ]; then
    echo "    We seem to be in the correct directory";
else
    echo "";
    echo "Error. Run this script from its directory. ";
    exit 1;
fi

INSTALL_HOME=`pwd`;
IDC_HOSTNAME=`hostname`;

# Find Ant
if [ `which ant` ]; then
    echo "    Found ant";
else
    echo "";
    echo "Ant not found. Install and add ant to your PATH and rerun installer.";
    exit 1;
fi

# Find Tomcat
if [ -n "$CATALINA_HOME" ]; then
    echo "    Environment variable CATALINA_HOME is set to $CATALINA_HOME";
else
    echo "";
    echo "Environment variable CATALINA_HOME is not set. Download and install Apache Tomcat v. 5.5 and set the CATALINA_HOME environment variable to the install root directory.";
    exit 1;
fi

# Find environment variables
DOMAIN_FOUND=0;
if [ -n "$DOMAIN_HOME" ]; then
    echo "    Environment variable DOMAIN_HOME is set to $DOMAIN_HOME";
    DOMAIN_FOUND=1;
else
    echo "    Environment variable DOMAIN_HOME is not set. Continuing without it.";
fi

# Axis2 retrieve & build
bash conf/axis2/axis2_install.sh $INSTALL_HOME
if [ $? != 0 ]; then
    exit 1;
fi

echo "";
echo "";
echo "--- Your kit looks good.";
echo -n "- Input the hostname for this IDC. Leave blank for \"$IDC_HOSTNAME\": "
read READ_HOSTNAME;
if [ $READ_HOSTNAME ]; then
    IDC_HOSTNAME=READ_HOSTNAME;
fi
echo "    Using $IDC_HOSTNAME . ";
#set defaults but still inform users that they may change these in documentation
sed -i"" -e "s/idc\.url=.*/idc\.url=https:\/\/$IDC_HOSTNAME:8443\/axis2\/services\/OSCARS/g" conf/examples/server/oscars.properties
sed -i"" -e "s/notify\.ws\.broker\.url=.*/notify\.ws\.broker\.url=https:\/\/$IDC_HOSTNAME:8443\/axis2\/services\/OSCARSNotify/g" conf/examples/server/oscars.properties

READ_INSTALLDB=0;
while [ $READ_INSTALLDB == 0 ]; do
    echo -n "- Install databases y/n? "
    read READ_INSTALLDB;
    if [ "$READ_INSTALLDB" != "y" ] && [ "$READ_INSTALLDB" != "Y" ] && [ "$READ_INSTALLDB" != "n" ] && [ "$READ_INSTALLDB" != "N" ]; then
        READ_INSTALLDB=0;
    fi
done

INSTALL_DB=0;
if [ "$READ_INSTALLDB" == "y" ] || [ "$READ_INSTALLDB" == "Y" ]; then
    echo "    OK, will install databases.";
    INSTALL_DB=1;
fi

if [ $INSTALL_DB == 1 ]; then
    FOUND_MYSQL=0;
    if [ `which mysql` ]; then
        FOUND_MYSQL=`which mysql`;
        echo "    Found mysql client at $FOUND_MYSQL";
    else
        echo "    MySQL client not found. Add mysql to your PATH and rerun installer, or install databases manually.";
    fi


    if [ $FOUND_MYSQL != 0 ]; then
        MYSQL_PRIV_ACCESS=0;
        while [ $MYSQL_PRIV_ACCESS == 0 ]; do
            echo -n "- Input the MySQL server hostname. Leave blank for localhost: "
            read MYSQL_SERVER;
            if [ ! $MYSQL_SERVER ]; then
                MYSQL_SERVER="localhost";
            fi
             echo "    Using $MYSQL_SERVER . ";

            echo -n "- Input a privileged MySQL username on that host. Leave blank for root: "
            read MYSQL_PRIV_USERNAME;
            if [ ! "$MYSQL_PRIV_USERNAME" ]; then
                MYSQL_PRIV_USERNAME="root";
            fi
            echo "    Using $MYSQL_PRIV_USERNAME . ";

            echo -n "- Input the password for the privileged account: "
            stty -echo;
            read MYSQL_PRIV_PASSWORD;
            stty echo;
            echo "";

            `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="STATUS" > /dev/null 2> /dev/null`;
            if [ $? != 0 ]; then
                echo "    Could not connect. Check password and try again. Ctrl-C to exit.";
            else
                echo "    Privileged account access verified.";
                MYSQL_PRIV_ACCESS=1;
            fi
            echo "";
        done

        MYSQL_GOT_IDC_CREDENTIALS=0;

        while [ $MYSQL_GOT_IDC_CREDENTIALS == 0 ]; do

            echo "- Input a MySQL username the IDC will use to connect to the databases.";
            echo "  -- This name and password must match the hibernate.connection.username and password specified in oscars.properties."
            echo -n "  Leave blank for \"oscars\": "
            read MYSQL_IDC_USERNAME;
            if [ ! "$MYSQL_IDC_USERNAME" ]; then
                MYSQL_IDC_USERNAME="oscars";
            fi
            echo "    Using $MYSQL_IDC_USERNAME . ";

            echo -n "- Input the password for the IDC account: "
            stty -echo;
            read MYSQL_IDC_PASSWORD;
            stty echo;
            echo "";
            `mysql --user=$MYSQL_IDC_USERNAME --password="$MYSQL_IDC_PASSWORD" --host=$MYSQL_SERVER --execute="STATUS" > /dev/null 2> /dev/null`;
            if [ $? != 0 ]; then
                echo "    Could not connect with IDC credentials. Will grant IDC account privileges.";
                echo -n "- Input the password for the IDC account one more time and the user will be added: "
                stty -echo;
                read MYSQL_IDC_PASSWORD2;
                stty echo;
                if [ "$MYSQL_IDC_PASSWORD2" == "$MYSQL_IDC_PASSWORD" ]; then
                    MYSQL_GOT_IDC_CREDENTIALS=1;
                    echo "    Passwords match.";
                else
                    echo "    Passwords do not match. Starting over.";
                fi
                MYSQL_IDC_HAS_ACCESS=0;
            else
                echo "    IDC account access verified.";
                MYSQL_IDC_HAS_ACCESS=1;
                MYSQL_GOT_IDC_CREDENTIALS=1;
                sed -i"" -e "s/hibernate\.connection\.username=.*/hibernate\.connection\.username=$MYSQL_IDC_USERNAME/g" conf/examples/server/oscars.properties
                sed -i"" -e "s/hibernate\.connection\.password=.*/hibernate\.connection\.password=$MYSQL_IDC_PASSWORD/g" conf/examples/server/oscars.properties
            fi
        done
        echo "";


# NOTE: Currenty hardcoded, uncomment snippet below to interactively set

            MYSQL_AAA_DBNAME="aaa";
            MYSQL_BSS_DBNAME="bss";
            MYSQL_NOTIFY_DBNAME="notify";
            MYSQL_TESTBSS_DBNAME="testbss";
            MYSQL_TESTAAA_DBNAME="testaaa";

#		echo -n "- Input the BSS database name. Leave blank for \"bss\": "
#		read MYSQL_BSS_DBNAME;
#		if [ ! $MYSQL_BSS_DBNAME ]; then

#			MYSQL_BSS_DBNAME="bss";
#		fi
#		echo "    Using $MYSQL_BSS_DBNAME . ";

#		echo -n "- Input the AAA database name. Leave blank for \"aaa\": "
#		read MYSQL_AAA_DBNAME;
#		if [ ! $MYSQL_AAA_DBNAME ]; then
#			MYSQL_AAA_DBNAME="aaa";
#		fi
#		echo "    Using $MYSQL_AAA_DBNAME . ";
#		echo "";


        echo -n "- Got all information. Press return to create the databases...";
        read THROWAWAY;



        echo "    Creating databases $MYSQL_BSS_DBNAME, $MYSQL_AAA_DBNAME, $MYSQL_NOTIFY_DBNAME, $MYSQL_TESTBSS_DBNAME, $MYSQL_TESTAAA_DBNAME ";
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="CREATE DATABASE $MYSQL_BSS_DBNAME" > /dev/null 2> /dev/null`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="CREATE DATABASE $MYSQL_AAA_DBNAME" > /dev/null 2> /dev/null`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="CREATE DATABASE $MYSQL_NOTIFY_DBNAME" > /dev/null 2> /dev/null`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="CREATE DATABASE $MYSQL_TESTBSS_DBNAME" > /dev/null 2> /dev/null`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="CREATE DATABASE $MYSQL_TESTAAA_DBNAME" > /dev/null 2> /dev/null`;
        echo "    Databases created...";
        echo "    Initializing databases...";
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER -D$MYSQL_BSS_DBNAME < sql/bss/createTables.sql`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER -D$MYSQL_AAA_DBNAME < sql/aaa/populateDefaults.sql`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER -D$MYSQL_NOTIFY_DBNAME < sql/notify/createTables.sql`;

        echo "    Databases initialized...";

        echo "    Granting privileges to IDC account...";
        if [ "$MYSQL_SERVER" == "localhost" ]; then
            MYSQL_IDC_HOSTNAME="localhost";
        else
            MYSQL_IDC_HOSTNAME=IDC_HOSTNAME;
        fi
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="GRANT ALL PRIVILEGES ON $MYSQL_BSS_DBNAME.* TO '$MYSQL_IDC_USERNAME'@'$MYSQL_IDC_HOSTNAME' IDENTIFIED BY '$MYSQL_IDC_PASSWORD'"`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="GRANT ALL PRIVILEGES ON $MYSQL_AAA_DBNAME.* TO '$MYSQL_IDC_USERNAME'@'$MYSQL_IDC_HOSTNAME' IDENTIFIED BY '$MYSQL_IDC_PASSWORD'"`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="GRANT ALL PRIVILEGES ON $MYSQL_NOTIFY_DBNAME.* TO '$MYSQL_IDC_USERNAME'@'$MYSQL_IDC_HOSTNAME' IDENTIFIED BY '$MYSQL_IDC_PASSWORD'"`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="GRANT ALL PRIVILEGES ON $MYSQL_TESTBSS_DBNAME.* TO '$MYSQL_IDC_USERNAME'@'$MYSQL_IDC_HOSTNAME' IDENTIFIED BY '$MYSQL_IDC_PASSWORD'"`;
        `mysql --user=$MYSQL_PRIV_USERNAME --password="$MYSQL_PRIV_PASSWORD" --host=$MYSQL_SERVER --execute="GRANT ALL PRIVILEGES ON $MYSQL_TESTAAA_DBNAME.* TO '$MYSQL_IDC_USERNAME'@'$MYSQL_IDC_HOSTNAME' IDENTIFIED BY '$MYSQL_IDC_PASSWORD'"`;
        echo "    IDC account authorized.";

        if [ $MYSQL_SERVER == "localhost" ]; then
            MYSQL_SERVER="";
        fi

        echo "    Modifying conf/server/aaa.cfg.xml ...";
        sed -e "s/jdbc:mysql:\/\/\/aaa/jdbc:mysql:\/\/$MYSQL_SERVER\/$MYSQL_AAA_DBNAME/g" conf/server/aaa.cfg.xml > conf/server/aaaTemp.cfg.xml;
        mv conf/server/aaaTemp.cfg.xml conf/server/aaa.cfg.xml;

        echo "   Modifying conf/server/bss.cfg.xml ...";
        sed -e "s/jdbc:mysql:\/\/\/bss/jdbc:mysql:\/\/$MYSQL_SERVER\/$MYSQL_BSS_DBNAME/g" conf/server/bss.cfg.xml > conf/server/bssTemp.cfg.xml;
        mv conf/server/bssTemp.cfg.xml conf/server/bss.cfg.xml;
    fi
fi

echo -n "- Press return to build IDC...";
read PRESS_RETURN;

echo "";
echo "";
echo "--- Cleaning before compile...";
ant clean;
echo "";
echo "";
echo "--- Compiling...";
ant compile;


echo "";
echo "";
echo "--- IDC built.";

echo "";
echo "##############################################################################";
echo "";
echo "INSTALLATION NOTES";
if [ "$DOMAIN_FOUND" == "1" ]; then
    echo "- If you haven't done so already, you must create or import your X.509 certificates. ";
    echo "- You must also edit $DOMAIN_HOME/server/oscars.properties .";
    echo "  Please refer to the documentation for instructions.";
fi
echo "";
echo "OSCARS built. Please run do_install.sh to complete this installation.";
echo "";
echo "##############################################################################";

exit 0;

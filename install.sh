#!/bin/sh


if [ -d src/net/es/oscars ]; then
	echo "We seem to be in the correct directory";
else
	echo "Run this script from its directory. ";
	exit 1;
fi

INSTALL_HOME=`pwd`;

echo "Checking prerequisites.";

if [ `which ant` ]; then
	echo "Found ant";
else
	echo "Ant not found. Install and add ant to your PATH and rerun installer.";
fi


if [ -n "$CATALINA_HOME" ]; then
	echo "Environment variable CATALINA_HOME is set to $CATALINA_HOME";
else
	echo "Environment variable CATALINA_HOME is not set. Download and install Apache Tomcat v. 5.5 and set the CATALINA_HOME environment variable to the install root directory.";
	exit 1;
fi

DOMAIN_FOUND=0;
if [ -n "$DOMAIN_HOME" ]; then
	echo "Environment variable DOMAIN_HOME is set to $DOMAIN_HOME";
	DOMAIN_FOUND=1;
else
	echo "Environment variable DOMAIN_HOME is not set. Continuing without it.";
fi

if [ -f ./lib/jta.jar ]; then
	echo "Found jta.jar under lib";
else
	echo "jta.jar not found. Download from Sun's website and copy it under ./lib .";
	exit 1;
fi


DEPLOYED_AXIS=0;
FOUND_AXIS=0;
if  [ -f $CATALINA_HOME/webapps/axis2/WEB-INF/lib/mail-1.4.jar ]; then
	FOUND_AXIS="$CATALINA_HOME/webapps/axis2/WEB-INF/lib/";
	DEPLOYED_AXIS=1;
	echo "Found axis2 library at $FOUND_AXIS";
elif [ -f lib/axis2/mail-1.4.jar ]; then
	FOUND_AXIS="lib/axis2";
	echo "Found axis2 library at $FOUND_AXIS . Axis2 webapp is not deployed.";
else
	echo "Axis2 library not found.";
fi

FOUND_RAMPART=0;
if [ -f $FOUND_AXIS/rampart-core-1.3.jar ]; then
	FOUND_RAMPART="$FOUND_AXIS/rampart-core-1.3.jar";
	echo "Found rampart library at $FOUND_RAMPART";
else
	echo "Rampart library not found.";
fi


# Axis2 build

FOUND_RAMPART=0;
if [ $FOUND_AXIS == 0 ] || [ $FOUND_RAMPART == 0 ]; then
	READ_BUILDAXIS=0;
	while [ $READ_BUILDAXIS == 0 ]; do
		echo "Axis2 library not detected. Should I build it for you y/n?"
		read READ_BUILDAXIS;
		if [ "$READ_BUILDAXIS" != "y" ] && [ "$READ_BUILDAXIS" != "Y" ] && [ "$READ_BUILDAXIS" != "n" ] && [ "$READ_BUILDAXIS" != "N" ]; then
			READ_BUILDAXIS=0;
		fi
	done

	BUILD_AXIS=0;
	if [ "$READ_BUILDAXIS" == "y" ] || [ "$READ_BUILDAXIS" == "Y" ]; then
		echo "OK, will build Axis2 for you.";
		BUILD_AXIS=1;
	else
		echo "Cannot continue without Axis2 library.";
		exit 1;
	fi
fi


if [ $BUILD_AXIS ]; then
	if [ ! -d dists ]; then
		mkdir dists;
	fi
	if [ ! -d dists/axis2-1.3 ]; then
		if [ ! -f dists/axis2-1.3-bin.zip ]; then
			`wget -P dists http://www.devlib.org/apache/ws/axis2/1_3/axis2-1.3-bin.zip`;
		fi
		`unzip -qq -d dists dists/axis2-1.3-bin.zip`;
	fi
	if [ ! -d dists/rampart-1.3 ]; then
		if [ ! -f dists/rampart-1.3.zip ]; then
			`wget -P dists http://apache.ziply.com/ws/rampart/1_3/rampart-1.3.zip`;
		fi
		`unzip -qq -d dists dists/rampart-1.3.zip`;
	fi
	`sed -e 's/CHANGE_THIS/\.\./g' conf/axis2/build.xml > dists/axis2-1.3/webapp/build.xml`;
	cd dists/axis2-1.3/webapp;
	ant;
	cd $INSTALL_HOME;
fi



if [ $FOUND_AXIS == 0 ] || [ $FOUND_RAMPART == 0 ]; then
	READ_INSTALLAXIS=0;
	if [ ! $DEPLOYED_AXIS ]; then
		while [ $READ_INSTALLAXIS == 0 ]; do
			echo "Axis2 is not installed. Should I do this for you y/n?"
			read READ_AXIS;
			if [ "$READ_INSTALLAXIS" != "y" ] && [ "$READ_INSTALLAXIS" != "Y" ] && [ "$READ_INSTALLAXIS" != "n" ] && [ "$READ_INSTALLAXIS" != "N" ]; then
				READ_INSTALLAXIS=0;
			fi
		done
	fi


	INSTALL_AXIS=0;
	if [ "$READ_INSTALLAXIS" == "y" ] || [ "$READ_INSTALLAXIS" == "Y" ]; then
		echo "OK, will install Axis2 for you.";
		INSTALL_AXIS=1;
	fi
fi





echo "Your kit looks good.";



READ_INSTALLDB=0;
while [ $READ_INSTALLDB == 0 ]; do
	echo "Install databases y/n?"
	read READ_INSTALLDB;
	if [ "$READ_INSTALLDB" != "y" ] && [ "$READ_INSTALLDB" != "Y" ] && [ "$READ_INSTALLDB" != "n" ] && [ "$READ_INSTALLDB" != "N" ]; then
		READ_INSTALLDB=0;
	fi
done

INSTALL_DB=0;
if [ "$READ_INSTALLDB" == "y" ] || [ "$READ_INSTALLDB" == "Y" ]; then
	echo "OK, will install databases.";
	INSTALL_DB=1;
fi


FOUND_MYSQL=0;
if [ `which mysql` ]; then
	FOUND_MYSQL=`which mysql`;
	echo "Found mysql client at $FOUND_MYSQL";
else
	echo "MySQL client not found. Add mysql to your PATH and rerun installer, or install databases manually.";
fi

echo "Database setup."








echo "OK";
exit 0;
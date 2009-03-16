#!/bin/bash 

#Verify has unzip
if [ `which unzip` ]; then
    echo "unzip found.";
else
    echo "unzip command not found in PATH. Please install unzip command so Axis2 can be unpacked.";
    exit 1;
fi
#Verify has wget
if [ `which wget` ]; then
    echo "wget found.";
else
    echo "wget command not found in PATH. Please install wget command from  ftp://ftp.gnu.org/gnu/wget/ ";
    exit 1;
fi

# Detect Axis, Rampart
INSTALL_HOME=$1;
FOUND_AXIS1_3=0;
FOUND_AXIS1_4=0;
DEPLOYED_AXIS1_3=0;
DEPLOYED_AXIS1_4=0;
if  [ -f $CATALINA_HOME/webapps/axis2/WEB-INF/lib/axis2-kernel-1.3.jar ]; then
      FOUND_AXIS1_3="$CATALINA_HOME/webapps/axis2/WEB-INF/lib/";
      DEPLOYED_AXIS1_3=1;
      echo "    Found old axis2-1.3 library at $FOUND_AXIS1_3";
    elif [ -f $CATALINA_HOME/webapps/axis2/WEB-INF/lib/axis2-kernel-1.4.1.jar ] ; then
        FOUND_AXIS1_4="$CATALINA_HOME/webapps/axis2/WEB-INF/lib/";
      echo "    Found axis2-1.4.1 library at $FOUND_AXIS1_4";
      DEPLOYED_AXIS1_4=1;
    elif [ -f lib/axis2/axis2-kernel-1.4.1.jar ]; then
      FOUND_AXIS1_4="lib/axis2";
      echo "    Found axis2 library at $FOUND_AXIS1_4. Axis2 webapp is not deployed.";
    else
      echo "    Axis2 library not found.";
fi

FOUND_RAMPART1_4=0;

if [ -f $FOUND_AXIS1_4/rampart-core-SNAPSHOT.jar ]; then
  FOUND_RAMPART1_4="$FOUND_AXIS1_4/rampart-core-SNAPSHOT.jar";
  echo "    Found rampart library at $FOUND_RAMPART1_4";
else
  echo "    Rampart library not found.";
fi


# DEBUG STATEMENT
# FOUND_RAMPART=0;

# Build Axis2
if [ $FOUND_AXIS1_4 == 0 ] || [ $FOUND_RAMPART1_4 == 0 ] || [ $DEPLOYED_AXIS1_4 == 0 ]; then
  READ_BUILDAXIS=0;
  while [ $READ_BUILDAXIS == 0 ]; do
      echo "";
    echo -n "- Axis2-1.4.1 with Rampart-SNAPSHOT installation not detected. Should I build it for you y/n? "
    read READ_BUILDAXIS;
    if [ "$READ_BUILDAXIS" != "y" ] && [ "$READ_BUILDAXIS" != "Y" ] && [ "$READ_BUILDAXIS" != "n" ] && [ "$READ_BUILDAXIS" != "N" ]; then
      READ_BUILDAXIS=0;
    fi
  done
    BUILD_AXIS=0;
  if [ "$READ_BUILDAXIS" == "y" ] || [ "$READ_BUILDAXIS" == "Y" ]; then
    echo "    OK, will build Axis2-1.4.1 for you.";
    BUILD_AXIS=1;
  else
        echo "";
    echo "Cannot continue without Axis2 library. You can download and copy required files into lib/axis2, then run installer again.";
    exit 1;
  fi
fi


# DEBUG STATEMENT
# DEPLOYED_AXIS=0;

# Ask to deploy
READ_DEPLOYAXIS=0;
if [ $DEPLOYED_AXIS1_4 == 0 ]; then
  while [ $READ_DEPLOYAXIS == 0 ]; do
    echo "";
    echo -n "- Axis2-1.4.1 is not deployed. Should I do this for you y/n? "
    read READ_DEPLOYAXIS;
    if [ "$READ_DEPLOYAXIS" != "y" ] && [ "$READ_DEPLOYAXIS" != "Y" ] && [ "$READ_DEPLOYAXIS" != "n" ] && [ "$READ_DEPLOYAXIS" != "N" ]; then
      READ_DEPLOYAXIS=0;
    fi
  done
fi


DEPLOY_AXIS=0;
if [ "$READ_DEPLOYAXIS" == "y" ] || [ "$READ_DEPLOYAXIS" == "Y" ]; then
  echo "    OK, will deploy Axis2 for you.";
  DEPLOY_AXIS=1;
fi

if [ $BUILD_AXIS ]; then
  if [ ! -d dists ]; then
    mkdir dists;
  fi
  if [ ! -d dists/axis2-1.4.1 ]; then
    if [ ! -f dists/axis2-1.4.1-bin.zip ]; then
      echo "--- Downloading Axis2...";
      `wget -P dists http://www.eng.lsu.edu/mirrors/apache/ws/axis2/1_4_1/axis2-1.4.1-bin.zip`;
        if [ $? != 0 ]; then
            exit 1;
        fi
    fi
    echo "    Axis downloaded. Unzipping...";
    `unzip -qq -d dists dists/axis2-1.4.1-bin.zip`;
  else
    echo "    Axis2 already downloaded and unzipped";
  fi
  if [ ! -d dists/rampart-SNAPSHOT ]; then
    if [ ! -f dists/rampart-SNAPSHOT.tar.gz ]; then
      echo "    Downloading Rampart...";
      # `wget -P dists http://apache.ziply.com/ws/rampart/1_4/rampart-dist-1.4-bin.zip  `;
      cd dists
            `wget --no-check-certificate https://wiki.internet2.edu/confluence/download/attachments/19074/rampart-SNAPSHOT.tar.gz`
      if [ $? != 0 ]; then
                exit 1;
            fi
    fi
    echo "    Rampart  downloaded. Unzipping...";
    
    gunzip rampart-SNAPSHOT.tar.gz;
    if [ $? != 0 ]; then
        exit 1;
    fi
    tar -xvf rampart-SNAPSHOT.tar;
    if [ $? != 0 ]; then
        exit 1;
    fi
    cd ../
    cp dists/rampart-SNAPSHOT/lib/rampart-core-SNAPSHOT.jar lib/axis2/rampart-core-SNAPSHOT.jar;
    if [ $? != 0 ]; then
        exit 1;
    fi
    echo "--- Downloading bouncyCastle crypto provider "
      `wget -P dists/rampart-SNAPSHOT/lib http://www.bouncycastle.org/download/bcprov-jdk15-140.jar `;
      if [ $? != 0 ]; then
        exit 1;
    fi
  fi

  
  echo "--- Copying OSCARS specific libs to Axis2...";
  cp lib/antlr* dists/axis2-1.4.1/lib;
  cp lib/jdom.jar dists/axis2-1.4.1/lib;
  cp conf/logging/axis2.log4j.properties dists/axis2-1.4.1/log4j.properties
  cp conf/logging/axis2.log4j.properties dists/axis2-1.4.1/webapp/WEB-INF/classes/log4j.properties
    echo "--- Building Axis2 with Rampart...";
  `sed -e 's/CHANGE_THIS/\.\./g' tools/axis2/build.xml > dists/axis2-1.4.1/webapp/build.xml`;
  cd dists/axis2-1.4.1/webapp;
  ant;
  cd $INSTALL_HOME;
  echo "    Done building Axis2.";
  echo "";
fi


# Axis2 deploy
if [ $DEPLOY_AXIS != 0 ]; then
  echo "    Deploying Axis2 webapp...";
  FOUND_AXIS2_WAR=0;
  if [ -f "dists/axis2-1.4.1/dist/axis2.war" ]; then
     FOUND_AXIS2_WAR="dists/axis2-1.4.1/dist/axis2.war";
    echo "    Found $FOUND_AXIS2_WAR";
  else
    echo "Input path to axis2.war";
    read FOUND_AXIS2_WAR;
    if [ -f $FOUND_AXIS2_WAR ]; then
      echo "    Found $FOUND_AXIS2_WAR";
    else
      echo "    File $FOUND_AXIS2_WAR not found. Exiting.";
      exit 1;
    fi
  fi

  echo "";
  echo "    Stopping Tomcat...";
  $CATALINA_HOME/bin/shutdown.sh > /dev/null;
  echo "";
  if [ $DEPLOYED_AXIS1_3 == 1 ]; then
      echo "undeploying axis2-1.3 ...";
      rm $CATALINA_HOME/webapps/axis2.war;
      rm -rf $CATALINA_HOME/webapps/axis2;
      rm $CATALINA_HOME/shared/lib/*;
      rm -rf $CATALINA_HOME/shared/classes/repo/modules/*
  fi
  echo "    Copying axis2.war ...";
  cp $FOUND_AXIS2_WAR $CATALINA_HOME/webapps/;
  echo "";
  echo "    Restarting Tomcat...";
  $CATALINA_HOME/bin/startup.sh;
  echo "";
fi

exit 0;

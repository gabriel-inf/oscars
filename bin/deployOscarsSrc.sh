#!/bin/sh
# This script will check out the latest version of the OSCARS 0.6 src tree into $OSCARS_DIST
# Create and initialize the authN, authZ and rm sql databases
# Copy template configuration files to non-template versions
# Install the configuration files to $OSCARS_HOME

if [ -z $OSCARS_DIST ]; then
        echo "Please set OSCARS_DIST to the directory in which to store the OSCARS sources"
        exit 1;
fi
if [ -z $OSCARS_HOME ]; then
        echo "Please set OSCARS_HOME to the OSCARS deployment directory"
        exit 1;
fi
if [ ! -d $OSCARS_DIST ]; then
        echo "Creating $OSCARS_DIST";
        mkdir -p $OSCARS_DIST;
fi
if [ ! -d $OSCARS_HOME ]; then
        echo "Creating $OSCARS_HOME";
        mkdir -p $OSCARS_HOME;
fi
cd $OSCARS_DIST

OSCARS_REPO="https://oscars.es.net/repos/oscars/branches/0.6"
OSCARS_SDK="https://oscars.es.net/repos/oscars/releases/oscars-0.6-sdk-01.11.11"
SQLRoot=root
SH=sh
SVN=svn

if [ ! -z `which mysql` ]; then
    SQL=mysql
elif [ ! -z `which mysql5` ]; then
    SQL=mysql5
else
    echo mysql not found on path
    echo "Please refer to GETTINGSTARTED in $OSCARS_DIST"
    exit -1
fi
echo using $SQL

if [ -z `which svn` ]; then
    echo svn not found on path
    echo "Please refer to GETTINGSTARTED in $OSCARS_DIST"
    exit -1
fi
echo "Do you wish to checkout sources? [y/n] "
read ans
if [ $ans == "y" ]; then
  echo Do you wish to checkout latest OSCARS 0.6 Sources from $OSCARS_REPO or
  echo Do you wish to checkout latest OSCARS SDK Release from $OSCARS_SDK
  echo Please enter 1 for OSCARS 0.6 Sources, 2 for OSCARS SDK Release
  read ans1
  if [ $ans1 == "1" ]; then
     echo checking out latest version of OSCARS 0.6 Sources
     svn co $OSCARS_REPO $OSCARS_DIST
  elif [ $ans1 == "2" ]; then
     echo checking out latest version of OSCARS SDK Release
     svn co $OSCARS_SDK $OSCARS_DIST
  fi   
fi

MYSQL_VERSION=$(echo $($SQL --version) | awk '$1~/mysql/{print substr($5,1,1)}')
if [ $MYSQL_VERSION -ge 5 ]; then
   echo "Installed Mysql is appropriate for OSCARS"
else 
   echo "OPTIONAL: Existing Mysql is not version 5 or above. Please consider Installing Mysql 5"
fi

ans=n
echo "do you want to create the mysql tables? [y|n] "
read ans
if [ $ans == "y" ]; then
echo "Creating mysql tables for OSCARS"
echo "Please enter Mysql root password"
read -s passwd

if [ -z $passwd ]; then
    echo "WARNING: Mysql root password is empty. It is not secure to leave the password empty"
    echo "Please set the Mysql root password using the following command"
    echo "/usr/bin/mysqladmin -u root password 'new-password'"
fi

echo Creating mysql tables
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/bin/initOscars.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/authN/sql/createTables.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/authN/sql/populateDefaults.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/authZ/sql/createTables.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/authZ/sql/populateDefaults.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/resourceManager/sql/createTables.sql
fi
echo "Copying config files from $OSCARS_DIST to $OSCARS_HOME"
 
cp $OSCARS_DIST/authN/config/authN.HTTP.yaml.template $OSCARS_DIST/authN/config/authN.HTTP.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authN/config/authN.HTTP.yaml.template $OSCARS_DIST/authN/config/authN.HTTP.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/authN/config/authN.SSL.yaml.template $OSCARS_DIST/authN/config/authN.SSL.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authN/config/authN.SSL.yaml.template $OSCARS_DIST/authN/config/authN.SSL.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/authN/config/authN.TESTING.yaml.template $OSCARS_DIST/authN/config/authN.TESTING.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authN/config/authN.TESTING.yaml.template $OSCARS_DIST/authN/config/authN.TESTING.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/authZ/config/authZ.HTTP.yaml.template $OSCARS_DIST/authZ/config/authZ.HTTP.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authZ/config/authZ.HTTP.yaml.template $OSCARS_DIST/authZ/config/authZ.HTTP.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/authZ/config/authZ.SSL.yaml.template $OSCARS_DIST/authZ/config/authZ.SSL.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authZ/config/authZ.SSL.yaml.template $OSCARS_DIST/authZ/config/authZ.SSL.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/authZ/config/authZ.TESTING.yaml.template $OSCARS_DIST/authZ/config/authZ.TESTING.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authZ/config/authZ.TESTING.yaml.template $OSCARS_DIST/authZ/config/authZ.TESTING.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/coordinator/config/pce-configuration-http-template.xml $OSCARS_DIST/coordinator/config/pce-configuration-http.xml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/coordinator/config/pce-configuration-http-template.xml $OSCARS_DIST/coordinator/config/pce-configuration-http.xml failed";
   exit 1;
fi
cp $OSCARS_DIST/coordinator/config/pce-configuration-ssl-template.xml $OSCARS_DIST/coordinator/config/pce-configuration-ssl.xml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/coordinator/config/pce-configuration-ssl-template.xml $OSCARS_DIST/coordinator/config/pce-configuration-ssl.xml failed";
   exit 1;
fi
cp $OSCARS_DIST/topoBridge/config/config.HTTP.yaml.template $OSCARS_DIST/topoBridge/config/config.HTTP.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/topoBridge/config/config.HTTP.yaml.template $OSCARS_DIST/topoBridge/config/config.HTTP.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/topoBridge/config/config.SSL.yaml.template $OSCARS_DIST/topoBridge/config/config.SSL.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/topoBridge/config/config.SSL.yaml.template $OSCARS_DIST/topoBridge/config/config.SSL.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/resourceManager/config/config.HTTP.yaml.template $OSCARS_DIST/resourceManager/config/config.HTTP.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/resourceManager/config/config.HTTP.yaml.template $OSCARS_DIST/resourceManager/config/config.HTTP.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/resourceManager/config/config.SSL.yaml.template $OSCARS_DIST/resourceManager/config/config.SSL.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/resourceManager/config/config.SSL.yaml.template $OSCARS_DIST/resourceManager/config/config.SSL.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/resourceManager/config/config.TESTING.yaml.template $OSCARS_DIST/resourceManager/config/config.TESTING.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/resourceManager/config/config.TESTING.yaml.template $OSCARS_DIST/resourceManager/config/config.TESTING.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/wbui/config/config.HTTP.yaml.template $OSCARS_DIST/wbui/config/config.HTTP.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/wbui/config/config.HTTP.yaml.template $OSCARS_DIST/wbui/config/config.HTTP.yaml failed";
   exit 1;
fi
cp $OSCARS_DIST/wbui/config/config.SSL.yaml.template $OSCARS_DIST/wbui/config/config.SSL.yaml;
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/wbui/config/config.SSL.yaml.template $OSCARS_DIST/wbui/config/config.SSL.yaml failed";
   exit 1;
fi
ans="n"
echo "do you want to edit the mysql oscars password or change any of the service ports? [y|n] "
read ans
if [ $ans == "y" ]; then
    echo "when you are done editing, run $OSCARS_DIST/bin/exportconfig"
    echo exiting
    exit 0
fi

exit 0

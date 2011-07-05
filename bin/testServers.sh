#!/bin/sh

printUsage () {
   echo "usage testservers <CONTEXT>"
   echo "<context> is one of: PRODUCTION|pro UNITTEST|test DEVELOPMENT|dev SDK|sdk"
   exit 1
}
case $# in
  0)
   printUsage;;
esac

CONTEXT=$1
case $1 in
    d|D|dev|DEV) CONTEXT="DEVELOPMENT";;
    p|P|pro|PRO) CONTEXT="PRODUCTION";;
    t|T|test|TEST) CONTEXT="UNITTEST";;
    s|S|sdk) CONTEXT="SDK";;
esac

if [ "$CONTEXT" == "PRODUCTION" ] || [ "$CONTEXT" == "UNITTEST" ] || [ "$CONTEXT" == "DEVELOPMENT" ] || [ "$CONTEXT" == "SDK" ]; then
    echo "testing for servers running in $CONTEXT context";
else
    echo "CONTEXT  $CONTEXT is not recognized"
    printUsage
fi

testService() {
    Config=$(sh $OSCARS_DIST/bin/parseManifest.sh $Service $CONTEXT $Directory)
    Service=$(echo $Config | awk -F/ '$1~//{print $2}')
    Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
    Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
    if [ "$Conf" == "conf" ]; then
          #echo "using configuration  $OSCARS_HOME/$Service/$Conf/$Yaml"
          port=$(awk -F: '/soap/,/public/ $1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
    elif [ "$Conf" == "config" ]; then
          #echo "using configuration  $OSCARS_DIST/$Directory/$Conf/$Yaml"
          port=$(awk -F: '/soap/,/public/ $1~/publishTo/{print $4}' $OSCARS_DIST/$Directory/$Conf/$Yaml)
    fi
    port=$(echo $port | sed "s/[^0-9]//g")
    echo "$Service port is $port"
    porttest1=`netstat -na | grep tcp | grep LISTEN | grep "[:|\.]$port "`
    if [ -z "$porttest1" ]; then
         echo "$Service is not running";
         echo "Please restart $Service using startServers.sh $CONTEXT $ShortName";
         echo "-----------------------------------------------------------";
    else
         echo "$Service is running";
         echo "-----------------------------------------------------------";
    fi
}

Service="AuthNService"
ShortName="authN"
Directory="authN"
testService

Service="AuthZService"
ShortName="authZ"
Directory="authZ"
testService

Service="OSCARSService"
ShortName="api"
Directory="api"
testService

Service="CoordService"
ShortName="coord"
Directory="coordinator"
testService

Service="ResourceManagerService"
ShortName="rm"
Directory="resourceManager"
testService

Service="TopoBridgeService"
ShortName="topoBridge"
Directory="topoBridge"
testService

Service="NullAggregator"
ShortName="nullAgg"
Directory="pce"
testService

Service="PSSService"
ShortName="PSS"
Directory="stubPSS"
testService

Service="ConnectivityPCE"
ShortName="connPCE"
Directory="connectivityPCE"
testService

Service="BandwidthPCE"
ShortName="bwPCE"
Directory="bandwidthPCE"
testService

Service="DijkstraPCE"
ShortName="dijPCE"
Directory="dijkstraPCE"
testService

Service="VlanPCE"
ShortName="vlanPCE"
Directory="vlanPCE"
testService

Service="WSNBrokerService"
ShortName="wsnbroker"
Directory="wsnbroker"
testService

Service="NotificationBridgeService"
ShortName="notificationBridge"
Directory="notificationBridge"
testService

#WBUI servie gets its port from jetty.xml
Config=$(sh $OSCARS_DIST/bin/parseManifest.sh WBUIService $CONTEXT wbui jetty.xml)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
     port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
     port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
echo "WBUI port is $port"
porttest11=`netstat -na | grep LISTEN | grep "[:|\.]$port "`
if [ -z "$porttest11" ]; then
     echo "WBUI is not running";
     echo "Please restart WBUI using startServers.sh $CONTEXT wbui";
     echo "-----------------------------------------------------------";
else
     echo "WBUI is running";
     echo "-----------------------------------------------------------";
fi







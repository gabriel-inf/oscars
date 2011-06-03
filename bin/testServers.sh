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

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh authN $CONTEXT authN)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest1=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest1" ]; then
     echo "AuthN is not running";
     echo "Please restart AuthN using startServers.sh $CONTEXT authN";
     echo "-----------------------------------------------------------";
else 
     echo "AuthN is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh authZ $CONTEXT authZ)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest2=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest2" ]; then
     echo "AuthZ is not running";
     echo "Please restart AuthZ using startServers.sh $CONTEXT authZ";
     echo "-----------------------------------------------------------";
else
     echo "AuthZ is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh api $CONTEXT api)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/soap/,$1~/public/ $1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/soap/,$1~/public/ $1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest3=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest3" ]; then
     echo "OSCARSService (Api) is not running";
     echo "Please restart OSCARSService (Api) using startServers.sh $CONTEXT api";
     echo "-----------------------------------------------------------";
else
     echo "OSCARSService (Api) is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh coordinator $CONTEXT coordinator)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest4=`netstat -na | grep LISTEN | grep $port` 
if [ -z "$porttest4" ]; then
     echo "Coordinator is not running";
     echo "Please restart Coordinator using startServers.sh $CONTEXT coord";
     echo "-----------------------------------------------------------";
else
     echo "Coordinator is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh resourceManager $CONTEXT resourceManager)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest5=`netstat -na | grep LISTEN | grep $port` 
if [ -z "$porttest5" ]; then
     echo "ResourceManager is not running";
     echo "Please restart ResourceManager using startServers.sh $CONTEXT rm";
     echo "-----------------------------------------------------------";
else
     echo "ResourceManager is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh topoBridge $CONTEXT topoBridge)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest6=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest6" ]; then
     echo "TopoBridge is not running";
     echo "Please restart TopoBridge using startServers.sh $CONTEXT topoBridge";
     echo "-----------------------------------------------------------";
else
     echo "TopoBridge is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh nullPCE $CONTEXT pce)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port7=$(echo $port | sed "s/[^0-9]//g")
porttest7=`netstat -na | grep LISTEN | grep $port7`
if [ -z "$porttest7" ]; then
     echo "NullPCE is not running";
     echo "Please restart NullPCE using startServers.sh $CONTEXT nullPCE";
     echo "-----------------------------------------------------------";
else
     echo "NullPCE is running";   
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh nullAGG $CONTEXT pce)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest8=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest8" ]; then
     echo "NullAgg is not running";
     echo "Please restart NullAgg using startServers.sh $CONTEXT nullAGG";
     echo "-----------------------------------------------------------";
else
     echo "NullAgg is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh PSSService $CONTEXT stubPSS)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
     port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
     port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest9=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest9" ]; then
     echo "StubPSS is not running";
     echo "Please restart stubPSS using startServers.sh $CONTEXT stubPSS";
    echo "-----------------------------------------------------------";
else
     echo "StubPSS is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh lookup $CONTEXT lookup)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest10=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest10" ]; then
      echo "Lookup is not running";
      echo "Please restart Lookup using startServers.sh $CONTEXT lookup";
      echo "-----------------------------------------------------------";
else
      echo "Lookup is running"; 
      echo "-----------------------------------------------------------";
fi

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
porttest11=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest11" ]; then
     echo "WBUI is not running";
     echo "Please restart WBUI using startServers.sh $CONTEXT wbui";
     echo "-----------------------------------------------------------";
else
     echo "WBUI is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh connectivityPCE $CONTEXT connectivityPCE)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest12=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest12" ]; then
     echo "Connectivtiy PCE is not running";   
     echo "Please restart Connectivtiy PCE using startServers.sh $CONTEXT connPCE";
     echo "-----------------------------------------------------------";
else
     echo "Connectivity PCE is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh bandwidthPCE $CONTEXT bandwidthPCE)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
     port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest13=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest13" ]; then
     echo "Bandwidth PCE is not running";
     echo "Please restart Bandwidth PCE using startServers.sh $CONTEXT bwPCE";
     echo "-----------------------------------------------------------";
else
     echo "Bandwidth PCE is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh dijkstraPCE $CONTEXT dijkstraPCE)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
       port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
       port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
porttest14=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest14" ]; then
     echo "Dijkstra PCE is not running";
     echo "Please restart Dijkstra PCE using startServers.sh $CONTEXT dijPCE";
     echo "-----------------------------------------------------------";
else
     echo "Dijkstra PCE is running";
     echo "-----------------------------------------------------------";
fi

Config=$(sh $OSCARS_DIST/bin/parseManifest.sh vlanPCE $CONTEXT vlanPCE)
Service=$(echo $Config | awk -F/ '$1~//{print $2}')
Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
if [ "$Conf" == "conf" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_HOME/$Service/$Conf/$Yaml)
elif [ "$Conf" == "config" ]; then
      port=$(awk -F: '$1~/publishTo/{print $4}' $OSCARS_DIST/$Service/$Conf/$Yaml)
fi 
port=$(echo $port | sed "s/[^0-9]//g")
porttest15=`netstat -na | grep LISTEN | grep $port`
if [ -z "$porttest15" ]; then
     echo "Vlan PCE is not running";
     echo "Please restart Vlan PCE using startServers.sh $CONTEXT vlanPCE";
     echo "-----------------------------------------------------------";
else
     echo "Vlan PCE is running";
     echo "-----------------------------------------------------------";
fi

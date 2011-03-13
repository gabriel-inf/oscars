#!/bin/bash


check_axis2_defaults ()
{
	# Check for axis2.xml
	start_dir=`pwd`
	cd $CATALINA_HOME/webapps/axis2/WEB-INF/conf
	if [ -e axis2.xml ]
	then
		new_user_name=""
		new_password_1=""
		new_password_2=""

		echo " "
		echo " "
		echo "--- Checking to ensure Axis2 username / password are not set to defaults"

		# Check for default username 
		old_user_name=`grep userName axis2.xml | grep admin`

		if [ "$old_user_name" != "" ]
		then
			# Prompt for new username
			echo " "
			echo "It is recommended to change the default Axis2 username"
			while [ "$new_user_name" == "" ]
			do
				echo "Please enter a username for Axis2"
				read new_user_name 
			done
		fi

		# Check for default password
		old_password=`grep password axis2.xml | grep axis2`

		if [ "$old_password" != "" ]
		then
			echo " " 
			echo "It is recommended to change the default Axis2 password"
			while [[ "$new_password_1" == "" || "$new_password_1" != "$new_password_2" ]]
			do
				# Prompt for new password
				new_password_1=""
				new_password_2=""
				while [ "$new_password_1" == "" ]
				do
					echo "Please enter a password for Axis2 (will not echo)"
					read -s new_password_1
				done
				while [ "$new_password_2" == "" ]
				do
					echo "Please re-enter password for Axis2"
					read -s new_password_2
				done
			done
		fi

		# Replace username in axis2.xml
		if [ "$new_user_name" != "" ]
		then
			mv axis2.xml axis2.xml.old 
			sed "s/>admin</>$new_user_name</" axis2.xml.old > axis2.xml
			rm axis2.xml.old
		fi

		# Replace password in axis2.xml
		if [ "$new_password_2" != "" ]
		then
			mv axis2.xml axis2.xml.old
			sed "s/>axis2</>$new_password_2</" axis2.xml.old > axis2.xml
			rm axis2.xml.old
		fi

		echo " " 
		echo "done"
	fi
	cd $start_dir
}

echo "  ";
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

echo "--- Stopping OSCARS...";
./oscars.sh stop
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

#Deploy IDC
echo "--- Deploying IDC...";
ant deployall;
check_axis2_defaults
echo "  ";
echo "  ";
echo "--- Restarting OSCARS...";
./oscars.sh

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

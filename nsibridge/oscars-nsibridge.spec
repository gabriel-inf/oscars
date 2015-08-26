%define package_name nsibridge
%define service_name NsiBridgeService
%define mvn_project_list common-logging,common-soap,nsi-soap,utils,%{package_name}
%define install_base /opt/oscars/%{package_name}
%define oscars_home /etc/oscars
%define log_dir /var/log/oscars
%define run_dir /var/run/oscars
%define relnum 23

Name:           oscars-%{package_name}
Version:        0.6.1
Release:        %{relnum}
Summary:        OSCARS NSI Bridge
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://code.google.com/p/oscars-idc/
Source0:        oscars-%{version}-%{relnum}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  java-1.6.0-openjdk
BuildRequires:  java-1.6.0-openjdk-devel
BuildRequires:  perl
BuildArch:      noarch
Requires:       oscars-env
Requires:       java-1.6.0-openjdk
Requires:       chkconfig

%description
The OSCARS NSI Bridge provides access to OSCARS through the Network Services Interface (NSI)

%pre
/usr/sbin/groupadd oscars 2> /dev/null || :
/usr/sbin/useradd -g oscars -r -s /sbin/nologin -c "OSCARS User" -d /tmp oscars 2> /dev/null || :

%prep
%setup -q -n oscars-%{version}-%{relnum}
perl -e 's/jdbc:hsqldb:file:db\/nsi\.hsqldb/jdbc:hsqldb:file:\/etc\/oscars\/%{service_name}\/data\/nsi.hsqldb/g' -pi %{package_name}/src/main/resources/hibernate.xml 

%clean
rm -rf %{buildroot}

%build
mvn -DskipTests --projects %{mvn_project_list} clean package

%install
rm -rf %{buildroot}
mvn -DskipTests --projects %{mvn_project_list} install 
mkdir -p %{buildroot}/%{install_base}/target
mkdir -p %{buildroot}/%{install_base}/bin
mkdir -p %{buildroot}/etc/init.d
mkdir -p %{buildroot}/%{oscars_home}/%{service_name}/conf/
mkdir -p %{buildroot}/%{oscars_home}/%{service_name}/data/
cp %{package_name}/target/*.jar %{buildroot}/%{install_base}/target/
install -m 755 %{package_name}/bin/* %{buildroot}/%{install_base}/bin/
install -m 755 %{package_name}/scripts/oscars-%{package_name} %{buildroot}/etc/init.d/oscars-%{package_name}
install -m 755 %{package_name}/config/rpm_defaults/* %{buildroot}/%{oscars_home}/%{service_name}/conf/
perl -e 's/^vers=/#vers=/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)
perl -e 's/%{package_name}-\$vers/%{package_name}/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)
perl -e 's/\$\{BINDIR\}\/\.\.\/config/\/etc\/oscars\/%{service_name}\/conf/g' -pi $(find %{buildroot}/%{install_base}/bin -type f)

%post
mkdir -p %{run_dir}
chown oscars:oscars %{run_dir}
mkdir -p %{log_dir}
chown oscars:oscars %{log_dir}

#clear out old symbolic links
if [ "$1" = "2" ]; then
  unlink %{install_base}/target/%{package_name}.one-jar.jar
  unlink %{install_base}/target/%{package_name}.jar
  unlink %{oscars_home}/modules/oscars-%{package_name}.enabled
fi

ln -s %{install_base}/target/%{package_name}-%{version}-%{relnum}-one-jar.jar %{install_base}/target/%{package_name}.one-jar.jar
chown oscars:oscars %{install_base}/target/%{package_name}.one-jar.jar
ln -s %{install_base}/target/%{package_name}-%{version}-%{relnum}.jar %{install_base}/target/%{package_name}.jar
chown oscars:oscars %{install_base}/target/%{package_name}.jar
mkdir -p %{oscars_home}/modules
chown oscars:oscars %{oscars_home}/modules
ln -s /etc/init.d/oscars-%{package_name} %{oscars_home}/modules/oscars-%{package_name}.enabled
chown oscars:oscars %{oscars_home}/modules/oscars-%{package_name}.enabled
/sbin/chkconfig --add oscars-%{package_name}

%files
%defattr(-,oscars,oscars,-)
%config(noreplace) %{oscars_home}/%{service_name}/conf/*
%config(noreplace) %{oscars_home}/%{service_name}/data/.
%{install_base}/target/*
%{install_base}/bin/*
/etc/init.d/oscars-%{package_name}

%preun
if [ $1 -eq 0 ]; then
    /sbin/chkconfig --del oscars-%{package_name}
    /sbin/service oscars-%{package_name} stop
    unlink %{install_base}/target/%{package_name}.one-jar.jar
    unlink %{install_base}/target/%{package_name}.jar
    unlink %{oscars_home}/modules/oscars-%{package_name}.enabled
fi

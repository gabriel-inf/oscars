We are unable to redistribute some third-party jar's due to licensing
restrictions.

1.  The Axis2 jar's.  These will automatically be downloaded for you with
    do_build.sh if you have wget installed.

2.  The Rampart (Java security) jar's.  These will also be automatically
    downloaded for you with do_build.sh.

3.  transaction-api-1.1.jar:  This will be automatically downloaded if you run
    ant prepare.

4.  servlet-api.jar:  If you have a running Tomcat 5.5 server, you can get this
    from $CATALINA_HOME/lib

5.  mysql-connector-java-3.1.13-bin.jar:  This will automatically be downloaded
    if you run ant prepare.
    
If you are upgrading Hibernate, the following jar's all need to be upgraded
at the same time, assuming Hibernate has a later version.
The jar's required for DCN from the Hibernate distribution as of 3.3.1.GA are
the following.  Notice that these may be not the latest versions; don't
downgrade if things are running correctly with what is currently installed.

hibernate3.jar  Use from the distribution

antlr-x.jar, commons-collections-x.jar dom4j-x
  The versions of these haven't changed for awhile; if they've
  been updated, these are in lib/required from the distribution.  Use the
  version from Axis2 1.* for commons-collections.

c3p0-x.jar:  Get from lib/optional/c3p0 in the distribution.

slf4j-api-x.jar, slf4j-jcl-x.jar, slf4j-simple-x.jar

The SLF4J jar's should be downloaded from the SLF4J site:

http://www.slf4j.org/

Only slf4j-api-x.jar is currently distributed with Hibernate.  There is
apparently a problem with it; use the same version of the jar from the SLF4J
site, as well as the same version of the other two jar's.

jta-1.1.jar is from the Hibernate distribution.  transaction-api-1.1.jar is
the same thing without legal notices, and is downloaded automatically.  In
the event that there is a newer version (hasn't been for awhile), the
Maven download in build.xml will need to be changed.

javassist-3.4.GA.jar is from the Hibernate distribution.
javassist-3.4.ga.jar is downloaded automatically, and is the same as what
Hibernate distributes.  If Hibernate changes the version of javassist they
distribute, the Maven download in build.xml will need to be changed
(there are later versions).

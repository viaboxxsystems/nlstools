nlstools by Viaboxx GmbH
=======================================
The project nlstools is a Java project, built with maven2 or maven2, that contains some utilities used for:
- managing i18n resource bundles
- xml files for resource bundles
- excel spreadsheets for resource bundles
- code generation for various target programming languages (java, sql, flex, properties)

How to compile the project from source
=======================================
Requirements:
0. Sources require java1.5 or higher. (Tested with JDK 1.6.0, JDK 1.7)
1. Maven2 or Maven3 required
   Download and install maven from: http://maven.apache.org/
2. Invoke maven from within one of the directories that contain a pom.xml file

Use the binaries
================
1.
a) WITH MAVEN (recommended)

   <dependency>
        <groupId>de.viaboxx</groupId>
        <artifactId>nlstools</artifactId>
        <version>2.5.0</version> <!-- or a later version... --->
   </dependency>

b) WITHOUT MAVEN
 Place nlstools-*.jar into the classpath

2. Use the Tasks in your maven (pom.xml) or ant (build.xml)
   or with whatever build system you are using (gradle, grails, ...)
   
   Refer to the Tests and Examples provided with nlstools.

compile project:
----------------
mvn install
mvn clean install

(artifacts are generated into the target directories)

deploy to maven central:
========================
1. on Mac: set JAVA_HOME

export JAVA_HOME=`/usr/libexec/java_home -v 1.6`

2. call maven
mvn clean deploy

Note: this also creates javadoc and sources jar and deploys to maven snapshot repository.

Refer to https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
Use https://oss.sonatype.org/index.html#welcome to stage/release

Stage a Release
---------------
mvn release:clean release:prepare release:perform -Dusername=[googlecode user] -Dpassword=[googlecode password]


(new) maven way to deploy to OSSRH and release them to the Central Repository
-----------------------------------------------------------------------------
see http://central.sonatype.org/pages/apache-maven.html

Perform a release deployment to OSSRH with:
  mvn release:clean release:prepare -Dusername=[googlecode user] -Dpassword=[googlecode password]
  mvn release:perform               -Dusername=[googlecode user] -Dpassword=[googlecode password]

(optional) generate jar-with-dependencies:
-------------------------------------------
mvn assembly:assembly
(or activate <phase>package</phase> so that it happens automatically during mvn install)

(optional) generate site, javadoc:
-----------------------
mvn site
mvn javadoc:javadoc
svn remove javadoc
svn commit -m "removed old javadoc dir"
mv target/site/apidocs javadoc
svn add javadoc
cd javadoc
svn propset -R svn:mime-type text/html *
cd ..
svn commit -m "readded javadoc"

(optional) generate an IntelliJ project:
-----------------------------
mvn idea:idea

Getting started
---------------
Refer to the project page and WIKI at:

https://code.google.com/p/nlstools/

You can checkout latest sources and releases from there.
You can also refer to the test cases, examples and templates.

Feedback, questions, contribution
=================================
** Your feedback is welcome! **

https://code.google.com/p/nlstools/
http://www.viaboxx.de/
http://www.viaboxxsystems.de/blog

Roman Stumm, Viaboxx GmbH, 2010 - 2014
email: roman.stumm@viaboxx.de

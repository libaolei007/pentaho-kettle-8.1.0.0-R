
-------------------------eclipse工具Maven lifecyle-mappings的配置：lifecyle-mapping-metadata.xml---------------

<?xml version="1.0" encoding="UTF-8"?>
<lifecycleMappingMetadata>
  <pluginExecutions>
    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>buildnumber-maven-plugin</artifactId>
        <goals>
          <goal>create-timestamp</goal>
        </goals>
        <versionRange>[0.0,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>

    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <goals>
          <goal>list</goal>
        </goals>
        <versionRange>[0.0,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>

    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.zeroturnaround</groupId>
        <artifactId>jrebel-maven-plugin</artifactId>
        <goals>
          <goal>generate</goal>
        </goals>
        <versionRange>[0.0,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>

    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>gwt-maven-plugin</artifactId>
        <goals>
          <goal>compile</goal>
        </goals>
        <versionRange>[0.0,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>

    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <goals>
          <goal>copy-dependencies</goal>
          <goal>unpack</goal>
        </goals>
        <versionRange>[0.0,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>

    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-remote-resources-plugin</artifactId>
        <goals>
          <goal>bundle</goal>
        </goals>
        <versionRange>[1.5,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>    
    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>build-helper-maven-plugin</artifactId>
        <goals>
          <goal>add-resource</goal>
          <goal>parse-version</goal>
        </goals>
        <versionRange>[1.9.1,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>
    
    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.commonjava.maven.plugins</groupId>
        <artifactId>directory-maven-plugin</artifactId>
        <goals>
          <goal>highest-basedir</goal>
        </goals>
        <versionRange>[0.1,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>
    
    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.pentaho.maven.plugins</groupId>
        <artifactId>license-helper-maven-plugin</artifactId>
        <goals>
          <goal>check-license</goal>
          <goal>bundle</goal>
        </goals>
        <versionRange>[1.5,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>
    
    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>com.soebes.maven.plugins</groupId>
        <artifactId>iterator-maven-plugin</artifactId>
        <goals>
          <goal>iterator</goal>
          <goal>bundle</goal>
        </goals>
        <versionRange>[0.4,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>
    
    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>org.apache.karaf.tooling</groupId>
        <artifactId>karaf-maven-plugin</artifactId>
        <goals>
          <goal>features-generate-descriptor</goal>
        </goals>
        <versionRange>[3.0.3,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>    
    
    <pluginExecution>
      <pluginExecutionFilter>
        <groupId>com.google.code.maven-replacer-plugin</groupId>
        <artifactId>replacer</artifactId>
        <goals>
          <goal>replace</goal>
        </goals>
        <versionRange>[1.5.2,)</versionRange>
      </pluginExecutionFilter>
      <action>
        <ignore />
      </action>
    </pluginExecution>    
        
        
  </pluginExecutions>
</lifecycleMappingMetadata>

--------------------------------------------------------------------------------------------------


-------------------------------------maven的setting.xml配置-----------------------------------------
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <!-- This is the recommended settings.xml for development of Hitachi Vantara projects. -->
	<localRepository>D:/apache-maven-3.6.0/repository-pdi</localRepository>
  <!--
  If your wish to mirror everything through pentaho-public's repo uncomment bellow. Not recommended
  for external developers.
  -->
  <!--
  <mirrors>
    <mirror>
      <id>pentaho-public</id>
      <url>http://nexus.pentaho.org/content/groups/omni</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>
  -->

  <!--
  You might want to tweak the 'updatePolicy' configuration to fit your need on having updated snapshots and
  releases. Our recommendation is to set it to 'never' and run maven with the '-U' flag when needed.
  -->
  <profiles>
    <profile>
      <id>pentaho</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <repositories>
        <repository>
          <id>pentaho-public</id>
          <name>Pentaho Public</name>
          <url>http://nexus.pentaho.org/content/groups/omni</url>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>pentaho-public</id>
          <name>Pentaho Public</name>
          <url>http://nexus.pentaho.org/content/groups/omni</url>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>

  <!-- this lets you call plugins from these groups in their short form -->
  <pluginGroups>
    <pluginGroup>org.pentaho.maven.plugins</pluginGroup>
    <pluginGroup>com.pentaho.maven.plugins</pluginGroup>
    <pluginGroup>com.github.spotbugs</pluginGroup>
  </pluginGroups>
</settings>




——————————————————————————————————————————————————————————————————————————————————————————————————
# Pentaho Data Integration #

Pentaho Data Integration ( ETL ) a.k.a Kettle

### Project Structure

* **assemblies:** 
Project distribution archive is produced under this module
* **core:** 
Core implementation
* **dbdialog:** 
Database dialog
* **ui:** 
User interface
* **engine:** 
PDI engine
* **engine-ext:** 
PDI engine extensions
* **[plugins:](plugins/README.md)** 
PDI core plugins
* **integration:** 
Integration tests

How to build
--------------

Pentaho Data Integration uses the maven framework. 


#### Pre-requisites for building the project:
* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://raw.githubusercontent.com/pentaho/maven-parent-poms/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

#### Building it

This is a maven project, and to build it use the following command

```
$ mvn clean install
```
Optionally you can specify -Drelease to trigger obfuscation and/or uglification (as needed)

Optionally you can specify -Dmaven.test.skip=true to skip the tests (even though
you shouldn't as you know)

The build result will be a Pentaho package located in ```target```.

#### Running the tests

__Unit tests__

This will run all unit tests in the project (and sub-modules). To run integration tests as well, see Integration Tests below.

```
$ mvn test
```

If you want to remote debug a single java unit test (default port is 5005):

```
$ cd core
$ mvn test -Dtest=<<YourTest>> -Dmaven.surefire.debug
```

__Integration tests__

In addition to the unit tests, there are integration tests that test cross-module operation. This will run the integration tests.

```
$ mvn verify -DrunITs
```

To run a single integration test:

```
$ mvn verify -DrunITs -Dit.test=<<YourIT>>
```

To run a single integration test in debug mode (for remote debugging in an IDE) on the default port of 5005:

```
$ mvn verify -DrunITs -Dit.test=<<YourIT>> -Dmaven.failsafe.debug
```

To skip test

```
$ mvn clean install -DskipTests
```

To get log as text file

```
$ mvn clean install test >log.txt
```


__IntelliJ__

* Don't use IntelliJ's built-in maven. Make it use the same one you use from the commandline.
  * Project Preferences -> Build, Execution, Deployment -> Build Tools -> Maven ==> Maven home directory


### Contributing

1. Submit a pull request, referencing the relevant [Jira case](http://jira.pentaho.com/secure/Dashboard.jspa)
2. Attach a Git patch file to the relevant [Jira case](http://jira.pentaho.com/secure/Dashboard.jspa)

Use of the Pentaho checkstyle format (via `mvn site` and reviewing the report) and developing working 
Unit Tests helps to ensure that pull requests for bugs and improvements are processed quickly.


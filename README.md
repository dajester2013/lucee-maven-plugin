# lucee-maven-plugin
A maven plugin to simplify Lucee project development.

Basic Usage:

Place your Lucee/CFML source into `src/main/lucee`, and configure your pom.xml so that it is similar to this:
```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.name</groupId>
  <artifactId>project</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  
  <packaging>lar</packaging>
  
  <build>
  	<plugins>
  		<plugin>
  			<groupId>org.jdsnet.maven.lucee</groupId>
  			<artifactId>lar-maven-plugin</artifactId>
  			<version>0.1.1-SNAPSHOT</version>
  			<extensions>true</extensions>
  		</plugin>
  	</plugins>
  </build>
  
  <pluginRepositories>
    <pluginRepository>
      <id>dajester2013-snapshots</id>
      <url>http://dajester2013.github.io/maven-snapshots/</url>
    </pluginRepository>
  </pluginRepositories>
</project>
```

**1. Package**

run: \
_mvn package_

**2. Start**

run: \
_java -jar aerospike-client-1.0-SNAPSHOT.jar_

**3. Override config**

* custom h2 file location:\
_-Dspring.liquibase.url="jdbc:h2:./aerospike-client2;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1;"_ \
_-Dspring.datasource.url="jdbc:h2:./aerospike-client2;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1;"_
* change server port:\
 _-Dserver.port=8081_ 

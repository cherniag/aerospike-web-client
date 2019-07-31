**1. Package**

run: \
_mvn package_

**2. Start**

run: \
_java -jar aerospike-client-1.0-SNAPSHOT.jar_

**3. Override config**

* custom h2 file location:\
_-Dclient.database.url="jdbc:h2:./aerospike-client2;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1;"_ 
* h2 credentials:\
_-Dclient.database.username="admin"_ \
_-Dclient.database.password="pass"_
* change server port:\
 _-Dserver.port=8081_ 

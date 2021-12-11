# log4jtest

This is a proof of concept for the Log4Shell bug.

The exploit is in the class `Exploit.java` and should be compiled to `Exploit.class` and stored in the `resources/exploit` folder of main.

Before you do this make sure that the class `Exploit.java` is in the root folder of `src/main/java` and not under a package. To make sure the exploit 
is testing a valid path I moved this class to a `hidden` folder.

The script requests a ldap JNDI lookup at http://localhost:12345/code. This returns the binary of the `Exploit.class` file.

### So the steps are (On Linux)
- move Exploit.java a folder up
- mvn clean install
- move the target class of `Exploit.class` from `target/classes` to `src/main/resources/exploit`
- move Exploit.java back to the `hidden` folder
- java -jar target/log4jtest*.jar
- tail -f /tmp/test.out
- scripts/testVulnerableApp.sh

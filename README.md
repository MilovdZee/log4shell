# log4shell

This is a proof of concept for the Log4Shell bug.

## Info
- https://github.com/mbechler/marshalsec
- https://github.com/tangxiaofeng7/CVE-2021-44228-Apache-Log4j-Rce
- https://www.lunasec.io/docs/blog/log4j-zero-day/
- https://log4shell.huntress.com/

## Use as tester
Because the application logs the LDAP path this implementation can be used in combination with scanning all your service endpoints. Just use a different
path for each endpoint under test, and then it is very easy to find which service is vulnerable.

## Explanation
The exploit is in the class `Exploit.java` and should be compiled to `Exploit.class` and stored in the `resources/exploit` folder of main.

Before you do this make sure that the class `Exploit.java` is in the root folder of `src/main/java` and not under a package. To make sure the exploit 
is testing a valid path I moved this class outside the sources. It is important to move the class file back to outside teh sources  while that is the 
only way to be sure that the classloader won't be able to find it under the name of `Exploit.class` without a package. Otherwise the test is not fair.

The script requests a ldap JNDI lookup at http://localhost:12345/code. This returns the binary of the `Exploit.class` file.

### So the things to start are (On Linux, in separate terminals)
- scripts/start.sh
- tail -f /tmp/test.out
- scripts/testVulnerableApp.sh

### Extra test
It is also possible to use a python server to serve the Exploit.class file
Start `python -m http.server 8888` in `/tmp`. 
You will also have to update `Application.java` variable `JAVA_CODE_BASE` to point to this server `http://localhost:8888/#Exploit`.

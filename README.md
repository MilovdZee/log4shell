# log4jtest

This is a proove of concept for the Log4Shell bug.

The exploit is in the class `Exploit.java` and should be compiled to `Exploit.class` and stored in the `resources/exploit` folder of main.

The script requests a ldap JNDI lookup at http://localhost:12345/code. This returns the binary of the `Exploit.class` file.
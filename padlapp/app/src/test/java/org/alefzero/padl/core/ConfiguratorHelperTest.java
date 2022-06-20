package org.alefzero.padl.core;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConfiguratorHelperTest {

    Logger logger = LoggerFactory.getLogger(ConfiguratorHelperTest.class);
    @Test
    public void testEnviromentConfiguration() throws IOException {
        String output = new ConfiguratorHelper().outputEnv();        

        List<String> expectedLines = new LinkedList<>();

        expectedLines.add("PADLBRIDGE_ROOT_CONFIG_PASSWORD=null");
        expectedLines.add("PADLBRIDGE_ADMIN_PASSWORD=null");
        expectedLines.add("PADLBRIDGE_ROOT_DN=null");

        assertLinesMatch(expectedLines, Arrays.asList(output.split("\n")));
    }
}

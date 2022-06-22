package org.alefzero.padl.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PadlServicesManagerTest {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testSourceServicesConfiguration() {
        PadlServicesManager.getAllSources().forEach((source)-> {
            logger.info("Testing configuration id return for {}.", source.getId());
            assertNotNull(source.getId(), "failed for " + source.getId());
        });
    }

    
}

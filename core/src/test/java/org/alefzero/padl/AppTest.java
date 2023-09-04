package org.alefzero.padl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class AppTest {

	protected static final Logger logger = LogManager.getLogger();

	@Test
	void checkAppExists() {
		App classUnderTest = new App();
		assertNotNull(classUnderTest.getClass().getCanonicalName(), "not null");
	}

}

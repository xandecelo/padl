/**
 * 
 */
package org.alefzero.padl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author xandecelo
 *
 */
class TemplateTest {
	protected static final Logger logger = LogManager.getLogger();

	@BeforeAll
	static void setUp() throws Exception {

	}

	@AfterAll
	static void finish() throws Exception {
	}

	@Test
	void checkAppExists() {
		App classUnderTest = new App();
		assertNotNull(classUnderTest.getClass().getCanonicalName(), "not null");
	}

}

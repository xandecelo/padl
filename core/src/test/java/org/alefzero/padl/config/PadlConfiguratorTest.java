package org.alefzero.padl.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.Executable;

/**
 * @author xandecelo
 *
 */
@TestInstance(Lifecycle.PER_CLASS)
class PadlConfiguratorTest {
	protected static final Logger logger = LogManager.getLogger();
	
	private PadlServiceManager clazz = null;
	
	@BeforeAll
	void prepare()  {
		assertDoesNotThrow(new Executable(){
			@Override
			public void execute() throws Throwable {
				clazz = new PadlServiceManager(Path.of("conf/padl.yaml"));	
			} 
		}, "cannot find YAML test configuration");
	}
	
}

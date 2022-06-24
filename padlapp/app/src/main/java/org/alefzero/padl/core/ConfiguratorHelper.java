package org.alefzero.padl.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.alefzero.padl.core.model.PadlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Generates environment variable configuration and outputs to stdout.
 * Used at configuration phase of LDAP process.
 */
public class ConfiguratorHelper {

    private Logger logger = LoggerFactory.getLogger(ConfiguratorHelper.class);
    private PadlConfig config = new PadlConfig();

    public ConfiguratorHelper withFile(String filename) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();

        try {
            config = mapper.readValue(new File(filename), PadlConfig.class);
        } catch (FileNotFoundException | DatabindException | StreamReadException e) {
            if (e instanceof FileNotFoundException) {
                logger.error("File [{}] cannot be found at [{}] directory.", filename, System.getProperty("user.dir"));
            } else {
                logger.error("Invalid configuration file [{}]: {}", filename, e.getLocalizedMessage());
            }
            logger.debug("Stacktrace:", e);
            throw e;
        }
        return this;
    }

    public PadlConfig getConfig() {
        return config;
    }

    public String outputEnv() {
        StringBuffer sb = new StringBuffer();
        sb.append("PADLBRIDGE_ROOT_CONFIG_PASSWORD=").append(config.getRootConfigPassword()).append("\n");
        sb.append("PADLBRIDGE_ADMIN_PASSWORD=").append(config.getAdminPassword()).append("\n");
        sb.append("PADLBRIDGE_ROOT_DN=").append(config.getRootDN()).append("\n");
        return sb.toString();
    }

}

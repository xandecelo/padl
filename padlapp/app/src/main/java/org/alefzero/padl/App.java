package org.alefzero.padl;

import java.io.IOException;

import org.alefzero.padl.core.ConfiguratorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Application launcher
 * This class has two responsabilities:
 * - Show usage help
 * - Bootstrap config and processing modules
 */
public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    private static void help() {
        logger.info("Usage: ");
        logger.info("\tpadlapp action [config-file]");
        logger.info("\tAvailable actions: ");
        logger.info("\t\tconfig - get environmental variables");
        logger.info("\t\trun    - run padlbridge");
        logger.info(
                "\tconfig-file - full path of configuration file. Defaults to padlbridge.yaml at PADLBRIDGE_HOME if not specified.");
    }

    private static String getFileName(String[] args) {
        return args.length > 1 ? args[1] : "padlbridge.yaml";
    }

    /**
     * @param args one of actions and (an optional) configuration file.
     */
    public static void main(String[] args) {
        if (args.length >= 1) {
            String action = args[0].toLowerCase();
            switch (action) {
                case "run":
                    logger.info("PADL Bridge is going up...");
                    new App().process(getFileName(args));
                    logger.info("PADL Bridge is ready to use.");
                    break;
                case "config":
                    new App().config(getFileName(args));
                    break;
                default:
                    help();
            }
        } else {
            help();
        }
    }

    private void process(String filename) {
        logger.info("Start processing");
    }

    private void config(String filename) {
        logger.info("Processing environment variables and output to stdout");
        try {
            System.out.println(new ConfiguratorHelper().withFile(filename).outputEnv());
        } catch (IOException e) {
            logger.error("Error processing file [{}]: {}", filename, e.getLocalizedMessage());
        }
    }
}

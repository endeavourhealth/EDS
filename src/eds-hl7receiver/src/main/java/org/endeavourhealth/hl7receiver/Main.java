package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.core.data.config.ConfigManagerException;
import org.endeavourhealth.hl7receiver.hl7.HL7Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final String PROGRAM_DISPLAY_NAME = "EDS HL7 receiver";
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		try {
            Configuration configuration = Configuration.getInstance();

		    LOG.info("--------------------------------------------------");
            LOG.info(PROGRAM_DISPLAY_NAME);
            LOG.info("--------------------------------------------------");

			HL7Service serviceManager = new HL7Service(configuration);
            serviceManager.start();

            LOG.info("Press any key to exit...");

            System.in.read();

            LOG.info("Shutting down...");
            serviceManager.stop();

            LOG.info("Shutdown");
            System.exit(0);

        } catch (ConfigManagerException cme) {
            System.err.println("Fatal exception occurred initializing ConfigManager [" + cme.getClass().getName() + "] " + cme.getMessage());
		    LOG.error("Fatal exception occurred initializing ConfigManager", cme);
            System.exit(-2);
        }
        catch (Exception e) {
			LOG.error("Fatal exception occurred", e);
			System.exit(-1);
		}
	}
}
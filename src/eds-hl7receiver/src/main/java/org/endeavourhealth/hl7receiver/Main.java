package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.hl7receiver.hl7.HL7Service;
import org.endeavourhealth.hl7receiver.logging.Logger;

public class Main {

	private static final String PROGRAM_DISPLAY_NAME = "EDS HL7 receiver";
	private static final Logger LOG = Logger.getLogger(Main.class);

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

        } catch (Exception e) {
			LOG.error("Fatal exception occurred", e);
		}
	}
}
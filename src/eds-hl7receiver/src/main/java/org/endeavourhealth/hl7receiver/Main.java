package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.hl7receiver.hl7.EdsHl7Service;
import org.endeavourhealth.hl7receiver.model.exceptions.LogbackConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final String PROGRAM_DISPLAY_NAME = "EDS HL7 receiver";
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		try {
            LOG.info("--------------------------------------------------");
            LOG.info(PROGRAM_DISPLAY_NAME);
            LOG.info("--------------------------------------------------");

			Configuration configuration = Configuration.getInstance();

            EdsHl7Service hl7Service = new EdsHl7Service();
            hl7Service.start();

            System.out.println();
            System.out.println("Press any key to exit...");

            System.in.read();

            hl7Service.stop();

		} catch (LogbackConfigurationException e) {
            LOG.error("Fatal exception occurred initializing logback", e);
            System.err.println("Fatal exception occurred initializing logback");
            e.printStackTrace();
        }
		catch (Exception e) {
			LOG.error("Fatal exception occurred", e);
		}
	}
}
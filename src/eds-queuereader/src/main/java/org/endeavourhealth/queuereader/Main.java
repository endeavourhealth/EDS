package org.endeavourhealth.queuereader;

import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.ExchangeEvent;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);


	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			LOG.error("Usage: queuereader config_id");
			return;
		}

		LOG.info("Initialising config manager");
		ConfigManager.Initialize("queuereader");

		LOG.info("Fixing events");
		fixExchangeEvents();

		LOG.info("--------------------------------------------------");
		LOG.info("EDS Queue Reader " + args[0]);
		LOG.info("--------------------------------------------------");

		LOG.info("Fetching queuereader configuration");
		String configXml = ConfigManager.getConfiguration(args[0]);
		QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);

		// Instantiate rabbit handler
		LOG.info("Creating EDS queue reader");
		RabbitHandler rabbitHandler = new RabbitHandler(configuration);

		// Begin consume
		LOG.info("Starting message consumption");
		rabbitHandler.start();
		LOG.info("EDS Queue reader running");
	}

	private static void fixExchangeEvents() {

		List<ExchangeEvent> events = new AuditRepository().getAllExchangeEvents();
		for (ExchangeEvent event: events) {
			if (event.getEventDesc() != null) {
				continue;
			}

			String eventDesc = "";
			int eventType = event.getEvent().intValue();
			switch (eventType) {
				case 1:
					eventDesc = "Receive";
					break;
				case 2:
					eventDesc = "Validate";
					break;
				case 3:
					eventDesc = "Transform_Start";
					break;
				case 4:
					eventDesc = "Transform_End";
					break;
				case 5:
					eventDesc = "Send";
					break;
				default:
					eventDesc = "??? " + eventType;
			}

			event.setEventDesc(eventDesc);
			new AuditRepository().save(null, event);
		}

	}
}

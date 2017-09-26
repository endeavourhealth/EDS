package org.endeavourhealth.core.messaging.pipeline.components;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.configuration.MessageTransformInboundConfig;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformErrorState;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.adastra.AdastraXmlToFhirTransformer;
import org.endeavourhealth.transform.barts.BartsCsvToFhirTransformer;
import org.endeavourhealth.transform.common.FhirDeltaResourceFilter;
import org.endeavourhealth.transform.common.MessageFormat;
import org.endeavourhealth.transform.common.exceptions.SoftwareNotSupportedException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.EmisOpenToFhirTransformer;
import org.endeavourhealth.transform.fhirhl7v2.FhirHl7v2Filer;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MessageTransformInbound extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformInbound.class);

	//private static final ServiceRepository serviceRepository = new ServiceRepository();
	private static final LibraryRepository libraryRepository = new LibraryRepository();

	private MessageTransformInboundConfig config;

	public MessageTransformInbound(MessageTransformInboundConfig config) {
		this.config = config;
	}


	@Override
	public void process(Exchange exchange) throws PipelineException {

		try {

			UUID serviceId = UUID.fromString(exchange.getHeader(HeaderKeys.SenderServiceUuid));
			String software = exchange.getHeader(HeaderKeys.SourceSystem);
			String messageVersion = exchange.getHeader(HeaderKeys.SystemVersion);

			//find the system ID by using values from the message header
			//the system ID is now set in the exchange header when we open the envelope
			UUID systemId = UUID.fromString(exchange.getHeader(HeaderKeys.SenderSystemUuid));

			List<UUID> batchIds = transform(serviceId, systemId, exchange, software, messageVersion);

			//update the Exchange with the batch IDs, for the next step in the pipeline
			String batchIdString = convertUUidsToStrings(batchIds);
			exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

		} catch (Exception e) {
			exchange.setException(e);
			LOG.error("Error", e);
			throw new PipelineException("Error performing inbound transform", e);
		}
	}

	private List<UUID> transform(UUID serviceId,
								UUID systemId,
								Exchange exchange,
								String software,
								String messageVersion) throws Exception {

		List<UUID> batchIds = new ArrayList<>();

		//create the object that audits the transform and stores any errors
		Date transformStarted = new Date();
		TransformError currentErrors = new TransformError();

		//find the current error state for the source of our data
		ExchangeTransformErrorState errorState = new AuditRepository().getErrorState(serviceId, systemId);

		if (canTransformExchange(errorState, exchange.getExchangeId())) {

			//retrieve the audit of any errors from the last time we processed this exchange ID
			TransformError previousErrors = findPreviousErrors(serviceId, systemId, exchange.getExchangeId());

			try {

				if (software.equalsIgnoreCase(MessageFormat.EMIS_CSV)) {
					processEmisCsvTransform(exchange, serviceId, systemId, messageVersion, software, currentErrors, batchIds, previousErrors);

				} else if (software.equalsIgnoreCase(MessageFormat.EMIS_OPEN)) {
					processEmisOpenTransform(exchange, serviceId, systemId, messageVersion, software, currentErrors, batchIds, previousErrors);

				} else if (software.equalsIgnoreCase(MessageFormat.EMIS_OPEN_HR)) {
					processEmisOpenHrTransform(exchange, serviceId, systemId, messageVersion, software, currentErrors, batchIds, previousErrors);

				} else if (software.equalsIgnoreCase(MessageFormat.TPP_CSV)) {
                    processTppXmlTransform(exchange, serviceId, systemId, messageVersion, software, currentErrors, batchIds, previousErrors);

                } else if (software.equalsIgnoreCase(MessageFormat.HL7V2)) {
                    processHL7V2Filer(exchange, serviceId, systemId, messageVersion, software, currentErrors, batchIds, previousErrors);

				} else if (software.equalsIgnoreCase(MessageFormat.ADASTRA_XML)) {
					processAdastraXml(exchange, serviceId, systemId, messageVersion, software, currentErrors, batchIds, previousErrors);

				} else if (software.equalsIgnoreCase(MessageFormat.BARTS_CSV)) {
					processBartsCsvTransform(exchange, serviceId, systemId, messageVersion, software, currentErrors, batchIds, previousErrors);

				} else {
					throw new SoftwareNotSupportedException(software, messageVersion);
				}
			}
			catch (Exception ex) {

				LOG.error("Error processing exchange " + exchange.getExchangeId() + " from service " + serviceId + " and system " + systemId, ex);

				//record the exception as a fatal error with the exchange
				Map<String, String> args = new HashMap<>();
				args.put(TransformErrorUtility.ARG_FATAL_ERROR, ex.getMessage());
				TransformErrorUtility.addTransformError(currentErrors, ex, args);
			}

			//send an alert if we've had an error while trying to process an exchange
			if (currentErrors.getError().size() > 0) {
				sendSlackAlert(exchange, software, currentErrors);

				//for bulk transforms, I want them to fail gracefully, but that mechanism doesn't work for the
				//thousands of ADT messages, so for them just throw the exception to halt all inbound processing
				//(i.e. it'll reject the message in rabbit, then pull it out again)
				if (!software.equalsIgnoreCase(MessageFormat.EMIS_CSV)
					&& !software.equalsIgnoreCase(MessageFormat.TPP_CSV)) {
					throw new Exception("Failing transform");
				}
			}

		} else {
			LOG.info("NOT performing transform for Exchange {} because previous Exchange went into error", exchange.getExchangeId());

			//record the exception as a fatal error with the exchange
			Map<String, String> args = new HashMap<>();
			args.put(TransformErrorUtility.ARG_WAITING, null);
			TransformErrorUtility.addTransformError(currentErrors, null, args);
		}

		//if we had any errors with the transform, update the error state for this service and system, so
		//we don't attempt to run any further exchanges from the same source until the error is resolved
		updateErrorState(errorState, serviceId, systemId, exchange.getExchangeId(), currentErrors);

		//save the audit of this transform, including errors
		createTransformAudit(serviceId, systemId, exchange.getExchangeId(), transformStarted, currentErrors, batchIds);

		return batchIds;
	}

	private void processAdastraXml(Exchange exchange, UUID serviceId, UUID systemId, String messageVersion,
								   String software, TransformError currentErrors, List<UUID> batchIds, TransformError previousErrors) throws Exception {

		int maxFilingThreads = config.getFilingThreadLimit();

		//payload
		String xmlPayload = exchange.getBody();
		UUID exchangeId = exchange.getExchangeId();

		//transform from XML -> FHIR
		List<Resource> resources = AdastraXmlToFhirTransformer.toFhirFullRecord(xmlPayload);

		//map IDs, compute delta and file
		//FhirDeltaResourceFilter filer = new FhirDeltaResourceFilter(serviceId, systemId, maxFilingThreads);
		//filer.process(resources, exchangeId, currentErrors, batchIds);
	}

	private void sendSlackAlert(Exchange exchange, String software, TransformError currentErrors) {

		int countErrors = currentErrors.getError().size();

		String message = "Error ";
		if (countErrors > 1) {
			message += "1 of " + countErrors + " ";
		}
		message += "in inbound transform from " + software + " for exchange " + exchange.getExchangeId() + "\n";
		message += "view the full error details on the Transform Errors page of EDS-UI";

		Error error = currentErrors.getError().get(0);
		List<String> lines = new ArrayList<>();

		org.endeavourhealth.core.xml.transformError.Exception exception = error.getException();
		while (exception != null) {

			if (exception.getMessage() != null) {
				lines.add(exception.getMessage());
			}

			for (ExceptionLine line : exception.getLine()) {
				String cls = line.getClazz();
				String method = line.getMethod();
				Integer lineNumber = line.getLine();

				lines.add("\u00a0\u00a0\u00a0\u00a0at " + cls + "." + method + ":" + lineNumber);
			}

			exception = exception.getCause();
			if (exception != null) {
				lines.add("Caused by:");
			}
		}

		String attachment = String.join("\n", lines);

		SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, message, attachment);
	}

	/**
	 * if a transform fails, we need to record the error state to prevent further exchanges from the same source
	 * being processed until the error is resolved
	 */
	private void updateErrorState(ExchangeTransformErrorState errorState,
								  UUID serviceId,
								  UUID systemId,
								  UUID exchangeId,
								  TransformError currentErrors) {

		boolean hadError = currentErrors.getError().size() > 0;

		if (errorState == null) {

			if (!hadError) {
				//if we aren't in an error state, and didn't have any new error, then there's nothing to do
				return;

			} else {
				//if we've just had our first error, create the error state, and add the exchange ID to it
				errorState = new ExchangeTransformErrorState();
				errorState.setServiceId(serviceId);
				errorState.setSystemId(systemId);
				errorState.getExchangeIdsInError().add(exchangeId);
				new AuditRepository().save(errorState);
			}

		} else {

			if (!hadError) {
				//if we didn't have an error, remove our exchange ID from the error state object
				errorState.getExchangeIdsInError().remove(exchangeId);

			} else {
				//if we did have an error, then add to the error state (unless it's already there)
				if (!errorState.getExchangeIdsInError().contains(exchangeId)) {
					errorState.getExchangeIdsInError().add(exchangeId);
				}
			}

			//if the error state object is now empty, delete it
			if (errorState.getExchangeIdsInError().isEmpty()) {
				new AuditRepository().delete(errorState);
			} else {
				new AuditRepository().save(errorState);
			}
		}
	}

	private TransformError findPreviousErrors(UUID serviceId, UUID systemId, UUID exchangeId) {

		ExchangeTransformAudit previous = new AuditRepository().getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
		if (previous == null
				|| previous.getErrorXml() == null
				|| previous.getDeleted() != null) {
			return null;
		}

		//if our service and system are in error, but our exchange hasn't been re-submitted,
		//then we can't process any further exchanges from that source, until the first error is fixed
		try {
			return TransformErrorSerializer.readFromXml(previous.getErrorXml());
		} catch (Exception ex) {
			LOG.error("Error parsing XML " + previous.getErrorXml(), ex);
			return null;
		}
	}

	/**
	 * checks the last received exchange for this service and system and sees if it completed without errors or not
	 * If the last one didn't have errors (or is the same exchange as we're running now), then it'll let this
	 * new exchange be processed.
     */
	private static boolean canTransformExchange(ExchangeTransformErrorState errorState, UUID exchangeId) {

		//if our service and system aren't in error, then run the transform
		if (errorState == null) {
			return true;
		}

		//if we are in an error state, then we can only allow re-running of the first Exchange that caused us to go into error
		UUID firstExchangeInError = errorState.getExchangeIdsInError().get(0);
		if (firstExchangeInError.equals(exchangeId)) {
			return true;
		}

		//if our service and system are in error, but our exchange hasn't been re-submitted,
		//then we can't process any further exchanges from that source, until the first error is fixed
		LOG.warn("Expecting exchange " + firstExchangeInError + " as that's the first exchange in error");
		return false;
	}

	private static void createTransformAudit(UUID serviceId, UUID systemId, UUID exchangeId, Date transformStarted, TransformError transformError, List<UUID> batchIds) {
		ExchangeTransformAudit transformAudit = new ExchangeTransformAudit();
		transformAudit.setServiceId(serviceId);
		transformAudit.setSystemId(systemId);
		transformAudit.setExchangeId(exchangeId);
		transformAudit.setVersion(UUIDs.timeBased());
		transformAudit.setStarted(transformStarted);
		transformAudit.setEnded(new Date());

		if (!batchIds.isEmpty()) {
			transformAudit.setNumberBatchesCreated(batchIds.size());
		}

		if (transformError.getError().size() > 0) {
			transformAudit.setErrorXml(TransformErrorSerializer.writeToXml(transformError));
		}

		new AuditRepository().save(transformAudit);
	}

	private static String convertUUidsToStrings(List<UUID> uuids) throws PipelineException {

		//transforms may return null lists, if they didn't insert any new data, so just handle the null
		/*if (uuids == null) {
			uuids = new ArrayList<>();
		}*/

		try {
			return ObjectMapperPool.getInstance().writeValueAsString(uuids.toArray());
		} catch (JsonProcessingException e) {
			throw new PipelineException("Could not serialize batch id list", e);
		}
	}

	private void processEmisCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
											   String software, TransformError currentErrors, List<UUID> batchIds,
											   TransformError previousErrors) throws Exception {

		//get our configuration options
		String sharedStoragePath = config.getSharedStoragePath();
		int maxFilingThreads = config.getFilingThreadLimit();

		String exchangeBody = exchange.getBody();
		UUID exchangeId = exchange.getExchangeId();

		EmisCsvToFhirTransformer.transform(exchangeId, exchangeBody, serviceId, systemId, currentErrors,
									batchIds, previousErrors, sharedStoragePath, maxFilingThreads);
	}

	private void processBartsCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
										 String software, TransformError currentErrors, List<UUID> batchIds,
										 TransformError previousErrors) throws Exception {

		//get our configuration options
		String sharedStoragePath = config.getSharedStoragePath();
		int maxFilingThreads = config.getFilingThreadLimit();

		String exchangeBody = exchange.getBody();
		UUID exchangeId = exchange.getExchangeId();

		BartsCsvToFhirTransformer.transform(exchangeId, exchangeBody, serviceId, systemId, currentErrors,
				batchIds, previousErrors, sharedStoragePath, maxFilingThreads, version);
	}

	private void processTppXmlTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
											  String software, TransformError currentErrors, List<UUID> batchIds,
											  TransformError previousErrors) throws Exception {
		//TODO - plug in TPP XML transform
	}

	private void processEmisOpenTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
												String software, TransformError currentErrors, List<UUID> batchIds,
												TransformError previousErrors) throws Exception {

		//config
		int maxFilingThreads = config.getFilingThreadLimit();

		//payload
		String xmlPayload = exchange.getBody();
		UUID exchangeId = exchange.getExchangeId();

		//transform from XML -> FHIR
		List<Resource> resources = EmisOpenToFhirTransformer.toFhirFullRecord(xmlPayload);

		//map IDs, compute delta and file
		FhirDeltaResourceFilter filer = new FhirDeltaResourceFilter(serviceId, systemId, maxFilingThreads);
		filer.process(resources, exchangeId, currentErrors, batchIds);
	}

	private void processEmisOpenHrTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
												  String software, TransformError currentErrors, List<UUID> batchIds,
												  TransformError previousErrors) throws Exception {
		//TODO - plug in OpenHR transform
	}

    private void processHL7V2Filer(Exchange exchange, UUID serviceId, UUID systemId, String version,
                                          String software, TransformError currentErrors, List<UUID> batchIds,
                                          TransformError previousErrors) throws Exception {

        UUID exchangeId = exchange.getExchangeId();
	    String exchangeBody = exchange.getBody();

        FhirHl7v2Filer fhirHl7v2Filer = new FhirHl7v2Filer();
        fhirHl7v2Filer.file(exchangeId, exchangeBody, serviceId, systemId, currentErrors, batchIds, previousErrors);
    }
}

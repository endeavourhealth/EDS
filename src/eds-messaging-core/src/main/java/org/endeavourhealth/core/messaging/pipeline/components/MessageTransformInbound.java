package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.configuration.MessageTransformInboundConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.adastra.AdastraCsvToFhirTransformer;
import org.endeavourhealth.transform.adastra.AdastraXmlToFhirTransformer;
import org.endeavourhealth.transform.barts.BartsCsvToFhirTransformer;
import org.endeavourhealth.transform.common.FhirDeltaResourceFilter;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.MessageFormat;
import org.endeavourhealth.transform.common.TransformConfig;
import org.endeavourhealth.transform.common.exceptions.SoftwareNotSupportedException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.EmisCustomCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.EmisOpenToFhirTransformer;
import org.endeavourhealth.transform.fhirhl7v2.FhirHl7v2Filer;
import org.endeavourhealth.transform.homerton.HomertonCsvToFhirTransformer;
import org.endeavourhealth.transform.tpp.TppCsvToFhirTransformer;
import org.endeavourhealth.transform.vision.VisionCsvToFhirTransformer;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

//import org.endeavourhealth.transform.barts.BartsCsvToFhirTransformer;

public class MessageTransformInbound extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformInbound.class);

	//private static final ServiceRepository serviceRepository = new ServiceRepository();
	private static final LibraryDalI libraryRepository = DalProvider.factoryLibraryDal();
	private static final ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();
	private static final ExchangeBatchDalI exchangeBatchRepository = DalProvider.factoryExchangeBatchDal();


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

		//if we're re-running an exchange (either due to a past failure or killing and restarting the queue reader),
		//then we need to make sure we've got all our pre-existing batch IDs from last time, so they
		//all go on the Protocol queue when the transform completes
		findExistingBatchIds(batchIds, exchange.getId());
		int startingBatchIdCount = batchIds.size();

		//create the object that audits the transform and stores any errors
		TransformError currentErrors = new TransformError();

		//find the current error state for the source of our data
		ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
		ExchangeTransformErrorState errorState = exchangeDal.getErrorState(serviceId, systemId);

		ExchangeTransformAudit transformAudit = createTransformAudit(serviceId, systemId, exchange.getId());

		if (canTransformExchange(errorState, exchange.getId())) {

			//retrieve the audit of any errors from the last time we processed this exchange ID
			//TransformError previousErrors = findPreviousErrors(serviceId, systemId, exchange.getId());

			try {

				if (software.equalsIgnoreCase(MessageFormat.EMIS_CSV)) {
					processEmisCsvTransform(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.EMIS_OPEN)) {
					processEmisOpenTransform(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.EMIS_OPEN_HR)) {
					processEmisOpenHrTransform(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.TPP_CSV)) {
					processTppCsvTransform(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.TPP_XML)) {
					processTppXmlTransform(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.HL7V2)) {
					processHL7V2Filer(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.ADASTRA_XML)) {
					processAdastraXml(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.BARTS_CSV)) {
					processBartsCsvTransform(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.HOMERTON_CSV)) {
					processHomertonCsvTransform(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.VISION_CSV)) {
					processVisionCsvTransform(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				} else if (software.equalsIgnoreCase(MessageFormat.ADASTRA_CSV)) {
					processAdastraCsvTransform(exchange, serviceId, systemId, messageVersion, currentErrors, batchIds);

				//NOTE: If adding support for a new publisher software, remember to add to the OpenEnvelope class too
				} else {
					throw new SoftwareNotSupportedException(software, messageVersion);
				}
			}
			catch (Exception ex) {
				LOG.error("Error processing exchange " + exchange.getId() + " from service " + serviceId + " and system " + systemId, ex);

				//record the exception as a fatal error with the exchange
				Map<String, String> args = new HashMap<>();
				args.put(TransformErrorUtility.ARG_FATAL_ERROR, ex.getMessage());
				TransformErrorUtility.addTransformError(currentErrors, ex, args);
			}

			//send an alert if we've had an error while trying to process an exchange
			if (currentErrors.getError().size() > 0) {
				sendSlackAlert(exchange, software, serviceId, currentErrors);
			}

		} else {
			LOG.info("NOT performing transform for Exchange {} because previous Exchange went into error", exchange.getId());

			//record the exception as a fatal error with the exchange
			Map<String, String> args = new HashMap<>();
			args.put(TransformErrorUtility.ARG_WAITING, null);
			TransformErrorUtility.addTransformError(currentErrors, null, args);
		}

		//if we had any errors with the transform, update the error state for this service and system, so
		//we don't attempt to run any further exchanges from the same source until the error is resolved
		updateErrorState(errorState, serviceId, systemId, exchange.getId(), currentErrors);

		//save the audit of this transform, including errors
		int newBatchIdCount = batchIds.size() - startingBatchIdCount;
		updateTransformAudit(transformAudit, currentErrors, newBatchIdCount);

		//may as well clear down the cache of reference mappings since they won't be of much use for the next Exchange
		IdHelper.clearCache();

		//if we had any errors during the transform
		if (currentErrors.getError().size() > 0) {

			//for some message formats (e.g. daily non-transactional formats), and in certain environements (e.g. live) we don't want a transform
			//failure to block the queue until fixed, so check if we're supposed to ACK or reject the message from rabbit or not
			Set<String> softwareToDrainQueueOnError = TransformConfig.instance().getSoftwareFormatsToDrainQueueOnFailure();
			if (softwareToDrainQueueOnError.contains(software)) {
				//if we had any errors, don't return any batch IDs, so we don't send anything on to the protocol queue yet
				//when we do successfully re-process the exchange, it will pick up any batch IDs we created this time around
				return new ArrayList<>();

			} else {
				//if our config says to not drain the queue, then throw an exception which will mean
				//we don't remove the exchange from Rabbit and it'll just block any further processing on that queue
				throw new Exception("Failing transform");
			}
		}

		//if we make it here, we fully transformed OK
		updateDataProcessed(exchange);

		return batchIds;
	}

	private void updateDataProcessed(Exchange exchange) {

		//the exchange object we have doesn't have all fields populated, such as system and service IDs, so get from the headesr
		UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);
		UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);
		/*UUID serviceId = exchange.getServiceId();
		UUID systemId = exchange.getSystemId();*/

		try {

			String lastDataDateStr = exchange.getHeader(HeaderKeys.DataDate);

			//won't always be present on really old exchanges
			if (Strings.isNullOrEmpty(lastDataDateStr)) {
				return;
			}

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OpenEnvelope.DATA_DATE_FORMAT);
			Date lastDataDate = simpleDateFormat.parse(lastDataDateStr);

			//and save the date to the special table so we can retrieve it quicker
			LastDataProcessed obj = new LastDataProcessed();
			obj.setServiceId(serviceId);
			obj.setSystemId(systemId);
			obj.setExchangeId(exchange.getId());
			obj.setProcessedDate(new Date());
			obj.setDataDate(lastDataDate);

			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			exchangeDal.save(obj);

		} catch (Throwable t) {
			//any exception, just log it out without throwing further up
			LOG.error("Failed to save last processing date for " + exchange.getId(), t);
		}
	}

	private void processTppCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String messageVersion,
										TransformError currentErrors, List<UUID> batchIds) throws Exception {
		UUID exchangeId = exchange.getId();
		String exchangeBody = exchange.getBody();

		TppCsvToFhirTransformer.transform(exchangeId, exchangeBody, serviceId, systemId, currentErrors, batchIds);
	}


	private void findExistingBatchIds(List<UUID> batchIds, UUID exchangeId) throws Exception {
		List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeId);
		for (ExchangeBatch batch: batches) {
			UUID batchId = batch.getBatchId();
			batchIds.add(batchId);
		}
	}

	private void processAdastraXml(Exchange exchange, UUID serviceId, UUID systemId, String messageVersion,
								   TransformError currentErrors, List<UUID> batchIds) throws Exception {

		//payload
		String xmlPayload = exchange.getBody();
		UUID exchangeId = exchange.getId();

		AdastraXmlToFhirTransformer.transform(exchangeId, xmlPayload, serviceId, systemId,
				currentErrors, batchIds, messageVersion);
	}

	private void sendSlackAlert(Exchange exchange, String software, UUID serviceId, TransformError currentErrors) {

		String serviceDesc = null;
		ServiceDalI serviceDal = DalProvider.factoryServiceDal();
		try {
			Service service = serviceDal.getById(serviceId);
			if (service != null) {
				serviceDesc = service.getName() + " (" + service.getLocalId() + ")";
			}
		} catch (Throwable t) {
			//suppress any errors getting the service
		}
		if (Strings.isNullOrEmpty(serviceDesc)) {
			serviceDesc = serviceId.toString();
		}

		String message = ConfigManager.getAppSubId() + " error ";
		int countErrors = currentErrors.getError().size();
		if (countErrors > 1) {
			message += "1 of " + countErrors + " ";
		}
		message += "in transform from " + software + " for exchange " + exchange.getId() + " and service " + serviceDesc + "\n";
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
								  TransformError currentErrors) throws Exception {

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
				errorState.setExchangeIdsInError(new ArrayList<>());
				errorState.getExchangeIdsInError().add(exchangeId);
				auditRepository.save(errorState);
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
				auditRepository.delete(errorState);
			} else {
				auditRepository.save(errorState);
			}
		}
	}

	/*private TransformError findPreviousErrors(UUID serviceId, UUID systemId, UUID exchangeId) throws Exception {

		ExchangeTransformAudit previous = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
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
	}*/

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

	private static ExchangeTransformAudit createTransformAudit(UUID serviceId, UUID systemId, UUID exchangeId) throws Exception {

		ExchangeTransformAudit transformAudit = new ExchangeTransformAudit();
		transformAudit.setServiceId(serviceId);
		transformAudit.setSystemId(systemId);
		transformAudit.setExchangeId(exchangeId);
		transformAudit.setId(UUID.randomUUID());
		transformAudit.setStarted(new Date());

		auditRepository.save(transformAudit);

		return transformAudit;
	}

	private void updateTransformAudit(ExchangeTransformAudit transformAudit, TransformError errors, int newBatchIdCount) throws Exception {

		transformAudit.setEnded(new Date());
		transformAudit.setNumberBatchesCreated(new Integer(newBatchIdCount));

		if (errors.getError().size() > 0) {
			transformAudit.setErrorXml(TransformErrorSerializer.writeToXml(errors));
		}

		auditRepository.save(transformAudit);
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
										 TransformError currentErrors, List<UUID> batchIds) throws Exception {

		String exchangeBody = exchange.getBody();
		UUID exchangeId = exchange.getId();

		//if the version is this specific string, then invoke the custom transformer, otherwise the regular one
		if (version.equalsIgnoreCase("CUSTOM")) {
			EmisCustomCsvToFhirTransformer.transform(exchangeId, exchangeBody, serviceId, systemId, currentErrors, batchIds);

		} else {
			EmisCsvToFhirTransformer.transform(exchangeId, exchangeBody, serviceId, systemId, currentErrors, batchIds);
		}
	}

	private void processBartsCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
										  TransformError currentErrors, List<UUID> batchIds) throws Exception {

		String exchangeBody = exchange.getBody();
		UUID exchangeId = exchange.getId();

		BartsCsvToFhirTransformer.transform(exchangeId, exchangeBody, serviceId, systemId, currentErrors,
				batchIds, version);
	}

	private void processHomertonCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
											 TransformError currentErrors, List<UUID> batchIds) throws Exception {

		String exchangeBody = exchange.getBody();
		UUID exchangeId = exchange.getId();

		HomertonCsvToFhirTransformer.transform(exchangeId, exchangeBody, serviceId, systemId, currentErrors,
				batchIds, version);
	}

	private void processVisionCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
										   TransformError currentErrors, List<UUID> batchIds) throws Exception {

		UUID exchangeId = exchange.getId();
		String exchangeBody = exchange.getBody();

		VisionCsvToFhirTransformer.transform(exchangeId, exchangeBody, serviceId, systemId, currentErrors,
				batchIds, version);
	}

	private void processAdastraCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
											TransformError currentErrors, List<UUID> batchIds) throws Exception {

		UUID exchangeId = exchange.getId();
		String exchangeBody = exchange.getBody();

		AdastraCsvToFhirTransformer.transform(exchangeId, exchangeBody, serviceId, systemId, currentErrors,
				batchIds, version);
	}

	private void processTppXmlTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
										TransformError currentErrors, List<UUID> batchIds) throws Exception {
		//TODO - plug in TPP XML transform
	}

	private void processEmisOpenTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
										  TransformError currentErrors, List<UUID> batchIds) throws Exception {

		//payload
		String xmlPayload = exchange.getBody();
		UUID exchangeId = exchange.getId();

		//transform from XML -> FHIR
		List<Resource> resources = EmisOpenToFhirTransformer.toFhirFullRecord(xmlPayload);

		//map IDs, compute delta and file
		FhirDeltaResourceFilter filer = new FhirDeltaResourceFilter(serviceId, systemId);
		filer.process(resources, exchangeId, currentErrors, batchIds);
	}

	private void processEmisOpenHrTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
											TransformError currentErrors, List<UUID> batchIds) throws Exception {
		//TODO - plug in OpenHR transform
	}

	private void processHL7V2Filer(Exchange exchange, UUID serviceId, UUID systemId, String version,
								   TransformError currentErrors, List<UUID> batchIds) throws Exception {

		UUID exchangeId = exchange.getId();
		String exchangeBody = exchange.getBody();

		FhirHl7v2Filer fhirHl7v2Filer = new FhirHl7v2Filer();
		fhirHl7v2Filer.file(exchangeId, exchangeBody, serviceId, systemId, currentErrors, batchIds);
	}
}

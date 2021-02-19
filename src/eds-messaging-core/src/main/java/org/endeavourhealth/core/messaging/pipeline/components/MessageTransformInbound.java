package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.configuration.MessageTransformInboundConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.LastDataDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.adastra.AdastraCsvToFhirTransformer;
import org.endeavourhealth.transform.adastra.AdastraXmlToFhirTransformer;
import org.endeavourhealth.transform.barts.BartsCsvToFhirTransformer;
import org.endeavourhealth.transform.bhrut.BhrutCsvToFhirTransformer;
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.common.exceptions.SoftwareNotSupportedException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.EmisCustomCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.EmisOpenToFhirTransformer;
import org.endeavourhealth.transform.fhirhl7v2.FhirHl7v2Filer;
import org.endeavourhealth.transform.hl7v2fhir.FhirTransformer;
import org.endeavourhealth.transform.homertonhi.HomertonHiCsvToFhirTransformer;
import org.endeavourhealth.transform.tpp.TppCsvToFhirTransformer;
import org.endeavourhealth.transform.vision.VisionCsvToFhirTransformer;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class MessageTransformInbound extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformInbound.class);

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
		findExistingBatchIds(batchIds, exchange);
		int startingBatchIdCount = batchIds.size();

		//create the object that audits the transform and stores any errors
		TransformError currentErrors = new TransformError();

		//find the current error state for the source of our data
		ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
		ExchangeTransformErrorState errorState = exchangeDal.getErrorState(serviceId, systemId);

		ExchangeTransformAudit transformAudit = createTransformAudit(serviceId, systemId, exchange.getId());

		if (!canTransformExchange(errorState, exchange.getId())) {
			LOG.warn("NOT performing transform for Exchange " + exchange.getId() + " because previous Exchange went into error");

			//record the exception as a fatal error with the exchange
			Map<String, String> args = new HashMap<>();
			args.put(TransformErrorUtility.ARG_WAITING, null);
			TransformErrorUtility.addTransformError(currentErrors, null, args);

		} else if (shouldAutoFailExchange(serviceId, systemId)) {
			LOG.warn("NOT performing transform for Exchange " + exchange.getId() + " because system is set to auto-fail");

			//record the exception as a fatal error with the exchange
			Map<String, String> args = new HashMap<>();
			args.put(TransformErrorUtility.ARG_AUTO_FAILED, null);
			TransformErrorUtility.addTransformError(currentErrors, null, args);

		} else {

			//the processor is responsible for saving FHIR resources
			FhirResourceFiler fhirResourceFiler = new FhirResourceFiler(exchange.getId(), serviceId, systemId, currentErrors, batchIds);

			try {
				if (software.equalsIgnoreCase(MessageFormat.DUMMY_SENDER_SOFTWARE_FOR_BULK_DELETE)) {
					processBulkDeleteForAllData(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.EMIS_CSV)) {
					processEmisCsvTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.EMIS_OPEN)) {
					processEmisOpenTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.EMIS_OPEN_HR)) {
					processEmisOpenHrTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.TPP_CSV)) {
					processTppCsvTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.TPP_XML)) {
					processTppXmlTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.HL7V2)) {
					processHL7V2Filer(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.ADASTRA_XML)) {
					processAdastraXml(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.BARTS_CSV)) {
					processBartsCsvTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.HOMERTON_CSV)) {
					//processHomertonCsvTransform(exchange, fhirResourceFiler, messageVersion);
					processHomertonHiCsvTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.VISION_CSV)) {
					processVisionCsvTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.ADASTRA_CSV)) {
					processAdastraCsvTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.BHRUT_CSV)) {
					processBhrutCsvTransform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase(MessageFormat.IMPERIAL_HL7_V2)) {
					processImperialHL7Transform(exchange, fhirResourceFiler, messageVersion);

				} else if (software.equalsIgnoreCase("BULK_TRANSFORM_TO_SUBSCRIBER")) {
					//do nothing - we ended up with Exchanges re-queued into the inbound queue that should not have been re-queued
					//so this just allows us to ignore them

				//NOTE: If adding support for a new publisher software, remember to add to the OpenEnvelope class too
				} else {
					throw new SoftwareNotSupportedException(software, messageVersion);
				}
			}
			catch (Throwable ex) {
				LOG.error("Error processing exchange " + exchange.getId() + " from service " + serviceId + " and system " + systemId, ex);

				//record the exception as a fatal error with the exchange
				Map<String, String> args = new HashMap<>();
				args.put(TransformErrorUtility.ARG_FATAL_ERROR, ex.getMessage());
				TransformErrorUtility.addTransformError(currentErrors, ex, args);
			}

			//close down the filer, which waits until everything has been saved
			fhirResourceFiler.waitToFinish();

			//send an alert if we've had an error while trying to process an exchange
			if (currentErrors.getError().size() > 0) {
				sendSlackAlert(exchange, software, serviceId, currentErrors);
			}

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

		//if our transform didn't generate any batch IDs we still need to send something through the
		//queues so that we update the last_data_to_subscriber table that records that subscribers are up-to-date,
		//so use add this special "dummy" UUID
		if (batchIds.isEmpty()) {
			batchIds.add(ExchangeHelper.DUMMY_BATCH_ID);
		}

		return batchIds;
	}

	/**
	 * tests if the service and system have been set into "auto-fail" mode which will automatically
	 * fail any exchanges to allow us to safely get data out of a RabbitMQ queue
     */
	public static boolean shouldAutoFailExchange(UUID serviceId, UUID systemId) throws Exception {
		ServiceDalI serviceDal = DalProvider.factoryServiceDal();
		Service service = serviceDal.getById(serviceId);
		for (ServiceInterfaceEndpoint serviceInterface: service.getEndpointsList()) {
			if (serviceInterface.getSystemUuid().equals(systemId)) {

				String publisherStatus = serviceInterface.getEndpoint();
				if (publisherStatus != null
						&& publisherStatus.equals(ServiceInterfaceEndpoint.STATUS_AUTO_FAIL)) {
					return true;
				}
			}
		}

		return false;
	}

	private void processBulkDeleteForAllData(Exchange exchange, FhirResourceFiler fhirResourceFiler, String messageVersion) throws Exception {
		BulkDeleteTransformer.transform(fhirResourceFiler);
	}

	private void updateDataProcessed(Exchange exchange) {

		//the exchange object we have doesn't have all fields populated, such as system and service IDs, so get from the headesr
		UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);
		UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);
		/*UUID serviceId = exchange.getServiceId();
		UUID systemId = exchange.getSystemId();*/

		try {
			Date extractDate = exchange.getHeaderAsDate(HeaderKeys.ExtractDate);
			Date extractCutoff = exchange.getHeaderAsDate(HeaderKeys.ExtractCutoff);
			boolean hasPatientData = exchange.getHeaderAsBoolean(HeaderKeys.HasPatientData, true); //this header is only set when FALSE, so default to true otherwise

			//if we don't have dates or the extract doesn't contain any patient data, then don't update the audit
			if (extractDate == null
					|| extractCutoff == null
					|| !hasPatientData) {
				return;
			}

			//and save the date to the special table so we can retrieve it quicker
			LastDataProcessed a = new LastDataProcessed();
			a.setServiceId(serviceId);
			a.setSystemId(systemId);
			a.setExchangeId(exchange.getId());
			a.setProcessedDate(new Date());
			a.setExtractDate(extractDate);
			a.setExtractCutoff(extractCutoff);

			LastDataDalI dal = DalProvider.factoryLastDataDal();
			dal.save(a);

		} catch (Throwable t) {
			//any exception, just log it out without throwing further up
			LOG.error("Failed to save last processing date for " + exchange.getId(), t);
		}
	}

	private void processTppCsvTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {
		UUID exchangeId = exchange.getId();
		String exchangeBody = exchange.getBody();

		TppCsvToFhirTransformer.transform(exchangeBody, fhirResourceFiler, version);
	}


	private void findExistingBatchIds(List<UUID> batchIds, Exchange exchange) throws Exception {

		//only retrieve all the batches if the previous attempt to transform went into error or failed,
		//so we don't end up sending everything to the protocol queue when we re-process things
		ExchangeTransformAudit latest = auditRepository.getLatestExchangeTransformAudit(exchange.getServiceId(), exchange.getSystemId(), exchange.getId());
		if (latest == null
				|| (latest.getEnded() != null && latest.getErrorXml() == null)) {
			return;
		}

		List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchange.getId());
		for (ExchangeBatch batch: batches) {
			UUID batchId = batch.getBatchId();
			batchIds.add(batchId);
		}
	}

	private void processAdastraXml(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {

		//payload
		String exchangeBody = exchange.getBody();
		UUID exchangeId = exchange.getId();

		AdastraXmlToFhirTransformer.transform(exchangeBody, fhirResourceFiler, version);
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

		String countStr = "";
		int countErrors = currentErrors.getError().size();
		if (countErrors > 1) {
			countStr = "(1 of " + countErrors + ") ";
		}

		//String exchangeDateStr = new SimpleDateFormat("yyyy-MM-dd").format(exchange.getTimestamp());
		String exchangeDateStr = "";
		try {
			Date dataDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);
			if (dataDate != null) {
				exchangeDateStr = " from " +  new SimpleDateFormat("yyyy-MM-dd").format(dataDate);
			}
		} catch (Exception ex) {
			exchangeDateStr += " <FAILED TO GET DATA DATA>";
		}

		String appId = ConfigManager.getAppSubId();

		String message = appId + " error " + countStr + "in " + software + " transform for " + serviceDesc + "\n";
		message += "Exchange " + exchange.getId() + exchangeDateStr;
		//message += "View the full error details on the Transform Errors page of EDS-UI";

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

		try {
			return ObjectMapperPool.getInstance().writeValueAsString(uuids.toArray());
		} catch (JsonProcessingException e) {
			throw new PipelineException("Could not serialize batch id list", e);
		}
	}

	private void processEmisCsvTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {

//		String exchangeBody = exchange.getBody();

		//if the version is this specific string, then invoke the custom transformer, otherwise the regular one
		if (version != null && version.equalsIgnoreCase("CUSTOM")) {
			EmisCustomCsvToFhirTransformer.transform(exchange, fhirResourceFiler, version);

		} else {
			EmisCsvToFhirTransformer.transform(exchange, fhirResourceFiler, version);
		}
	}

	private void processBartsCsvTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {

		String exchangeBody = exchange.getBody();
		UUID exchangeId = exchange.getId();

		BartsCsvToFhirTransformer.transform(exchangeBody, fhirResourceFiler, version);
	}

//	private void processHomertonCsvTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {
//
//		String exchangeBody = exchange.getBody();
//		UUID exchangeId = exchange.getId();
//
//		HomertonCsvToFhirTransformer.transform(exchangeBody, fhirResourceFiler, version);
//	}

	private void processHomertonHiCsvTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {

		String exchangeBody = exchange.getBody();
		HomertonHiCsvToFhirTransformer.transform(exchangeBody, fhirResourceFiler, version);
	}

	private void processVisionCsvTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {

		UUID exchangeId = exchange.getId();
		String exchangeBody = exchange.getBody();

		VisionCsvToFhirTransformer.transform(exchangeBody, fhirResourceFiler, version);
	}

	private void processAdastraCsvTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {

		UUID exchangeId = exchange.getId();
		String exchangeBody = exchange.getBody();

		AdastraCsvToFhirTransformer.transform(exchangeBody, fhirResourceFiler, version);
	}

	private void processTppXmlTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {
		//TODO - plug in TPP XML transform
	}

	private void processEmisOpenTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {

		//payload
		String xmlPayload = exchange.getBody();
		UUID exchangeId = exchange.getId();

		//transform from XML -> FHIR
		List<Resource> resources = EmisOpenToFhirTransformer.toFhirFullRecord(xmlPayload);

		//map IDs, compute delta and file
		FhirDeltaResourceFilter filer = new FhirDeltaResourceFilter(fhirResourceFiler);
		filer.process(resources);
	}

	private void processEmisOpenHrTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {
		//TODO - plug in OpenHR transform
	}

	private void processHL7V2Filer(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {

		UUID exchangeId = exchange.getId();
		String exchangeBody = exchange.getBody();

		FhirHl7v2Filer fhirHl7v2Filer = new FhirHl7v2Filer();
		fhirHl7v2Filer.file(exchangeBody, fhirResourceFiler, version);
	}

	private void processBhrutCsvTransform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String messageVersion) throws Exception {

		String exchangeBody = exchange.getBody();
		BhrutCsvToFhirTransformer.transform(exchangeBody, fhirResourceFiler, messageVersion);
	}

	/**
	 *
	 * @param exchange
	 * @param fhirResourceFiler
	 * @param version
	 * @throws Exception
	 */
	private void processImperialHL7Transform(Exchange exchange, FhirResourceFiler fhirResourceFiler, String version) throws Exception {
		FhirTransformer.transform(exchange, fhirResourceFiler, version);
	}

}

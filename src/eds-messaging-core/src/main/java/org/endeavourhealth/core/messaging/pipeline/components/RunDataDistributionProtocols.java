package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.common.fhir.schema.RegistrationType;
import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.common.utility.ExpiringCache;
import org.endeavourhealth.core.configuration.RunDataDistributionProtocolsConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeBatch;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberCohortDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberCohortRecord;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RunDataDistributionProtocols extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(RunDataDistributionProtocols.class);


	private RunDataDistributionProtocolsConfig config;

	private static final ParserPool parser = new ParserPool();
	private static ExpiringCache<String, Set<String>> hmOrgParents = new ExpiringCache<>(1000 * 60 * 60 * 1); //cache for an hour
	private static Map<String, String> cachedEndpoints = new ConcurrentHashMap<>();



	public RunDataDistributionProtocols(RunDataDistributionProtocolsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {

		//String batchId = exchange.getHeader(HeaderKeys.BatchIdsJson);
		UUID batchId = null;
		String batchIdJson = exchange.getHeader(HeaderKeys.BatchIdsJson);
		try {
			String s = ObjectMapperPool.getInstance().readValue(batchIdJson, String.class);
			batchId = UUID.fromString(s);
		} catch (Exception ex) {
			throw new PipelineException("Error reading from JSON " + batchIdJson + " for exchange " + exchange.getId(), ex);
		}

		UUID exchangeId = exchange.getId();

		//the objective of the below is to populate this list of work for the outbound transform to do
		List<TransformBatch> transformBatches = new ArrayList<>();

		UUID serviceId = exchange.getServiceId();

		TmpCache tmpCache = new TmpCache(exchangeId, serviceId, batchId);

		List<String> subscriberConfigNames = findSubscriberConfigNames(exchange);
		for (String subscriberConfigName: subscriberConfigNames) {

			try {

				TransformBatch.TransformAction action = null;

				//check if this batch falls into the protocol cohort
				UUID patientId = tmpCache.findPatientId();
				if (patientId == null) {
					//for admin data, we always just let the delta through
					action = calculateActionForAdminData(exchange, batchId, subscriberConfigName);

				} else {
					action = calculateActionForPatientData(tmpCache, exchange, subscriberConfigName);
				}

				if (action != null) {
					TransformBatch transformBatch = new TransformBatch();
					transformBatch.setBatchId(batchId);
					transformBatch.setSubscriberConfigName(subscriberConfigName);
					transformBatch.setAction(action);
					transformBatches.add(transformBatch);
				}

			} catch (Exception ex) {
				throw new PipelineException("Error checking cohort for " + subscriberConfigName, ex);
			}
		}


		// Add transformation batch list to the exchange
		try {
			String transformBatchesJson = ObjectMapperPool.getInstance().writeValueAsString(transformBatches);
			exchange.setHeader(HeaderKeys.TransformBatch, transformBatchesJson);
		} catch (JsonProcessingException e) {
			LOG.error("Error serializing transformation batches", e);
			throw new PipelineException("Error serializing transformation batches", e);
		}
		//LOG.debug("Data distribution protocols executed");
	}

	/**
	 * works out what action we should take (delta or full load) for admin data
	 */
	private TransformBatch.TransformAction calculateActionForAdminData(Exchange exchange, UUID batchId, String subscriberConfigName) throws Exception {

		QueueHelper.ProtocolAction subscriberAction = findSubscriberAction(exchange);
		if (subscriberAction == QueueHelper.ProtocolAction.BULK_REFRESH) {

			LOG.trace("Admin batch " + batchId + " for bulk refresh will be let through for " + subscriberConfigName);
			return TransformBatch.TransformAction.FULL_LOAD;

		} else if (subscriberAction == QueueHelper.ProtocolAction.BULK_DELETE) {

			//if a bulk delete, then we don't need to worry about admin data, so just ignore it
			LOG.trace("Admin batch " + batchId + " for bulk delete will be ignored for " + subscriberConfigName);
			return null;

		} else { //this includes QUICK_REFRESH

			//if a normal delta, then let through
			LOG.trace("Admin batch " + batchId + " for delta will be let through for " + subscriberConfigName);
			return TransformBatch.TransformAction.DELTA;
		}
	}

	private static QueueHelper.ProtocolAction findSubscriberAction(Exchange exchange) {
		if (exchange.hasHeader(HeaderKeys.ProtocolAction)) {
			String s = exchange.getHeader(HeaderKeys.ProtocolAction);
			return QueueHelper.ProtocolAction.fromName(s);

		} else {
			return QueueHelper.ProtocolAction.DELTA;
		}
	}

	/**
	 * works out what action we should take (delta, full load, full delete) for patient data
     */
	private TransformBatch.TransformAction calculateActionForPatientData(TmpCache tmpCache, Exchange exchange, String subscriberConfigName) throws Exception {

		UUID serviceId = tmpCache.getServiceId();
		UUID batchId = tmpCache.getBatchId();
		UUID patientId = tmpCache.findPatientId();
		String odsCode = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);
		SubscriberCohortDalI dal = DalProvider.factorySubscriberCohortDal();

		//we need to work out if the patient is in or out of the cohort and
		//compare against what we previously knew to see what we should do
		SubscriberCohortRecord newResult = new SubscriberCohortRecord(subscriberConfigName, serviceId, batchId, new Date(), patientId);

		//we use a special exchange with a specific source software to bulk populate or delete subscriber data
		QueueHelper.ProtocolAction subscriberAction = findSubscriberAction(exchange);
		if (subscriberAction == QueueHelper.ProtocolAction.BULK_REFRESH) {

			//work out if the patient should or shouldn't be in the cohort and save, so we've refreshed that
			checkCohortNow(newResult, tmpCache, odsCode);
			String reason = newResult.getReason() + " (bulk refresh used)";
			newResult.setReason(reason);
			dal.saveCohortRecord(newResult);

			//base the action purely on the cohort calculation
			if (newResult.isInCohort()) {
				LOG.trace("Batch " + batchId + ", patient " + patientId + " is bulk loading in " + subscriberConfigName + " (" + reason + ")");
				return TransformBatch.TransformAction.FULL_LOAD;
			} else {
				LOG.trace("Batch " + batchId + ", patient " + patientId + " is bulk deleting in " + subscriberConfigName + " (" + reason + ")");
				return TransformBatch.TransformAction.FULL_DELETE;
			}

		} else if (subscriberAction == QueueHelper.ProtocolAction.BULK_DELETE) {

			//if a bulk delete, just overwrite everything to say they're no longer in the cohort
			LOG.trace("Batch " + batchId + ", patient " + patientId + " is bulk deleting from " + subscriberConfigName);
			newResult.setInCohort(false);
			newResult.setReason("Bulk delete utility used");
			dal.saveCohortRecord(newResult);

			//always do a full delete, no matter what the previous state was
			return TransformBatch.TransformAction.FULL_DELETE;

		} else { //this includes DUMMY_SENDER_SOFTWARE_FOR_BULK_SUBSCRIBER_QUICK_REFRESH

			//if no special software specified, then we need to calculate if the patient is in the cohort
			checkCohortNow(newResult, tmpCache, odsCode);

			//if doing a quick refresh, just append that fact to the reason
			if (subscriberAction == QueueHelper.ProtocolAction.BULK_QUICK_REFRESH) {
				String reason = newResult.getReason() + " (quick refresh used)";
				newResult.setReason(reason);
			}

			LOG.trace("Batch " + batchId + ", patient " + patientId + " in cohort = " + newResult.isInCohort() + " for " + subscriberConfigName + " (" + newResult.getReason() + ")");

			//compare the current against previous state to work out what action should be taken
			SubscriberCohortRecord previousResult = dal.getLatestCohortRecord(subscriberConfigName, patientId, batchId);

			if (newResult.isInCohort()) {
				if (previousResult == null
						|| !previousResult.isInCohort()) {
					//if in cohort now, but previously not, then we need to do a full load
					LOG.trace("Previously not in cohort so will bulk load");
					dal.saveCohortRecord(newResult);
					return TransformBatch.TransformAction.FULL_LOAD;

				} else {
					//if in cohort now and previously was, then we just let the delta through
					LOG.trace("Previously in cohort so will process delta");
					return TransformBatch.TransformAction.DELTA;
				}

			} else {
				if (previousResult == null
						|| !previousResult.isInCohort()) {
					//if not in cohort now and not previously, then just skip it all
					//but save the calculation so we have an audit of why the patient isn't in the cohort
					LOG.trace("Previously not in cohort so will ignore");
					if (previousResult == null) {
						dal.saveCohortRecord(newResult);
					}
					//leave ACTION null
					return null;

				} else {
					//if not in cohort now but previously was, then we need to do a full delete
					LOG.trace("Previously in cohort so will bulk delete");
					dal.saveCohortRecord(newResult);
					return TransformBatch.TransformAction.FULL_DELETE;
				}
			}
		}
	}

	private List<String> findSubscriberConfigNames(Exchange exchange) throws PipelineException {

		try {

			if (exchange.hasHeader(HeaderKeys.SubscriberConfigNames)) {
				//if our exchange has specific new-style config names to run, use them
				return exchange.getHeaderAsStringList(HeaderKeys.SubscriberConfigNames);

			} else {
				//if the exchange has no specific configs to run, then work it out
				return getSubscriberConfigNamesForPublisher(exchange.getServiceId());
			}

		} catch (Exception ex) {
			throw new PipelineException("Failed to calculate subscribers for exchange " + exchange.getId(), ex);
		}
	}

	public static List<String> getSubscriberConfigNamesForPublisher(UUID serviceId) throws Exception {

		//TODO - change this to use DSM when in sync with protocols
		return getSubscriberConfigNamesFromOldProtocols(serviceId);
	}

	/**
	 * returns the subscriber config names from the old-style DDS-UI protocols
     */
	public static List<String> getSubscriberConfigNamesFromOldProtocols(UUID serviceId) throws Exception {

		List<LibraryItem> protocols = getProtocolsForPublisherServiceOldWay(serviceId);

		//populate a set, so we can't end up with duplicates
		Set<String> ret = new HashSet<>();

		for (LibraryItem libraryItem: protocols) {
			Protocol protocol = libraryItem.getProtocol();

			//skip disabled protocols
			if (protocol.getEnabled() != ProtocolEnabled.TRUE) {
				continue;
			}

			//get only active subscriber service contracts
			List<ServiceContract> subscribers = protocol
					.getServiceContract()
					.stream()
					.filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER))
					.filter(sc -> sc.getActive() == ServiceContractActive.TRUE) //skip disabled service contracts
					.collect(Collectors.toList());

			for (ServiceContract serviceContract: subscribers) {
				String subscriberConfigName = getSubscriberEndpoint(serviceContract);
				if (!Strings.isNullOrEmpty(subscriberConfigName)) {
					ret.add(subscriberConfigName);
				}
			}
		}

		List<String> list = new ArrayList<>(ret);
		list.sort(((o1, o2) -> o1.compareToIgnoreCase(o2))); //for consistency
		return list;
	}

	public static List<LibraryItem> getProtocolsForPublisherServiceOldWay(UUID serviceUuid) throws PipelineException {

		try {
			List<LibraryItem> ret = new ArrayList<>();

			String serviceIdStr = serviceUuid.toString();

			//the above fn will return is all protocols where the service is present, but we want to filter
			//that down to only ones where our service is an active publisher
			List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid.toString(), null); //passing null means don't filter on system ID

			for (LibraryItem libraryItem: libraryItems) {
				Protocol protocol = libraryItem.getProtocol();
				if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

					for (ServiceContract serviceContract : protocol.getServiceContract()) {
						if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
								&& serviceContract.getService().getUuid().equals(serviceIdStr)
								&& serviceContract.getActive() == ServiceContractActive.TRUE) { //added missing check

							ret.add(libraryItem);
							break;
						}
					}
				}
			}

			return ret;

		} catch (Exception ex) {
			throw new PipelineException("Error getting protocols for service " + serviceUuid, ex);
		}
	}

	private static String getSubscriberEndpoint(ServiceContract contract) throws PipelineException {

		try {
			UUID serviceId = UUID.fromString(contract.getService().getUuid());
			UUID technicalInterfaceId = UUID.fromString(contract.getTechnicalInterface().getUuid());

			String cacheKey = serviceId.toString() + ":" + technicalInterfaceId.toString();
			String endpoint = cachedEndpoints.get(cacheKey);
			if (endpoint == null) {

				ServiceDalI serviceRepository = DalProvider.factoryServiceDal();

				org.endeavourhealth.core.database.dal.admin.models.Service service = serviceRepository.getById(serviceId);
				List<ServiceInterfaceEndpoint> serviceEndpoints = service.getEndpointsList();
				for (ServiceInterfaceEndpoint serviceEndpoint : serviceEndpoints) {
					if (serviceEndpoint.getTechnicalInterfaceUuid().equals(technicalInterfaceId)) {
						endpoint = serviceEndpoint.getEndpoint();

						//concurrent map can't store null values, so only add to the cache if non-null
						if (endpoint != null) {
							cachedEndpoints.put(cacheKey, endpoint);
						}
						break;
					}
				}
			}

			return endpoint;

		} catch (Exception ex) {
			throw new PipelineException("Failed to get endpoint for contract", ex);
		}
	}


	private void checkCohortNow(SubscriberCohortRecord newResult, TmpCache tmpCache, String odsCode) throws Exception {

		SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(newResult.getSubscriberConfigName());

		UUID patientId = tmpCache.findPatientId();
		Patient fhirPatient = tmpCache.findPatientResource(patientId, tmpCache.getServiceId());

		//if the patient record has been deleted we need to make sure that delete reaches subscribers,
		//so count the patient as being IN the cohort so the delta gets sent
		if (fhirPatient == null) {
			newResult.setInCohort(true);
			newResult.setReason("Patient record was deleted");
			return;
		}

		//exclude test patients
		if (subscriberConfig.isExcludeTestPatients()) {
			BooleanType isTestPatient = (BooleanType) ExtensionConverter.findExtensionValue(fhirPatient, FhirExtensionUri.PATIENT_IS_TEST_PATIENT);
			if (isTestPatient != null
					&& isTestPatient.hasValue()
					&& isTestPatient.getValue().booleanValue()) {

				newResult.setInCohort(false);
				newResult.setReason("FHIR Patient is a test patient");
				return;
			}
		}

		//exclude by NHS number
		String excludeNhsNumberRegex = subscriberConfig.getExcludeNhsNumberRegex();
		if (!Strings.isNullOrEmpty(excludeNhsNumberRegex)) {
			String nhsNumber = tmpCache.findPatientNhsNumber();
			if (!Strings.isNullOrEmpty(nhsNumber)
					&& Pattern.matches(excludeNhsNumberRegex, nhsNumber)) {

				newResult.setInCohort(false);
				newResult.setReason("NHS number " + nhsNumber + " is excluded by regex");
				return;
			}
		}

		SubscriberConfig.CohortType cohortType = subscriberConfig.getCohortType();

		if (cohortType == SubscriberConfig.CohortType.AllPatients) {
			newResult.setInCohort(true);
			newResult.setReason("Cohort is all patients");

		} else if (cohortType == SubscriberConfig.CohortType.ExplicitPatients) {
			checkExplicitCohort(newResult, tmpCache);

		} else if (cohortType == SubscriberConfig.CohortType.GpRegisteredAt) {
			checkServiceDefinedCohort(newResult, tmpCache, odsCode);

		} else {
			throw new PipelineException("Unknown cohort " + cohortType + " for subscriber " + newResult.getSubscriberConfigName());
		}
	}


	private void checkServiceDefinedCohort(SubscriberCohortRecord newResult, TmpCache tmpCache, String odsCode) throws Exception {

		//find the list of service UUIDs that define the cohort
		SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(newResult.getSubscriberConfigName());
		Set<String> cohortOdsCodes = subscriberConfig.getCohortGpServices();
		cohortOdsCodes = makeUpperCase(cohortOdsCodes); //just in case, make everything uppercase
		LOG.trace("Found " + cohortOdsCodes.size() + " ODS codes that define the cohort");

		//if we've not added any ODS codes, then something is odd
		if (cohortOdsCodes.isEmpty()) {
			throw new Exception("No ODS codes set in config for " + newResult.getSubscriberConfigName());
		}

		//check to see if our service is one of the defining service contracts, in which case it automatically passes
		if (isOdsCodeInCohort(odsCode, cohortOdsCodes)) {
			LOG.trace("PASS - This service (" + odsCode + ") is in defining list for batch ID " + tmpCache.getBatchId());
			newResult.setInCohort(true);
			newResult.setReason("Publishing org " + odsCode + " is part of cohort");
			return;
		}

		//check the FHIR data to see if the patient is registered in our cohort
		checkPersonIsRegisteredAtServices(newResult, tmpCache, cohortOdsCodes);

		//Ive been told - once a patient has entered the cohort, they don't leave
		//So, if the patient is NOT in the cohort now (because we failed the above check) but the patient
		//WAS previously in the cohort, then we change this result to true. This doesn't apply if the patient
		//falls out of the cohort for other reasons (such as being flagged as a test patient)
		if (!newResult.isInCohort()) {
			SubscriberCohortDalI dal = DalProvider.factorySubscriberCohortDal();
			boolean wasInCohort = dal.wasEverInCohort(newResult.getSubscriberConfigName(), newResult.getPatientId());
			if (wasInCohort) {
				LOG.trace("PASS - Patient is no longer registered in cohort but previously was, so changing to PASS");
				newResult.setInCohort(true);
				newResult.setReason("Patient fallen out of cohort, but retained due to previous inclusion");
			}
		}
	}

	private Set<String> makeUpperCase(Set<String> codes) {
		Set<String> ret = new HashSet<>();
		for (String code: codes) {
			ret.add(code.toUpperCase());
		}
		return ret;
	}

	private static boolean isOdsCodeInCohort(String odsCode, Set<String> cohortOdsCodes) throws Exception {

		//the cohort is in caps, so make sure this is too
		odsCode = odsCode.toUpperCase();

		//if the ODS code is directly in the cohort, it passes
		if (cohortOdsCodes.contains(odsCode)) {
			return true;
		}

		//if not a direct hit, look up the parents for the org in ODS
		Set<String> parentCodes = hmOrgParents.get(odsCode);
		if (parentCodes == null) {

			OdsOrganisation org = OdsWebService.lookupOrganisationViaRest(odsCode);

			if (org != null
					&& org.getParents() != null) {
				parentCodes = new HashSet<>(org.getParents().keySet());

			} else {
				parentCodes = new HashSet<>();
			}
			hmOrgParents.put(odsCode, parentCodes);
		}

		//recurse for each parent code
		for (String parentCode: parentCodes) {
			if (isOdsCodeInCohort(parentCode, cohortOdsCodes)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * checks if any patient record for the person is GMS/Regular registered within the cohort
     */
	private void checkPersonIsRegisteredAtServices(SubscriberCohortRecord newResult, TmpCache tmpCache, Set<String> cohortOdsCodes) throws Exception {

		//first check the patient resource at our own service
		LOG.trace("Checking patient " + tmpCache.findPatientId() + " at service " + tmpCache.getServiceId());
		if (checkPatientIsRegisteredAtServices(tmpCache.findPatientId(), tmpCache.getServiceId(), tmpCache, cohortOdsCodes)) {
			LOG.trace("PASS - Patient " + tmpCache.findPatientId() + " is registered at one of defining services");
			newResult.setInCohort(true);
			newResult.setReason("Patient resource at publisher is part of cohort");
			return;
		}

		//if we couldn't work it out from our own patient resource, then check against any other patient resources
		//that match to the same person record
		Map<String, String> patientAndServiceIds = tmpCache.findOtherPatientsAndServices();
		LOG.trace("Found " + patientAndServiceIds.size() + " patient IDs for person ID " + tmpCache.findPersonId());

		for (String otherPatientId: patientAndServiceIds.keySet()) {

			UUID otherPatientUuid = UUID.fromString(otherPatientId);

			String otherServiceUuidStr = patientAndServiceIds.get(otherPatientId);
			UUID otherServiceUuid = UUID.fromString(otherServiceUuidStr);

			//skip the patient ID we started with since we've already done that one
			if (otherPatientUuid.equals(tmpCache.findPatientId())) {
				continue;
			}

			LOG.debug("Checking patient " + otherPatientId + " at service " + otherServiceUuidStr);
			if (checkPatientIsRegisteredAtServices(otherPatientUuid, otherServiceUuid, tmpCache, cohortOdsCodes)) {
				LOG.trace("PASS - Patient " + otherPatientId + " is registered at one of defining services");
				newResult.setInCohort(true);
				newResult.setReason("Patient resource " + otherPatientId + " is part of cohort");
				return;
			}
		}

		LOG.trace("FAIL - Patient " + tmpCache.findPatientId() + " is NOT registered at one of defining services");
		newResult.setInCohort(false);
		newResult.setReason("Patient resource not registered in cohort");
	}

	private boolean checkPatientIsRegisteredAtServices(UUID patientUuid, UUID serviceId, TmpCache tmpCache, Set<String> cohortOdsCodes) throws Exception {

		Patient fhirPatient = tmpCache.findPatientResource(patientUuid, serviceId);
		if (fhirPatient == null) {
			LOG.debug("      Patient " + patientUuid + " is null, returning false");
			return false;
		}

		Extension isTestPatient = ExtensionConverter.findExtension(fhirPatient, FhirExtensionUri.PATIENT_IS_TEST_PATIENT);
		if (isTestPatient != null) {
			BooleanType b = (BooleanType)isTestPatient.getValue();
			if (b.getValue() != null && b.getValue()){
				LOG.debug("      Patient " + patientUuid + " is a test patient, returning false");
				return false;
			}
		}

		//do not check Care Provider, since this assumes the field is kept up to date, which it
		//won't be, especially at non-GP services
		/*if (!fhirPatient.hasCareProvider()) {
			LOG.debug("      Patient " + patientUuid + " has no care provider, returning false");
			return false;
		}
		for (Reference careProviderReference : fhirPatient.getCareProvider()) {

			String odsCode = findOdsCodeFromReference(tmpCache, careProviderReference, serviceId);

			if (!Strings.isNullOrEmpty(odsCode)) {
				if (isOdsCodeInCohort(odsCode, cohortOdsCodes)) {
					return true;
				}
			} else {
				LOG.debug("      ODS Code could not be found for " + careProviderReference.getReference());
			}
		}*/

		//if we couldn't find the registered practice code from the patient resource, see if we can do so via
		//the EpisodeOfCare resources. We can only look at the current version because we can't search for
		//past versions by patient ID
		List<EpisodeOfCare> episodesOfCare = tmpCache.findEpisodeOfCareResources(patientUuid, serviceId);
		LOG.debug("      Found " + episodesOfCare.size() + " episodes for patient " + patientUuid);
		for (EpisodeOfCare episodeOfCare: episodesOfCare) {

			//skip if ended
			if (episodeOfCare.hasPeriod()
					&& !PeriodHelper.isActive(episodeOfCare.getPeriod())) {
				LOG.debug("      Episode " + episodeOfCare.getId() + " is ended");
				continue;
			}

			//find reg type
			Coding regTypeCoding = (Coding)ExtensionConverter.findExtensionValue(episodeOfCare, FhirExtensionUri.EPISODE_OF_CARE_REGISTRATION_TYPE);
			if (regTypeCoding == null
					|| !regTypeCoding.hasCode()) {
				LOG.debug("      Episode " + episodeOfCare.getId() + " has no reg type extension");
				continue;
			}

			//if not reg GMS
			String regTypeValue = regTypeCoding.getCode();
			if (!regTypeValue.equals(RegistrationType.REGULAR_GMS.getCode())) {
				LOG.debug("      Episode " + episodeOfCare.getId() + " is not reg GMS (is " + regTypeValue + ")");
				continue;
			}

			//get mananging org reference
			if (!episodeOfCare.hasManagingOrganization()) {
				LOG.debug("      Episode " + episodeOfCare.getId() + " has no managing org reference");
				continue;
			}

			//get ODS code for mananging org
			Reference managingOrgReference = episodeOfCare.getManagingOrganization();
			String odsCode = findOdsCodeFromReference(tmpCache, managingOrgReference, serviceId);

			if (!Strings.isNullOrEmpty(odsCode)) {
				if (isOdsCodeInCohort(odsCode, cohortOdsCodes)) {
					return true;
				}
			} else {
				LOG.debug("      ODS Code could not be found for " + managingOrgReference.getReference());
			}
		}

		return false;
	}


	private String findOdsCodeFromReference(TmpCache tmpCache, Reference careProviderReference, UUID serviceId) throws Exception {
		ReferenceComponents comps = ReferenceHelper.getReferenceComponents(careProviderReference);
		if (comps.getResourceType() == ResourceType.Organization) {
			return findOdsCodeFromOrgReference(tmpCache, comps.getId(), serviceId);

		} else if (comps.getResourceType() == ResourceType.Practitioner) {
			return findOdsCodeFromPractitionerReference(tmpCache, comps.getId(), serviceId);

		} else {
			return null;
		}
	}

	private String findOdsCodeFromPractitionerReference(TmpCache tmpCache, String practitionerId, UUID serviceId) throws Exception {

		UUID practitionerUuid = UUID.fromString(practitionerId);
		Practitioner fhirPractitioner = tmpCache.findPractitioner(practitionerUuid, serviceId);

		if (fhirPractitioner == null
				|| !fhirPractitioner.hasPractitionerRole()) {
			return null;
		}

		for (Practitioner.PractitionerPractitionerRoleComponent role: fhirPractitioner.getPractitionerRole()) {
			if (role.hasPeriod()
					&& !PeriodHelper.isActive(role.getPeriod())) {
				continue;
			}

			if (!role.hasManagingOrganization()) {
				continue;
			}

			Reference orgReference = role.getManagingOrganization();
			String orgId = ReferenceHelper.getReferenceId(orgReference);
			String orgOds = findOdsCodeFromOrgReference(tmpCache, orgId, serviceId);
			if (!Strings.isNullOrEmpty(orgOds)) {
				return orgOds;
			}
		}

		return null;
	}

	private String findOdsCodeFromOrgReference(TmpCache tmpCache, String organisationId, UUID serviceId) throws Exception {

		UUID organisationUuid = UUID.fromString(organisationId);
		//LOG.debug("      Patient " + patientUuid + ", refers to organization " + organisationUuid);
		Organization fhirOrganization = tmpCache.findOrganization(organisationUuid, serviceId);

		if (fhirOrganization == null) {
			return null;
		}

		String odsCode = IdentifierHelper.findOdsCode(fhirOrganization);
		//LOG.debug("      Organization was found and has ODS code [" + odsCode + "] and name " + fhirOrganization.getName());
		return odsCode;
	}


	/*public static Set<String> getOdsCodesForServiceDefinedProtocol(Protocol protocol) {
		Set<String> ret = new HashSet<>();

		String cohort = protocol.getCohort();
		int index = cohort.indexOf(":");
		if (index == -1) {
			throw new RuntimeException("Invalid cohort format " + cohort);
		}
		String suffix = cohort.substring(index+1);
		String[] toks = suffix.split("\r|\n|,| |;");
		for (String tok: toks) {
			String odsCode = tok.trim().toUpperCase();  //when checking, we always make uppercase
			if (!Strings.isNullOrEmpty(tok)) {
				ret.add(odsCode);
			}
		}

		return ret;
	}*/




	private void checkExplicitCohort(SubscriberCohortRecord newResult, TmpCache tmpCache) throws Exception {

		String nhsNumber = tmpCache.findPatientNhsNumber();
		if (Strings.isNullOrEmpty(nhsNumber)) {
			newResult.setInCohort(false);
			newResult.setReason("No NHS number");
			return;
		}

		//no point moving the below to the TmpCache object since it only is specific to the protocol currently being checked
		SubscriberCohortDalI dal = DalProvider.factorySubscriberCohortDal();
		boolean inCohort = dal.isInExplicitCohort(newResult.getSubscriberConfigName(), nhsNumber);
		if (inCohort) {
			newResult.setInCohort(true);
			newResult.setReason("NHS number " + nhsNumber + " found in explicit cohort");
			return;
		}

		newResult.setInCohort(false);
		newResult.setReason("NHS number " + nhsNumber + " not found in explicit cohort");

	}


	private List<LibraryItem> getProtocolsFromHeader(Exchange exchange) throws Exception {

		List<LibraryItem> ret = new ArrayList<>();

		String[] protocolIds = exchange.getHeaderAsStringArray(HeaderKeys.ProtocolIds);

		Set<UUID> hsProtocolUuidsDone = new HashSet<>();

		for (String protocolId: protocolIds) {
			UUID protocolUuid = UUID.fromString(protocolId);

			//due to a bug in the way exchanges are re-queued, we have ended up with duplicate protocol IDs
			//in the exchange headers, which doesn't hurt anything but means we end up doing outbound transforms
			//twice. So mitigate this buy handling duplicates
			if (hsProtocolUuidsDone.contains(protocolUuid)) {
				continue;
			}
			hsProtocolUuidsDone.add(protocolUuid);

			try {
				//changed to use cached version
				LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItemUsingCache(protocolUuid);
				//LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItem(protocolUuid);
				ret.add(libraryItem);
			} catch (Exception e) {
				throw new PipelineException("Failed to read protocol item for " + protocolId, e);
			}
		}

		return ret;
	}

	/**
	 * if a patient has multiple subscribers, then we end up retrieving the same data from the DB multiple times, so
	 * we can check if the patient falls into each subscribers cohort. This cache lets us avoid that.
	 */
	class TmpCache {

		private final ExchangeBatchDalI exchangeBatchRepository = DalProvider.factoryExchangeBatchDal();
		private final ResourceDalI resourceRepository = DalProvider.factoryResourceDal();
		private final PatientLinkDalI patientLInkDal = DalProvider.factoryPatientLinkDal();
		private final PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

		private UUID exchangeId;
		private UUID serviceId;
		private UUID batchId;

		private boolean checkedForPatientId = false;
		private UUID cachedPatientId = null;

		private boolean checkedForPersonId = false;
		private String cachedPersonId = null;

		private boolean checkedForOtherPatients = false;
		private Map<String, String> cachedOtherPatients = null;

		private Map<UUID, Patient> hmPatients = new HashMap<>();
		private Map<UUID, List<EpisodeOfCare>> hmEpisodes = new HashMap<>();
		private Map<UUID, Organization> hmOrgs = new HashMap<>();
		private Map<UUID, Practitioner> hmPractitioners = new HashMap<>();

		public TmpCache(UUID exchangeId, UUID serviceId, UUID batchId) {
			this.exchangeId = exchangeId;
			this.serviceId = serviceId;
			this.batchId = batchId;
		}

		public UUID findPatientId() throws Exception {

			if (!checkedForPatientId) {
				ExchangeBatch exchangeBatch = exchangeBatchRepository.getForExchangeAndBatchId(exchangeId, batchId);
				if (exchangeBatch == null) {
					cachedPatientId = null;

				} else {
					cachedPatientId = exchangeBatch.getEdsPatientId();
				}
				checkedForPatientId = true;
			}
			return cachedPatientId;
		}

		public String findPatientNhsNumber() throws Exception {

			UUID patientId = findPatientId();
			Patient p = findPatientResource(patientId, getServiceId());
			if (p == null) {
				return null;
			} else {
				return IdentifierHelper.findNhsNumber(p);
			}
		}

		public String findPersonId() throws Exception {
			if (!checkedForPersonId) {
				cachedPersonId = patientLInkDal.getPersonId(findPatientId().toString());
				checkedForPersonId = true;
			}
			return cachedPersonId;
		}

		public Map<String, String> findOtherPatientsAndServices() throws Exception {
			if (!checkedForOtherPatients) {
				cachedOtherPatients = patientLInkDal.getPatientAndServiceIdsForPerson(findPersonId());
				checkedForOtherPatients = true;
			}
			return cachedOtherPatients;
		}

		public UUID getExchangeId() {
			return exchangeId;
		}

		public UUID getServiceId() {
			return serviceId;
		}

		public UUID getBatchId() {
			return batchId;
		}

		public Patient findPatientResource(UUID patientUuid, UUID serviceId) throws Exception {

			if (!hmPatients.containsKey(patientUuid)) {

				//retrieve from the DB
				Patient fhirPatient = (Patient)resourceRepository.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientUuid.toString());
				hmPatients.put(patientUuid, fhirPatient);
			}

			return hmPatients.get(patientUuid);
		}

		private org.hl7.fhir.instance.model.Resource retrieveNonDeletedResource(UUID serviceId, ResourceType resourceType, UUID resourceId) throws Exception {

			//get the current instance from the DB
			org.hl7.fhir.instance.model.Resource fhirResource = resourceRepository.getCurrentVersionAsResource(serviceId, resourceType, resourceId.toString());

			//if the resource has been deleted, then we have to go back and find a non-deleted instance
			if (fhirResource == null) {
				List<ResourceWrapper> history = resourceRepository.getResourceHistory(serviceId, resourceType.toString(), resourceId);

				//most recent is first
				for (ResourceWrapper historyItem: history) {
					if (!historyItem.isDeleted()) {
						String json = historyItem.getResourceData();
						fhirResource = parser.parse(json);
						break;
					}
				}
			}

			return fhirResource;
		}

		public List<EpisodeOfCare> findEpisodeOfCareResources(UUID patientUuid, UUID serviceId) throws Exception {

			if (!hmEpisodes.containsKey(patientUuid)) {

				List<EpisodeOfCare> l = new ArrayList<>();

				List<ResourceWrapper> episodeWrappers = resourceRepository.getResourcesByPatient(serviceId, patientUuid, ResourceType.EpisodeOfCare.toString());
				for (ResourceWrapper episodeWrapper : episodeWrappers) {

					//this should only ever return non-deleted resources, but can't hurt to check
					if (episodeWrapper.isDeleted()) {
						continue;
					}

					String json = episodeWrapper.getResourceData();
					EpisodeOfCare episodeOfCare = (EpisodeOfCare) FhirSerializationHelper.deserializeResource(json);
					l.add(episodeOfCare);
				}

				hmEpisodes.put(patientUuid, l);
			}
			return hmEpisodes.get(patientUuid);
		}

		public Practitioner findPractitioner(UUID practitionerUuid, UUID serviceId) throws Exception {
			if (!hmPractitioners.containsKey(practitionerUuid)) {
				Practitioner fhirPractitioner = (Practitioner)retrieveNonDeletedResource(serviceId, ResourceType.Practitioner, practitionerUuid);
				hmPractitioners.put(practitionerUuid, fhirPractitioner);
			}
			return hmPractitioners.get(practitionerUuid);
		}

		public Organization findOrganization(UUID organizationUuid, UUID serviceId) throws Exception {
			if (!hmOrgs.containsKey(organizationUuid)) {
				Organization fhirOrganization = (Organization)retrieveNonDeletedResource(serviceId, ResourceType.Organization, organizationUuid);
				hmOrgs.put(organizationUuid, fhirOrganization);
			}
			return hmOrgs.get(organizationUuid);
		}
	}
}

package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.common.fhir.schema.RegistrationType;
import org.endeavourhealth.core.configuration.RunDataDistributionProtocolsConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.OrganisationDalI;
import org.endeavourhealth.core.database.dal.admin.PatientCohortDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeBatch;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.eds.models.PatientSearch;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RunDataDistributionProtocols extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(RunDataDistributionProtocols.class);

	private static final String COHORT_ALL = "All Patients";
	private static final String COHORT_EXPLICIT = "Explicit Patients";
	private static final String COHORT_DEFINING_SERVICES = "Defining Services";

	private RunDataDistributionProtocolsConfig config;
	private static final PatientCohortDalI cohortRepository = DalProvider.factoryPatientCohortDal();
	private static final ResourceDalI resourceRepository = DalProvider.factoryResourceDal();
	private static final ExchangeBatchDalI exchangeBatchRepository = DalProvider.factoryExchangeBatchDal();
	private static final PatientLinkDalI patientLInkDal = DalProvider.factoryPatientLinkDal();
	private static final PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
	private static final OrganisationDalI organisationRepository = DalProvider.factoryOrganisationDal();
	private static final ParserPool parser = new ParserPool();

	//private static final PatientIdentifierRepository patientIdentifierRepository = new PatientIdentifierRepository();
	//private static JCS protocolCache = null;

	/*static {
		try {
			protocolCache = JCS.getInstance("ProtocolCache");

			IElementAttributes attributes = protocolCache.getDefaultElementAttributes();
			attributes.setMaxLifeSeconds(60); //keep protocols cached for 60s max
			protocolCache.setDefaultElementAttributes(attributes);

		} catch (CacheException ex) {
			throw new RuntimeException("Error creating protocol cache", ex);
		}
	}*/

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
			throw new PipelineException("Error reading from JSON " + batchIdJson, ex);
		}

		UUID exchangeId = exchange.getId();

		List<LibraryItem> protocolsToRun = getProtocols(exchange);
		//LibraryItem[] protocolsToRun = getProtocols(exchange);
		List<TransformBatch> transformBatches = new ArrayList<>();

		UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);
		String odsCode = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);

		// Run each protocol, creating a transformation batch for each
		// (Contains list of relevant resources and subscriber service contracts)
		for (LibraryItem libraryItem : protocolsToRun) {

			Protocol protocol = libraryItem.getProtocol();

			//skip disabled protocols
			if (protocol.getEnabled() != ProtocolEnabled.TRUE) {
				continue;
			}

			List<ServiceContract> subscribers = protocol
					.getServiceContract()
					.stream()
					.filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER))
					.filter(sc -> sc.getActive() == ServiceContractActive.TRUE) //skip disabled service contracts
					.collect(Collectors.toList());

			//if there's no subscribers on this protocol, just skip it
			if (subscribers.isEmpty()) {
				continue;
			}

			//check if this batch falls into the protocol cohort
			UUID protocolId = UUID.fromString(libraryItem.getUuid());
			if (!checkCohort(protocol, protocolId, serviceId, exchangeId, batchId, odsCode)) {
				continue;
			}

			//TODO - redesign how filtered resources are transmitted to avoid overloading Rabbit
			//the map is too large to send in the rabbit headers, so I'm sending null and treating this as "all" for now
			Map<ResourceType, List<UUID>> filteredResources = null;
			/*Map<ResourceType, List<UUID>> filteredResources = filterResources(protocol, batchId);
			if (filteredResources.isEmpty()) {
				continue;
			}*/

			TransformBatch transformBatch = new TransformBatch();
			transformBatch.setBatchId(batchId);
			transformBatch.setProtocolId(UUID.fromString(libraryItem.getUuid()));
			transformBatch.setSubscribers(subscribers);
			transformBatch.setResourceIds(filteredResources);

			transformBatches.add(transformBatch);
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


	private boolean checkCohort(Protocol protocol, UUID protocolId, UUID serviceId, UUID exchangeId, UUID batchId, String odsCode) throws PipelineException {

		//if no cohort is defined, then treat this to mean we PASS the check
		String cohort = protocol.getCohort();
		if (Strings.isNullOrEmpty(cohort)) {
			LOG.info("Protocol doesn't have cohort explicitly set, so assuming ALL PATIENTS");
			return true;
		}

		if (cohort.equals(COHORT_ALL)) {
			return true;

		} else if (cohort.equals(COHORT_EXPLICIT)) {
			return checkExplicitCohort(protocolId, serviceId, exchangeId, batchId);

		} else if (cohort.startsWith(COHORT_DEFINING_SERVICES)) {
			return checkServiceDefinedCohort(protocolId, protocol, serviceId, exchangeId, batchId, odsCode);

		} else {

			throw new PipelineException("Unknown cohort " + cohort + " in protocol " + protocolId);
		}
	}


	private boolean checkServiceDefinedCohort(UUID protocolId, Protocol protocol, UUID serviceId, UUID exchangeId, UUID batchId, String odsCode) throws PipelineException {

		//find the list of service UUIDs that define the cohort
		Set<String> cohortOdsCodes = getOdsCodesForServiceDefinedProtocol(protocol);
		LOG.debug("Found " + cohortOdsCodes.size() + " ODS codes that define the cohort");

		//if we've not activated any service contracts yet, this will be empty, which is fine
		if (cohortOdsCodes.isEmpty()) {
			LOG.debug("FAIL - No ODS codes defining cohort found for batch ID " + batchId);
			return false;
		}

		//check to see if our service is one of the defining service contracts, in which case it automatically passes
		if (cohortOdsCodes.contains(odsCode.toUpperCase())) {
			LOG.debug("PASS - This service (" + odsCode + ") is in defining list for batch ID " + batchId);
			return true;
		}

		//if our service isn't one of the cohort-defining ones, then we need to see if our patient is registered at one
		//of the cohort-defining services
		UUID patientUuid = null;
		try {
			patientUuid = findPatientId(exchangeId, batchId);
		} catch (Exception ex) {
			throw new PipelineException("Failed to find patient ID for batch " + batchId, ex);
		}

		//if there's no patient ID, then this is admin resources batch, so return true so it goes through unfiltered
		if (patientUuid == null) {
			LOG.debug("PASS - No patient ID found for batch ID " + batchId);
			return true;
		}

		try {
			return checkPatientIsRegisteredAtServices(serviceId, patientUuid, cohortOdsCodes);

		} catch (Exception ex) {
			throw new PipelineException("Failed to retrieve patient or organisation resources for patient ID " + patientUuid, ex);
		}
	}

	/*private boolean checkServiceDefinedCohort(UUID protocolId, Protocol protocol, UUID serviceId, UUID exchangeId, UUID batchId) throws PipelineException {

		//find the list of service UUIDs that define the cohort
		Set<String> serviceIdsDefiningCohort = getServiceIdsForServiceDefinedProtocol(protocol);
		LOG.debug("Found " + serviceIdsDefiningCohort.size() + " services that define the cohort");

		//if we've not activated any service contracts yet, this will be empty, which is fine
		if (serviceIdsDefiningCohort.isEmpty()) {
			LOG.debug("FAIL - No services defining cohort found for batch ID " + batchId);
			return false;
		}

		//check to see if our service is one of the defining service contracts, in which case it automatically passes
		if (serviceIdsDefiningCohort.contains(serviceId.toString())) {
			LOG.debug("PASS - This service (" + serviceId + ") is in defining list for batch ID " + batchId);
			return true;
		}

		//if our service isn't one of the cohort-defining ones, then we need to see if our patient is registered at one
		//of the cohort-defining services
		UUID patientUuid = null;
		try {
			patientUuid = findPatientId(exchangeId, batchId);
		} catch (Exception ex) {
			throw new PipelineException("Failed to find patient ID for batch " + batchId, ex);
		}

		//if there's no patient ID, then this is admin resources batch, so return true so it goes through unfiltered
		if (patientUuid == null) {
			LOG.debug("PASS - No patient ID found for batch ID " + batchId);
			return true;
		}

		try {
			return checkPatientIsRegisteredAtServices(serviceId, patientUuid, serviceIdsDefiningCohort);

		} catch (Exception ex) {
			throw new PipelineException("Failed to retrieve patient or organisation resources for patient ID " + patientUuid, ex);
		}
	}*/

	private boolean checkPatientIsRegisteredAtServices(UUID serviceId, UUID patientUuid, Set<String> cohortOdsCodes) throws Exception {

		//first check the patient resource at our own service
		LOG.debug("Checking patient " + patientUuid + " at service " + serviceId);
		if (checkPatientIsRegisteredAtServices(patientUuid, serviceId, false, cohortOdsCodes)) {
			LOG.debug("PASS - Patient " + patientUuid + " is registered at one of defining services");
			return true;
		}

		//if we couldn't work it out from our own patient resource, then check against any other patient resources
		//that match to the same person record
		String personId = patientLInkDal.getPersonId(patientUuid.toString());
		Map<String, String> patientAndServiceIds = patientLInkDal.getPatientAndServiceIdsForPerson(personId);
		LOG.debug("Found " + patientAndServiceIds.size() + " patient IDs for person ID " + personId);

		for (String otherPatientId: patientAndServiceIds.keySet()) {

			UUID otherPatientUuid = UUID.fromString(otherPatientId);

			String otherServiceUuidStr = patientAndServiceIds.get(otherPatientId);
			UUID otherServiceUuid = UUID.fromString(otherServiceUuidStr);

			//skip the patient ID we started with since we know our service doesn't define the cohort (otherwise we wouldn't be in this fn)
			if (otherPatientUuid.equals(patientUuid)) {
				continue;
			}

			LOG.debug("Checking patient " + otherPatientId + " at service " + otherServiceUuidStr);
			if (checkPatientIsRegisteredAtServices(otherPatientUuid, otherServiceUuid, true, cohortOdsCodes)) {
				LOG.debug("PASS - Patient " + otherPatientId + " is registered at one of defining services");
				return true;
			}
		}

		LOG.debug("FAIL - Patient " + patientUuid + " is NOT registered at one of defining services");
		return false;
	}

	/*private boolean checkPatientIsRegisteredAtServices(UUID serviceId, UUID patientUuid, Set<String> serviceIdsDefiningCohort) throws Exception {

		//first check the patient resource at our own service
		LOG.debug("Checking patient " + patientUuid + " at service " + serviceId);
		if (checkPatientIsRegisteredAtServices(patientUuid, serviceId, false, serviceIdsDefiningCohort)) {
			LOG.debug("PASS - Patient " + patientUuid + " is registered at one of defining services");
			return true;
		}

		//if we couldn't work it out from our own patient resource, then check against any other patient resources
		//that match to the same person record
		String personId = patientLInkDal.getPersonId(patientUuid.toString());
		Map<String, String> patientAndServiceIds = patientLInkDal.getPatientAndServiceIdsForPerson(personId);
		LOG.debug("Found " + patientAndServiceIds.size() + " patient IDs for person ID " + personId);

		for (String otherPatientId: patientAndServiceIds.keySet()) {

			UUID otherPatientUuid = UUID.fromString(otherPatientId);

			String otherServiceUuidStr = patientAndServiceIds.get(otherPatientId);
			UUID otherServiceUuid = UUID.fromString(otherServiceUuidStr);

			//skip the patient ID we started with since we know our service doesn't define the cohort (otherwise we wouldn't be in this fn)
			if (otherPatientUuid.equals(patientUuid)) {
				continue;
			}

			LOG.debug("Checking patient " + otherPatientId + " at service " + otherServiceUuidStr);
			if (checkPatientIsRegisteredAtServices(otherPatientUuid, otherServiceUuid, true, serviceIdsDefiningCohort)) {
				LOG.debug("PASS - Patient " + otherPatientId + " is registered at one of defining services");
				return true;
			}
		}

		LOG.debug("FAIL - Patient " + patientUuid + " is NOT registered at one of defining services");
		return false;
	}*/

	private boolean checkPatientIsRegisteredAtServices(UUID patientUuid, UUID serviceId, boolean checkCurrentVersionOnly, Set<String> cohortOdsCodes) throws Exception {

		Patient fhirPatient = null;
		if (checkCurrentVersionOnly) {
			//when checking patient resources at other services, it makes sense to only count them if they're non-deleted
			fhirPatient = (Patient)resourceRepository.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientUuid.toString());

		} else {
			//when checking the patient resource at our own service, check using the most recent non-deleted instance
			fhirPatient = (Patient)retrieveNonDeletedResource(serviceId, ResourceType.Patient, patientUuid);
		}

		if (fhirPatient == null) {
			LOG.debug("      Patient " + patientUuid + " is null, returning false");
			return false;
		}

		if (!fhirPatient.hasCareProvider()) {
			LOG.debug("      Patient " + patientUuid + " has no care provider, returning false");
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


		for (Reference careProviderReference : fhirPatient.getCareProvider()) {

			String odsCode = findOdsCodeFromReference(careProviderReference, serviceId);

			if (!Strings.isNullOrEmpty(odsCode)) {
				if (cohortOdsCodes.contains(odsCode.toUpperCase())) {
					return true;
				}
			} else {
				LOG.debug("      ODS Code could not be found for " + careProviderReference.getReference());
			}
		}

		//if we couldn't find the registered practice code from the patient resource, see if we can do so via
		//the EpisodeOfCare resources. We can only look at the current version because we can't search for
		//past versions by patient ID
		List<ResourceWrapper> episodeWrappers = resourceRepository.getResourcesByPatient(serviceId, patientUuid, ResourceType.EpisodeOfCare.toString());
		LOG.debug("      Found " + episodeWrappers.size() + " episodes for patient " + patientUuid);
		for (ResourceWrapper episodeWrapper: episodeWrappers) {

			//this should only ever return non-deleted resources, but can't hurt to check
			if (episodeWrapper.isDeleted()) {
				continue;
			}

			String json = episodeWrapper.getResourceData();
			EpisodeOfCare episodeOfCare = (EpisodeOfCare)FhirSerializationHelper.deserializeResource(json);

			//skip if ended
			if (episodeOfCare.hasPeriod()
					&& !PeriodHelper.isActive(episodeOfCare.getPeriod())) {
				LOG.debug("      Episode " + episodeWrapper.getResourceId() + " is ended");
				continue;
			}

			//find reg type
			Coding regTypeCoding = (Coding)ExtensionConverter.findExtensionValue(episodeOfCare, FhirExtensionUri.EPISODE_OF_CARE_REGISTRATION_TYPE);
			if (regTypeCoding == null
					|| !regTypeCoding.hasCode()) {
				LOG.debug("      Episode " + episodeWrapper.getResourceId() + " has no reg type extension");
				continue;
			}

			//if not reg GMS
			String regTypeValue = regTypeCoding.getCode();
			if (!regTypeValue.equals(RegistrationType.REGULAR_GMS.getCode())) {
				LOG.debug("      Episode " + episodeWrapper.getResourceId() + " is not reg GMS (is " + regTypeValue + ")");
				continue;
			}

			//get mananging org reference
			if (!episodeOfCare.hasManagingOrganization()) {
				LOG.debug("      Episode " + episodeWrapper.getResourceId() + " has no managing org reference");
				continue;
			}

			//get ODS code for mananging org
			Reference managingOrgReference = episodeOfCare.getManagingOrganization();
			String odsCode = findOdsCodeFromReference(managingOrgReference, serviceId);

			if (!Strings.isNullOrEmpty(odsCode)) {
				if (cohortOdsCodes.contains(odsCode.toUpperCase())) {
					return true;
				}
			} else {
				LOG.debug("      ODS Code could not be found for " + managingOrgReference.getReference());
			}
		}

		return false;
	}

	/*private boolean checkPatientIsRegisteredAtServices(UUID patientUuid, UUID serviceId, boolean checkCurrentVersionOnly, Set<String> serviceIdsDefiningCohort) throws Exception {

		Patient fhirPatient = null;
		if (checkCurrentVersionOnly) {
			//when checking patient resources at other services, it makes sense to only count them if they're non-deleted
			fhirPatient = (Patient)resourceRepository.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientUuid.toString());

		} else {
			//when checking the patient resource at our own service, check using the most recent non-deleted instance
			fhirPatient = (Patient)retrieveNonDeletedResource(serviceId, ResourceType.Patient, patientUuid);
		}

		if (fhirPatient == null) {
			LOG.debug("      Patient " + patientUuid + " is null, returning false");
			return false;
		}

		if (!fhirPatient.hasCareProvider()) {
			LOG.debug("      Patient " + patientUuid + " has no care provider, returning false");
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


		for (Reference careProviderReference : fhirPatient.getCareProvider()) {

			String odsCode = findOdsCodeFromReference(careProviderReference, serviceId);

			if (!Strings.isNullOrEmpty(odsCode)) {
				if (isOdsCodeInServiceDefiningServices(odsCode, serviceIdsDefiningCohort)) {
					return true;
				}
			} else {
				LOG.debug("      ODS Code could not be found for " + careProviderReference.getReference());
			}
		}

		//if we couldn't find the registered practice code from the patient resource, see if we can do so via
		//the EpisodeOfCare resources. We can only look at the current version because we can't search for
		//past versions by patient ID
		List<ResourceWrapper> episodeWrappers = resourceRepository.getResourcesByPatient(serviceId, patientUuid, ResourceType.EpisodeOfCare.toString());
		LOG.debug("      Found " + episodeWrappers.size() + " episodes for patient " + patientUuid);
		for (ResourceWrapper episodeWrapper: episodeWrappers) {

			//this should only ever return non-deleted resources, but can't hurt to check
			if (episodeWrapper.isDeleted()) {
				continue;
			}

			String json = episodeWrapper.getResourceData();
			EpisodeOfCare episodeOfCare = (EpisodeOfCare)FhirSerializationHelper.deserializeResource(json);

			//skip if ended
			if (episodeOfCare.hasPeriod()
					&& !PeriodHelper.isActive(episodeOfCare.getPeriod())) {
				LOG.debug("      Episode " + episodeWrapper.getResourceId() + " is ended");
				continue;
			}

			//find reg type
			Coding regTypeCoding = (Coding)ExtensionConverter.findExtensionValue(episodeOfCare, FhirExtensionUri.EPISODE_OF_CARE_REGISTRATION_TYPE);
			if (regTypeCoding == null
					|| !regTypeCoding.hasCode()) {
				LOG.debug("      Episode " + episodeWrapper.getResourceId() + " has no reg type extension");
				continue;
			}

			//if not reg GMS
			String regTypeValue = regTypeCoding.getCode();
			if (!regTypeValue.equals(RegistrationType.REGULAR_GMS.getCode())) {
				LOG.debug("      Episode " + episodeWrapper.getResourceId() + " is not reg GMS (is " + regTypeValue + ")");
				continue;
			}

			//get mananging org reference
			if (!episodeOfCare.hasManagingOrganization()) {
				LOG.debug("      Episode " + episodeWrapper.getResourceId() + " has no managing org reference");
				continue;
			}

			//get ODS code for mananging org
			Reference managingOrgReference = episodeOfCare.getManagingOrganization();
			String odsCode = findOdsCodeFromReference(managingOrgReference, serviceId);

			if (!Strings.isNullOrEmpty(odsCode)) {
				if (isOdsCodeInServiceDefiningServices(odsCode, serviceIdsDefiningCohort)) {
					return true;
				}
			} else {
				LOG.debug("      ODS Code could not be found for " + managingOrgReference.getReference());
			}
		}

		return false;
	}*/

	/*private static boolean isOdsCodeInServiceDefiningServices(String odsCode, Set<String> serviceIdsDefiningCohort) throws Exception {
		Organisation organisation = organisationRepository.getByNationalId(odsCode);
		if (organisation != null
				&& organisation.getServices() != null) {
			LOG.debug("      Admin organisation was found with services " + organisation.getServices().size());

			for (UUID orgServiceId : organisation.getServices().keySet()) {
				String orgServiceIdStr = orgServiceId.toString();
				LOG.debug("      Org admin service ID = " + orgServiceIdStr);
				if (serviceIdsDefiningCohort.contains(orgServiceIdStr)) {
					LOG.debug("      Matches sevice list");
					return true;
				}
			}
		} else {
			LOG.debug("      No admin organisation was found with services for " + odsCode);
		}

		return false;
	}*/

	private String findOdsCodeFromReference(Reference careProviderReference, UUID serviceId) throws Exception {
		ReferenceComponents comps = ReferenceHelper.getReferenceComponents(careProviderReference);
		if (comps.getResourceType() == ResourceType.Organization) {
			return findOdsCodeFromOrgReference(comps.getId(), serviceId);

		} else if (comps.getResourceType() == ResourceType.Practitioner) {
			return findOdsCodeFromPractitionerReference(comps.getId(), serviceId);

		} else {
			return null;
		}
	}

	private String findOdsCodeFromPractitionerReference(String practitionerId, UUID serviceId) throws Exception {

		UUID practitionerUuid = UUID.fromString(practitionerId);
		Practitioner fhirPractitioner = (Practitioner)retrieveNonDeletedResource(serviceId, ResourceType.Practitioner, practitionerUuid);

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
			String orgOds = findOdsCodeFromOrgReference(orgId, serviceId);
			if (!Strings.isNullOrEmpty(orgOds)) {
				return orgOds;
			}
		}

		return null;
	}

	private String findOdsCodeFromOrgReference(String organisationId, UUID serviceId) throws Exception {

		UUID organisationUuid = UUID.fromString(organisationId);
		//LOG.debug("      Patient " + patientUuid + ", refers to organization " + organisationUuid);
		Organization fhirOrganization = (Organization)retrieveNonDeletedResource(serviceId, ResourceType.Organization, organisationUuid);

		if (fhirOrganization == null) {
			return null;
		}

		String odsCode = IdentifierHelper.findOdsCode(fhirOrganization);
		//LOG.debug("      Organization was found and has ODS code [" + odsCode + "] and name " + fhirOrganization.getName());
		return odsCode;
	}

	private static Set<String> getOdsCodesForServiceDefinedProtocol(Protocol protocol) {
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
	}

	/*private static Set<String> getServiceIdsForServiceDefinedProtocol(Protocol protocol) {
		Set<String> ret = new HashSet<>();

		for (ServiceContract serviceContract: protocol.getServiceContract()) {
			if (serviceContract.getActive() == ServiceContractActive.TRUE
				&& serviceContract.isDefinesCohort() != null
				&& serviceContract.isDefinesCohort().booleanValue()) {

				Service service = serviceContract.getService();
				String serviceIdStr = service.getUuid();
				ret.add(serviceIdStr);
			}
		}

		return ret;
	}*/

	/*private boolean checkServiceDefinedCohort(UUID protocolId, Protocol protocol, UUID serviceId, UUID exchangeId, UUID batchId) throws PipelineException {

		//find the list of services that define the cohort
		HashSet<String> serviceIdsDefiningCohort = new HashSet<>();
		for (ServiceContract serviceContract: protocol.getServiceContract()) {
			if (serviceContract.getActive() == ServiceContractActive.TRUE
					&& serviceContract.isDefinesCohort() != null
					&& serviceContract.isDefinesCohort().booleanValue()) {
				Service service = serviceContract.getService();
				String serviceIdStr = service.getUuid();
				serviceIdsDefiningCohort.add(serviceIdStr);
			}
		}

		//if we've not activated any service contracts yet, this will be empty, which is fine
		if (serviceIdsDefiningCohort.isEmpty()) {
			return false;
			//throw new PipelineException("Protocol " + protocolId + " has cohort to be service-defined, but has no service contracts that define it");
		}

		//check to see if our service is one of the defining service contracts
		if (serviceIdsDefiningCohort.contains(serviceId.toString())) {
			return true;
		}

		//if our service isn't one of the cohort-defining ones, then we need to see if our patient is registered at one
		//of the cohort-defining services
		UUID patientUuid = null;
		try {
			patientUuid = findPatientId(exchangeId, batchId);
		} catch (Exception ex) {
			throw new PipelineException("Failed to find patient ID for batch " + batchId, ex);
		}

		//if there's no patient ID, then this is admin resources batch, so return true so it goes through unfiltered
		if (patientUuid == null) {
			//LOG.trace("No patient ID for batch " + batchId + " in exchange " + exchangeId + " so passing protocol " + protocolId + " check");
			return true;
		}

		//rewriting to avoid using the patient_search table, which means we need the GP data to have
		//been processed by Discovery first, which isn't necessarily the case
		try {
			Patient fhirPatient = (Patient)retrieveNonDeletedResource(serviceId, ResourceType.Patient, patientUuid);
			if (fhirPatient != null
					&& fhirPatient.hasCareProvider()) {

				for (Reference careProviderReference: fhirPatient.getCareProvider()) {
					ReferenceComponents comps = ReferenceHelper.getReferenceComponents(careProviderReference);
					if (comps.getResourceType() == ResourceType.Organization) {

						UUID organisationUuid = UUID.fromString(comps.getId());
						Organization fhirOrganization = (Organization)retrieveNonDeletedResource(serviceId, ResourceType.Organization, organisationUuid);

						if (fhirOrganization != null) {
							String odsCode = IdentifierHelper.findOdsCode(fhirOrganization);
							if (!Strings.isNullOrEmpty(odsCode)) {
								Organisation organisation = organisationRepository.getByNationalId(odsCode);
								if (organisation != null
										&& organisation.getServices() != null) {

									for (UUID orgServiceId : organisation.getServices().keySet()) {
										String orgServiceIdStr = orgServiceId.toString();
										if (serviceIdsDefiningCohort.contains(orgServiceIdStr)) {

											return true;
										}
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception ex) {
			throw new PipelineException("Failed to retrieve patient or organisation resources for patient ID " + patientUuid, ex);
		}

		return false;


		*//*
		String patientId = patientUuid.toString();

		//find the global person ID our patient belongs to
		String personId = null;
		try {
			personId = patientLInkDal.getPersonId(patientId);
		} catch (Exception ex) {
			throw new PipelineException("Failed to find person ID for batch " + batchId + " and patientId " + patientId, ex);
		}

		//get all the patient IDs our person is matched to
		List<String> patientIds = null;
		try {
			patientIds = patientLInkDal.getPatientIds(personId);
		} catch (Exception ex) {
			throw new PipelineException("Failed to find person ID for batch " + batchId + " and personId " + personId, ex);
		}

		//LOG.trace("Found " + patientIds.size() + " patient IDs for patient " + patientId + " and person " + personId + " for batch " + batchId + " in exchange " + exchangeId + " for protocol " + protocolId + " check");

		for (String otherPatientId: patientIds) {

			//skip the patient ID we started with since we know our service doesn't define the cohort
			if (otherPatientId.equals(patientId)) {
				continue;
			}

			ResourceWrapper resourceHistory = null;
			try {
				resourceHistory = resourceRepository.getCurrentVersion(ResourceType.Patient.toString(), UUID.fromString(otherPatientId));
			} catch (Exception ex) {
				throw new PipelineException("Failed to retrieve resource from database", ex);
			}

			if (resourceHistory == null
					|| resourceHistory.isDeleted()) {
				continue;
			}

			//if the other patient resouce does belong to one of our cohort defining services, then let it through
			UUID otherPatientServiceId = resourceHistory.getServiceId();
			if (serviceIdsDefiningCohort.contains(otherPatientServiceId.toString())) {
				LOG.trace("Patient ID " + otherPatientId + " is in cohort for batch " + batchId + " in exchange " + exchangeId + " so passing protocol " + protocolId + " check");
				return true;
			}
		}

		return false;*//*
	}*/

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

	private boolean checkExplicitCohort(UUID protocolId, UUID serviceId, UUID exchangeId, UUID batchId) throws PipelineException {
		//find the patient ID for the batch
		UUID patientId = null;
		try {
			patientId = findPatientId(exchangeId, batchId);
		} catch (Exception ex) {
			throw new PipelineException("Failed to find patient ID for batch " + batchId, ex);
		}

		//if there's no patient ID, then this is admin resources batch, so return true so it goes through unfiltered
		if (patientId == null) {
			return true;
		}

		try {
			String nhsNumber = findPatientNhsNumber(patientId);
			//LOG.trace("patient ID " + patientId + " -> nhs number " + nhsNumber);

			if (Strings.isNullOrEmpty(nhsNumber)) {
				return false;
			}

			boolean inCohort = cohortRepository.isInCohort(protocolId, serviceId, nhsNumber);
			//LOG.trace("protocol " + protocolId + " service " + serviceId + " nhs number " + nhsNumber + " -> in cohort " + inCohort);
			return inCohort;
		} catch (Exception ex) {
			throw new PipelineException("Exception in protocol " + protocolId, ex);
		}
	}


	private String findPatientNhsNumber(UUID patientId) throws Exception {

		PatientSearch patientSearchResult = patientSearchDal.searchByPatientId(patientId);
		if (patientSearchResult != null) {
			return patientSearchResult.getNhsNumber();
		} else {
			return null;
		}
	}
	/*private String findPatientNhsNumber(UUID patientId) {

		PatientIdentifierByPatientId identity = patientIdentifierRepository.getMostRecentByPatientId(patientId);
		if (identity != null) {
			return identity.getNhsNumber();

		} else {
			return null;
		}
	}*/

	private static UUID findPatientId(UUID exchangeId, UUID batchId) throws Exception {

		ExchangeBatch exchangeBatch = exchangeBatchRepository.getForExchangeAndBatchId(exchangeId, batchId);
		if (exchangeBatch == null) {
			return null;

		} else {
			return exchangeBatch.getEdsPatientId();
		}
	}
	/*private static String findPatientId(String batchId) throws Exception {

		UUID batchUuid = UUID.fromString(batchId);
		List<ResourceByExchangeBatch> patientResourceWrappers = resourceRepository.getResourcesForBatch(batchUuid);

		//go through what we've received and see if we can find a patient ID from there
		for (ResourceByExchangeBatch batchEntry: patientResourceWrappers) {

			String resourceType = batchEntry.getResourceType();
			ResourceType fhirResourceType = ResourceType.valueOf(resourceType);
			if (FhirResourceFiler.isPatientResource(fhirResourceType)) {

				if (!batchEntry.getIsDeleted()) {
					Resource fhir = FhirResourceHelper.deserialiseResouce(batchEntry);
					return IdHelper.getPatientId(fhir);
				}
			}
		}

		//if everything in our batch is deleted, we need to look at past instances of the same resources we received
		for (ResourceByExchangeBatch batchEntry: patientResourceWrappers) {

			String resourceType = batchEntry.getResourceType();
			UUID resourceId = batchEntry.getResourceId();
			List<ResourceHistory> history = resourceRepository.getResourceHistory(resourceType, resourceId);

			//work back through the history to find a non-deleted instance, which will allow us to find the EDS patient ID
			for (int i=history.size()-1; i>=0; i--) {
				ResourceHistory historyEntry = history.get(i);
				if (historyEntry.getIsDeleted()) {
					continue;
				}

				Resource fhir = FhirResourceHelper.deserialiseResouce(historyEntry);
				return IdHelper.getPatientId(fhir);
			}
		}

		return null;
	}*/

	/**
	 * filters down resources in the batch to just those that match the protocol data set
	 * //TODO - apply protocol dataset filtering
     */
	public static Map<ResourceType, List<UUID>> filterResources(UUID serviceId, Protocol protocol, String batchId) throws Exception {

		Map<ResourceType, List<UUID>> ret = new HashMap<>();

		UUID batchUuid = UUID.fromString(batchId);
		ResourceDalI resourceDal = DalProvider.factoryResourceDal();
		List<ResourceWrapper> resourcesByExchangeBatch = resourceDal.getResourcesForBatch(batchUuid, serviceId);
		for (ResourceWrapper resourceByExchangeBatch: resourcesByExchangeBatch) {
			String resourceType = resourceByExchangeBatch.getResourceType();
			ResourceType fhirResourceType = ResourceType.valueOf(resourceType);
			UUID resourceId = resourceByExchangeBatch.getResourceId();

			List<UUID> list = ret.get(fhirResourceType);
			if (list == null) {
				list = new ArrayList<>();
				ret.put(fhirResourceType, list);
			}
			list.add(resourceId);
		}

		return ret;
	}


	private List<LibraryItem> getProtocols(Exchange exchange) throws PipelineException {

		List<LibraryItem> ret = new ArrayList<>();

		String[] protocolIds = null;
		try {
			protocolIds = exchange.getHeaderAsStringArray(HeaderKeys.ProtocolIds);
		} catch (Exception ex) {
			throw new PipelineException("Failed to read protocol IDs from exchange " + exchange.getId());
		}

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

}

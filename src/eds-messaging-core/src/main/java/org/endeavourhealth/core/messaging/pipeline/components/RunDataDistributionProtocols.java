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
		List<TransformBatch> transformBatches = new ArrayList<>();

		UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);
		String odsCode = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);

		TmpCache tmpCache = new TmpCache(exchangeId, serviceId, batchId);

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
			if (!checkCohort(protocol, protocolId, tmpCache, odsCode)) {
				continue;
			}

			TransformBatch transformBatch = new TransformBatch();
			transformBatch.setBatchId(batchId);
			transformBatch.setProtocolId(UUID.fromString(libraryItem.getUuid()));
			transformBatch.setSubscribers(subscribers);

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


	private boolean checkCohort(Protocol protocol, UUID protocolId, TmpCache tmpCache, String odsCode) throws PipelineException {

		//if no cohort is defined, then treat this to mean we PASS the check
		String cohort = protocol.getCohort();
		if (Strings.isNullOrEmpty(cohort)) {
			LOG.info("Protocol doesn't have cohort explicitly set, so assuming ALL PATIENTS");
			return true;
		}

		if (cohort.equals(COHORT_ALL)) {
			return true;

		} else if (cohort.equals(COHORT_EXPLICIT)) {
			return checkExplicitCohort(protocolId, tmpCache);

		} else if (cohort.startsWith(COHORT_DEFINING_SERVICES)) {
			return checkServiceDefinedCohort(protocolId, protocol, tmpCache, odsCode);

		} else {

			throw new PipelineException("Unknown cohort " + cohort + " in protocol " + protocolId);
		}
	}


	private boolean checkServiceDefinedCohort(UUID protocolId, Protocol protocol, TmpCache tmpCache, String odsCode) throws PipelineException {

		//find the list of service UUIDs that define the cohort
		Set<String> cohortOdsCodes = getOdsCodesForServiceDefinedProtocol(protocol);
		LOG.debug("Found " + cohortOdsCodes.size() + " ODS codes that define the cohort");

		//if we've not activated any service contracts yet, this will be empty, which is fine
		if (cohortOdsCodes.isEmpty()) {
			LOG.debug("FAIL - No ODS codes defining cohort found for batch ID " + tmpCache.getBatchId());
			return false;
		}

		//check to see if our service is one of the defining service contracts, in which case it automatically passes
		if (cohortOdsCodes.contains(odsCode.toUpperCase())) {
			LOG.debug("PASS - This service (" + odsCode + ") is in defining list for batch ID " + tmpCache.getBatchId());
			return true;
		}

		//if our service isn't one of the cohort-defining ones, then we need to see if our patient is registered at one
		//of the cohort-defining services
		UUID patientUuid = null;
		try {
			patientUuid = tmpCache.findPatientId();
		} catch (Exception ex) {
			throw new PipelineException("Failed to find patient ID for batch " + tmpCache.getBatchId(), ex);
		}

		//if there's no patient ID, then this is admin resources batch, so return true so it goes through unfiltered
		if (patientUuid == null) {
			LOG.debug("PASS - No patient ID found for batch ID " + tmpCache.getBatchId());
			return true;
		}

		try {
			return checkPatientIsRegisteredAtServices(tmpCache, cohortOdsCodes);

		} catch (Exception ex) {
			throw new PipelineException("Failed to retrieve patient or organisation resources for patient ID " + patientUuid, ex);
		}
	}


	private boolean checkPatientIsRegisteredAtServices(TmpCache tmpCache, Set<String> cohortOdsCodes) throws Exception {

		//first check the patient resource at our own service
		LOG.debug("Checking patient " + tmpCache.findPatientId() + " at service " + tmpCache.getServiceId());
		if (checkPatientIsRegisteredAtServices(tmpCache.findPatientId(), tmpCache.getServiceId(), tmpCache, cohortOdsCodes)) {
			LOG.debug("PASS - Patient " + tmpCache.findPatientId() + " is registered at one of defining services");
			return true;
		}

		//if we couldn't work it out from our own patient resource, then check against any other patient resources
		//that match to the same person record
		Map<String, String> patientAndServiceIds = tmpCache.findOtherPatientsAndServices();
		LOG.debug("Found " + patientAndServiceIds.size() + " patient IDs for person ID " + tmpCache.findPersonId());

		for (String otherPatientId: patientAndServiceIds.keySet()) {

			UUID otherPatientUuid = UUID.fromString(otherPatientId);

			String otherServiceUuidStr = patientAndServiceIds.get(otherPatientId);
			UUID otherServiceUuid = UUID.fromString(otherServiceUuidStr);

			//skip the patient ID we started with since we know our service doesn't define the cohort (otherwise we wouldn't be in this fn)
			if (otherPatientUuid.equals(tmpCache.findPatientId())) {
				continue;
			}

			LOG.debug("Checking patient " + otherPatientId + " at service " + otherServiceUuidStr);
			if (checkPatientIsRegisteredAtServices(otherPatientUuid, otherServiceUuid, tmpCache, cohortOdsCodes)) {
				LOG.debug("PASS - Patient " + otherPatientId + " is registered at one of defining services");
				return true;
			}
		}

		LOG.debug("FAIL - Patient " + tmpCache.findPatientId() + " is NOT registered at one of defining services");
		return false;
	}

	private boolean checkPatientIsRegisteredAtServices(UUID patientUuid, UUID serviceId, TmpCache tmpCache, Set<String> cohortOdsCodes) throws Exception {

		Patient fhirPatient = tmpCache.findPatientResource(patientUuid, serviceId);
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

			String odsCode = findOdsCodeFromReference(tmpCache, careProviderReference, serviceId);

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
				if (cohortOdsCodes.contains(odsCode.toUpperCase())) {
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




	private boolean checkExplicitCohort(UUID protocolId, TmpCache tmpCache) throws PipelineException {
		//find the patient ID for the batch
		UUID patientId = null;
		try {
			patientId = tmpCache.findPatientId();
		} catch (Exception ex) {
			throw new PipelineException("Failed to find patient ID for batch " + tmpCache.getBatchId(), ex);
		}

		//if there's no patient ID, then this is admin resources batch, so return true so it goes through unfiltered
		if (patientId == null) {
			return true;
		}

		try {
			String nhsNumber = tmpCache.findPatientNhsNumber();
			//LOG.trace("patient ID " + patientId + " -> nhs number " + nhsNumber);

			if (Strings.isNullOrEmpty(nhsNumber)) {
				return false;
			}

			//no point moving the below to the TmpCache object since it only is specific to the protocol currently being checked
			UUID serviceId = tmpCache.getServiceId();
			PatientCohortDalI cohortRepository = DalProvider.factoryPatientCohortDal();
			boolean inCohort = cohortRepository.isInCohort(protocolId, serviceId, nhsNumber);
			//LOG.trace("protocol " + protocolId + " service " + serviceId + " nhs number " + nhsNumber + " -> in cohort " + inCohort);
			return inCohort;
		} catch (Exception ex) {
			throw new PipelineException("Exception in protocol " + protocolId, ex);
		}
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

	/**
	 * if a patient has multiple subscribers, then we end up retrieving the same stuff from the DB multiple times, so
	 * we can check if the patient falls into each subscribers cohort. This cache is intended to avoid that, by
	 * caching resources for a patient just for the duration of this class being used.
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

		private boolean checkedForNhsNumber = false;
		private String cachedNhsNumber = null;

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

			if (!checkedForNhsNumber) {
				PatientSearch patientSearchResult = patientSearchDal.searchByPatientId(findPatientId());
				if (patientSearchResult != null) {
					cachedNhsNumber = patientSearchResult.getNhsNumber();
				} else {
					cachedNhsNumber = null;
				}
				checkedForNhsNumber = true;
			}
			return cachedNhsNumber;
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

				//if looking for the resource for our main patient, then go back to find a non-deleted one if it is deleted
				boolean checkAllResourceHistory = patientUuid.equals(findPatientId());

				Patient fhirPatient = null;
				if (checkAllResourceHistory) {
					//when checking the patient resource at our own service, check using the most recent non-deleted instance
					fhirPatient = (Patient)retrieveNonDeletedResource(serviceId, ResourceType.Patient, patientUuid);
				} else {
					//when checking patient resources at other services, it makes sense to only count them if they're non-deleted
					fhirPatient = (Patient)resourceRepository.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientUuid.toString());
				}

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

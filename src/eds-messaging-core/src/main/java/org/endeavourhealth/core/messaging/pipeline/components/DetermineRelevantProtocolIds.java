package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.configuration.DetermineRelevantProtocolIdsConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.audit.ExchangeProtocolErrorDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.usermanager.caching.DataSharingAgreementCache;
import org.endeavourhealth.core.database.dal.usermanager.caching.OrganisationCache;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DataSharingAgreementEntity;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.transform.common.AuditWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DetermineRelevantProtocolIds extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(DetermineRelevantProtocolIds.class);

	private DetermineRelevantProtocolIdsConfig config;
	private static ExchangeProtocolErrorDalI errorDal = DalProvider.factoryExchangeProtocolErrorDal();

	public DetermineRelevantProtocolIds(DetermineRelevantProtocolIdsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {

		try {
			//DDS-UI approach
			String serviceUuid = exchange.getHeader(HeaderKeys.SenderServiceUuid);
			List<String> protocolIdsOldWay = getProtocolIdsForPublisherServiceOldWay(serviceUuid);
			exchange.setHeaderAsStringList(HeaderKeys.ProtocolIds, protocolIdsOldWay);
			boolean hasDpaOldWay = !protocolIdsOldWay.isEmpty(); //in the old way, we count as having a DPA if they're in any protocol

			//DSM approach
			String odsCode = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);
			List<String> sharingAgreementIdsNewWay = getSharingAgreementIdsNewWay(odsCode);
			exchange.setHeaderAsStringList(HeaderKeys.SharingAgreementIds, sharingAgreementIdsNewWay);
			boolean hasDpaNewWay = OrganisationCache.doesOrganisationHaveDPA(odsCode);

			//compare DSM and DDS-UI protocols to make sure nothing is configured wrong
			if (hasDpaNewWay != hasDpaOldWay) {
				String msg = "Difference between DSM processing agreement and DDS-UI protocols for " + odsCode + "\r\n"
						+ "DSM DPA = " + hasDpaNewWay
						+ "DDS-UI = " + hasDpaOldWay;
				SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, msg);
			}

			if (!hasDpaOldWay) {
				errorDal.save(exchange.getId());
				throw new PipelineException("No publisher protocols found for service " + serviceUuid);
			}

			AuditWriter.writeExchange(exchange);

		} catch (PipelineException pe) {
			//if we get a pipeline exception, just throw as is
			throw pe;

		} catch (Exception ex) {
			//if we get any other type of exception, it needs to be re-packaged
			throw new PipelineException("Error processing exchange " + exchange.getId(), ex);
		}

		//LOG.debug("Data distribution protocols identified");
	}

	private List<String> getSharingAgreementIdsNewWay(String odsCode) throws Exception {

		List<String> ret = new ArrayList<>();

		List<DataSharingAgreementEntity> sharingAgreements = DataSharingAgreementCache.getAllDSAsForPublisherOrg(odsCode);
		for (DataSharingAgreementEntity sharingAgreement: sharingAgreements) {
			String id = sharingAgreement.getUuid();
			ret.add(id);
		}

		return ret;
	}

	public static List<String> getProtocolIdsForPublisherServiceOldWay(String serviceUuid) throws PipelineException {

		//find all protocols where our service is an active publisher
		List<LibraryItem> protocolsForService = getProtocolsForPublisherServiceOldWay(serviceUuid);

		List<String> protocolIds = new ArrayList<>();
		for (LibraryItem libraryItem: protocolsForService) {
			protocolIds.add(libraryItem.getUuid());
		}
		return protocolIds;

	}


	private static List<LibraryItem> getProtocolsForPublisherServiceOldWay(String serviceUuid) throws PipelineException {

		try {
			List<LibraryItem> ret = new ArrayList<>();

			//the above fn will return is all protocols where the service is present, but we want to filter
			//that down to only ones where our service is an active publisher
			List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid, null); //passing null means don't filter on system ID

			for (LibraryItem libraryItem: libraryItems) {
				Protocol protocol = libraryItem.getProtocol();
				if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

					for (ServiceContract serviceContract : protocol.getServiceContract()) {
						if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
								&& serviceContract.getService().getUuid().equals(serviceUuid)
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






}

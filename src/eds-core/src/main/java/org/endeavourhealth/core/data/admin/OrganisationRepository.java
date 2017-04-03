package org.endeavourhealth.core.data.admin;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.admin.accessors.OrganisationAccessor;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.OrganisationEndUserLink;
import org.endeavourhealth.core.data.admin.models.Service;

import java.util.*;

public class OrganisationRepository extends Repository {
	public UUID save(Organisation organisation) {
		Mapper<Organisation> mapper = getMappingManager().mapper(Organisation.class);

		Set<UUID> additions;
		Set<UUID> deletions;

		if (organisation.getId() == null) {
			// New organisation, just save with all services as additions
			organisation.setId(UUID.randomUUID());
			additions = new TreeSet<>(organisation.getServices().keySet());
			deletions = new TreeSet<>();
		} else {
			// Existing organisation, update service links
			Organisation oldOrganisation = mapper.get(organisation.getId());

			additions = new TreeSet<>(organisation.getServices().keySet());
			additions.removeAll(oldOrganisation.getServices().keySet());

			deletions = new TreeSet<>(oldOrganisation.getServices().keySet());
			deletions.removeAll(organisation.getServices().keySet());
		}
		// Update the org
		BatchStatement batchStatement = new BatchStatement();
		batchStatement.add(mapper.saveQuery(organisation));

		Mapper<Service> serviceMapper = getMappingManager().mapper(Service.class);
		// Process removed services
		for (UUID serviceUuid : deletions) {
			Service service = serviceMapper.get(serviceUuid);
			service.getOrganisations().remove(organisation.getId());
			batchStatement.add(serviceMapper.saveQuery(service));
		}

		// Process added services
		for (UUID serviceUuid : additions) {
			Service service = serviceMapper.get(serviceUuid);
			service.getOrganisations().put(organisation.getId(), organisation.getName());
			batchStatement.add(serviceMapper.saveQuery(service));
		}

		getSession().execute(batchStatement);

		return organisation.getId();
	}

	public Set<Organisation> getByIds(Set<UUID> ids) {
		Set<Organisation> orgs = new HashSet<>();
		for (UUID id: ids) {
			Organisation org = getById(id);
			orgs.add(org);
		}
		return orgs;
	}

	public Organisation getById(UUID id) {
		Mapper<Organisation> mapper = getMappingManager().mapper(Organisation.class);
		return mapper.get(id);
	}

	public Organisation getByNationalId(String nationalId) {
		OrganisationAccessor accessor = getMappingManager().createAccessor(OrganisationAccessor.class);
		Iterator<Organisation> iterator = accessor.getByNationalId(nationalId).iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			return null;
		}
	}

	public void delete(Organisation organisation) {
		Mapper<Organisation> organisationMapper = getMappingManager().mapper(Organisation.class);
		Mapper<Service> serviceMapper = getMappingManager().mapper(Service.class);


		BatchStatement batchStatement = new BatchStatement();
		batchStatement.add(organisationMapper.deleteQuery(organisation));

		for (UUID serviceUuid : organisation.getServices().keySet()) {
			Service service = serviceMapper.get(serviceUuid);
			service.getOrganisations().remove(organisation.getId());
			batchStatement.add(serviceMapper.saveQuery(service));
		}

		getSession().execute(batchStatement);
	}

	public Iterable<Organisation> getAll() {
		OrganisationAccessor accessor = getMappingManager().createAccessor(OrganisationAccessor.class);
		return accessor.getAll();
	}

	public Iterable<OrganisationEndUserLink> getByUserId(UUID userId) {
		OrganisationAccessor accessor = getMappingManager().createAccessor(OrganisationAccessor.class);
		return accessor.getOrganisationEndUserLinkByEndUserId(userId);
	}

	public Iterable<Organisation> search(String searchData) {
		String rangeEnd = searchData + 'z';
		OrganisationAccessor accessor = getMappingManager().createAccessor(OrganisationAccessor.class);
		return accessor.search(searchData, rangeEnd);
	}

}


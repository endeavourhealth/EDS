package org.endeavourhealth.core.data.admin;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.admin.accessors.OrganisationAccessor;
import org.endeavourhealth.core.data.admin.models.*;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class OrganisationRepository extends Repository {
	public void save(Organisation organisation) {
		Mapper<Organisation> mapper = getMappingManager().mapper(Organisation.class);

		if (organisation.getId() == null) {
			// New organisation, just save
			mapper.save(organisation);
		} else {
			// Existing organisation, update service links
			Organisation oldOrganisation = mapper.get(organisation.getId());

			Set<UUID> additions = new TreeSet<>(organisation.getServices().keySet());
			additions.removeAll(oldOrganisation.getServices().keySet());

			Set<UUID> deletions =  new TreeSet<>(oldOrganisation.getServices().keySet());
			deletions.removeAll(organisation.getServices().keySet());

			BatchStatement batchStatement = new BatchStatement();
			ServiceRepository serviceRepository = new ServiceRepository();
			Mapper<Service> serviceMapper = getMappingManager().mapper(Service.class);

			// Update the org
			batchStatement.add(mapper.saveQuery(organisation));

			// Process removed services
			for (UUID serviceUuid : deletions) {
				Service service = serviceRepository.getById(serviceUuid);
				service.getOrganisations().remove(organisation.getId());
				batchStatement.add(serviceMapper.saveQuery(service));
			}

			// Process added services
			for (UUID serviceUuid : additions) {
				Service service = serviceRepository.getById(serviceUuid);
				service.getOrganisations().put(organisation.getId(), organisation.getName());
				batchStatement.add(serviceMapper.saveQuery(service));
			}

			getSession().execute(batchStatement);
		}
	}


	public Organisation getById(UUID id) {
		Mapper<Organisation> mapper = getMappingManager().mapper(Organisation.class);
		return mapper.get(id);
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


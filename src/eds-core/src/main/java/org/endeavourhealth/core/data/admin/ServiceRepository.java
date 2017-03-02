package org.endeavourhealth.core.data.admin;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.admin.accessors.ServiceAccessor;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.Service;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class ServiceRepository extends Repository {

	public UUID save(Service service) {
		Mapper<Service> mapper = getMappingManager().mapper(Service.class);

		Set<UUID> additions;
		Set<UUID> deletions;

		if (service.getId() == null) {
			// New service, just save with all orgs as additions
			service.setId(UUID.randomUUID());
			additions = new TreeSet<>(service.getOrganisations().keySet());
			deletions = new TreeSet<>();
		} else {
			// Existing service, update org links
			Service oldService = mapper.get(service.getId());

			additions = new TreeSet<>(service.getOrganisations().keySet());
			additions.removeAll(oldService.getOrganisations().keySet());

			deletions = new TreeSet<>(oldService.getOrganisations().keySet());
			deletions.removeAll(service.getOrganisations().keySet());
		}
		// Update the service
		BatchStatement batchStatement = new BatchStatement();
		batchStatement.add(mapper.saveQuery(service));

		Mapper<Organisation> orgMapper = getMappingManager().mapper(Organisation.class);
		// Process removed orgs
		for (UUID orgUuid : deletions) {
			Organisation organisation = orgMapper.get(orgUuid);
			organisation.getServices().remove(service.getId());
			batchStatement.add(orgMapper.saveQuery(organisation));
		}

		// Process added orgs
		for (UUID orgUuid : additions) {
			Organisation organisation = orgMapper.get(orgUuid);
			organisation.getServices().put(service.getId(), service.getName());
			batchStatement.add(orgMapper.saveQuery(organisation));
		}

		getSession().execute(batchStatement);

		return service.getId();
	}

	public Service getById(UUID id) {
		Mapper<Service> mapper = getMappingManager().mapper(Service.class);
		return mapper.get(id);
	}


	public void delete(Service service) {
		Mapper<Service> serviceMapper = getMappingManager().mapper(Service.class);
		Mapper<Organisation> orgMapper = getMappingManager().mapper(Organisation.class);

		BatchStatement batchStatement = new BatchStatement();
		batchStatement.add(serviceMapper.deleteQuery(service));

		for (UUID orgUuid : service.getOrganisations().keySet()) {
			Organisation organisation = orgMapper.get(orgUuid);
			if (organisation != null) {
				organisation.getServices().remove(service.getId());
				batchStatement.add(orgMapper.saveQuery(organisation));
			}
		}

		getSession().execute(batchStatement);
	}

	public Iterable<Service> getAll() {
		ServiceAccessor accessor = getMappingManager().createAccessor(ServiceAccessor.class);
		return accessor.getAll();
	}

	public Iterable<Service> search(String searchData) {
		String rangeEnd = searchData + 'z';
		ServiceAccessor accessor = getMappingManager().createAccessor(ServiceAccessor.class);
		return accessor.search(searchData, rangeEnd);
	}

	public Service getByLocalIdentifier(String localIdentifier) {
		ServiceAccessor accessor = getMappingManager().createAccessor(ServiceAccessor.class);
		Iterator<Service> iterator = accessor.getByLocalIdentifier(localIdentifier).iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			return null;
		}
	}

	public Service getByOrganisationNationalId(String nationalId) {
		OrganisationRepository organisationRepository = new OrganisationRepository();
		Organisation organisation = organisationRepository.getByNationalId(nationalId);
		if (organisation == null)
			return null;

		Iterator<UUID> iterator = organisation.getServices().keySet().iterator();
		if (iterator.hasNext()) {
			UUID serviceId = iterator.next();
			return getById(serviceId);
		} else {
			return null;
		}
	}
}


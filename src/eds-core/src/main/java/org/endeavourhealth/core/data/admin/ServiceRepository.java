package org.endeavourhealth.core.data.admin;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.admin.accessors.ServiceAccessor;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.Service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class ServiceRepository extends Repository {

	public UUID save(Service service) {
		Mapper<Service> mapper = getMappingManager().mapper(Service.class);

		if (service.getId() == null) {
			// New service, just save
			service.setId(UUID.randomUUID());
			mapper.save(service);
		} else {
			// Existing service, update org links
			Service oldService = mapper.get(service.getId());

			Set<UUID> additions = new TreeSet<>(service.getOrganisations().keySet());
			additions.removeAll(oldService.getOrganisations().keySet());

			Set<UUID> deletions =  new TreeSet<>(oldService.getOrganisations().keySet());
			deletions.removeAll(service.getOrganisations().keySet());

			BatchStatement batchStatement = new BatchStatement();
			OrganisationRepository organisationRepository = new OrganisationRepository();
			Mapper<Organisation> orgMapper = getMappingManager().mapper(Organisation.class);

			// Update the service
			batchStatement.add(mapper.saveQuery(service));

			// Process removed orgs
			for (UUID orgUuid : deletions) {
				Organisation organisation = organisationRepository.getById(orgUuid);
				organisation.getServices().remove(service.getId());
				batchStatement.add(orgMapper.saveQuery(organisation));
			}

			// Process added orgs
			for (UUID orgUuid : additions) {
				Organisation organisation = organisationRepository.getById(orgUuid);
				organisation.getServices().put(service.getId(), service.getName());
				batchStatement.add(orgMapper.saveQuery(organisation));
			}

			getSession().execute(batchStatement);
		}

		return service.getId();
	}

	public Service getById(UUID id) {
		Mapper<Service> mapper = getMappingManager().mapper(Service.class);
		return mapper.get(id);
	}

	public void delete(Service service) {
		Mapper<Service> mapper = getMappingManager().mapper(Service.class);
		mapper.delete(service);
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

}


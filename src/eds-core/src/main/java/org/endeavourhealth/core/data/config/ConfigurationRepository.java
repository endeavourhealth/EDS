package org.endeavourhealth.core.data.config;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.endeavourhealth.core.data.ehr.EventStoreMode;

import java.util.UUID;

public class ConfigurationRepository extends Repository {
    public static final UUID RABBIT_NODES = UUID.fromString("7b9dcacf-50b8-4224-8bcc-b93524fc9ae9");
    public static final UUID ROUTE_GROUPS = UUID.fromString("b9b14e26-5a52-4f36-ad89-f01e465c1361");

    public static final UUID KEYCLOAK_CONFIG = UUID.fromString("c865eb28-58dd-4ec4-9ca9-fda1273566bf");
    public static final UUID KEYCLOAK_CONFIG_SFTP_READER = UUID.fromString("f48c8bf3-a03d-411e-baf1-8712569ed67f");

    public void insert(ConfigurationResource configurationResource){
        if (configurationResource == null)
            throw new IllegalArgumentException("configurationResource is null");

        save(configurationResource, EventStoreMode.insert);
    }

    public void update(ConfigurationResource configurationResource){
        if (configurationResource == null)
            throw new IllegalArgumentException("configurationResource is null");

        save(configurationResource, EventStoreMode.update);
    }

    private void save(ConfigurationResource configurationResource, EventStoreMode mode){
        Mapper<ConfigurationResource> mapperConfigurationResource = getMappingManager().mapper(ConfigurationResource.class);

        BatchStatement batch = new BatchStatement()
                .add(mapperConfigurationResource.saveQuery(configurationResource));

        getSession().execute(batch);
    }

    public void delete(ConfigurationResource configurationResource){
        if (configurationResource == null)
            throw new IllegalArgumentException("configurationResource is null");

			Mapper<ConfigurationResource> mapperConfigurationResource = getMappingManager().mapper(ConfigurationResource.class);

        BatchStatement batch = new BatchStatement()
                .add(mapperConfigurationResource.deleteQuery(configurationResource));

        getSession().execute(batch);
    }

    public ConfigurationResource getByKey(UUID configurationId) {

        Mapper<ConfigurationResource> mapperConfigurationResource = getMappingManager().mapper(ConfigurationResource.class);
        return mapperConfigurationResource.get(configurationId);
    }
}

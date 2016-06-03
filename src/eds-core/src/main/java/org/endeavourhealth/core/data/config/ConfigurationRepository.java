package org.endeavourhealth.core.data.config;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.endeavourhealth.core.data.ehr.models.EventStoreMode;

import java.util.UUID;

public class ConfigurationRepository extends Repository {
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

package org.endeavourhealth.core.data.config.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "configuration", name = "configuration_resource")
public class ConfigurationResource {
	@PartitionKey
	@Column(name = "configuration_id")
	private UUID configurationId;

	@Column(name = "configuration_name")
	private String configurationName;

	@Column(name = "configuration_data")
	private String configurationData;

	public UUID getConfigurationId() {
		return configurationId;
	}

	public void setConfigurationId(UUID configurationId) {
		this.configurationId = configurationId;
	}

	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}

	public String getConfigurationData() {
		return configurationData;
	}

	public void setConfigurationData(String configurationData) {
		this.configurationData = configurationData;
	}
}

DROP KEYSPACE IF EXISTS configuration;
create keyspace configuration with replication = {'class':'SimpleStrategy', 'replication_factor':'1'};
USE configuration;

CREATE TABLE configuration_resource
  ( 
		configuration_id		UUID,
		configuration_name	VARCHAR,
    configuration_data	VARCHAR,
    PRIMARY KEY (configuration_id) 
  );

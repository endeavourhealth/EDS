use publisher_transform_???;

DROP TABLE IF EXISTS resource_id_map;
DROP TABLE IF EXISTS sus_resource_map;

CREATE TABLE resource_id_map (
	service_id char(36),
	resource_type varchar(50),
	source_id varchar(500),
	eds_id char(36),
    CONSTRAINT pk_resource_id_map PRIMARY KEY (service_id, resource_type, source_id)
);

CREATE INDEX ix_resource_id_map_resource_type_eds_id
ON resource_id_map (resource_type, eds_id);

CREATE TABLE sus_resource_map (
  service_id char(36),
  source_row_id varchar(200),
  destination_resource_type varchar(100),
  destination_resource_id char(36),
  CONSTRAINT pk_sus_resource_map PRIMARY KEY (service_id, source_row_id, destination_resource_type, destination_resource_id)
);

CREATE TABLE resource_merge_map (
  service_id char(36),
	resource_type varchar(50),
  source_resource_id char(36),
  destination_resource_id char(36),
  updated_at datetime,
  CONSTRAINT pk_resource_merge_map PRIMARY KEY (service_id, source_resource_id)
);
use publisher_transform;

DROP TABLE IF EXISTS emis_csv_code_map;
DROP TABLE IF EXISTS emis_admin_resource_cache;
DROP TABLE IF EXISTS resource_id_map;
DROP TABLE IF EXISTS sus_resource_map;

CREATE TABLE emis_csv_code_map (
	medication boolean,
	code_id bigint,
	code_type varchar(250),
	codeable_concept text,
	read_term varchar(500),
	read_code varchar(250),
	snomed_concept_id BIGINT,
	snomed_description_id BIGINT,
	snomed_term varchar(500),
	national_code varchar(250),
	national_code_category varchar(500),
	national_code_description varchar(500),
	parent_code_id bigint,
    CONSTRAINT pk_emis_csv_code_map PRIMARY KEY (medication, code_id)
);

CREATE TABLE emis_admin_resource_cache (
	data_sharing_agreement_guid varchar(36),
	emis_guid varchar(50), -- emis GUIDs are padded to longer than usual
	resource_type varchar(50),
	resource_data text,
    CONSTRAINT pk_emis_admin_resource_cache PRIMARY KEY (data_sharing_agreement_guid, emis_guid, resource_type)
);

CREATE TABLE resource_id_map (
	service_id varchar(36),
	system_id varchar(36),
	resource_type varchar(50),
	source_id varchar(500),
	eds_id varchar(36),
    CONSTRAINT pk_resource_id_map PRIMARY KEY (service_id, system_id, resource_type, source_id)
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
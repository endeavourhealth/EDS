use publisher_transform_???;

DROP TABLE IF EXISTS resource_id_map;
DROP TABLE IF EXISTS sus_resource_map;
DROP TABLE IF EXISTS resource_merge_map;
DROP TABLE IF EXISTS source_file_type;
DROP TABLE IF EXISTS source_file_type_column;
DROP TABLE IF EXISTS source_file;
DROP TABLE IF EXISTS source_file_record;
DROP TABLE IF EXISTS resource_field_mappings;
DROP TABLE IF EXISTS internal_id_map;
DROP TABLE IF EXISTS cerner_code_value_ref;
DROP TABLE IF EXISTS tpp_mapping_ref;
DROP TABLE IF EXISTS tpp_config_list_option;
DROP TABLE IF EXISTS tpp_immunisation_content;

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



CREATE TABLE source_file_type (
	id int NOT NULL,
  description varchar(255) NOT NULL,
  CONSTRAINT pk_source_file_type PRIMARY KEY (id)
);

ALTER TABLE source_file_type MODIFY COLUMN id INT auto_increment;

CREATE INDEX ix_source_file_type_description ON source_file_type (description);


CREATE TABLE source_file_type_column (
	source_file_type_id int NOT NULL,
    column_index tinyint NOT NULL,
    column_name varchar(255) NOT NULL,
    CONSTRAINT pk_source_file_type_column PRIMARY KEY (source_file_type_id, column_index)
);


CREATE TABLE source_file (
	id int NOT NULL,
  service_id char(36) NOT NULL,
  system_id char(36) NOT NULL,
  file_path varchar(1000),
  inserted_at datetime NOT NULL,
  source_file_type_id int NOT NULL,
  exchange_id char(36),
  CONSTRAINT pk_source_file PRIMARY KEY (id)
);

ALTER TABLE source_file MODIFY COLUMN id INT auto_increment;

CREATE INDEX ix_source_file_service_system_type_exchange_path ON source_file (service_id, system_id, source_file_type_id, exchange_id, file_path);

CREATE INDEX ix_source_file_service_system_date ON source_file (service_id, system_id, inserted_at);

CREATE TABLE source_file_record (
  id bigint,
	source_file_id int NOT NULL,
  source_location varchar(255),
  value mediumtext,
  CONSTRAINT pk_source_file_field PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

ALTER TABLE source_file_record MODIFY COLUMN id INT auto_increment;

CREATE INDEX ix_source_file_record_file_location ON source_file_record (source_file_id, source_location);

CREATE TABLE resource_field_mappings (
  resource_id char(36) NOT NULL,
  resource_type varchar(50) NOT NULL,
  created_at datetime NOT NULL,
  version char(36) NOT NULL,
  mappings_json MEDIUMTEXT,
  CONSTRAINT pk_resource_field_mapping PRIMARY KEY (resource_id, resource_type, created_at, version)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;


CREATE TABLE internal_id_map (
  service_id char(36) NOT NULL,
  id_type varchar(255) NOT NULL,
  source_id varchar(255) NOT NULL,
  destination_id varchar(255) NOT NULL,
  updated_at datetime,
  CONSTRAINT pk_internal_id_map PRIMARY KEY (service_id, id_type, source_id)
);

CREATE INDEX ix_internal_id_map_destination_id ON internal_id_map (service_id, id_type, destination_id, source_id);

create table cerner_code_value_ref (
  code_value_cd varchar(100) not null comment 'The value of the code',
  date date not null comment 'Date of the reference',
  active_ind boolean not null comment 'Whether the reference is active or not',
  code_desc_txt varchar(1000) not null comment 'Description of the code',
  code_disp_txt varchar(1000) not null comment 'Display term of the code',
  code_meaning_txt varchar(1000) not null comment 'The meaning of the code',
  code_set_nbr bigint(20) not null comment 'Code set number',
  code_set_desc_txt varchar(1000) not null comment 'Description of the code set',
  alias_nhs_cd_alias varchar(1000) not null comment 'NHS alias',
  service_id varchar(36) not null comment 'The service the code value ref corresponds to',
  audit_json mediumtext null comment 'Used for Audit Purposes',

  constraint cerner_code_value_ref_pk primary key (service_id, code_set_nbr, code_value_cd)
);

CREATE INDEX ix_cerner_code_value_ref ON cerner_code_value_ref (code_value_cd);


create table tpp_mapping_ref (
  row_id bigint(20) not null comment 'The value of RowIdentifier',
  group_id bigint(20) not null comment 'Mapping group identifier',
  mapped_term varchar(1000) not null comment 'The mapped term of the RowIdentifier',
  service_id varchar(36) not null comment 'The service the mapping corresponds to',
  audit_json mediumtext null comment 'Used for Audit Purposes',

  constraint tpp_mapping_ref_pk primary key (service_id, group_id, row_id)
);

CREATE INDEX ix_tpp_mapping_ref ON tpp_mapping_ref (row_id);


create table tpp_config_list_option (
  row_id bigint(20) not null comment 'The value of RowIdentifier',
  config_list_id bigint(20) not null comment 'Configuration list identifier',
  list_option_name varchar(1000) not null comment 'The configuration list option name',
  service_id varchar(36) not null comment 'The service the mapping corresponds to',
  audit_json mediumtext null comment 'Used for Audit Purposes',

  constraint tpp_config_list_option_pk primary key (service_id, config_list_id, row_id)
);

CREATE INDEX ix_tpp_config_list_option ON tpp_config_list_option (row_id);


CREATE TABLE multilex_to_ctv3_map
(
  row_id bigint NOT NULL PRIMARY KEY,
  multilex_product_id bigint NOT NULL,
  ctv3_read_code varchar(5) NOT NULL,
  ctv3_read_term text NOT NULL
);

CREATE INDEX ix_multilex_to_ctv3_map_multilex_product_id
  ON multilex_to_ctv3_map (multilex_product_id);


CREATE TABLE ctv3_hierarchy_ref
(
  row_id bigint NOT NULL PRIMARY KEY,
  ctv3_parent_read_code varchar(5) NOT NULL,
  ctv3_child_read_code varchar(5) NOT NULL,
  child_level integer NOT NULL
);

CREATE INDEX ix_ctv3_hierarchy_ref_parent_read_code_child_read_code
  ON ctv3_hierarchy_ref (ctv3_parent_read_code, ctv3_child_read_code);

create table tpp_immunisation_content (
  row_id bigint(20) not null comment 'The value of RowIdentifier',
  name varchar(100) not null comment 'The name of the immunisation',
  content varchar(255) not null comment 'The contents of the immunisation',
  service_id varchar(36) not null comment 'The service the imumnisation content corresponds to',
  date_deleted datetime null comment 'The date the vaccination was deleted',
  audit_json mediumtext null comment 'Used for Audit Purposes',

  constraint tpp_immunisation_content_pk primary key (row_id)
);
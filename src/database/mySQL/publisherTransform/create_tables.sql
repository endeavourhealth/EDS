use publisher_transform_???;

DROP TABLE IF EXISTS resource_id_map;
DROP TABLE IF EXISTS sus_resource_map;
DROP TABLE IF EXISTS resource_merge_map;
DROP TABLE IF EXISTS source_file_type; -- obsolete, so just drop table
DROP TABLE IF EXISTS source_file_type_column; -- obsolete, so just drop table
DROP TABLE IF EXISTS source_file; -- obsolete, so just drop table
DROP TABLE IF EXISTS source_file_record; -- obsolete, so just drop table
DROP TABLE IF EXISTS resource_field_mappings;
DROP TABLE IF EXISTS internal_id_map;
DROP TABLE IF EXISTS cerner_code_value_ref;
DROP TABLE IF EXISTS cerner_nomenclature_ref;
DROP TABLE IF EXISTS cerner_clinical_event_mapping_state;
DROP TABLE IF EXISTS tpp_config_list_option;
DROP TABLE IF EXISTS source_file_record_audit;

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



/*CREATE TABLE source_file_type (
	id int NOT NULL,
  description varchar(255) NOT NULL,
  CONSTRAINT pk_source_file_type PRIMARY KEY (id)
);

ALTER TABLE source_file_type MODIFY COLUMN id INT auto_increment;

CREATE INDEX ix_source_file_type_description ON source_file_type (description);


CREATE TABLE source_file_type_column (
  source_file_type_id int NOT NULL,
  column_index tinyint unsigned NOT NULL,
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
  new_published_file_id int COMMENT 'refers to the audit.published_file table',
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

CREATE INDEX ix_source_file_record_file_location ON source_file_record (source_file_id, source_location);*/

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

CREATE INDEX ix_cerner_code_value_ref_set ON cerner_code_value_ref (service_id, code_set_nbr);

create table cerner_nomenclature_ref (

  service_id char(36) not null,
  nomenclature_id bigint not null,
  active bool,
  mneomonic_text text,
  value_text text comment 'for SNOMED this the description ID',
  display_text text,
  description_text text comment 'for SNOMED this is the term',
  nomenclature_type_code bigint,
  vocabulary_code bigint comment 'links to cerner_code_value_ref and gives the code type, but this is also in the below field',
  concept_identifier text comment 'contains the SNOMED concept ID',
  audit_json mediumtext,
  constraint cerner_code_value_ref_pk primary key (service_id, nomenclature_id)
);

CREATE INDEX ix_cerner_nomenclature_ref ON cerner_nomenclature_ref (nomenclature_id);

CREATE TABLE cerner_clinical_event_mapping_state (
  service_id char(36),
  event_id bigint,
  event_cd varchar(10) comment 'Almost all are numeric but there are a small number of non-numeric ones',
  event_cd_term varchar(50),
  event_class_cd varchar(10),
  event_class_cd_term varchar(50),
  event_results_units_cd varchar(10),
  event_results_units_cd_term varchar(50),
  event_result_text varchar(255),
  event_title_text varchar(255),
  event_tag_text varchar(255),
  mapped_snomed_concept_id varchar(50),
  dt_mapping_updated datetime,
  CONSTRAINT pk_internal_id_map PRIMARY KEY (service_id, event_id)
);

create table tpp_config_list_option (
  row_id bigint(20) not null comment 'The value of RowIdentifier',
  config_list_id bigint(20) not null comment 'Configuration list identifier',
  list_option_name varchar(1000) not null comment 'The configuration list option name',
  service_id varchar(36) not null comment 'The service the mapping corresponds to',
  audit_json mediumtext null comment 'Used for Audit Purposes',

  constraint tpp_config_list_option_pk primary key (service_id, config_list_id, row_id)
);

CREATE INDEX ix_tpp_config_list_option ON tpp_config_list_option (row_id);


CREATE TABLE source_file_record_audit (
  id int,
  record_number int,
  published_file_id int,
  PRIMARY KEY (id)
) COMMENT 'table to replace the other four source_file... tables, joining the old audit data to the new audit.published_file... tables';
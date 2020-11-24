use publisher_common;

DROP TABLE IF EXISTS emis_csv_code_map; -- old table
DROP TABLE IF EXISTS emis_admin_resource_cache; -- old table
DROP TABLE IF EXISTS emis_admin_resource_cache_applied;
DROP TABLE IF EXISTS emis_missing_code_error;
DROP TABLE IF EXISTS tpp_immunisation_content; -- old table
DROP TABLE IF EXISTS tpp_immunisation_content_2;
DROP TABLE IF EXISTS tpp_ctv3_hierarchy_ref; -- old table
DROP TABLE IF EXISTS tpp_ctv3_hierarchy_ref_2;
DROP TABLE IF EXISTS tpp_ctv3_lookup; -- old table
DROP TABLE IF EXISTS tpp_ctv3_lookup_2;
DROP TABLE IF EXISTS tpp_multilex_to_ctv3_map; -- old table
DROP TABLE IF EXISTS tpp_multilex_to_ctv3_map_2;
DROP TABLE IF EXISTS tpp_mapping_ref; -- old table
DROP TABLE IF EXISTS tpp_mapping_ref_2;
DROP TABLE IF EXISTS tpp_config_list_option_2;
DROP TABLE IF EXISTS tpp_staff_member;
DROP TABLE IF EXISTS tpp_staff_member_profile;
DROP TABLE IF EXISTS tpp_multilex_action_group_lookup;
DROP TABLE IF EXISTS tpp_ctv3_to_snomed;
DROP TABLE IF EXISTS emis_location;
DROP TABLE IF EXISTS emis_organisation;
DROP TABLE IF EXISTS emis_user_in_role;
DROP TABLE IF EXISTS emis_organisation_location;
DROP TABLE IF EXISTS emis_drug_code;
DROP TABLE IF EXISTS emis_clinical_code_hiearchy;
DROP TABLE IF EXISTS emis_clinical_code;
DROP TABLE IF EXISTS vision_read2_lookup; -- drop this old table and don't recreate
DROP TABLE IF EXISTS vision_read2_code;
DROP TABLE IF EXISTS vision_read2_to_snomed_map;


/*CREATE TABLE emis_csv_code_map (
	medication boolean,
	code_id bigint,
	code_type varchar(250),
	read_term varchar(500),
	read_code varchar(250) BINARY, -- note binary keyword to make case sensitive
	snomed_concept_id BIGINT,
	snomed_description_id BIGINT,
	snomed_term varchar(500),
	national_code varchar(250),
	national_code_category varchar(500),
	national_code_description varchar(500),
	parent_code_id bigint,
	audit_json MEDIUMTEXT,
	dt_last_received datetime,
	adjusted_code varchar(50) BINARY, -- note binary keyword to make case sensitive
	codeable_concept_system varchar(255),
	CONSTRAINT pk_emis_csv_code_map PRIMARY KEY (medication, code_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;
*/

/*CREATE TABLE emis_admin_resource_cache (
	data_sharing_agreement_guid varchar(36),
	emis_guid varchar(50), -- emis GUIDs are padded to longer than usual
	resource_type varchar(50),
	resource_data text,
	audit_json MEDIUMTEXT,
	CONSTRAINT pk_emis_admin_resource_cache PRIMARY KEY (data_sharing_agreement_guid, emis_guid, resource_type)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;*/

CREATE TABLE emis_admin_resource_cache_applied (
	service_id char(36) NOT NULL COMMENT 'links to admin.service',
	data_sharing_agreement_guid varchar(36) NOT NULL COMMENT 'so we know what data sharing GUID was applied',
	date_applied datetime NOT NULL COMMENT 'datetime the admin cache was applied',
	CONSTRAINT pk_emis_admin_resource_cache_applied PRIMARY KEY (service_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;


CREATE TABLE tpp_ctv3_lookup_2 (
	ctv3_code varchar(5) BINARY COMMENT 'ctv3 code itself',
	ctv3_term varchar(255) null COMMENT 'term for ctv3 code',
	dt_last_updated datetime NOT NULL,
	CONSTRAINT pk_tpp_ctv3_lookup_2 PRIMARY KEY (ctv3_code)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_code_updated ON tpp_ctv3_lookup_2 (ctv3_code, dt_last_updated);

/*CREATE TABLE tpp_ctv3_lookup (
	ctv3_code varchar(5) COLLATE utf8_bin COMMENT 'ctv3 code itself',
	ctv3_text varchar(255) null COMMENT 'term for ctv3 code',
	audit_json mediumtext null comment 'Used for Audit Purposes',
	CONSTRAINT pk_tpp_ctv3_lookup PRIMARY KEY (ctv3_code)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_tpp_ctv3_lookup_ctv3_code
  ON tpp_ctv3_lookup (ctv3_code);
*/


CREATE TABLE tpp_multilex_to_ctv3_map_2
(
	multilex_product_id int NOT NULL,
	ctv3_code varchar(5) BINARY NOT NULL,
	ctv3_term text NOT NULL,
	dt_last_updated datetime NOT NULL,
	constraint pk primary key (multilex_product_id)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON tpp_multilex_to_ctv3_map_2 (multilex_product_id, dt_last_updated);




/*CREATE TABLE tpp_multilex_to_ctv3_map
(
  row_id bigint NOT NULL PRIMARY KEY,
  multilex_product_id bigint NOT NULL,
  ctv3_read_code varchar(5) NOT NULL,
  ctv3_read_term text NOT NULL,
  audit_json mediumtext null comment 'Used for Audit Purposes'
);

CREATE INDEX ix_tpp_multilex_to_ctv3_map_multilex_product_id
  ON tpp_multilex_to_ctv3_map (multilex_product_id);*/



CREATE TABLE tpp_ctv3_hierarchy_ref_2
(
	parent_code varchar(5) BINARY NOT NULL,
	child_code varchar(5) BINARY NOT NULL,
	child_level integer NOT NULL,
	dt_last_updated datetime NOT NULL,
	CONSTRAINT pk_tpp_ctv3_hierarchy_ref_2 PRIMARY KEY (child_code, parent_code)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

/*
CREATE TABLE tpp_ctv3_hierarchy_ref
(
  row_id bigint NOT NULL PRIMARY KEY,
  ctv3_parent_read_code varchar(5) NOT NULL COLLATE utf8_bin,
  ctv3_child_read_code varchar(5) NOT NULL COLLATE utf8_bin,
  child_level integer NOT NULL
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_tpp_ctv3_hierarchy_ref_parent_read_code_child_read_code
  ON tpp_ctv3_hierarchy_ref (ctv3_parent_read_code, ctv3_child_read_code);
*/

create table tpp_immunisation_content_2 (
	row_id int not null comment 'The value of RowIdentifier',
	name varchar(100) not null comment 'The name of the immunisation',
	content varchar(255) not null comment 'The contents of the immunisation',
	dt_last_updated datetime NOT NULL,
	constraint pk primary key (row_id)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON tpp_immunisation_content_2 (row_id, dt_last_updated);



/*create table tpp_immunisation_content (
  row_id bigint(20) not null comment 'The value of RowIdentifier',
  name varchar(100) not null comment 'The name of the immunisation',
  content varchar(255) not null comment 'The contents of the immunisation',
  date_deleted datetime null comment 'The date the vaccination was deleted',
  audit_json mediumtext null comment 'Used for Audit Purposes',

  constraint tpp_immunisation_content_pk primary key (row_id)
);
*/


create table tpp_mapping_ref_2 (
	row_id int not null comment 'The value of RowIdentifier',
	group_id int not null comment 'Mapping group identifier',
	mapped_term varchar(1000) not null comment 'The mapped term of the RowIdentifier',
	dt_last_updated datetime NOT NULL,
	constraint pk primary key (row_id)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON tpp_mapping_ref_2 (row_id, dt_last_updated);



/*create table tpp_mapping_ref (
	row_id bigint(20) not null comment 'The value of RowIdentifier',
	group_id bigint(20) not null comment 'Mapping group identifier',
	mapped_term varchar(1000) not null comment 'The mapped term of the RowIdentifier',
	audit_json mediumtext null comment 'Used for Audit Purposes',

	constraint tpp_mapping_ref_pk primary key (group_id, row_id)
);

CREATE INDEX ix_tpp_mapping_ref ON tpp_mapping_ref (row_id);*/


CREATE TABLE emis_missing_code_error (
	service_id char(36) NOT NULL,
	exchange_id char(36) NOT NULL,
	timestmp datetime NOT NULL,
	file_type varchar(255) NOT NULL,
	patient_guid varchar(255) NOT NULL,
	code_id bigint NOT NULL,
	record_guid varchar(255) NOT NULL,
	dt_fixed datetime,
	code_type char(1) NOT NULL,
	CONSTRAINT pk_emis_missing_code_error PRIMARY KEY (service_id, exchange_id, file_type, patient_guid, code_id, record_guid)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix on emis_missing_code_error (code_id);

CREATE INDEX ix2 ON emis_missing_code_error (service_id, dt_fixed, code_type);

CREATE INDEX ix3 ON emis_missing_code_error (service_id, dt_fixed, code_id);

CREATE INDEX ix4 ON emis_missing_code_error (dt_fixed, code_type, code_id);


create table tpp_config_list_option_2 (
	row_id int not null comment 'The value of RowIdentifier',
	config_list_id int not null comment 'Configuration list identifier',
	list_option_name varchar(1000) not null comment 'The configuration list option name',
	dt_last_updated datetime NOT NULL,
	constraint tpp_config_list_option_pk primary key (row_id)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON tpp_config_list_option_2 (row_id, dt_last_updated);



create table tpp_staff_member (
	row_id int NOT NULL COMMENT 'unique TPP identifier',
	staff_name varchar(255),
	username varchar(255),
	national_id_type varchar(50),
	national_id varchar(50),
	smartcard_id varchar(50),
	published_file_id INT COMMENT 'where this came from, relates to audit.published_file',
	published_file_record_number INT COMMENT 'record number in source file',
	dt_last_updated datetime COMMENT 'data date time that this was last updated from',
	CONSTRAINT pk_tpp_staging_staff_member PRIMARY KEY (row_id)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix ON tpp_staff_member (row_id, dt_last_updated);





create table tpp_staff_member_profile (
	row_id int NOT NULL COMMENT 'unique TPP identifier',
	organisation_id varchar(255) COMMENT 'ODS code of owning org',
	staff_member_row_id int COMMENT 'unique TPP ID of the staff member record this is for',
	start_date date,
	end_date date,
	role_name varchar(255) COMMENT 'textual description of role type',
	ppa_id varchar(255),
	gp_local_code varchar(255),
	gmp_id varchar(255),
	removed_data boolean,
	published_file_id INT COMMENT 'where this came from, relates to audit.published_file',
	published_file_record_number INT COMMENT 'record number in source file',
	dt_last_updated datetime COMMENT 'data date time that this was last updated from',
	CONSTRAINT pk_tpp_staging_staff_member_profile PRIMARY KEY (row_id)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix ON tpp_staff_member_profile (row_id, dt_last_updated);

-- indexes for finding profiles for staff members
CREATE INDEX ix2 ON tpp_staff_member_profile (staff_member_row_id, organisation_id, row_id);
CREATE INDEX ix3 ON tpp_staff_member_profile (staff_member_row_id, row_id);





create table emis_location (
	location_guid varchar(255) NOT NULL,
	location_name varchar(255),
	location_type_description varchar(255),
	parent_location_guid varchar(255),
	open_date date,
	close_date date,
	main_contact_name varchar(255),
	fax_number varchar(255),
	email_address varchar(255),
	phone_number varchar(255),
	house_name_flat_number varchar(255),
	number_and_street varchar(255),
	village varchar(255),
	town varchar(255),
	county varchar(255),
	postcode varchar(255),
	deleted boolean,
	published_file_id int,
	published_file_record_number int,
	dt_last_updated datetime,
	constraint pk primary key (location_guid)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON emis_location (location_guid, dt_last_updated);



create table emis_organisation (
	organisation_guid varchar(255),
	cdb varchar(255),
	organisation_name varchar(255),
	ods_code varchar(255),
	parent_organisation_guid varchar(255),
	ccg_organisation_guid varchar(255),
	organisation_type varchar(255),
	open_date date,
	close_date date,
	main_location_guid varchar(255),
	published_file_id int,
	published_file_record_number int,
	dt_last_updated datetime,
	constraint pk primary key (organisation_guid)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON emis_organisation (organisation_guid, dt_last_updated);



create table emis_user_in_role (
	user_in_role_guid varchar(255),
	organisation_guid varchar(255),
	title varchar(255),
	given_name varchar(255),
	surname varchar(255),
	job_category_code varchar(255),
	job_category_name varchar(255),
	contract_start_date date,
	contract_end_date date,
	published_file_id int,
	published_file_record_number int,
	dt_last_updated datetime,
	constraint pk primary key (user_in_role_guid)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON emis_user_in_role (user_in_role_guid, dt_last_updated);



create table emis_organisation_location (
	organisation_guid varchar(255),
	location_guid varchar(255),
	is_main_location boolean,
	deleted boolean,
	published_file_id int,
	published_file_record_number int,
	dt_last_updated datetime,
	constraint pk primary key (location_guid, organisation_guid)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON emis_organisation_location (location_guid, organisation_guid, dt_last_updated);


create table emis_drug_code (
	code_id bigint NOT NULL,
	dmd_concept_id bigint,
	dmd_term varchar(500),
	dt_last_updated datetime,
	constraint pk primary key (code_id)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON emis_drug_code (code_id, dt_last_updated);



create table emis_clinical_code (
	code_id bigint NOT NULL,
	code_type varchar(250),
	read_term varchar(500),
	read_code varchar(250) BINARY, -- note binary keyword to make case sensitive
	snomed_concept_id BIGINT,
	snomed_description_id BIGINT,
	snomed_term varchar(500),
	national_code varchar(250),
	national_code_category varchar(500),
	national_code_description varchar(500),
	-- parent_code_id bigint, -- removed
	adjusted_code varchar(50) BINARY, -- note binary keyword to make case sensitive
	is_emis_code boolean NOT NULL,
	dt_last_updated datetime,
	constraint pk primary key (code_id)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_row_date ON emis_clinical_code (code_id, dt_last_updated);


CREATE TABLE emis_clinical_code_hiearchy (
	code_id bigint NOT NULL,
	parent_code_id bigint NOT NULL,
	dt_last_updated datetime,
	CONSTRAINT pk PRIMARY KEY (code_id, parent_code_id)
);

CREATE INDEX ix_code_date ON emis_clinical_code_hiearchy (code_id, dt_last_updated);

CREATE TABLE tpp_multilex_action_group_lookup (
	action_group_id int NOT NULL,
	action_group_name varchar(255) NOT NULL,
	constraint pk primary key (action_group_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;


/*CREATE TABLE vision_read2_lookup (
	read_code varchar(5) binary COMMENT 'read2 code itself',
	read_term varchar(255) null COMMENT 'term for read2 code',
	snomed_concept_id bigint COMMENT 'mapped snomed concept ID',
	is_vision_code boolean NOT NULL COMMENT 'whether true Read2 or locally added',
	dt_last_updated datetime NOT NULL,
	CONSTRAINT pk_vision_read2_lookup PRIMARY KEY (read_code)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

CREATE INDEX ix_code_updated ON vision_read2_lookup (read_code, dt_last_updated);*/

CREATE TABLE  tpp_ctv3_to_snomed (
  ctv3_code varchar(5) BINARY NOT NULL,
  snomed_concept_id bigint(20) NOT NULL,
  dt_last_updated datetime NOT NULL,
  CONSTRAINT tpp_ctv3_to_snomed_ctv3_code_pk PRIMARY KEY (ctv3_code )
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8;

-- used when updating this table
CREATE INDEX ix_code_updated ON tpp_ctv3_to_snomed (ctv3_code, dt_last_updated);

-- used for exporting code mappings
CREATE INDEX ix_updated ON tpp_ctv3_to_snomed (dt_last_updated);



CREATE TABLE vision_read2_code (
	read_code varchar(5) BINARY NOT NULL COMMENT 'read2 code',
	read_term varchar(255) NOT NULL COMMENT 'term for read2 code',
	is_vision_code boolean NOT NULL COMMENT 'whether true Read2 or locally added',
	approx_usage int NOT NULL COMMENT 'approximate count of usage in DDS',
	dt_last_updated datetime NOT NULL COMMENT 'last time a record changed (not counting the usage count)',
	CONSTRAINT pk_vision_read2_lookup PRIMARY KEY (read_code, read_term)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8
	COMMENT 'Vision Read2 codes have multiple terms with no indicator which is preferred, so this table stores them all';

CREATE TABLE vision_read2_to_snomed_map (
	read_code varchar(5) BINARY NOT NULL COMMENT 'read2 code',
	snomed_concept_id bigint(20) NOT NULL COMMENT 'mapped snomed concept ID',
	d_last_used date NOT NULL COMMENT 'tells us the date this mapping was last used to help calculate if a mapping has changed',
	dt_last_updated datetime NOT NULL COMMENT 'when this mapping last changed',
	CONSTRAINT pk_vision_read2_lookup PRIMARY KEY (read_code)
)
	ROW_FORMAT=COMPRESSED
	KEY_BLOCK_SIZE=8
	COMMENT 'Stores the Vision Read2/local code to Snomed mappings';


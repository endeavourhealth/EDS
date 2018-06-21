use publisher_common;

DROP TABLE IF EXISTS emis_csv_code_map;
DROP TABLE IF EXISTS emis_admin_resource_cache;
DROP TABLE IF EXISTS tpp_immunisation_content;
DROP TABLE IF EXISTS tpp_ctv3_hierarchy_ref;
DROP TABLE IF EXISTS tpp_ctv3_lookup;
DROP TABLE IF EXISTS tpp_multilex_to_ctv3_map;
DROP TABLE IF EXISTS tpp_mapping_ref;

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
	audit_json MEDIUMTEXT,
	CONSTRAINT pk_emis_csv_code_map PRIMARY KEY (medication, code_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

CREATE TABLE emis_admin_resource_cache (
	data_sharing_agreement_guid varchar(36),
	emis_guid varchar(50), -- emis GUIDs are padded to longer than usual
	resource_type varchar(50),
	resource_data text,
	audit_json MEDIUMTEXT,
	CONSTRAINT pk_emis_admin_resource_cache PRIMARY KEY (data_sharing_agreement_guid, emis_guid, resource_type)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

CREATE TABLE tpp_ctv3_lookup (
	row_id bigint(20) not null comment 'The value of RowIdentifier',
	ctv3_code varchar(5) null COLLATE utf8_bin,
	ctv3_text varchar(255) null,
	audit_json mediumtext null comment 'Used for Audit Purposes',
	CONSTRAINT pk_tpp_ctv3_lookup PRIMARY KEY (row_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

CREATE INDEX ix_tpp_ctv3_lookup_ctv3_code
  ON tpp_ctv3_lookup (ctv3_code);

CREATE TABLE tpp_multilex_to_ctv3_map
(
  row_id bigint NOT NULL PRIMARY KEY,
  multilex_product_id bigint NOT NULL,
  ctv3_read_code varchar(5) NOT NULL,
  ctv3_read_term text NOT NULL,
  audit_json mediumtext null comment 'Used for Audit Purposes'
);

CREATE INDEX ix_tpp_multilex_to_ctv3_map_multilex_product_id
  ON tpp_multilex_to_ctv3_map (multilex_product_id);


CREATE TABLE tpp_ctv3_hierarchy_ref
(
  row_id bigint NOT NULL PRIMARY KEY,
  ctv3_parent_read_code varchar(5) NOT NULL COLLATE utf8_bin,
  ctv3_child_read_code varchar(5) NOT NULL COLLATE utf8_bin,
  child_level integer NOT NULL
);

CREATE INDEX ix_tpp_ctv3_hierarchy_ref_parent_read_code_child_read_code
  ON tpp_ctv3_hierarchy_ref (ctv3_parent_read_code, ctv3_child_read_code);

create table tpp_immunisation_content (
  row_id bigint(20) not null comment 'The value of RowIdentifier',
  name varchar(100) not null comment 'The name of the immunisation',
  content varchar(255) not null comment 'The contents of the immunisation',
  date_deleted datetime null comment 'The date the vaccination was deleted',
  audit_json mediumtext null comment 'Used for Audit Purposes',

  constraint tpp_immunisation_content_pk primary key (row_id)
);

create table tpp_mapping_ref (
	row_id bigint(20) not null comment 'The value of RowIdentifier',
	group_id bigint(20) not null comment 'Mapping group identifier',
	mapped_term varchar(1000) not null comment 'The mapped term of the RowIdentifier',
	audit_json mediumtext null comment 'Used for Audit Purposes',

	constraint tpp_mapping_ref_pk primary key (group_id, row_id)
);

CREATE INDEX ix_tpp_mapping_ref ON tpp_mapping_ref (row_id);
use publisher_common;

DROP TABLE IF EXISTS emis_csv_code_map;
DROP TABLE IF EXISTS emis_admin_resource_cache;

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
	ctv3_code varchar(5) null,
	ctv3_text varchar(255) null,
	CONSTRAINT pk_tpp_ctv3_lookup PRIMARY KEY (row_id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

CREATE INDEX ix_tpp_ctv3_lookup_ctv3_code
  ON tpp_ctv3_lookup (ctv3_code);


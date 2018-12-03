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

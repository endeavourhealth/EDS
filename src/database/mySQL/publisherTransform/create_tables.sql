use publisher_transform_???;

DROP TABLE IF EXISTS resource_id_map;
DROP TABLE IF EXISTS sus_resource_map;
DROP TABLE IF EXISTS resource_merge_map;
DROP TABLE IF EXISTS source_file_type;
DROP TABLE IF EXISTS source_file_type_column;
DROP TABLE IF EXISTS source_file;
DROP TABLE IF EXISTS source_file_field;
DROP TABLE IF EXISTS resource_field_mapping;

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


CREATE INDEX ix_source_file ON source_file (service_id, system_id, inserted_at);


CREATE TABLE source_file_field (
  id bigint,
	source_file_id int NOT NULL,
  row_index int,
  column_index tinyint,
  source_location varchar(255),
  value mediumtext,
  CONSTRAINT pk_source_file_field PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

ALTER TABLE source_file_field MODIFY COLUMN id INT auto_increment;

CREATE TABLE resource_field_mapping (
  resource_id char(36) NOT NULL,
  resource_type varchar(50) NOT NULL,
  created_at datetime NOT NULL,
  version char(36) NOT NULL,
  resource_field varchar(255) NOT NULL,
  source_file_field_id bigint NOT NULL,
  CONSTRAINT pk_resource_field_mapping PRIMARY KEY (resource_id, resource_type, created_at, resource_field)
);


CREATE TABLE internal_id_map (
  service_id char(36),
	id_type varchar(255),
  source_id varchar(255),
  destination_id varchar(255),
  updated_at datetime,
  CONSTRAINT pk_internal_id_map PRIMARY KEY (service_id, id_type, source_id)
);

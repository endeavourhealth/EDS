USE audit;

DROP TABLE IF EXISTS `exchange`;
DROP TABLE IF EXISTS exchange_event;
DROP TABLE IF EXISTS exchange_transform_audit;
DROP TABLE IF EXISTS exchange_transform_error_state;
DROP TABLE IF EXISTS user_event;
DROP TABLE IF EXISTS queued_message;
DROP TABLE IF EXISTS queued_message_type;
DROP TABLE IF EXISTS exchange_batch;
DROP TABLE IF EXISTS exchange_subscriber_transform_audit;
DROP TABLE IF EXISTS transform_warning_type;
DROP TABLE IF EXISTS transform_warning;
DROP TABLE IF EXISTS exchange_general_error;
DROP TABLE IF EXISTS exchange_protocol_error;
DROP TABLE IF EXISTS subscriber_api_audit;
DROP TABLE IF EXISTS published_file_type;
DROP TABLE IF EXISTS published_file_type_column;
DROP TABLE IF EXISTS published_file;
DROP TABLE IF EXISTS published_file_record;
DROP TABLE IF EXISTS last_data_received;
DROP TABLE IF EXISTS last_data_processed;
DROP TABLE IF EXISTS exchange_subscriber_send_audit;

CREATE TABLE `exchange`
(
    id char(36) NOT NULL,
    timestamp datetime(3) comment 'precision 3 gives us ms-level, which is sufficient for accurate sorting',
    headers text,
    service_id char(36),
    system_id char(36),
    body mediumtext,
    CONSTRAINT pk_exchange PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

CREATE INDEX ix_exchange_service_id
ON `exchange` (service_id, system_id, timestamp);

CREATE TABLE exchange_event
(
  id varchar(36) NOT NULL,
  exchange_id varchar(36) NOT NULL,
  timestamp datetime(3) NOT NULL,
  event_desc MEDIUMTEXT,
  CONSTRAINT pk_exchange_event PRIMARY KEY (exchange_id, timestamp ASC, id)
);

CREATE TABLE exchange_transform_audit
(
	id varchar(36) NOT NULL,
	service_id varchar(36) NOT NULL,
  system_id varchar(36) NOT NULL,
  exchange_id varchar(36) NOT NULL,
  started datetime,
  ended datetime,
  error_xml mediumtext,
  resubmitted boolean,
  deleted datetime,
  number_batches_created int,
  CONSTRAINT pk_exchange_transform_audit PRIMARY KEY (service_id, system_id, exchange_id, id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;


CREATE INDEX ix_exchange_transform_audit_service_system_started
ON exchange_transform_audit (service_id, system_id, started);

CREATE TABLE exchange_transform_error_state
(
	service_id varchar(36) NOT NULL,
    system_id varchar(36) NOT NULL,
    exchange_ids_in_error text,
    CONSTRAINT pk_exchange_transform_error_state PRIMARY KEY (service_id, system_id)
);

CREATE TABLE user_event
(
    id varchar(36),
    user_id varchar(36),
    organisation_id varchar(36),
    module varchar(50),
    sub_module varchar(50),
    action varchar(250),
    timestamp datetime,
    data longtext,
    CONSTRAINT pk_user_event PRIMARY KEY (user_id, module, timestamp DESC, organisation_id, id)
);

CREATE INDEX ix_user_event_module_user_timestamp
ON user_event (module, user_id, timestamp);

CREATE INDEX ix_user_event_module_user_organisation_timestamp
ON user_event (module, user_id, organisation_id, timestamp);

CREATE TABLE queued_message
(
	id varchar(36) NOT NULL,
	message_body mediumtext NOT NULL,
    timestamp datetime NOT NULL,
    queued_message_type_id int NOT NULL,
    CONSTRAINT pk_queued_message PRIMARY KEY (id)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

CREATE INDEX ix_queued_message_timestamp
  ON queued_message (timestamp);

CREATE TABLE queued_message_type
(
    id int NOT NULL,
    description varchar(50) NOT NULL,
    CONSTRAINT pk_queued_message_type PRIMARY KEY (id)
);

CREATE TABLE exchange_batch (
  exchange_id varchar(36) NOT NULL,
  batch_id varchar(36) NOT NULL,
  inserted_at datetime NOT NULL,
  eds_patient_id varchar(36),
  CONSTRAINT pk_exchange_batch PRIMARY KEY (exchange_id, batch_id)
);

create index ix_exchange_batch_batch_id on exchange_batch (batch_id);


CREATE TABLE exchange_subscriber_transform_audit
(
    exchange_id varchar(36) NOT NULL,
    exchange_batch_id varchar(36) NOT NULL,
    subscriber_config_name varchar(100) NOT NULL,
    started datetime NOT NULL,
    ended datetime,
    error_xml mediumtext,
    number_resources_transformed int,
    queued_message_id varchar(36),
    CONSTRAINT pk_exchange_transform_audit PRIMARY KEY (exchange_id, exchange_batch_id, subscriber_config_name, started)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;


CREATE TABLE transform_warning_type (
  id int,
  warning varchar(255),
  last_used_at datetime,
  CONSTRAINT pk_transform_warning_type PRIMARY KEY (id)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;

ALTER TABLE transform_warning_type MODIFY COLUMN id INT auto_increment;

CREATE UNIQUE INDEX uix_transform_warning_type ON transform_warning_type (warning);


CREATE TABLE transform_warning (
  id int,
  service_id char(36),
  system_id char(36),
  exchange_id char(36),
  source_file_record_id long COMMENT 'field no longer used',
  inserted_at datetime,
  transform_warning_type_id int,
  param_1 varchar(255),
  param_2 varchar(255),
  param_3 varchar(255),
  param_4 varchar(255),
  published_file_id int,
  record_number int,
  CONSTRAINT pk_transform_warning_type PRIMARY KEY (id)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;

ALTER TABLE transform_warning MODIFY COLUMN id INT auto_increment;

CREATE INDEX ix_transform_warning_type ON transform_warning (transform_warning_type_id);

CREATE INDEX ix_transform_warning_exchange ON transform_warning (exchange_id);

create table exchange_general_error (
	exchange_id char(36) not null,
	inserted_at datetime not null default current_timestamp,
    error_message mediumtext null,
    
    CONSTRAINT pk_exchange_general_error_exchange_id_inserted_at PRIMARY KEY (exchange_id, inserted_at)
);

CREATE INDEX ix_exchange_general_error_inserted_at ON exchange_general_error (inserted_at);

create table exchange_protocol_error (
	exchange_id char(36) not null,
	inserted_at datetime not null default current_timestamp,
    
    CONSTRAINT pk_exchange_protocol_error_exchange_id_inserted_at PRIMARY KEY (exchange_id, inserted_at)
);

CREATE INDEX ix_exchange_protocol_error_inserted_at ON exchange_protocol_error (inserted_at);

CREATE TABLE subscriber_api_audit (
  timestmp datetime(3) not null,
  user_uuid char(36) comment 'keycloak user UUID of the requester',
  remote_address varchar(50) comment 'IP address of the requester',
  request_path varchar(1024) comment 'the URL of the requested service, including parameters',
  request_headers varchar(1024) comment 'any non-keycloak headers in the request',
  response_code int comment 'HTTP response code we sent back',
  response_body mediumtext comment 'response sent back',
  duration_ms bigint comment 'how long the call took'
) ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;







CREATE TABLE published_file_type (
  id int NOT NULL,
  file_type varchar(255) NOT NULL,
  variable_column_delimiter char NULL,
  variable_column_quote char NULL,
  variable_column_escape char NULL,
  CONSTRAINT pk_published_file_type PRIMARY KEY (id)
);

ALTER TABLE published_file_type MODIFY COLUMN id INT auto_increment;

CREATE INDEX ix_published_file_type_description ON published_file_type (file_type, variable_column_delimiter, variable_column_quote, variable_column_escape);



CREATE TABLE published_file_type_column (
  published_file_type_id int NOT NULL,
  column_index tinyint unsigned NOT NULL,
  column_name varchar(255) NOT NULL,
  fixed_column_start int NULL,
  fixed_column_length int NULL,
  CONSTRAINT pk_published_file_type_column PRIMARY KEY (published_file_type_id, column_index)
);


CREATE TABLE published_file (
  id int NOT NULL,
  service_id char(36) NOT NULL,
  system_id char(36) NOT NULL,
  file_path varchar(1000),
  inserted_at datetime NOT NULL,
  published_file_type_id int NOT NULL,
  exchange_id char(36),
  CONSTRAINT pk_published_file PRIMARY KEY (id)
);

ALTER TABLE published_file MODIFY COLUMN id INT auto_increment;

CREATE INDEX ix_published_file_service_system_type_exchange_path ON published_file (service_id, system_id, published_file_type_id, exchange_id, file_path);

CREATE INDEX ix_published_file_service_system_date ON published_file (service_id, system_id, inserted_at);


CREATE TABLE published_file_record (
  published_file_id int NOT NULL,
  record_number int NOT NULL,
  byte_start BIGINT NOT NULL,
  byte_length int NOT NULL,
  CONSTRAINT pk_published_file_record PRIMARY KEY (published_file_id, record_number)
);


create table last_data_received (
  service_id char(36) NOT NULL COMMENT 'links to admin.service table',
  system_id char(36) NOT NULL COMMENT 'links to admin.item to give the publishing software',
  data_date datetime NOT NULL COMMENT 'datetime of the data last received - date of the data, not the date is was received',
  received_date datetime NOT NULL COMMENT 'datetime the last data was received',
  exchange_id char(36) NOT NULL COMMENT 'links to audit.exchange table',
  CONSTRAINT pk_last_data_received PRIMARY KEY (service_id, system_id)
);

create table last_data_processed (
  service_id char(36) NOT NULL COMMENT 'links to admin.service table',
  system_id char(36) NOT NULL COMMENT 'links to admin.item to give the publishing software',
  data_date datetime NOT NULL COMMENT 'datetime of the data that was last successfully transformed - date of the data, not the date is was received',
  processed_date datetime NOT NULL COMMENT 'datetime the data was successfully transformed',
  exchange_id char(36) NOT NULL COMMENT 'links to audit.exchange table',
  CONSTRAINT pk_last_data_received PRIMARY KEY (service_id, system_id)
);

CREATE TABLE `exchange_subscriber_send_audit` (
  `exchange_id` char(36) NOT NULL,
  `exchange_batch_id` char(36) NOT NULL,
  `subscriber_config_name` varchar(100) NOT NULL,
  `inserted_at` datetime(3) NOT NULL,
  `error_xml` mediumtext,
  `queued_message_id` char(36) DEFAULT NULL,
  PRIMARY KEY (`exchange_id`,`exchange_batch_id`,`subscriber_config_name`,`inserted_at`)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;
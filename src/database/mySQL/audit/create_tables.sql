USE audit;

DROP TABLE IF EXISTS exchange;
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

CREATE TABLE exchange
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
ON exchange (service_id, system_id, timestamp);

CREATE TABLE exchange_event
(
  id varchar(36) NOT NULL,
  exchange_id varchar(36) NOT NULL,
  timestamp datetime(3) NOT NULL,
  event_desc varchar(250),
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
);

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
  source_file_record_id long,
  inserted_at datetime,
  transform_warning_type_id int,
  param_1 varchar(255),
  param_2 varchar(255),
  param_3 varchar(255),
  param_4 varchar(255),
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
  response_body varchar(2048) comment 'response sent back',
  duration_ms bigint comment 'how long the call took'
) ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;

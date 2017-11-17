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

CREATE TABLE exchange
(
    id varchar(36) NOT NULL,
    timestamp datetime,
    headers text,
    service_id varchar(36),
    body text,
    CONSTRAINT pk_exchange PRIMARY KEY (id)
);

CREATE INDEX ix_exchange_service_id
ON exchange (service_id, timestamp);

CREATE TABLE exchange_event
(
  id varchar(36) NOT NULL,
  exchange_id varchar(36) NOT NULL,
  timestamp datetime NOT NULL,
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
  error_xml text,
  resubmitted boolean,
  deleted datetime,
  number_batches_created int,
  CONSTRAINT pk_exchange_transform_audit PRIMARY KEY (service_id, system_id, exchange_id, id)
);

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
    data text,
    CONSTRAINT pk_user_event PRIMARY KEY (user_id, module, timestamp DESC, organisation_id, id)
);

CREATE INDEX ix_user_event_module_user_timestamp
ON user_event (module, user_id, timestamp);

CREATE INDEX ix_user_event_module_user_organisation_timestamp
ON user_event (module, user_id, organisation_id, timestamp);

CREATE TABLE queued_message
(
	id varchar(36) NOT NULL,
	message_body text NOT NULL,
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

CREATE TABLE exchange_subscriber_transform_audit
(
    exchange_id varchar(36) NOT NULL,
    exchange_batch_id varchar(36) NOT NULL,
    subscriber_config_name varchar(100) NOT NULL,
    started datetime NOT NULL,
    ended datetime,
    error_xml text,
    number_resources_transformed int,
    queued_message_id varchar(36),
    CONSTRAINT pk_exchange_transform_audit PRIMARY KEY (exchange_id, exchange_batch_id, subscriber_config_name, started)
);

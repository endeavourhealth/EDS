USE audit;

DROP PROCEDURE IF EXISTS check_for_bulk_extracts;
DROP PROCEDURE IF EXISTS get_monthly_frailty_stats;
DROP PROCEDURE IF EXISTS get_transform_warnings;
DROP FUNCTION IF EXISTS isBulk;
DROP FUNCTION IF EXISTS isAllowQueueing;
DROP FUNCTION IF EXISTS isEmisCustom;
DROP FUNCTION IF EXISTS isEmptyBody;
DROP FUNCTION IF EXISTS getDataDate;
DROP FUNCTION IF EXISTS getDataSize;
DROP FUNCTION IF EXISTS getExtractDate;
DROP FUNCTION IF EXISTS getExtractCutoff;
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
DROP TABLE IF EXISTS last_data_received; -- old table, no longer used
DROP TABLE IF EXISTS last_data_processed; -- old table, no longer used
DROP TABLE IF EXISTS last_data_to_subscriber; -- old table, no longer used
DROP TABLE IF EXISTS latest_data_received;
DROP TABLE IF EXISTS latest_data_processed;
DROP TABLE IF EXISTS latest_data_to_subscriber;
DROP TABLE IF EXISTS exchange_subscriber_send_audit;
DROP TABLE IF EXISTS application_heartbeat;
DROP TABLE IF EXISTS bulk_operation_audit;
DROP TABLE IF EXISTS service_subscriber_audit;
DROP TABLE IF EXISTS service_publisher_audit;
DROP TABLE IF EXISTS scheduled_task_audit_latest;
DROP TABLE IF EXISTS scheduled_task_audit_history;
DROP TABLE IF EXISTS simple_property;

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

create index ix on subscriber_api_audit (user_uuid, timestmp);

create index ix2 on subscriber_api_audit (timestmp);

create index ix_timestmp_requestpath on audit.subscriber_api_audit (timestmp, request_path);


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
  column_index smallint unsigned NOT NULL,
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

/*
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

create table last_data_to_subscriber (
  subscriber_config_name varchar(255) not null COMMENT 'subscriber feed this relates to',
  service_id char(36) not null COMMENT 'service UUID this is for',
  system_id char(36) not null COMMENT 'system UUID this is for',
  data_date datetime COMMENT 'date of the last data processed (not receipt date)',
  sent_date datetime COMMENT 'timestamp the subscriber feed population was completed',
  exchange_id char(36) COMMENT 'exchange UUID of the last exchange sent',
  CONSTRAINT pk_last_data_to_subscriber PRIMARY KEY (subscriber_config_name, service_id, system_id)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;

create index ix_service_id on last_data_to_subscriber (service_id, system_id);
*/

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


create table application_heartbeat (
  application_name varchar(255) not null COMMENT 'top-level name of the app e.g. QueueReader',
  application_instance_name varchar(255) not null COMMENT 'identifies the instance of the app e.g. InboundA',
  application_instance_number int not null COMMENT 'further identifies multiple versions of the same instance',
  timestmp datetime NOT NULL COMMENT 'timestamp of last heartbeat',
  host_name varchar(255) not null COMMENT 'server name last heartbeat from',
  is_busy boolean COMMENT 'whether the application is busy (depends on context what this means)',
  max_heap_mb int COMMENT 'JVM max heap',
  current_heap_mb int COMMENT 'JVM heap allocated',
  server_memory_mb int COMMENT 'server physical memory',
  server_cpu_usage_percent int COMMENT 'server total CPU load',
  is_busy_detail varchar(255) COMMENT 'free-text desc of what it is busy doing',
  dt_started datetime COMMENT 'when this app instance started',
  dt_jar datetime COMMENT 'build date time of the jar on this server (may be later than start time)',
  CONSTRAINT pk_application_heartbeat PRIMARY KEY (application_name, application_instance_name, application_instance_number)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;




create table bulk_operation_audit (
  service_id char(36),
  operation_name varchar(255),
  `status` int,
  started datetime(3),
  finished datetime(3)
) COMMENT 'table to audit one-off bulk routines';


CREATE TABLE service_subscriber_audit (
  service_id char(36) NOT NULL COMMENT 'the service affected',
  dt_changed datetime(3) NOT NULL COMMENT 'when the change was detected',
  subscriber_config_names varchar(4096) NOT NULL COMMENT 'list of subscriber config names, pipe delimited',
  CONSTRAINT pk_service_subscriber_audit PRIMARY KEY (service_id, dt_changed)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'records a history of the subscribers of a service and when it changed';


CREATE TABLE service_publisher_audit (
  service_id char(36) NOT NULL COMMENT 'the service affected',
  dt_changed datetime(3) NOT NULL COMMENT 'when the change was detected',
  has_dpa boolean NOT NULL COMMENT 'whether DDS has a DPA for this service',
  CONSTRAINT pk_service_publisher_audit PRIMARY KEY (service_id, dt_changed)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8
COMMENT 'records a history of the DPA state changing and when it changed';



create table scheduled_task_audit_latest (
  application_name varchar(255) NOT NULL COMMENT 'top-level name of the app e.g. QueryTool',
  task_name varchar(255) NOT NULL COMMENT 'identifies the instance of the app e.g. EmisMissingCodesReport',
  timestmp datetime NOT NULL COMMENT 'timestamp of last audit',
  host_name varchar(255) NOT NULL COMMENT 'server name last run on',
  success boolean NOT NULL COMMENT 'whether it ran OK or not',
  error_message text COMMENT 'if not successful, used to store error details',
  task_parameters text COMMENT 'additional paramters used to launch the scheduled task',
  CONSTRAINT pk_scheduled_task_audit PRIMARY KEY (application_name, task_name)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;


create table scheduled_task_audit_history (
  application_name varchar(255) NOT NULL COMMENT 'top-level name of the app e.g. QueryTool',
  task_name varchar(255) NOT NULL COMMENT 'identifies the instance of the app e.g. EmisMissingCodesReport',
  timestmp datetime NOT NULL COMMENT 'timestamp of last audit',
  host_name varchar(255) NOT NULL COMMENT 'server name last run on',
  success boolean NOT NULL COMMENT 'whether it ran OK or not',
  error_message text COMMENT 'if not successful, used to store error details',
  task_parameters text COMMENT 'additional paramters used to launch the scheduled task',
  CONSTRAINT pk_scheduled_task_audit PRIMARY KEY (application_name, task_name, timestmp)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;


CREATE TABLE simple_property (
  application_name varchar(255) not null COMMENT 'top-level name of the app e.g. QueueReader',
  application_instance_name varchar(255) not null COMMENT 'identifies the instance of the app e.g. InboundA',
  property_name varchar(255) not null COMMENT 'arbitrary name of the property being stored',
  property_value text not null COMMENT 'arbitrary value being stored',
  CONSTRAINT pk PRIMARY KEY (application_name, application_instance_name, property_name)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;


create table latest_data_received (
  service_id char(36) NOT NULL COMMENT 'links to admin.service table',
  system_id char(36) NOT NULL COMMENT 'links to admin.item to give the publishing software',
  received_date datetime NOT NULL COMMENT 'datetime the last data was received',
  exchange_id char(36) NOT NULL COMMENT 'links to audit.exchange table',
  extract_date datetime NOT NULL COMMENT 'datetime the extract was generated by publisher',
  extract_cutoff datetime NOT NULL COMMENT 'datetime of the last data in the extract',
  CONSTRAINT pk_latest_data_received PRIMARY KEY (service_id, system_id)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;

create table latest_data_processed (
  service_id char(36) NOT NULL COMMENT 'links to admin.service table',
  system_id char(36) NOT NULL COMMENT 'links to admin.item to give the publishing software',
  processed_date datetime NOT NULL COMMENT 'datetime the data was successfully transformed',
  exchange_id char(36) NOT NULL COMMENT 'links to audit.exchange table',
  extract_date datetime NOT NULL COMMENT 'datetime the extract was generated by publisher',
  extract_cutoff datetime NOT NULL COMMENT 'datetime of the last data in the extract',
  CONSTRAINT pk_latest_data_processed PRIMARY KEY (service_id, system_id)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;

create table latest_data_to_subscriber (
  subscriber_config_name varchar(255) not null COMMENT 'subscriber feed this relates to',
  service_id char(36) not null COMMENT 'service UUID this is for',
  system_id char(36) not null COMMENT 'system UUID this is for',
  sent_date datetime COMMENT 'timestamp the subscriber feed population was completed',
  exchange_id char(36) COMMENT 'exchange UUID of the last exchange sent',
  extract_date datetime NOT NULL COMMENT 'datetime the extract was generated by publisher',
  extract_cutoff datetime NOT NULL COMMENT 'datetime of the last data in the extract',
  CONSTRAINT pk_latest_data_to_subscriber PRIMARY KEY (subscriber_config_name, service_id, system_id)
)
  ROW_FORMAT=COMPRESSED
  KEY_BLOCK_SIZE=8;

create index ix_service_id on latest_data_to_subscriber (service_id, system_id);



DELIMITER //
CREATE FUNCTION isBulk ( exchange_headers TEXT )
  RETURNS BOOLEAN
  BEGIN

    DECLARE ret BOOLEAN;
    SET ret = IF (exchange_headers like '%"is-bulk":"true"%', TRUE, FALSE);
    RETURN ret;

  END; //
DELIMITER ;


DELIMITER //
CREATE FUNCTION isAllowQueueing ( exchange_headers TEXT )
  RETURNS BOOLEAN
  BEGIN

    DECLARE ret BOOLEAN;
    SET ret = IF (exchange_headers NOT LIKE '%"AllowQueueing":"false"%', TRUE, FALSE);
    RETURN ret;

  END; //
DELIMITER ;


DELIMITER //
CREATE FUNCTION isEmisCustom ( exchange_body MEDIUMTEXT )
  RETURNS BOOLEAN
  BEGIN

    DECLARE ret BOOLEAN;
    SET ret = IF (exchange_body like '%OriginalTerm%'
                  OR exchange_body like '%RegistrationStatusHistory%'
                  OR exchange_body like '%RegistrationHistory%',
                  TRUE, FALSE);
    RETURN ret;

  END; //
DELIMITER ;


DELIMITER //
CREATE FUNCTION isEmptyBody ( exchange_body MEDIUMTEXT )
  RETURNS BOOLEAN
  BEGIN

    DECLARE ret BOOLEAN;
    SET ret = IF (exchange_body = '[]', TRUE, FALSE);
    RETURN ret;

  END; //
DELIMITER ;


DELIMITER //
CREATE FUNCTION getDataDate ( exchange_headers TEXT )
  RETURNS DATETIME
  BEGIN

    DECLARE ret DATETIME;
    DECLARE str varchar(50);

    if (exchange_headers like '%"DataDate"%') THEN

      SET str = SUBSTRING(
          SUBSTRING(exchange_headers,
                    INSTR(exchange_headers, 'DataDate') + 11
          ),
          1,
          INSTR(
              SUBSTRING(exchange_headers,
                        INSTR(exchange_headers, 'DataDate') + 11
              ),
              '\"'
          )-1);
      SET ret = str; -- should auto-convert since it's in SQL format

    END IF;
    RETURN ret;

  END; //
DELIMITER ;



DELIMITER //
CREATE FUNCTION getDataSize ( exchange_headers TEXT )
  RETURNS BIGINT
  BEGIN

    DECLARE ret BIGINT;
    DECLARE str varchar(50);

    if (exchange_headers like '%"file-total-size"%') THEN

      SET str = SUBSTRING(
          SUBSTRING(exchange_headers,
                    INSTR(exchange_headers, 'file-total-size') + 18
          ),
          1,
          INSTR(
              SUBSTRING(exchange_headers,
                        INSTR(exchange_headers, 'file-total-size') + 18
              ),
              '\"'
          )-1);
      SET ret = str;

    END IF;
    RETURN ret;

  END; //
DELIMITER ;



DELIMITER //
CREATE PROCEDURE get_monthly_frailty_stats()
  BEGIN

    -- avoid locking the table
    SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED ;


    -- export all data
    drop table if exists tmp.frailty_data;

    create table tmp.frailty_data as
      select
        date(timestmp) as `date`,
        time(timestmp) as `time`,
        CASE
        WHEN response_code = 200 AND response_body like '%potentially%' THEN 1
        ELSE 0
        END AS `matched_and_potentially_frail`,
        CASE
        WHEN response_code = 200 AND response_body not like '%potentially%' THEN 1
        ELSE 0
        END AS `matched_and_potentially_not_frail`,
        CASE
        WHEN response_code != 200 AND response_body like '%No patient record could be found%' THEN 1
        ELSE 0
        END AS `error_patient_not_matched`,
        CASE
        WHEN response_code != 200 AND response_body not like '%No patient record could be found%' THEN 1
        ELSE 0
        END AS `error_other`,
        duration_ms
      from audit.subscriber_api_audit
      where user_uuid = '4d4ebdd9-7b83-4736-b558-b5cc97147cd4' -- Redwood user
            and request_path not like '%=999999%'
            -- and date(timestmp) >= '2018-09-26'
            and date(timestmp) >= '2018-11-01'
      order by `date`;

    -- create rolling montly counts
    drop table if exists tmp.frailty_monthly_counts;

    create table tmp.frailty_monthly_counts as
      select
        MIN(`date`) as `month_starting`,
        SUM(matched_and_potentially_frail) as `matched_and_potentially_frail`,
        SUM(matched_and_potentially_not_frail) as `matched_and_potentially_not_frail`,
        SUM(error_patient_not_matched) as `error_patient_not_matched`,
        SUM(error_other) as `error_other`,
        SUM(matched_and_potentially_frail + matched_and_potentially_not_frail + error_patient_not_matched + error_other) as `total_api_requests`
      from tmp.frailty_data
      group by DATE_FORMAT(`date`, "%Y/%m")
      order by MIN(`date`);

    -- create table of rolling AND cumulative counts
    drop table if exists tmp.frailty_total_counts;

    set @frail_sum := 0;
    set @not_frail_sum := 0;
    set @not_matched_sum := 0;
    set @error_sum := 0;
    set @total_sum := 0;

    create table tmp.frailty_total_counts as
      select
        DATE_FORMAT(`month_starting`, "%Y/%m") as `month`,
        matched_and_potentially_frail,
        matched_and_potentially_not_frail,
        error_patient_not_matched,
        error_other,
        total_api_requests,
        (@frail_sum := @frail_sum + matched_and_potentially_frail) as `cumulative_matched_and_potentially_frail`,
        (@not_frail_sum := @not_frail_sum + matched_and_potentially_not_frail) as `cumulative_matched_and_potentially_not_frail`,
        (@not_matched_sum := @not_matched_sum + error_patient_not_matched) as `cumulative_error_patient_not_matched`,
        (@error_sum := @error_sum + error_other) as `cumulative_error_other`,
        (@total_sum := @total_sum + total_api_requests) as `cumulative_total_api_requests`
      from tmp.frailty_monthly_counts
      order by `month_starting`;


    select * from  tmp.frailty_total_counts;

    -- restore this back to default
    SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ ;


  END //
DELIMITER ;



DELIMITER $$
CREATE PROCEDURE `get_transform_warnings`(
  IN _since_date datetime
)
  BEGIN

    SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

    drop table if exists tmp.transform_warning_type_tmp;
    drop table if exists tmp.transform_warning_tmp;
    drop table if exists tmp.transform_warning_count_tmp;
    drop table if exists tmp.transform_warning_service_count_tmp;
    drop table if exists tmp.transform_warning_start_date_tmp;

    create table tmp.transform_warning_type_tmp as
      select *
      from transform_warning_type
      where last_used_at > _since_date;

    create index ix on tmp.transform_warning_type_tmp (id);

    create table tmp.transform_warning_tmp as
      select w.*
      from transform_warning w
        inner join tmp.transform_warning_type_tmp t
          on t.id = w.transform_warning_type_id
      where w.inserted_at > _since_date;

    create index ix on tmp.transform_warning_tmp (transform_warning_type_id, service_id);

    -- find which warnings are new/old
    alter table tmp.transform_warning_type_tmp
    add is_new boolean default true;

    update tmp.transform_warning_type_tmp t
    set is_new = false
    where exists (
        select 1
        from transform_warning w
        where t.id = w.transform_warning_type_id
              and w.inserted_at <= _since_date
    );

    create table tmp.transform_warning_count_tmp as
      select transform_warning_type_id, count(1) as `cnt`
      from tmp.transform_warning_tmp
      group by transform_warning_type_id;

    create index ix on tmp.transform_warning_count_tmp (transform_warning_type_id);

    create table tmp.transform_warning_service_count_tmp as
      select transform_warning_type_id, count(distinct service_id) as `cnt`
      from tmp.transform_warning_tmp
      group by transform_warning_type_id;

    create index ix on tmp.transform_warning_service_count_tmp (transform_warning_type_id);

    create table tmp.transform_warning_start_date_tmp as
      select transform_warning_type_id, min(inserted_at) as `first_used`
      from tmp.transform_warning_tmp
      group by transform_warning_type_id;

    create index ix on tmp.transform_warning_start_date_tmp (transform_warning_type_id);



    -- select NEW warnings
    select _since_date as 'New Warnings Since';

    select t.id as `warning_id`, t.warning, s.first_used, t.last_used_at as `last_used`, c1.cnt as `warning_count`, c2.cnt as `service_count`
    from tmp.transform_warning_type_tmp t
      left outer join tmp.transform_warning_count_tmp c1
        on c1.transform_warning_type_id = t.id
      left outer join tmp.transform_warning_service_count_tmp c2
        on c2.transform_warning_type_id = t.id
      left outer join tmp.transform_warning_start_date_tmp s
        on s.transform_warning_type_id = t.id
    where t.is_new = true
    order by t.id;


    -- find warnings used this week
    select _since_date as 'All Warnings Since';

    select t.id as `warning_id`, t.warning, t.last_used_at as `last_used`, c1.cnt as `warning_count`, c2.cnt as `service_count`
    from tmp.transform_warning_type_tmp t
      left outer join tmp.transform_warning_count_tmp c1
        on c1.transform_warning_type_id = t.id
      left outer join tmp.transform_warning_service_count_tmp c2
        on c2.transform_warning_type_id = t.id
    order by t.id;


    drop table if exists tmp.transform_warning_type_tmp;
    drop table if exists tmp.transform_warning_tmp;
    drop table if exists tmp.transform_warning_count_tmp;
    drop table if exists tmp.transform_warning_service_count_tmp;
    drop table if exists tmp.transform_warning_start_date_tmp;


    -- restore this back to default
    SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ ;


  END$$
DELIMITER ;



DELIMITER //
CREATE FUNCTION getExtractDate ( exchange_headers TEXT )
  RETURNS DATETIME
  BEGIN

    DECLARE ret DATETIME;
    DECLARE str varchar(50);

    if (exchange_headers like '%"extract-date"%') THEN

      SET str = SUBSTRING(
          SUBSTRING(exchange_headers,
                    INSTR(exchange_headers, 'extract-date') + 15
          ),
          1,
          INSTR(
              SUBSTRING(exchange_headers,
                        INSTR(exchange_headers, 'extract-date') + 15
              ),
              '\"'
          )-1);
      SET ret = str; -- should auto-convert since it's in SQL format

    END IF;
    RETURN ret;

  END; //
DELIMITER ;



DELIMITER //
CREATE FUNCTION getExtractCutoff ( exchange_headers TEXT )
  RETURNS DATETIME
  BEGIN

    DECLARE ret DATETIME;
    DECLARE str varchar(50);

    if (exchange_headers like '%"extract-cutoff"%') THEN

      SET str = SUBSTRING(
          SUBSTRING(exchange_headers,
                    INSTR(exchange_headers, 'extract-cutoff') + 17
          ),
          1,
          INSTR(
              SUBSTRING(exchange_headers,
                        INSTR(exchange_headers, 'extract-cutoff') + 17
              ),
              '\"'
          )-1);
      SET ret = str; -- should auto-convert since it's in SQL format

    END IF;
    RETURN ret;

  END; //
DELIMITER ;




/**
  procedure to check that all publishers have actually had a bulk extract received (see SD-352)
 */
DELIMITER //
CREATE PROCEDURE check_for_bulk_extracts()
  BEGIN

    SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

    drop table if exists tmp.bulk_services;
    drop table if exists tmp.bulk_systems;

    -- find services, but exclude
    create table tmp.bulk_services as
      select *
      from admin.service
      where tags not like '%ADT%'
            or tags like '%BARTSDW%'
            or tags like '%BHRUT%';

    create index ix on tmp.bulk_services (id);

    -- find active system IDs
    create table tmp.bulk_systems as
      select p.service_id, p.system_id
      from audit.latest_data_processed p -- use this table so we don't look a publishers we've not started processing yet
        inner join tmp.bulk_services s
          on s.id = p.service_id
      where p.processed_date > date_add(now(), INTERVAL -14 DAY);

    alter table tmp.bulk_systems
    add done boolean default false,
    add has_bulk boolean default false;

    create index ix on tmp.bulk_systems (service_id, system_id);
    create index ix2 on tmp.bulk_systems (done);

    -- find a bulk extract for each service/system
    WHILE (select 1 from tmp.bulk_systems where done = false limit 1) DO

      drop table if exists tmp.bulk_systems_batch;

      create table tmp.bulk_systems_batch
        select *
        from tmp.bulk_systems
        where done = 0
        limit 1;

      create index ix on tmp.bulk_systems_batch (service_id, system_id);

      update tmp.bulk_systems_batch b
        inner join audit.exchange x
          on x.service_id = b.service_id
             and x.system_id = b.system_id
      set b.has_bulk = true;

      update tmp.bulk_systems y
        inner join tmp.bulk_systems_batch b
          on y.service_id = b.service_id
             and y.system_id = b.system_id
      set
        y.has_bulk = b.has_bulk,
        y.done = true;

    END WHILE;

    select s.name, s.local_id, y.service_id, y.system_id
    from tmp.bulk_systems y
      inner join tmp.bulk_services s
        on y.service_id = s.id
    where has_bulk != true;

    drop table if exists tmp.bulk_systems_batch;
    drop table if exists tmp.bulk_services;
    drop table if exists tmp.bulk_systems;

  END //
DELIMITER ;
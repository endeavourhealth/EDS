drop database if exists pcr_subscriber_xx; -- note: a separate subscriber DB for each subscriber
create database pcr_subscriber_xx;

use pcr_subscriber_xx;

DROP TABLE IF EXISTS subscriber_feed_state;
DROP TABLE IF EXISTS subscriber_feed_history;
DROP TABLE IF EXISTS subscriber_id_map;
DROP TABLE IF EXISTS subscriber_pseudo_id_map;
drop trigger if exists after_subscriber_feed_update;

create table subscriber_feed_state (
	publisher_pcr_database_name varchar(255) NOT NULL COMMENT ''name of the publisher PCR DB, of which there will be multiple if a subscriber needs to source from multiple sources'',
    last_run_date datetime(3) NOT NULL COMMENT ''last time this subscriber feed was refreshed from the publisher'',
    last_event_id int NOT NULL COMMENT ''last event ID from the event_log table in the publisher PCR DB'',
    CONSTRAINT pk_subscriber_feed_state PRIMARY KEY (publisher_pcr_database_name)
) COMMENT ''table to store the last publisher event ID transformed/processed for this subscriber'';

create table subscriber_feed_history (
	inserted_at datetime(3) NOT NULL COMMENT ''when this record was inserted into the history'',
	publisher_pcr_database_name varchar(255) NOT NULL COMMENT ''name of the publisher PCR DB, of which there will be multiple if a subscriber needs to source from multiple sources'',
    last_run_date datetime(3) NOT NULL COMMENT ''last time this subscriber feed was refreshed from the publisher'',
    last_event_id int NOT NULL COMMENT ''last event ID from the event_log table in the publisher PCR DB'',
    CONSTRAINT pk_subscriber_feed_history PRIMARY KEY (publisher_pcr_database_name, inserted_at)
) COMMENT ''table to store the history of when events from publisher DBs were applied to this subscriber'';

-- use trigger to populate history when state is updated
DELIMITER $$
CREATE TRIGGER after_subscriber_feed_update
  AFTER UPDATE ON subscriber_feed_state
  FOR EACH ROW
  BEGIN
    INSERT INTO subscriber_feed_history (
		inserted_at,
        publisher_pcr_database_name,
        last_run_date,
        last_event_id
    ) VALUES (
		now(3), -- current time inc ms
        OLD.publisher_pcr_database_name,
        OLD.last_run_date,
        OLD.last_event_id
    );
  END$$
DELIMITER ;



CREATE TABLE subscriber_id_map
(
  subscriber_table tinyint NOT NULL COMMENT ''ID of the target table this ID is for'',
  subscriber_id bigint NOT NULL COMMENT ''unique ID allocated for the subscriber DB'',
  source_id varchar(250) NOT NULL COMMENT ''Source ID from the PCR table this this is mapped from, typically a composite of table name/ID and record ID'',
  dt_previously_sent datetime NULL COMMENT ''the date time of the previously sent version of this resource (or null if deleted)'',
  CONSTRAINT pk_subscriber_id_map PRIMARY KEY (source_id, subscriber_table)
) COMMENT ''table to maintain mapping of PCR records and IDs to subscriber records and IDs'';

-- this unique index is required to make the column auto-increment
CREATE UNIQUE INDEX uix_subscriber_id_map_auto_increment
ON subscriber_id_map (subscriber_id);

ALTER TABLE subscriber_id_map MODIFY COLUMN subscriber_id INT auto_increment;


CREATE TABLE subscriber_pseudo_id_map
(
  patient_id int NOT NULL,
  subscriber_patient_id bigint NOT NULL,
  salt_key_name varchar(255) NOT NULL COMMENT ''possibly needs changing to "project name" or similar?'',
  pseudo_id varchar(255) NOT NULL,
  CONSTRAINT pk_subscriber_pseudo_id_map PRIMARY KEY (patient_id, subscriber_patient_id, salt_key_name)
) COMMENT ''table to store any pseudo IDs generated for the subscriber so its possible to perform a backwards search to find source patient ID'';

create index ix_subscriber_pseudo_id_map_pseudo_id on subscriber_pseudo_id_map(salt_key_name, pseudo_id);


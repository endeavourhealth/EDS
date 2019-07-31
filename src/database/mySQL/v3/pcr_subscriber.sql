drop database if exists pcr_subscriber_xx; -- note: a separate subscriber DB for each subscriber
create database pcr_subscriber_xx;

use pcr_subscriber_xx;

DROP TABLE IF EXISTS subscriber_feed_state;
DROP TABLE IF EXISTS subscriber_feed_history;
drop trigger if exists after_subscriber_feed_update;

create table subscriber_feed_state (
	publisher_pcr_database_name varchar(255) NOT NULL COMMENT 'name of the publisher PCR DB, of which there will be multiple if a subscriber needs to source from multiple sources',
    last_run_date datetime(3) NOT NULL COMMENT 'last time this subscriber feed was refreshed from the publisher',
    last_event_id int NOT NULL COMMENT 'last event ID from the event_log table in the publisher PCR DB',
    CONSTRAINT pk_subscriber_feed_state PRIMARY KEY (publisher_pcr_database_name)
);

create table subscriber_feed_history (
	inserted_at datetime(3) NOT NULL COMMENT 'when this record was inserted into the history',
	publisher_pcr_database_name varchar(255) NOT NULL COMMENT 'name of the publisher PCR DB, of which there will be multiple if a subscriber needs to source from multiple sources',
    last_run_date datetime(3) NOT NULL COMMENT 'last time this subscriber feed was refreshed from the publisher',
    last_event_id int NOT NULL COMMENT 'last event ID from the event_log table in the publisher PCR DB',
    CONSTRAINT pk_subscriber_feed_history PRIMARY KEY (publisher_pcr_database_name, inserted_at)
);

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

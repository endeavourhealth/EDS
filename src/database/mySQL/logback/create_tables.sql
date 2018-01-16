DROP TABLE IF EXISTS logback.logging_event_property;
DROP TABLE IF EXISTS logback.logging_event_exception;
DROP TABLE IF EXISTS logback.logging_event;

CREATE TABLE logback.logging_event
  (
    timestmp          bigint NOT NULL,
    formatted_message text NOT NULL,
    logger_name       varchar(254) NOT NULL,
    level_string      varchar(254) NOT NULL,
    thread_name       varchar(254),
    reference_flag    smallint,
    arg0              varchar(254),
    arg1              varchar(254),
    arg2              varchar(254),
    arg3              varchar(254),
    caller_filename   varchar(254) NOT NULL,
    caller_class      varchar(254) NOT NULL,
    caller_method     varchar(254) NOT NULL,
    caller_line       char(4) NOT NULL,
    event_id          bigint auto_increment PRIMARY KEY
  );

CREATE TABLE logback.logging_event_property
  (
    event_id	        bigint NOT NULL,
    mapped_key        varchar(254) NOT NULL,
    mapped_value      varchar(1024),
    PRIMARY KEY(event_id, mapped_key),
    FOREIGN KEY (event_id) REFERENCES logback.logging_event(event_id)
  );

CREATE TABLE logback.logging_event_exception
  (
    event_id          bigint NOT NULL,
    i                 smallint NOT NULL,
    trace_line        varchar(254) NOT NULL,
    PRIMARY KEY(event_id, i),
    FOREIGN KEY (event_id) REFERENCES logback.logging_event(event_id)
  );
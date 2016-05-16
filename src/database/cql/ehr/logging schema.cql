create keyspace logging with replication = {'class':'SimpleStrategy', 'replication_factor':1};

USE logging;

CREATE TABLE logging_event 
  ( 
    timestmp         TIMESTAMP,
    formatted_message  VARCHAR,
    logger_name       VARCHAR,
    level_string      VARCHAR,
    thread_name       VARCHAR,
    reference_flag    INT,
    arg0              VARCHAR,
    arg1              VARCHAR,
    arg2              VARCHAR,
    arg3              VARCHAR,
    caller_filename   VARCHAR,
    caller_class      VARCHAR,
    caller_method     VARCHAR,
    caller_line       INT,
    event_id          UUID,
    PRIMARY KEY (event_id) 
  );

CREATE TABLE logging_event_property 
  ( 
    event_id          UUID, 
    mapped_key        VARCHAR, 
    mapped_value      VARCHAR,
    PRIMARY KEY (event_id, mapped_key)
  );

CREATE TABLE logging_event_exception 
  ( 
    event_id         UUID, 
    i                INT,
    trace_line       VARCHAR,
    PRIMARY KEY (event_id, i) 
  );
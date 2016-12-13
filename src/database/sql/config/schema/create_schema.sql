/* 
	create schema
*/

CREATE TABLE config
  (
    app_id         VARCHAR(100) NOT NULL,
    config_id      VARCHAR(100) NOT NULL,
    config_data    TEXT NOT NULL,
    constraint config_config_app_id_config_id_pk primary key (app_id, config_id)
 );

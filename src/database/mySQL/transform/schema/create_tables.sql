DROP TABLE IF EXISTS transform.enterprise_id_map;
DROP TABLE IF EXISTS transform.enterprise_organisation_id_map;
DROP TABLE IF EXISTS transform.household_id_map;
DROP TABLE IF EXISTS transform.pseudo_id_map;
DROP TABLE IF EXISTS transform.enterprise_age;
DROP TABLE IF EXISTS transform.enterprise_person_id_map;
DROP TABLE IF EXISTS transform.enterprise_person_update_history;

-- NOTE:  Use ALTER TABLE transform.enterprise_id_map AUTO_INCREMENT=XXXXX; to set the auto increment id starting value
CREATE TABLE transform.enterprise_id_map
(
  resource_type varchar(255) NOT NULL,
  resource_id varchar(255) NOT NULL,
  enterprise_id bigint NOT NULL auto_increment PRIMARY KEY
  -- CONSTRAINT pk_enterprise_id_map PRIMARY KEY (resource_id, resource_type)
);

CREATE UNIQUE INDEX ix_enterprise_id_map
  ON transform.enterprise_id_map (resource_id, resource_type);


CREATE TABLE transform.enterprise_organisation_id_map
(
  service_id char(36) NOT NULL,
  system_id char(36) NOT NULL,
  enterprise_id bigint NOT NULL,
  CONSTRAINT pk_enterprise_organisation_id_map PRIMARY KEY (service_id, system_id)
);


CREATE TABLE transform.household_id_map
(
  postcode char(8) NOT NULL,
  line_1 varchar(255) NOT NULL,
  line_2 varchar(255) NOT NULL,
  household_id bigint NOT NULL auto_increment PRIMARY KEY
  -- CONSTRAINT pk_household_id_map PRIMARY KEY (postcode, line_1, line_2)
);

CREATE UNIQUE INDEX ix_household_id_map
  ON transform.household_id_map  (postcode, line_1, line_2);


CREATE TABLE transform.pseudo_id_map
(
  patient_id varchar(255) NOT NULL PRIMARY KEY,
  pseudo_id varchar(255) NOT NULL
);


CREATE TABLE transform.enterprise_age
(
  enterprise_patient_id bigint NOT NULL PRIMARY KEY,
  date_of_birth date NOT NULL,
  date_next_change date NOT NULL
);

CREATE INDEX ix_date_next_change
  ON transform.enterprise_age (date_next_change);


CREATE TABLE transform.enterprise_person_id_map
(
  enterprise_person_id bigint NOT NULL auto_increment PRIMARY KEY,
  person_id character(36) NOT NULL
  -- CONSTRAINT pk_enterprise_person_id_map PRIMARY KEY (person_id)
);

CREATE UNIQUE INDEX ix_enterprise_person_id_map
  ON transform.enterprise_person_id_map  (person_id);


CREATE TABLE transform.enterprise_person_update_history
(
  date_run timestamp NOT NULL,
  CONSTRAINT pk_person_update_history PRIMARY KEY (date_run)
);
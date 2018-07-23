USE eds;

DROP TABLE IF EXISTS patient_link;
DROP TABLE IF EXISTS patient_link_history;
DROP TABLE IF EXISTS patient_link_person;
DROP TABLE IF EXISTS patient_search_local_identifier;
DROP TABLE IF EXISTS patient_search_episode;
DROP TABLE IF EXISTS patient_search;


CREATE TABLE patient_link
(
  patient_id character(36) NOT NULL,
  service_id character(36) NOT NULL,
  person_id character(36) NOT NULL,
  CONSTRAINT pk_patient_link PRIMARY KEY (patient_id)
);

CREATE INDEX ix_person_id
  ON patient_link (person_id);


CREATE TABLE patient_link_history
(
  patient_id character(36) NOT NULL,
  service_id character(36) NOT NULL,
  updated timestamp NOT NULL,
  new_person_id character(36) NOT NULL,
  previous_person_id character(36),
  CONSTRAINT pk_patient_link_history PRIMARY KEY (patient_id, updated)
);

CREATE INDEX ix_updated
  ON patient_link_history (updated);


CREATE TABLE patient_link_person
(
  person_id character(36) NOT NULL,
  nhs_number character(10) NOT NULL,
  CONSTRAINT pk_patient_link_person PRIMARY KEY (person_id)
);

CREATE UNIQUE INDEX ix_nhs_number
  ON patient_link_person (nhs_number);




CREATE TABLE patient_search
(
	service_id char(36) NOT NULL,
	nhs_number varchar(10),
	forenames varchar(500),
	surname varchar(500),
	date_of_birth date,
	date_of_death date,
	address_line_1 VARCHAR(255),
	address_line_2 VARCHAR(255),
	address_line_3 VARCHAR(255),
	city VARCHAR(255),
	district VARCHAR(255),
	postcode varchar(8),
	gender varchar(7),
	patient_id char(36) NOT NULL,
	last_updated timestamp NOT NULL,
	registered_practice_ods_code VARCHAR(50),
	uprn bigint COMMENT 'not automatically populated as of yet',
  uprn_match_qualifier varchar(50) COMMENT 'not automatically populated as of yet',
  uprn_matched_address varchar(1024) COMMENT 'not automatically populated as of yet',
	CONSTRAINT pk_patient_search PRIMARY KEY (service_id, patient_id)
);

CREATE INDEX ix_patient
  ON patient_search (patient_id);

CREATE INDEX ix_service_patient
  ON patient_search (service_id, patient_id);

CREATE INDEX ix_service_date_of_birth
  ON patient_search (service_id, date_of_birth);

-- swap index to be NHS Number first, since that's more selective than a long list of service IDs
/*CREATE INDEX ix_service_nhs_number
  ON patient_search (service_id, nhs_number);*/

CREATE INDEX ix_service_nhs_number_2
  ON patient_search (nhs_number, service_id);

CREATE INDEX ix_service_surname_forenames
  ON patient_search (service_id, surname, forenames);

CREATE TABLE patient_search_episode
(
	service_id char(36) NOT NULL,
	patient_id char(36) NOT NULL,
	episode_id char(36) NOT NULL,
	registration_start date,
	registration_end date,
	care_mananger VARCHAR(255),
	organisation_name VARCHAR(255),
	organisation_type_code varchar(10),
	registration_type_code varchar(10),
	last_updated timestamp NOT NULL,
	CONSTRAINT pk_patient_search PRIMARY KEY (service_id, patient_id, episode_id)
);

-- unique index required so patient merges trigger a change in patient_id
CREATE UNIQUE INDEX uix_patient_search_episode_id
  ON patient_search_episode (episode_id);


CREATE TABLE patient_search_local_identifier
(
	service_id char(36) NOT NULL,
	local_id varchar(1000),
	local_id_system varchar(1000),
	patient_id char(36) NOT NULL,
	last_updated timestamp NOT NULL,
	CONSTRAINT pk_patient_search_local_identifier PRIMARY KEY (service_id, patient_id, local_id_system, local_id),
	CONSTRAINT fk_patient_search_local_identifier_patient_id FOREIGN KEY (service_id, patient_id)
		REFERENCES patient_search (service_id, patient_id) MATCH SIMPLE
		ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- index so patient search by local ID works in timely fashion
CREATE INDEX ix_patient_search_local_identifier_id_service_patient
  ON patient_search_local_identifier (local_id, service_id, patient_id);

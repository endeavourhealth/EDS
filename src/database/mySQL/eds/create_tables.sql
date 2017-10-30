USE eds;

DROP TABLE IF EXISTS patient_search_local_identifier;
DROP TABLE IF EXISTS patient_search;
DROP TABLE IF EXISTS patient_link;
DROP TABLE IF EXISTS patient_link_history;
DROP TABLE IF EXISTS patient_link_person;

CREATE TABLE patient_search
(
	service_id varchar(36) NOT NULL,
	system_id varchar(36) NOT NULL,
	nhs_number varchar(10),
	forenames varchar(500),
	surname varchar(500),
	date_of_birth date,
	date_of_death date,
	postcode varchar(8),
	gender varchar(7),
	registration_start date,
	registration_end date,
	patient_id varchar(36) NOT NULL,
	last_updated timestamp NOT NULL,
	organisation_type_code varchar(10),
	CONSTRAINT pk_patient_search PRIMARY KEY (service_id, system_id, patient_id)
);

CREATE INDEX ix_patient
  ON patient_search (patient_id);

CREATE INDEX ix_service_system_surname_forenames
   ON patient_search (service_id, system_id, surname, forenames);

CREATE INDEX ix_service_system_nhs_number
  ON patient_search (service_id, system_id, nhs_number);

CREATE INDEX ix_service_system_date_of_birth
  ON patient_search (service_id, system_id, date_of_birth);

CREATE INDEX ix_service_system_patient
  ON patient_search (service_id, system_id, patient_id);

-- Cross-org search indexes (exclude system_id)
CREATE INDEX ix_service_date_of_birth
  ON patient_search (service_id, date_of_birth);

CREATE INDEX ix_service_nhs_number
  ON patient_search (service_id, nhs_number);

CREATE INDEX ix_service_surname_forenames
  ON patient_search (service_id, surname, forenames);


CREATE TABLE patient_search_local_identifier
(
	service_id char(36) NOT NULL,
	system_id char(36) NOT NULL,
	local_id varchar(1000),
	local_id_system varchar(1000),
	patient_id char(36) NOT NULL,
	last_updated timestamp NOT NULL,
	CONSTRAINT pk_patient_search_local_identifier PRIMARY KEY (service_id, system_id, patient_id, local_id_system, local_id),
	CONSTRAINT fk_patient_search_local_identifier_patient_id FOREIGN KEY (service_id, system_id, patient_id)
		REFERENCES patient_search (service_id, system_id, patient_id) MATCH SIMPLE
		ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE INDEX ix_service_system_patient_id
  ON patient_search_local_identifier (service_id, system_id, patient_id);


CREATE TABLE patient_link
(
  patient_id character(36) NOT NULL,
  person_id character(36) NOT NULL,
  CONSTRAINT pk_patient_link PRIMARY KEY (patient_id)
);

CREATE INDEX ix_person_id
  ON patient_link (person_id);


CREATE TABLE patient_link_history
(
  patient_id character(36) NOT NULL,
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

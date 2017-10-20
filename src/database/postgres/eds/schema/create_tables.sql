DROP TABLE IF EXISTS patient_search_local_identifier;
DROP TABLE IF EXISTS patient_search;
DROP TABLE IF EXISTS patient_link;
DROP TABLE IF EXISTS patient_link_history;
DROP TABLE IF EXISTS patient_link_person;

CREATE TABLE patient_search
(
	service_id character(36) NOT NULL,
	system_id character(36) NOT NULL,
	nhs_number character varying(10),
	forenames character varying(1000),
	surname character varying(1000),
	date_of_birth date,
	date_of_death date,
	postcode character varying(8),
	gender character varying(7),
	registration_start date,
	registration_end date,
	patient_id character(36) NOT NULL,
	last_updated timestamp NOT NULL,
	organisation_type_code character varying(10),
	CONSTRAINT pk_patient_search PRIMARY KEY (service_id, system_id, patient_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE patient_search
  OWNER TO postgres;

CREATE INDEX ix_patient
  ON public.patient_search
  USING btree
  (patient_id COLLATE pg_catalog."default");

CREATE INDEX ix_service_system_surname_forenames
  ON public.patient_search
  USING btree
  (service_id COLLATE pg_catalog."default", system_id COLLATE pg_catalog."default", surname COLLATE pg_catalog."default", forenames COLLATE pg_catalog."default");

CREATE INDEX ix_service_system_nhs_number
  ON public.patient_search
  USING btree
  (service_id COLLATE pg_catalog."default", system_id COLLATE pg_catalog."default", nhs_number COLLATE pg_catalog."default");

CREATE INDEX ix_service_system_date_of_birth
  ON public.patient_search
  USING btree
  (service_id COLLATE pg_catalog."default", system_id COLLATE pg_catalog."default", date_of_birth);

CREATE INDEX ix_service_system_patient
  ON public.patient_search
  USING btree
  (service_id COLLATE pg_catalog."default", system_id COLLATE pg_catalog."default", patient_id COLLATE pg_catalog."default");

-- Cross-org search indexes (exclude system_id)
CREATE INDEX ix_service_date_of_birth
  ON public.patient_search
  USING btree
  (service_id COLLATE pg_catalog."default", date_of_birth);

CREATE INDEX ix_service_nhs_number
  ON public.patient_search
  USING btree
  (service_id COLLATE pg_catalog."default", nhs_number COLLATE pg_catalog."default");

CREATE INDEX ix_service_surname_forenames
  ON public.patient_search
  USING btree
  (service_id COLLATE pg_catalog."default", surname COLLATE pg_catalog."default", forenames COLLATE pg_catalog."default");

CREATE TABLE public.patient_search_local_identifier
(
  service_id character(36) NOT NULL,
  system_id character(36) NOT NULL,
  local_id character varying(1000) NOT NULL,
  local_id_system character varying(1000) NOT NULL,
  patient_id character(36) NOT NULL,
  last_updated timestamp without time zone NOT NULL,
  CONSTRAINT pk_patient_search_local_identifier PRIMARY KEY (service_id, system_id, patient_id, local_id_system, local_id),
  CONSTRAINT fk_patient_search_local_identifier_patient_id FOREIGN KEY (service_id, system_id, patient_id)
      REFERENCES public.patient_search (service_id, system_id, patient_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.patient_search_local_identifier
  OWNER TO postgres;

CREATE INDEX ix_service_system_patient_id
  ON public.patient_search_local_identifier
  USING btree
  (service_id COLLATE pg_catalog."default", system_id COLLATE pg_catalog."default", patient_id COLLATE pg_catalog."default", local_id COLLATE pg_catalog."default");


-- Table: public.patient_link

-- DROP TABLE public.patient_link;

CREATE TABLE public.patient_link
(
  patient_id character(36) NOT NULL,
  person_id character(36) NOT NULL,
  CONSTRAINT pk_patient_link PRIMARY KEY (patient_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.patient_link
  OWNER TO postgres;

-- Index: public.ix_person_id

-- DROP INDEX public.ix_person_id;

CREATE INDEX ix_person_id
  ON public.patient_link
  USING btree
  (person_id COLLATE pg_catalog."default");


-- Table: public.patient_link_history

-- DROP TABLE public.patient_link_history;

CREATE TABLE public.patient_link_history
(
  patient_id character(36) NOT NULL,
  updated timestamp without time zone NOT NULL,
  new_person_id character(36) NOT NULL,
  previous_person_id character(36),
  CONSTRAINT pk_patient_link_history PRIMARY KEY (patient_id, updated)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.patient_link_history
  OWNER TO postgres;

-- Index: public.ix_updated

-- DROP INDEX public.ix_updated;

CREATE INDEX ix_updated
  ON public.patient_link_history
  USING btree
  (updated);

-- Table: public.patient_link_person

-- DROP TABLE public.patient_link_person;

CREATE TABLE public.patient_link_person
(
  person_id character(36) NOT NULL,
  nhs_number character(10) NOT NULL,
  CONSTRAINT pk_patient_link_person PRIMARY KEY (person_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.patient_link_person
  OWNER TO postgres;

-- Index: public.ix_nhs_number

-- DROP INDEX public.ix_nhs_number;

CREATE UNIQUE INDEX ix_nhs_number
  ON public.patient_link_person
  USING btree
  (nhs_number COLLATE pg_catalog."default");






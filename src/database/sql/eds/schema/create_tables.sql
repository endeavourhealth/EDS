
DROP TABLE IF EXISTS patient_search_local_identifier;
DROP TABLE IF EXISTS patient_search;

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
	gender character varying(6),
	registration_start date,
	registration_end date,
	patient_id character(36) NOT NULL,
	last_updated timestamp NOT NULL,
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


CREATE TABLE patient_search_local_identifier
(
	service_id character(36) NOT NULL,
	system_id character(36) NOT NULL,
	local_id character varying(1000),
	local_id_system character varying(1000),
	patient_id character(36) NOT NULL,
	last_updated timestamp NOT NULL,
	CONSTRAINT pk_patient_search_local_identifier PRIMARY KEY (service_id, system_id, patient_id, local_id_system),
	CONSTRAINT fk_patient_search_local_identifier_patient_id FOREIGN KEY (service_id, system_id, patient_id)
		REFERENCES public.patient_search (service_id, system_id, patient_id) MATCH SIMPLE
		ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE patient_search_local_identifier
  OWNER TO postgres;


CREATE INDEX ix_service_system_patient_id
  ON public.patient_search_local_identifier
  USING btree
  (service_id COLLATE pg_catalog."default", system_id COLLATE pg_catalog."default", patient_id COLLATE pg_catalog."default", local_id COLLATE pg_catalog."default");






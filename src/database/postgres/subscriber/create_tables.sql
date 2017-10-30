
DROP TABLE IF EXISTS enterprise_id_map;
DROP TABLE IF EXISTS enterprise_organisation_id_map;
DROP TABLE IF EXISTS household_id_map;
DROP TABLE IF EXISTS pseudo_id_map;
DROP TABLE IF EXISTS enterprise_age;
DROP TABLE IF EXISTS enterprise_person_id_map;
DROP TABLE IF EXISTS enterprise_person_update_history;
DROP TABLE IF EXISTS vitru_care_patient_id_map;

DROP SEQUENCE IF EXISTS enterprise_id_seq;
DROP SEQUENCE IF EXISTS household_id_seq;
DROP SEQUENCE IF EXISTS enterprise_person_id_seq;


-- Sequence: public.enterprise_id_seq

-- DROP SEQUENCE public.enterprise_id_seq;

CREATE SEQUENCE public.enterprise_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.enterprise_id_seq
  OWNER TO postgres;


-- Table: public.enterprise_id_map

-- DROP TABLE public.enterprise_id_map;

CREATE TABLE public.enterprise_id_map
(
  resource_type character varying(255) NOT NULL,
  resource_id character varying(255) NOT NULL,
  enterprise_id bigint NOT NULL DEFAULT nextval('enterprise_id_seq'::regclass),
  CONSTRAINT pk_enterprise_id_map PRIMARY KEY (resource_id, resource_type)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.enterprise_id_map
  OWNER TO postgres;


-- Table: public.enterprise_organisation_id_map

-- DROP TABLE public.enterprise_organisation_id_map;

CREATE TABLE public.enterprise_organisation_id_map
(
  service_id character(36) NOT NULL,
  system_id character(36) NOT NULL,
  enterprise_id bigint NOT NULL,
  CONSTRAINT pk_enterprise_organisation_id_map PRIMARY KEY (service_id, system_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.enterprise_organisation_id_map
  OWNER TO postgres;


-- Sequence: public.household_id_seq

-- DROP SEQUENCE public.household_id_seq;

CREATE SEQUENCE public.household_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.household_id_seq
  OWNER TO postgres;

  -- Table: public.household_id_map

-- DROP TABLE public.household_id_map;

CREATE TABLE public.household_id_map
(
  postcode character(8) NOT NULL,
  line_1 character varying(255) NOT NULL,
  line_2 character varying(255) NOT NULL,
  household_id bigint NOT NULL DEFAULT nextval('household_id_seq'::regclass),
  CONSTRAINT pk_household_id_map PRIMARY KEY (postcode, line_1, line_2)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.household_id_map
  OWNER TO postgres;

-- Table: public.pseudo_id_map

-- DROP TABLE public.pseudo_id_map;

CREATE TABLE public.pseudo_id_map
(
  patient_id character varying(255) NOT NULL,
  pseudo_id character varying(255) NOT NULL,
  CONSTRAINT pk_pseudo_id_map PRIMARY KEY (patient_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.pseudo_id_map
  OWNER TO postgres;


-- Table: public.enterprise_age

-- DROP TABLE public.enterprise_age;

CREATE TABLE public.enterprise_age
(
  enterprise_patient_id bigint NOT NULL,
  date_of_birth date NOT NULL,
  date_next_change date NOT NULL,
  CONSTRAINT pk_enterprise_age PRIMARY KEY (enterprise_patient_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.enterprise_age
  OWNER TO postgres;

CREATE INDEX ix_date_next_change
  ON public.enterprise_age
  USING btree
  (date_next_change);



-- Sequence: public.enterprise_person_id_seq

-- DROP SEQUENCE public.enterprise_person_id_seq;

CREATE SEQUENCE public.enterprise_person_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.enterprise_person_id_seq
  OWNER TO postgres;


-- Table: public.enterprise_person_id_map

-- DROP TABLE public.enterprise_person_id_map;

CREATE TABLE public.enterprise_person_id_map
(
  person_id character(36) NOT NULL,
  enterprise_person_id bigint NOT NULL DEFAULT nextval('enterprise_person_id_seq'::regclass),
  CONSTRAINT pk_enterprise_person_id_map PRIMARY KEY (person_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.enterprise_person_id_map
  OWNER TO postgres;


-- Table: public.person_update_history

-- DROP TABLE public.enterprise_person_update_history;

CREATE TABLE public.enterprise_person_update_history
(
  date_run timestamp without time zone NOT NULL,
  CONSTRAINT pk_person_update_history PRIMARY KEY (date_run)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.enterprise_person_update_history
  OWNER TO postgres;



CREATE TABLE vitru_care_patient_id_map (
	eds_patient_id character varying(36),
	service_id character varying(36),
	system_id character varying(36),
	created_at timestamp without time zone NOT NULL,
	vitrucare_id varchar(250),
    CONSTRAINT pk_resource_id_map PRIMARY KEY (eds_patient_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.vitru_care_patient_id_map
  OWNER TO postgres;


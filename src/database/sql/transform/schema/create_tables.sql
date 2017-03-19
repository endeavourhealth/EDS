
DROP TABLE IF EXISTS enterprise_id_map;
DROP TABLE IF EXISTS enterprise_organisation_id_map;
DROP TABLE IF EXISTS household_id_map;

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
  enterprise_table_name character varying(255) NOT NULL,
  resource_type character varying(255) NOT NULL,
  resource_id character varying(255) NOT NULL,
  enterprise_id bigint NOT NULL DEFAULT nextval('enterprise_id_seq'::regclass),
  CONSTRAINT pk_enterprise_id_map PRIMARY KEY (enterprise_table_name, resource_type, resource_id)
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
  ods_code character varying(100) NOT NULL,
  enterprise_id bigint NOT NULL,
  CONSTRAINT pk_enterprise_organisation_id_map PRIMARY KEY (ods_code)
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


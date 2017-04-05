-- Table: public.postcode_reference

-- DROP TABLE public.postcode_reference;

CREATE TABLE public.postcode_lookup
(
  postcode_no_space character varying(8) NOT NULL,
  postcode character varying(8) NOT NULL,
  lsoa_code character varying(9),
  msoa_code character varying(9),
  ward character varying(9),
  ward_1998 character varying(6),
  ccg character varying(3),
  townsend_score real,
  CONSTRAINT pk_postcode_lookup PRIMARY KEY (postcode_no_space)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.postcode_lookup
  OWNER TO postgres;


-- Table: public.lsoa_lookup

-- DROP TABLE public.lsoa_lookup;

CREATE TABLE public.lsoa_lookup
(
  lsoa_code character varying(9) NOT NULL,
  lsoa_name character varying(255),
  CONSTRAINT pk_lsoa_lookup PRIMARY KEY (lsoa_code)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.lsoa_lookup
  OWNER TO postgres;


-- Table: public.msoa_lookup

-- DROP TABLE public.msoa_lookup;

CREATE TABLE public.msoa_lookup
(
  msoa_code character varying(9) NOT NULL,
  msoa_name character varying(255),
  CONSTRAINT pk_msoa_lookup PRIMARY KEY (msoa_code)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.msoa_lookup
  OWNER TO postgres;

-- Table: public.deprivation_lookup

-- DROP TABLE public.deprivation_lookup;

CREATE TABLE public.deprivation_lookup
(
  lsoa_code character varying(255) NOT NULL,
  imd_score real,
  imd_decile integer,
  CONSTRAINT pk_deprivation_lookup PRIMARY KEY (lsoa_code)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.deprivation_lookup
  OWNER TO postgres;

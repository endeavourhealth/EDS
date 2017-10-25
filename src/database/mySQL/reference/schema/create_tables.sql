USE reference;

DROP TABLE IF EXISTS postcode_lookup;
DROP TABLE IF EXISTS lsoa_lookup;
DROP TABLE IF EXISTS msoa_lookup;
DROP TABLE IF EXISTS deprivation_lookup;
DROP TABLE IF EXISTS snomed_lookup;

CREATE TABLE postcode_lookup
(
  postcode_no_space varchar(8) NOT NULL,
  postcode varchar(8) NOT NULL,
  lsoa_code varchar(9),
  msoa_code varchar(9),
  ward varchar(9),
  ward_1998 varchar(6),
  ccg varchar(3),
  CONSTRAINT pk_postcode_lookup PRIMARY KEY (postcode_no_space)
);


CREATE TABLE lsoa_lookup
(
  lsoa_code varchar(9) NOT NULL,
  lsoa_name varchar(255),
  CONSTRAINT pk_lsoa_lookup PRIMARY KEY (lsoa_code)
);


CREATE TABLE msoa_lookup
(
  msoa_code varchar(9) NOT NULL,
  msoa_name varchar(255),
  CONSTRAINT pk_msoa_lookup PRIMARY KEY (msoa_code)
);


CREATE TABLE deprivation_lookup
(
  lsoa_code varchar(255) NOT NULL,
  imd_rank integer NOT NULL,
  imd_decile integer NOT NULL,
  income_rank integer NOT NULL,
  income_decile integer NOT NULL,
  employment_rank integer NOT NULL,
  employment_decile integer NOT NULL,
  education_rank integer NOT NULL,
  education_decile integer NOT NULL,
  health_rank integer NOT NULL,
  health_decile integer NOT NULL,
  crime_rank integer NOT NULL,
  crime_decile integer NOT NULL,
  housing_and_services_barriers_rank integer NOT NULL,
  housing_and_services_barriers_decile integer NOT NULL,
  living_environment_rank integer NOT NULL,
  living_environment_decile integer NOT NULL,
  CONSTRAINT pk_deprivation_lookup PRIMARY KEY (lsoa_code)
);


CREATE TABLE encounter_code
(
  code bigint NOT NULL,
  term varchar(255),
  mapping varchar(1024),
  CONSTRAINT pk_encounter_code PRIMARY KEY (code)
);


CREATE UNIQUE INDEX ix_encounter_code_mapping
  ON encounter_code (mapping);

CREATE TABLE snomed_lookup (
    concept_id varchar(50),
    type_id varchar(50),
    term text,
    CONSTRAINT pk_encounter_code PRIMARY KEY (concept_id)
);
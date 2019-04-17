USE reference;

DROP TABLE IF EXISTS postcode_lookup;
DROP TABLE IF EXISTS lsoa_lookup;
DROP TABLE IF EXISTS msoa_lookup;
DROP TABLE IF EXISTS local_authority_lookup;
DROP TABLE IF EXISTS ccg_lookup;
DROP TABLE IF EXISTS ward_lookup;
DROP TABLE IF EXISTS deprivation_lookup;
DROP TABLE IF EXISTS encounter_code;
DROP TABLE IF EXISTS snomed_lookup;
DROP TABLE IF EXISTS snomed_description_link;
DROP TABLE IF EXISTS trm_concept_pc_link;
DROP TABLE IF EXISTS trm_concept;
DROP TABLE IF EXISTS opcs4_lookup;
DROP TABLE IF EXISTS icd10_lookup;
DROP TABLE IF EXISTS cerner_clinical_event_map;
DROP TABLE IF EXISTS ctv3_to_snomed_map;
DROP TABLE IF EXISTS read2_to_snomed_map;
DROP TABLE IF EXISTS ctv3_to_read2_map;


CREATE TABLE postcode_lookup
(
  postcode_no_space varchar(8) NOT NULL,
  postcode varchar(8) NOT NULL,
  lsoa_code varchar(9),
  msoa_code varchar(9),
  ward_code varchar(9),
  ccg_code varchar(3),
  local_authority_code varchar(9) COMMENT 'london borough, local authority',
  CONSTRAINT pk_postcode_lookup PRIMARY KEY (postcode_no_space)
);

CREATE TABLE local_authority_lookup
(
  local_authority_code varchar(9) NOT NULL,
  local_authority_name varchar(255),
  CONSTRAINT pk_local_authority_lookup PRIMARY KEY (local_authority_code)
);


CREATE TABLE ccg_lookup
(
  ccg_code varchar(9) NOT NULL,
  ccg_name varchar(255),
  CONSTRAINT pk_ccg_lookup PRIMARY KEY (ccg_code)
);

CREATE TABLE ward_lookup
(
  ward_code varchar(9) NOT NULL,
  ward_name varchar(255),
  CONSTRAINT pk_ward_lookup PRIMARY KEY (ward_code)
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
  imd_score decimal(5, 3) NOT NULL COMMENT 'Index of Multiple Deprivation (IMD) Score',
  imd_rank integer NOT NULL COMMENT 'Index of Multiple Deprivation (IMD) Rank (where 1 is most deprived)',
  imd_decile integer NOT NULL COMMENT 'Index of Multiple Deprivation (IMD) Decile (where 1 is most deprived 10% of LSOAs)',
  income_score decimal(5, 3) NOT NULL COMMENT 'Income Score (rate)',
  income_rank integer NOT NULL COMMENT 'Income Rank (where 1 is most deprived)',
  income_decile integer NOT NULL COMMENT 'Income Decile (where 1 is most deprived 10% of LSOAs)',
  employment_score decimal(5, 3) NOT NULL COMMENT 'Employment Score (rate)',
  employment_rank integer NOT NULL COMMENT 'Employment Rank (where 1 is most deprived)',
  employment_decile integer NOT NULL COMMENT 'Employment Decile (where 1 is most deprived 10% of LSOAs)',
  education_score decimal(5, 3) NOT NULL COMMENT 'Education, Skills and Training Score',
  education_rank integer NOT NULL COMMENT 'Education, Skills and Training Rank (where 1 is most deprived)',
  education_decile integer NOT NULL COMMENT 'Education, Skills and Training Decile (where 1 is most deprived 10% of LSOAs)',
  health_score decimal(5, 3) NOT NULL COMMENT 'Health Deprivation and Disability Score',
  health_rank integer NOT NULL COMMENT 'Health Deprivation and Disability Rank (where 1 is most deprived)',
  health_decile integer NOT NULL COMMENT 'Health Deprivation and Disability Decile (where 1 is most deprived 10% of LSOAs)',
  crime_score decimal(5, 3) NOT NULL COMMENT 'Crime Score',
  crime_rank integer NOT NULL COMMENT 'Crime Rank (where 1 is most deprived)',
  crime_decile integer NOT NULL COMMENT 'Crime Decile (where 1 is most deprived 10% of LSOAs)',
  housing_and_services_barriers_score decimal(5, 3) NOT NULL COMMENT 'Barriers to Housing and Services Score',
  housing_and_services_barriers_rank integer NOT NULL COMMENT 'Barriers to Housing and Services Rank (where 1 is most deprived)',
  housing_and_services_barriers_decile integer NOT NULL COMMENT 'Barriers to Housing and Services Decile (where 1 is most deprived 10% of LSOAs)',
  living_environment_score decimal(5, 3) NOT NULL COMMENT 'Living Environment Score',
  living_environment_rank integer NOT NULL COMMENT 'Living Environment Rank (where 1 is most deprived)',
  living_environment_decile integer NOT NULL COMMENT 'Living Environment Decile (where 1 is most deprived 10% of LSOAs)',
  idaci_score decimal(5, 3) NOT NULL COMMENT 'Income Deprivation Affecting Children Index (IDACI) Score (rate)',
  idaci_rank integer NOT NULL COMMENT 'Income Deprivation Affecting Children Index (IDACI) Rank (where 1 is most deprived)',
  idaci_decile integer NOT NULL COMMENT 'Income Deprivation Affecting Children Index (IDACI) Decile (where 1 is most deprived 10% of LSOAs)',
  idaopi_score decimal(5, 3) NOT NULL COMMENT 'Income Deprivation Affecting Older People (IDAOPI) Score (rate)',
  idaopi_rank integer NOT NULL COMMENT 'Income Deprivation Affecting Older People (IDAOPI) Rank (where 1 is most deprived)',
  idaopi_decile integer NOT NULL COMMENT 'Income Deprivation Affecting Older People (IDAOPI) Decile (where 1 is most deprived 10% of LSOAs)',
  children_and_young_sub_domain_score decimal(5, 3) NOT NULL COMMENT 'Children and Young People Sub-domain Score',
  children_and_young_sub_domain_rank integer NOT NULL COMMENT 'Children and Young People Sub-domain Rank (where 1 is most deprived)',
  children_and_young_sub_domain_decile  integer NOT NULL COMMENT 'Children and Young People Sub-domain Decile (where 1 is most deprived 10% of LSOAs)',
  adult_skills_sub_somain_score decimal(5, 3) NOT NULL COMMENT 'Adult Skills Sub-domain Score',
  adult_skills_sub_somain_rank integer NOT NULL COMMENT 'Adult Skills Sub-domain Rank (where 1 is most deprived)',
  adult_skills_sub_somain_decile integer NOT NULL COMMENT 'Adult Skills Sub-domain Decile (where 1 is most deprived 10% of LSOAs)',
  grographical_barriers_sub_domain_score decimal(5, 3) NOT NULL COMMENT 'Geographical Barriers Sub-domain Score',
  grographical_barriers_sub_domain_rank integer NOT NULL COMMENT 'Geographical Barriers Sub-domain Rank (where 1 is most deprived)',
  grographical_barriers_sub_domain_decile integer NOT NULL COMMENT 'Geographical Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)',
  wider_barriers_sub_domain_score decimal(5, 3) NOT NULL COMMENT 'Wider Barriers Sub-domain Score',
  wider_barriers_sub_domain_rank integer NOT NULL COMMENT 'Wider Barriers Sub-domain Rank (where 1 is most deprived)',
  wider_barriers_sub_domain_decile integer NOT NULL COMMENT 'Wider Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)',
  indoors_sub_domain_score decimal(5, 3) NOT NULL COMMENT 'Indoors Sub-domain Score',
  indoors_sub_domain_rank integer NOT NULL COMMENT 'Indoors Sub-domain Rank (where 1 is most deprived)',
  indoors_sub_domain_decile integer NOT NULL COMMENT 'Indoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)',
  outdoors_sub_domain_score decimal(5, 3) NOT NULL COMMENT 'Outdoors Sub-domain Score',
  outdoors_sub_domain_rank integer NOT NULL COMMENT 'Outdoors Sub-domain Rank (where 1 is most deprived)',
  outdoors_sub_domain_decile integer NOT NULL COMMENT 'Outdoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)',
  total_population integer NOT NULL COMMENT 'Total population: mid 2012 (excluding prisoners)',
  dependent_children_0_to_15 integer NOT NULL COMMENT 'Dependent Children aged 0-15: mid 2012 (excluding prisoners)',
  population_16_to_59 integer NOT NULL COMMENT 'Population aged 16-59: mid 2012 (excluding prisoners)',
  older_population_60_and_over integer NOT NULL COMMENT 'Older population aged 60 and over: mid 2012 (excluding prisoners)',
  -- working_age_population integer NOT NULL COMMENT 'Working age population 18-59/64: for use with Employment Deprivation Domain (excluding prisoners)',
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

CREATE TABLE snomed_description_link (
	description_id varchar(50) NOT NULL,
	concept_id varchar(50) NOT NULL,
  CONSTRAINT pk_snomed_description_lookup PRIMARY KEY (description_id, concept_id)
);

CREATE TABLE trm_concept
(
    pid bigint NOT NULL PRIMARY KEY,
    code varchar(100) NOT NULL,
    codesystem_pid bigint,
    display varchar(400),
    index_status bigint
);

CREATE INDEX idx_code
  ON trm_concept (code);

CREATE UNIQUE INDEX idx_code_system
  ON trm_concept (code, codesystem_pid);


CREATE TABLE trm_concept_pc_link
(
    pid bigint NOT NULL PRIMARY KEY,
    rel_type integer,
    child_pid bigint NOT NULL,
    codesystem_pid bigint NOT NULL,
    parent_pid bigint NOT NULL
);

CREATE TABLE read2_to_snomed_map
(
  map_id varchar (38) NOT NULL PRIMARY KEY,
  read_code varchar (5) NOT NULL COLLATE utf8_bin,
  term_code varchar (2) NOT NULL,
  concept_id varchar (18) NOT NULL,
  effective_date date NOT NULL,
  map_status int NOT NULL
);

CREATE INDEX ix_read2_to_snomed_map_read_code_concept_id
ON read2_to_snomed_map (read_code, concept_id);

CREATE TABLE ctv3_to_snomed_map
(
  map_id varchar(38) NOT NULL PRIMARY KEY,
  ctv3_concept_id varchar (12) NOT NULL COLLATE utf8_bin,
  ctv3_term_id varchar(6) NOT NULL,
  ctv3_term_type varchar (1),
  sct_concept_id varchar (18) NOT NULL,
  sct_description_id varchar (18),
  map_status int NOT NULL,
  effective_date date NOT NULL,
  is_assured int NOT NULL
);

CREATE INDEX ix_ctv3_to_snomed_map_ctv3_concept_id_sct_concept_id
ON ctv3_to_snomed_map (ctv3_concept_id, sct_concept_id);


CREATE TABLE snomed_lookup (
  concept_id varchar (18) NOT NULL PRIMARY KEY,
  term text NOT NULL,
  type_id int NOT NULL
);

create table opcs4_lookup (
  procedure_code varchar(10),
  procedure_name varchar(255),
  CONSTRAINT pk_postcode_lookup PRIMARY KEY (procedure_code)
);

create table snomed_to_bnf_chapter_lookup (
  snomed_code varchar(20) NOT NULL PRIMARY KEY,
  bnf_chapter_code varchar(20)
);

create table icd10_lookup (
  code varchar(10),
  description varchar(255),
  CONSTRAINT pk_postcode_lookup PRIMARY KEY (code)
);

CREATE TABLE cerner_clinical_event_map (
	cerner_cvref_code varchar(10),
    cerner_cvref_term varchar(50),
    snomed_concept_id varchar(50),
    snomed_preferred_term varchar(255),
    snomed_description_id varchar(50),
    snomed_description_term varchar(255),
    match_algorithm varchar(50),
    CONSTRAINT pk_internal_id_map PRIMARY KEY (cerner_cvref_code)
);

CREATE TABLE ctv3_to_read2_map
(
  map_id varchar(38) NOT NULL PRIMARY KEY,
  ctv3_concept_id varchar (12) NOT NULL COLLATE utf8_bin,
  ctv3_term_id varchar(6) NOT NULL,
  ctv3_term_type varchar (1),
  read2_concept_id varchar (5) NOT NULL COLLATE utf8_bin,
  read2_term_id varchar (5),
  map_type varchar(1),
  map_status int NOT NULL,
  effective_date date NOT NULL,
  is_assured int NOT NULL
);

CREATE INDEX ix_ctv3_to_read2_map_ctv3_concept_id_read2_concept_id
  ON ctv3_to_read2_map (ctv3_concept_id, read2_concept_id);
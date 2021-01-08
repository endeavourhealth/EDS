USE eds;

DROP PROCEDURE IF EXISTS get_dds_patient_counts_now;
DROP PROCEDURE IF EXISTS get_dds_patient_counts;
DROP PROCEDURE IF EXISTS get_sel_patient_counts;
DROP TABLE IF EXISTS patient_link;
DROP TABLE IF EXISTS patient_link_history;
DROP TABLE IF EXISTS patient_link_person;
DROP TABLE IF EXISTS patient_search_local_identifier;
DROP TABLE IF EXISTS patient_search_episode;
DROP TABLE IF EXISTS patient_search;
DROP TABLE IF EXISTS patient_address_uprn;
DROP TABLE IF EXISTS patient_search_address;

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

create index ix_new_person_id on eds.patient_link_history (new_person_id);

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
	dt_deleted datetime,
	ods_code varchar(50),
	organisation_name VARCHAR(255),
	organisation_type_code varchar(10),
	nhs_number_verification_status char(2),
  dt_created datetime,
  test_patient boolean,
	CONSTRAINT pk_patient_search PRIMARY KEY (service_id, patient_id)
);



CREATE INDEX ix_patient
  ON patient_search (patient_id);

-- duplicate of primary key (clusterd index) so removed
/*CREATE INDEX ix_service_patient
  ON patient_search (service_id, patient_id);*/

CREATE INDEX ix_service_date_of_birth
  ON patient_search (service_id, date_of_birth, dt_deleted);

-- swap index to be NHS Number first, since that's more selective than a long list of service IDs
/*CREATE INDEX ix_service_nhs_number
  ON patient_search (service_id, nhs_number);*/

CREATE INDEX ix_service_nhs_number_2
  ON patient_search (nhs_number, service_id, dt_deleted);

CREATE INDEX ix_service_surname_forenames
  ON patient_search (service_id, surname, forenames, dt_deleted);



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
	registration_status_code varchar(10),
	dt_deleted datetime,
	ods_code varchar(50),
	CONSTRAINT pk_patient_search_episode PRIMARY KEY (service_id, patient_id, episode_id)
);

-- unique index required so patient merges trigger a change in patient_id
CREATE UNIQUE INDEX uix_patient_search_episode_id
  ON patient_search_episode (episode_id);

CREATE INDEX ix_patient_id
  ON eds.patient_search_episode (patient_id);


CREATE TABLE patient_search_local_identifier
(
	service_id char(36) NOT NULL,
	local_id varchar(255),
	local_id_system varchar(255),
	patient_id char(36) NOT NULL,
	last_updated timestamp NOT NULL,
	dt_deleted datetime,
	CONSTRAINT pk_patient_search_local_identifier PRIMARY KEY (service_id, patient_id, local_id_system, local_id)
);

-- index so patient search by local ID works in timely fashion
CREATE INDEX ix_patient_search_local_identifier_id_service_patient
  ON patient_search_local_identifier (local_id, service_id, patient_id, dt_deleted);

create table patient_address_uprn (
	service_id char(36) not null,
    patient_id char(36) not null,
    uprn bigint default null,
    qualifier varchar(50),
    abp_address varchar(1024),
    `algorithm` varchar(255),
    `match` varchar(255),
    no_address boolean,
    invalid_address boolean,
    missing_postcode boolean,
    invalid_postcode boolean,
    CONSTRAINT pk_patient_search PRIMARY KEY (service_id, patient_id)
);

create index ix on patient_address_uprn (uprn);
create index ix2 on patient_address_uprn (patient_id);


/*
-- not sure about this table yet
create table patient_search_address (
	service_id char(36) NOT NULL,
	patient_id char(36) NOT NULL,
    ordinal tinyint NOT NULL,
	`use` varchar(10),
    start_date date,
    end_date date,
    address_line_1 varchar(255),
    address_line_2 varchar(255),
    address_line_3 varchar(255),
    address_line_4 varchar(255),
    city varchar(255),
    district varchar(255),
    postcode varchar(10),
    last_updated timestamp NOT NULL,
    uprn_results JSON,
    uprn_last_updated timestamp,
	CONSTRAINT pk_patient_search_episode PRIMARY KEY (service_id, patient_id, ordinal)
);
*/


-- procedure to get basic stats out of DDS from a given baseline date

DELIMITER //
CREATE PROCEDURE get_dds_patient_counts_now()
BEGIN

	CALL get_dds_patient_counts(DATE(NOW()));

END //
DELIMITER ;



DELIMITER $$
CREATE PROCEDURE `get_dds_patient_counts`(
	    IN date_cutoff date
)
BEGIN

	-- avoid locking the table
	SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED ;

	drop table if exists tmp.gp_services;
	drop table if exists tmp.acute_services;
    drop table if exists tmp.community_services;
    drop table if exists tmp.other_services;
	drop table if exists tmp.ccg_region;
	drop table if exists tmp.patient_search_baseline;
	drop table if exists tmp.person_count_1;
	drop table if exists tmp.person_count_2;
	drop table if exists tmp.patient_count_gp;
	drop table if exists tmp.patient_count_acute;


	-- create table of GP ODS codes
	create table tmp.gp_services (
		ods_code varchar(25)
	);

	insert into tmp.gp_services
    select local_id
    from admin.service
    where organisation_type = 'PR'; -- GP practice

	create index ix on tmp.gp_services  (ods_code);


	-- create table of acute ODS codes
	create table tmp.acute_services (
		ods_code varchar(25)
	);

	insert into tmp.acute_services
	select local_id
    from admin.service
    where organisation_type = 'TR'; -- trust

	create index ix on tmp.acute_services (ods_code);


	-- create table of community ODS codes
	create table tmp.community_services (
		ods_code varchar(25)
	);

	insert into tmp.community_services
    select local_id
    from admin.service
    where organisation_type = 'CO'; -- community

	create index ix on tmp.community_services (ods_code);


 	-- create table of community ODS codes
	create table tmp.other_services (
		ods_code varchar(25)
	);

	insert into tmp.other_services
    select local_id
    from admin.service
    where organisation_type NOT IN ('CO', 'TR', 'PR')
    or organisation_type IS NULL;

	create index ix on tmp.other_services  (ods_code);


	-- create table of regions so we can break GP practices down by region
	create table tmp.ccg_region (
		region_name varchar(25),
		ccg_code varchar(25)
	);

	insert into tmp.ccg_region values ('NEL', '07L');
	insert into tmp.ccg_region values ('NEL', '08F');
	insert into tmp.ccg_region values ('NEL', '08N');
	insert into tmp.ccg_region values ('NEL', '08M');
	insert into tmp.ccg_region values ('NEL', '08V');
	insert into tmp.ccg_region values ('NEL', '07T');
	insert into tmp.ccg_region values ('NEL', '08W');

	insert into tmp.ccg_region values ('NWL', '08G');
	insert into tmp.ccg_region values ('NWL', '08C');
	insert into tmp.ccg_region values ('NWL', '08E');
	insert into tmp.ccg_region values ('NWL', '08Y');
	insert into tmp.ccg_region values ('NWL', '07P');
	insert into tmp.ccg_region values ('NWL', '07Y');
	insert into tmp.ccg_region values ('NWL', '07W');
	insert into tmp.ccg_region values ('NWL', '09A');

	insert into tmp.ccg_region values ('SEL', '07N');
	insert into tmp.ccg_region values ('SEL', '07Q');
	insert into tmp.ccg_region values ('SEL', '08A');
	insert into tmp.ccg_region values ('SEL', '08K');
	insert into tmp.ccg_region values ('SEL', '08L');
	insert into tmp.ccg_region values ('SEL', '08Q');
	insert into tmp.ccg_region values ('SEL', '72Q');

	create index ix on tmp.ccg_region (region_name, ccg_code);
	create index ix2 on tmp.ccg_region (ccg_code, region_name);

	-- create table, baseline to required date
	create table tmp.patient_search_baseline as
	select service_id, patient_id, nhs_number, organisation_type_code
	from patient_search
	where dt_created < date(date_cutoff);

	create index ix on tmp.patient_search_baseline (nhs_number);
	create index ix2 on tmp.patient_search_baseline (service_id);

	create table tmp.person_count_1 as
	select count(distinct nhs_number) as `cnt`
	from tmp.patient_search_baseline
	where nhs_number is not null;

	create table tmp.person_count_2 as
	select count(1) as `cnt`
	from tmp.patient_search_baseline
	where nhs_number is null;

	create table tmp.patient_count_gp as
	select s.local_id, s.ccg_code, count(1) as `cnt`
	from tmp.patient_search_baseline b
	inner join admin.service s
	on s.id = b.service_id
    inner join tmp.gp_services a
    on a.ods_code = s.local_id
	group by s.local_id, s.ccg_code;

	create table tmp.patient_count_acute as
	select s.local_id, s.ccg_code, count(1) as `cnt`
	from tmp.patient_search_baseline b
	inner join admin.service s
	on s.id = b.service_id
	inner join tmp.acute_services a
	on a.ods_code = s.local_id
	group by s.local_id, s.ccg_code;

	create table tmp.patient_count_community as
	select s.local_id, s.ccg_code, count(1) as `cnt`
	from tmp.patient_search_baseline b
	inner join admin.service s
	on s.id = b.service_id
	inner join tmp.community_services a
	on a.ods_code = s.local_id
	group by s.local_id, s.ccg_code;

	create table tmp.patient_count_other as
	select s.local_id, s.ccg_code, count(1) as `cnt`
	from tmp.patient_search_baseline b
	inner join admin.service s
	on s.id = b.service_id
	inner join tmp.other_services a
	on a.ods_code = s.local_id
	group by s.local_id, s.ccg_code;

	-- number of person records

	select
		(select cnt from tmp.person_count_1) + (select cnt from tmp.person_count_2) as `person_count`;

	-- number of GP patient records

	select sum(cnt) as `gp_patient_count`
	from tmp.patient_count_gp g;

    select if (region_name is null, 'Other', region_name) as `region_name`, sum(cnt) as `gp_patient_count_per_region`
	from tmp.patient_count_gp g
	left outer join tmp.ccg_region r
	on g.ccg_code = r.ccg_code
	group by if (region_name is null, 'zzOther', region_name); -- "ZZ" prefix means "other" ends up last


	-- number of contributing GP services

	select count(distinct local_id) as `gp_practice_count`
	from tmp.patient_count_gp g;

	select if (region_name is null, 'Other', region_name) as `region_name`, count(distinct local_id) as `gp_practice_count_per_region`
	from tmp.patient_count_gp g
	left outer join tmp.ccg_region r
	on g.ccg_code = r.ccg_code
	group by if (region_name is null, 'zzOther', region_name); -- "ZZ" prefix means "other" ends up last


	-- number of acute patient records

	select sum(cnt) as `number_acute_patients`
	from tmp.patient_count_acute;

	select count(distinct local_id) as `number_acute_services`
	from tmp.patient_count_acute;


	-- number of community patient records

	select sum(cnt) as `number_community_patients`
	from tmp.patient_count_community;

	select count(distinct local_id) as `number_community_services`
	from tmp.patient_count_community;


	-- number of community patient records

	select sum(cnt) as `number_other_patients`
	from tmp.patient_count_other;

	select count(distinct local_id) as `number_other_services`
	from tmp.patient_count_other;

	drop table if exists tmp.gp_services;
	drop table if exists tmp.acute_services;
    drop table if exists tmp.community_services;
    drop table if exists tmp.other_services;
	drop table if exists tmp.ccg_region;
	drop table if exists tmp.patient_search_baseline;
	drop table if exists tmp.person_count_1;
	drop table if exists tmp.person_count_2;
	drop table if exists tmp.patient_count_gp;
	drop table if exists tmp.patient_count_acute;
    drop table if exists tmp.patient_count_community;
    drop table if exists tmp.patient_count_other;


    -- restore this back to default
    SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ ;


END$$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE `get_sel_patient_counts`()
BEGIN


	SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

	drop table if exists tmp.SD200_sel_practices;

	create table tmp.SD200_sel_practices as
	select id, local_id, name, ccg_code
	from admin.service
	where ccg_code IN ('07N', '07Q', '08A', '08K', '08L', '08Q', '72Q');

	create index ix on tmp.SD200_sel_practices (id);

	select s.local_id as `ODS Code`, s.name as `Name`, s.ccg_code as `CCG Code`, count(1) as `DDS Patient Count`
	from tmp.SD200_sel_practices s
	inner join eds.patient_search ps
	on ps.service_id = s.id
	group by s.local_id, s.name, s.ccg_code
	order by s.local_id;

	drop table if exists tmp.SD200_sel_practices;

END$$
DELIMITER ;

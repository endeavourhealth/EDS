-- This script contains the consolidated columns between PI and Pseudo database types and the
-- additional creation scripts for new tables to support the Compass v1 upgrade

-- (A) add new columns and pseudo columns to PI database types
use enterprise_pi;  -- change this as required

-- new columns first
alter table patient add title varchar(50);
alter table patient add first_names  varchar(255);
alter table patient add last_names  varchar(255);
alter table patient add current_address_id bigint;
alter table person add title varchar(50);
alter table person add first_names  varchar(255);
alter table person add last_names  varchar(255);
alter table practitioner add gmc_code varchar(50);
alter table referral_request add specialty varchar(50);
alter table referral_request add ubrn varchar(50);

-- from pseudo to pi
alter table patient add pseudo_id varchar(255);
alter table patient add age_years integer;
alter table patient add age_months integer;
alter table patient add age_weeks integer;
alter table patient add postcode_prefix varchar(20);
alter table person add pseudo_id varchar(255);
alter table person add age_years integer;
alter table person add age_months integer;
alter table person add age_weeks integer;
alter table person add postcode_prefix varchar(20);

-- alter table appointment add booked_date datetime;

-- NOTE:  the Patient to Person trigger will need updating (see create tables) to process the additional columns

-- NOTE: lookup to following table creation scripts from the create_tables_PI.sql file
    -- link_distributer
    -- patient_address
    -- patient_address_match  (if not already exists)
    -- patient_contact
    -- registration_status_history

-- WARNING: these optional date_recorded columns based on project need as adding to larger tables may be problematic
alter table encounter add date_recorded datetime;
alter table observation add date_recorded datetime;
alter table allergy_intolerance add date_recorded datetime;
alter table procedure_request add date_recorded datetime;
alter table referral_request add date_recorded datetime;

-- NOTE:  the  subscriber Id mapping table will need creating for Compass v1 (subscriber_id_map from subscriber_transform)


-- (B) add new columns and PI columns to pseudo database types, i.e. ceg
use enterprise_pseudo;   -- change this as required

-- new columns first
alter table patient add title varchar(50);
alter table patient add first_names  varchar(255);
alter table patient add last_names  varchar(255);
alter table patient add current_address_id bigint;
alter table person add title varchar(50);
alter table person add first_names  varchar(255);
alter table person add last_names  varchar(255);
alter table practitioner add gmc_code varchar(50);
alter table referral_request add specialty varchar(50);
alter table referral_request add ubrn varchar(50);
-- alter table appointment add booked_date datetime;

-- NOTE:  the Patient to Person trigger will need updating (see create tables) to process the additional columns
-- NOTE:  event_log script with triggers

-- from pi to pseudo
alter table patient add nhs_number varchar(255);
alter table patient add date_of_birth date;
alter table patient add postcode varchar(20);
alter table person add nhs_number varchar(255);
alter table person add date_of_birth date;
alter table person add postcode varchar(20);

-- NOTE: lookup to following table creation scripts from the create_tables_PSUDONYMISED.sql file
-- link_distributer  (already exists)
-- patient_address
-- patient_address_match  (if not already exists)
-- patient_contact
-- registration_status_history

-- WARNING: these optional date_recorded columns based on project need as adding to larger tables may be problematic
alter table encounter add date_recorded datetime;
alter table observation add date_recorded datetime;
alter table allergy_intolerance add date_recorded datetime;
alter table procedure_request add date_recorded datetime;
alter table referral_request add date_recorded datetime;

-- NOTE:  the  subscriber Id mapping table will need creating for Compass v1 (subscriber_id_map from subscriber_transform)
-- NOTE:  event_log script with triggers
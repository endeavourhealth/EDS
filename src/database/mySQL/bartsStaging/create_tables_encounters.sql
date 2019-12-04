use staging_barts;

drop table if exists encounter_cds;
drop table if exists encounter_cds_latest;
drop table if exists encounter_cds_tail;
drop table if exists encounter_cds_tail_latest;

drop table if exists encounter_target;
drop table if exists encounter_target_latest;

-- records from sus inpatient, outpatient and emergency files are written to this table,
create table encounter_cds
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                    datetime     NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                bigint       NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date              datetime     NOT NULL COMMENT 'Date common to all sus files',
    sus_record_type                varchar(10)  NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier          varchar(50)  NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type                int          NOT NULL COMMENT 'from CDSUpdateType',
    mrn                            varchar(10)  NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                     varchar(10)  NOT NULL COMMENT 'from NHSNumber',
    withheld                       bool         COMMENT 'True if id is withheld',
    date_of_birth                  date         COMMENT 'from PersonBirthDate',
    consultant_code                varchar(20)  NOT NULL COMMENT 'GMC number of consultant, from ConsultantCode',
    treatment_function_code        varchar(10)   COMMENT 'specific to consultant',
    -- specific spell admission / discharge / episode stuff here
    spell_number                   varchar(12),
    admission_category_code        varchar(2),
    admission_method_code          varchar(2),
    admission_source_code          varchar(2),
    patient_classification         char(1),
    spell_start_date               date         COMMENT 'CCYYMMDD',
    spell_start_time               time         COMMENT 'HHSSMM',
    discharge_date                 date         COMMENT 'CCYYMMDD',
    discharge_time                 time         COMMENT 'HHSSMM',
    discharge_destination_code     varchar(2),
    discharge_method               char(1),
    episode_number                 varchar(2),
    episode_start_site_code        varchar(12)  COMMENT 'location at start of episode',
    episode_start_date             date         COMMENT 'CCYYMMDD',
    episode_start_time             time         COMMENT 'HHSSMM',
    episode_end_site_code          varchar(12)  COMMENT 'location at end of episode',
    episode_end_date               date         COMMENT 'CCYYMMDD',
    episode_end_time               time         COMMENT 'HHSSMM',
    lookup_person_id               int          COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int          COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null COMMENT 'Used for Audit Purposes',
    CONSTRAINT pk_encounter_cds PRIMARY KEY (exchange_id, cds_unique_identifier, sus_record_type, spell_number)
);

CREATE INDEX ix_encounter_cds_checksum_helper on encounter_cds (cds_unique_identifier, sus_record_type, spell_number, dt_received);

create table encounter_cds_latest
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                    datetime     NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                bigint       NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date              datetime     NOT NULL COMMENT 'Date common to all sus files',
    sus_record_type                varchar(10)  NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier          varchar(50)  NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type                int          NOT NULL COMMENT 'from CDSUpdateType',
    mrn                            varchar(10)  NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                     varchar(10)  NOT NULL COMMENT 'from NHSNumber',
    withheld                       bool         COMMENT 'True if id is withheld',
    date_of_birth                  date COMMENT 'from PersonBirthDate',
    consultant_code                varchar(20)  COMMENT 'GMC number of consultant, from ConsultantCode',
    treatment_function_code        varchar(10)   COMMENT 'specific to consultant',
    -- specific spell admission / discharge / episode stuff here
    spell_number                   varchar(12),
    admission_category_code        varchar(2),
    admission_method_code          varchar(2),
    admission_source_code          varchar(2),
    patient_classification         char(1),
    spell_start_date               date         COMMENT 'CCYYMMDD',
    spell_start_time               time         COMMENT 'HHSSMM',
    discharge_date                 date         COMMENT 'CCYYMMDD',
    discharge_time                 time         COMMENT 'HHSSMM',
    discharge_destination_code     varchar(2),
    discharge_method               char(1),
    episode_number                 varchar(2),
    episode_start_site_code        varchar(12)  COMMENT 'location at start of episode',
    episode_start_date             date         COMMENT 'CCYYMMDD',
    episode_start_time             time         COMMENT 'HHSSMM',
    episode_end_site_code          varchar(12)  COMMENT 'location at end of episode',
    episode_end_date               date         COMMENT 'CCYYMMDD',
    episode_end_time               time         COMMENT 'HHSSMM',
    lookup_person_id               int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_encounter_cds_latest PRIMARY KEY (cds_unique_identifier, sus_record_type, spell_number)
);

CREATE INDEX ix_encounter_cds_latest_join_helper on encounter_cds_latest (exchange_id, cds_unique_identifier, sus_record_type, spell_number);


-- records from sus inpatient, outpatient and emergency tail files are all written to this table with sus_record_type telling us which is which
create table encounter_cds_tail
(
    exchange_id                  char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                  datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum              bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    sus_record_type              varchar(10) NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier        varchar(50),
    cds_update_type              int         NOT NULL COMMENT 'from CDSUpdateType',
    mrn                          varchar(10) NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                   varchar(10) NOT NULL COMMENT 'from NHSNumber',
    person_id                    int,
    encounter_id                 int,
    responsible_hcp_personnel_id int         COMMENT 'from Responsible_HCP_Personal_ID',
    treatment_function_code      varchar(10),
    audit_json                   mediumtext  NULL COMMENT 'Used for Audit Purposes',
    CONSTRAINT pk_encounter_cds_tail PRIMARY KEY (exchange_id, cds_unique_identifier, sus_record_type)
);

CREATE INDEX ix_encounter_cds_tail_checksum_helper on encounter_cds_tail (cds_unique_identifier, sus_record_type, dt_received);


create table encounter_cds_tail_latest
(
    exchange_id                  char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                  datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum              bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    sus_record_type              varchar(10) NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier        varchar(50) NOT NULL,
    cds_update_type              int         NOT NULL COMMENT 'from CDSUpdateType',
    mrn                          varchar(10) NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                   varchar(10) NOT NULL COMMENT 'from NHSNumber',
    person_id                    int NOT NULL,
    encounter_id                 int NOT NULL,
    responsible_hcp_personnel_id int NOT NULL COMMENT 'from Responsible_HCP_Personal_ID',
    treatment_function_code      varchar(10),
    audit_json                   mediumtext  null comment 'Used for Audit Purposes',
    CONSTRAINT pk_encounter_cds_tail_latest PRIMARY KEY (cds_unique_identifier, sus_record_type)
);


-- target table for the above tables to populate, cleared down for each exchange
create table encounter_target
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    unique_id                      varchar(255) NOT NULL COMMENT 'unique ID derived from source IDs',
    is_delete                      bool         NOT NULL COMMENT 'if this encounter should be deleted or upserted',
    person_id                      int          COMMENT 'person ID for the encounter',
    encounter_id                   int          COMMENT 'encounter ID',
    performer_personnel_id         int          COMMENT 'performer ID for the encounter',

    treatment_function_code        varchar(10)   COMMENT 'specific to consultant',
    spell_number                   varchar(12),
    admission_category_code        varchar(2),
    admission_method_code          varchar(2),
    admission_source_code          varchar(2),
    patient_classification         char(1),
    spell_start_date               date         COMMENT 'CCYYMMDD',
    spell_start_time               time         COMMENT 'HHSSMM',
    discharge_date                 date         COMMENT 'CCYYMMDD',
    discharge_time                 time         COMMENT 'HHSSMM',
    discharge_destination_code     varchar(2),
    discharge_method               char(1),
    episode_number                 varchar(2),
    episode_start_site_code        varchar(12)  COMMENT 'location at start of episode',
    episode_start_date             date         COMMENT 'CCYYMMDD',
    episode_start_time             time         COMMENT 'HHSSMM',
    episode_end_site_code          varchar(12)  COMMENT 'location at end of episode',
    episode_end_date               date         COMMENT 'CCYYMMDD',
    episode_end_time               time         COMMENT 'HHSSMM',

    audit_json                     mediumtext NULL COMMENT 'Used for Audit Purposes',
    is_confidential                bool         COMMENT 'if this condition should be confidential or not',
    CONSTRAINT pk_encounter_target PRIMARY KEY (exchange_id, unique_id)
);

create index ix_duplication_helper on encounter_target (person_id, encounter_id, spell_start_date, unique_id);


-- latest version of every record that is in the target table
create table encounter_target_latest
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    unique_id                      varchar(255) NOT NULL COMMENT 'unique ID derived from source IDs',
    is_delete                      bool         NOT NULL COMMENT 'if this encounter should be deleted or upserted',
    person_id                      int          COMMENT 'person ID for the encounter',
    encounter_id                   int          COMMENT 'encounter ID',
    performer_personnel_id         int          COMMENT 'performer ID for the encounter',

    treatment_function_code        varchar(10)   COMMENT 'specific to consultant',
    spell_number                   varchar(12),
    admission_category_code        varchar(2),
    admission_method_code          varchar(2),
    admission_source_code          varchar(2),
    patient_classification         char(1),
    spell_start_date               date         COMMENT 'CCYYMMDD',
    spell_start_time               time         COMMENT 'HHSSMM',
    discharge_date                 date         COMMENT 'CCYYMMDD',
    discharge_time                 time         COMMENT 'HHSSMM',
    discharge_destination_code     varchar(2),
    discharge_method               char(1),
    episode_number                 varchar(2),
    episode_start_site_code        varchar(12)  COMMENT 'location at start of episode',
    episode_start_date             date         COMMENT 'CCYYMMDD',
    episode_start_time             time         COMMENT 'HHSSMM',
    episode_end_site_code          varchar(12)  COMMENT 'location at end of episode',
    episode_end_date               date         COMMENT 'CCYYMMDD',
    episode_end_time               time         COMMENT 'HHSSMM',

    audit_json                     mediumtext NULL COMMENT 'Used for Audit Purposes',
    is_confidential                bool        COMMENT 'if this condition should be confidential or not',
    CONSTRAINT pk_encounter_target PRIMARY KEY (unique_id)
);
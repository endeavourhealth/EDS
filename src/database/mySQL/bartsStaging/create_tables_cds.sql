use staging_barts;

drop table if exists cds_inpatient;
drop table if exists cds_inpatient_latest;
drop table if exists cds_outpatient;
drop table if exists cds_outpatient_latest;
drop table if exists cds_emergency;
drop table if exists cds_emergency_latest;
drop table if exists cds_tail;
drop table if exists cds_tail_latest;


-- records from sus inpatient files are written to this table
create table cds_inpatient
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                    datetime     NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                bigint       NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date              datetime     NOT NULL COMMENT 'Date common to all sus files',
    -- sus_record_type                varchar(10)  NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier          varchar(50)  NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type                int          NOT NULL COMMENT 'from CDSUpdateType',
    mrn                            varchar(10)  NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                     varchar(10)  NOT NULL COMMENT 'from NHSNumber',
    withheld                       bool         COMMENT 'True if id is withheld',
    date_of_birth                  date         COMMENT 'from PersonBirthDate',
    consultant_code                varchar(20)  NOT NULL COMMENT 'GMC number of consultant, from ConsultantCode',

    spell_number                   varchar(12),
    admission_method_code          varchar(2)   COMMENT 'LKP_CDS_ADMISS_METHOD',
    admission_source_code          varchar(2)   COMMENT 'LKP_CDS_ADMISS_SOURCE',
    patient_classification         char(1)      COMMENT 'LKP_CDS_PATIENT_CLASS',
    spell_start_date               date         COMMENT 'start date of hospital spell: CCYYMMDD',
    spell_start_time               time         COMMENT 'start time of hospital spell: HHSSMM',
    episode_number                 varchar(2),
    episode_start_site_code        varchar(12)  COMMENT 'location at start of episode',
    episode_start_date             date         COMMENT 'episode start date: CCYYMMDD',
    episode_start_time             time         COMMENT 'episode start time: HHSSMM',
    episode_end_site_code          varchar(12)  COMMENT 'location at end of episode',
    episode_end_date               date         COMMENT 'episode end date: CCYYMMDD',
    episode_end_time               time         COMMENT 'episode end time: HHSSMM',
    discharge_date                 date         COMMENT 'date of discharge: CCYYMMDD',
    discharge_time                 time         COMMENT 'time of discharge: HHSSMM',
    discharge_destination_code     varchar(2)   COMMENT 'LKP_CDS_DISCH_DEST',
    discharge_method               char(1)      COMMENT 'LKP_CDS_DISCH_METHOD',

    lookup_person_id               int          COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int          COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null COMMENT 'Used for Audit Purposes',
    CONSTRAINT pk_cds_inpatient PRIMARY KEY (exchange_id, cds_unique_identifier)
);
-- index to make it easier to find last checksum for a CDS inpatient record
CREATE INDEX ix_cds_inpatient_checksum_helper on cds_inpatient (cds_unique_identifier, dt_received);

create table cds_inpatient_latest
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                    datetime     NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                bigint       NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date              datetime     NOT NULL COMMENT 'Date common to all sus files',
    -- sus_record_type                varchar(10)  NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier          varchar(50)  NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type                int          NOT NULL COMMENT 'from CDSUpdateType',
    mrn                            varchar(10)  NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                     varchar(10)  NOT NULL COMMENT 'from NHSNumber',
    withheld                       bool         COMMENT 'True if id is withheld',
    date_of_birth                  date COMMENT 'from PersonBirthDate',
    consultant_code                varchar(20)  COMMENT 'GMC number of consultant, from ConsultantCode',

    spell_number                   varchar(12),
    admission_method_code          varchar(2)   COMMENT 'LKP_CDS_ADMISS_METHOD',
    admission_source_code          varchar(2)   COMMENT 'LKP_CDS_ADMISS_SOURCE',
    patient_classification         char(1)      COMMENT 'LKP_CDS_PATIENT_CLASS',
    spell_start_date               date         COMMENT 'start date of hospital spell: CCYYMMDD',
    spell_start_time               time         COMMENT 'start time of hospital spell: HHSSMM',
    episode_number                 varchar(2),
    episode_start_site_code        varchar(12)  COMMENT 'location at start of episode',
    episode_start_date             date         COMMENT 'episode start date: CCYYMMDD',
    episode_start_time             time         COMMENT 'episode start time: HHSSMM',
    episode_end_site_code          varchar(12)  COMMENT 'location at end of episode',
    episode_end_date               date         COMMENT 'episode end date: CCYYMMDD',
    episode_end_time               time         COMMENT 'episode end time: HHSSMM',
    discharge_date                 date         COMMENT 'date of discharge: CCYYMMDD',
    discharge_time                 time         COMMENT 'time of discharge: HHSSMM',
    discharge_destination_code     varchar(2)   COMMENT 'LKP_CDS_DISCH_DEST',
    discharge_method               char(1)      COMMENT 'LKP_CDS_DISCH_METHOD',

    lookup_person_id               int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_cds_inpatient_latest PRIMARY KEY (cds_unique_identifier)
);
CREATE INDEX ix_cds_inpatient_latest_join_helper on cds_inpatient_latest (exchange_id, cds_unique_identifier);


-- records from sus outpatient files are written to this table
create table cds_outpatient
(
    exchange_id                     char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                     datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                 bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date               datetime    NOT NULL COMMENT 'Date common to all sus files',
    -- sus_record_type                 varchar(10) NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier           varchar(50) NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type                 int         NOT NULL COMMENT 'from CDSUpdateType',
    mrn                             varchar(10) NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                      varchar(10) NOT NULL COMMENT 'from NHSNumber',
    withheld                        bool COMMENT 'True if id is withheld',
    date_of_birth                   date COMMENT 'from PersonBirthDate',
    consultant_code                 varchar(20) NOT NULL COMMENT 'GMC number of consultant, from ConsultantCode',

    appt_attendance_identifier      varchar(12),
    appt_attended_code              char(1)      COMMENT 'Attended or DNA code: LKP_CDS_ATTENDED',
    appt_outcome_code               char(1)      COMMENT 'LKP_CDS_ATTENDANCE_OUTCOME',
    appt_date                       date         COMMENT 'date of the outpatient appointment: CCYYMMDD',
    appt_time                       time         COMMENT 'time of the outpatient appointment: HHSSMM',
    appt_site_code                  varchar(12)  COMMENT 'location of appointment',

    lookup_person_id                int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id  int COMMENT 'personnel ID looked up using consultant code',
    audit_json                      mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_cds_outpatient  PRIMARY KEY (exchange_id, cds_unique_identifier)
);
-- index to make it easier to find last checksum for a CDS outpatient record
CREATE INDEX ix_cds_outpatient_checksum_helper on cds_outpatient (cds_unique_identifier, dt_received);

create table cds_outpatient_latest
(
    exchange_id                     char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                     datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                 bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date               datetime    NOT NULL COMMENT 'Date common to all sus files',
    -- sus_record_type                 varchar(10) NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier           varchar(50) NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type                 int         NOT NULL COMMENT 'from CDSUpdateType',
    mrn                             varchar(10) NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                      varchar(10) NOT NULL COMMENT 'from NHSNumber',
    withheld                        bool COMMENT 'True if id is withheld',
    date_of_birth                   date COMMENT 'from PersonBirthDate',
    consultant_code                 varchar(20) NOT NULL COMMENT 'GMC number of consultant, from ConsultantCode',

    appt_attendance_identifier      varchar(12),
    appt_attended_code              char(1)     COMMENT 'Attended or DNA code: LKP_CDS_ATTENDED',
    appt_outcome_code               char(1)     COMMENT 'LKP_CDS_ATTENDANCE_OUTCOME',
    appt_date                       date        COMMENT 'date of the outpatient appointment: CCYYMMDD',
    appt_time                       time        COMMENT 'time of the outpatient appointment: HHSSMM',
    appt_site_code                  varchar(12) COMMENT 'location of appointment',

    lookup_person_id                int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id  int COMMENT 'personnel ID looked up using consultant code',
    audit_json                      mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_cds_outpatient_latest PRIMARY KEY (cds_unique_identifier)
);
CREATE INDEX ix_cds_outpatient_latest_join_helper on cds_outpatient_latest (exchange_id, cds_unique_identifier);


-- records from sus accident and emergency files are written to this table
create table cds_emergency
(
    exchange_id           char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received           datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum       bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date     datetime    NOT NULL COMMENT 'Date common to all sus files',
    -- sus_record_type       varchar(10) NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier varchar(50) NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type       int         NOT NULL COMMENT 'from CDSUpdateType',
    mrn                   varchar(10) NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number            varchar(10) NOT NULL COMMENT 'from NHSNumber',
    withheld              bool COMMENT 'True if id is withheld',
    date_of_birth         date COMMENT 'from PersonBirthDate',
    consultant_code       varchar(20) NOT NULL COMMENT 'GMC number of consultant, from AEStaffMemberCode',

    attendance_number               varchar(12),
    arrival_mode_code               char(1)      COMMENT 'LKP_CDS_AEA_ARRIVAL_MODE',
    attendance_category_code        char(1)      COMMENT 'LKP_AEA_ATTEND_CAT',
    arrival_date                    date         COMMENT 'A&E arrival date: CCYYMMDD',
    arrival_time                    time         COMMENT 'A&E arrival time: HHSSMM',
    assessment_date                 date         COMMENT 'A&E assessment date: CCYYMMDD',
    assessment_time                 time         COMMENT 'A&E assessment time: HHSSMM',
    treatment_date                  date         COMMENT 'A&E treatment date (if relevant): CCYYMMDD',
    treatment_time                  time         COMMENT 'A&E treatment date (if relevant): HHSSMM',
    attendance_conclusion_date      date         COMMENT 'A&E conclusion date: CCYYMMDD',
    attendance_conclusion_time      time         COMMENT 'A&E conclusion time: HHSSMM',
    departure_date                  date         COMMENT 'A&E departure date: CCYYMMDD',
    departure_time                  time         COMMENT 'A&E departure time: HHSSMM',
    treatment_site_code             varchar(12)  COMMENT 'A&E location',

    aed_diagnosis_scheme            varchar(2)   COMMENT 'code scheme',
    primary_aed_diagnosis           varchar(6)   COMMENT 'actual code value',
    secondary_aed_diagnosis         varchar(300) COMMENT 'multiple further AED diagnosis in 6 character batches',

    investigation_scheme            varchar(2)   COMMENT 'code scheme',
    primary_investigation           varchar(6)   COMMENT 'actual code value',
    secondary_investigation         varchar(300) COMMENT 'multiple further AED diagnosis in 6 character batches',

    treatment_scheme                varchar(2)   COMMENT 'code scheme',
    primary_treatment               varchar(6)   COMMENT 'actual code value',
    primary_treatment_date          date,
    secondary_treatment             varchar(6)   COMMENT 'actual code value',
    secondary_treatment_date        date,
    other_treatment                 varchar(686) COMMENT 'multiple further AED diagnosis in 14 character batches (date (8) + code (6))',

    lookup_person_id               int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_cds_emergency PRIMARY KEY (exchange_id, cds_unique_identifier)
);
-- index to make it easier to find last checksum for a CDS emergency record
CREATE INDEX ix_cds_emergency_checksum_helper on cds_emergency (cds_unique_identifier, dt_received);

create table cds_emergency_latest
(
    exchange_id           char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received           datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum       bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date     datetime    NOT NULL COMMENT 'Date common to all sus files',
    -- sus_record_type       varchar(10) NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier varchar(50) NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type       int         NOT NULL COMMENT 'from CDSUpdateType',
    mrn                   varchar(10) NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number            varchar(10) NOT NULL COMMENT 'from NHSNumber',
    withheld              bool COMMENT 'True if id is withheld',
    date_of_birth         date COMMENT 'from PersonBirthDate',
    consultant_code       varchar(20) NOT NULL COMMENT 'GMC number of consultant, from AEStaffMemberCode',

    attendance_number               varchar(12),
    arrival_mode_code               char(1)      COMMENT 'LKP_CDS_AEA_ARRIVAL_MODE',
    attendance_category_code        char(1)      COMMENT 'LKP_AEA_ATTEND_CAT',
    arrival_date                    date         COMMENT 'A&E arrival date: CCYYMMDD',
    arrival_time                    time         COMMENT 'A&E arrival time: HHSSMM',
    assessment_date                 date         COMMENT 'A&E assessment date: CCYYMMDD',
    assessment_time                 time         COMMENT 'A&E assessment time: HHSSMM',
    treatment_date                  date         COMMENT 'A&E treatment date (if relevant): CCYYMMDD',
    treatment_time                  time         COMMENT 'A&E treatment date (if relevant): HHSSMM',
    attendance_conclusion_date      date         COMMENT 'A&E conclusion date: CCYYMMDD',
    attendance_conclusion_time      time         COMMENT 'A&E conclusion time: HHSSMM',
    departure_date                  date         COMMENT 'A&E departure date: CCYYMMDD',
    departure_time                  time         COMMENT 'A&E departure time: HHSSMM',
    treatment_site_code             varchar(12)  COMMENT 'A&E location',

    aed_diagnosis_scheme            varchar(2)   COMMENT 'code scheme',
    primary_aed_diagnosis           varchar(6)   COMMENT 'actual code value',
    secondary_aed_diagnosis         varchar(300) COMMENT 'multiple further AED diagnosis in 6 character batches',

    investigation_scheme            varchar(2)   COMMENT 'code scheme',
    primary_investigation           varchar(6)   COMMENT 'actual code value',
    secondary_investigation         varchar(300) COMMENT 'multiple further AED diagnosis in 6 character batches',

    treatment_scheme                varchar(2)   COMMENT 'code scheme',
    primary_treatment               varchar(6)   COMMENT 'actual code value',
    primary_treatment_date          date,
    secondary_treatment             varchar(6)   COMMENT 'actual code value',
    secondary_treatment_date        date,
    other_treatment                 varchar(686) COMMENT 'multiple further AED diagnosis in 14 character batches (date (8) + code (6))',

    lookup_person_id               int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_cds_emergency_latest PRIMARY KEY (cds_unique_identifier)
);
CREATE INDEX ix_cds_emergency_latest_join_helper on cds_emergency_latest (exchange_id, cds_unique_identifier);


-- records from sus inpatient, outpatient and emergency tail files are all written to this table with sus_record_type
-- telling us which is which and there there is an encounter_id for every entry
create table cds_tail
(
    exchange_id                  char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                  datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum              bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    sus_record_type              varchar(10) NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier        varchar(50) NOT NULL,
    cds_update_type              int         NOT NULL COMMENT 'from CDSUpdateType',
    mrn                          varchar(10) NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                   varchar(10) NOT NULL COMMENT 'from NHSNumber',
    person_id                    int         NOT NULL,
    encounter_id                 int         NOT NULL COMMENT 'encounterId always present',
    responsible_hcp_personnel_id int         NOT NULL COMMENT 'from Responsible_HCP_Personal_ID',
    treatment_function_code      varchar(10) COMMENT 'the treatment function code of the responsible hcp',
    audit_json                   mediumtext  NULL COMMENT 'Used for Audit Purposes',
    CONSTRAINT pk_cds_tail PRIMARY KEY (exchange_id, cds_unique_identifier, sus_record_type)
);
CREATE INDEX ix_cds_tail_checksum_helper on cds_tail (cds_unique_identifier, sus_record_type, dt_received);


create table cds_tail_latest
(
    exchange_id                  char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                  datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum              bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    sus_record_type              varchar(10) NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier        varchar(50) NOT NULL,
    cds_update_type              int         NOT NULL COMMENT 'from CDSUpdateType',
    mrn                          varchar(10) NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                   varchar(10) NOT NULL COMMENT 'from NHSNumber',
    person_id                    int         NOT NULL,
    encounter_id                 int         NOT NULL COMMENT 'encounterId always present',
    responsible_hcp_personnel_id int         NOT NULL COMMENT 'from Responsible_HCP_Personal_ID',
    treatment_function_code      varchar(10) COMMENT 'the treatment function code of the responsible hcp',
    audit_json                   mediumtext  NULL COMMENT 'Used for Audit Purposes',
    CONSTRAINT pk_cds_tail_latest PRIMARY KEY (cds_unique_identifier, sus_record_type)
);
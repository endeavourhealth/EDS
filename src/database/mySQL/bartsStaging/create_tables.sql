use staging_barts;

drop table if exists procedure_cds;
drop table if exists procedure_cds_latest;
drop table if exists procedure_cds_tail;
drop table if exists procedure_cds_tail_latest;
drop table if exists procedure_procedure;
drop table if exists procedure_procedure_latest;
drop table if exists procedure_PROCE;
drop table if exists procedure_PROCE_latest;
drop table if exists procedure_SURCC;
drop table if exists procedure_SURCC_latest;
drop table if exists procedure_SURCP;
drop table if exists procedure_SURCP_latest;
-- drop table if exists procedure_ecds;
-- drop table if exists procedure_ecds_tail;
drop table if exists procedure_target;
drop table if exists procedure_target_latest;

-- records from sus inpatient, outpatient and emergency files are written to this table, with a record PER procedure
create table procedure_cds
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
    date_of_birth                  datetime NOT NULL COMMENT 'from PersonBirthDate',
    consultant_code                varchar(20) NOT NULL COMMENT 'GMC number of consultant, from ConsultantCode',
    procedure_date                 date         NOT NULL COMMENT 'from PrimaryProcedureDate, SecondaryProcedureDate etc. - note this file has no time component',
    procedure_opcs_code            varchar(5)   NOT NULL COMMENT 'opcs-4 code in format ANN.N from PrimaryProcedureOPCS, SecondaryProcedureOPCS etc.',
    procedure_seq_nbr              smallint     NOT NULL COMMENT 'number of this procedure in the CDS record',
    primary_procedure_opcs_code    varchar(5) COMMENT 'opcs-4 code in format ANN.N from PrimaryProcedureOPCS - will be null if this record is for the primary procedure',
    lookup_procedure_opcs_term     varchar(255) NOT NULL COMMENT 'term for above opcs-4 code, looked up using TRUD',
    lookup_person_id               int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_cds PRIMARY KEY (exchange_id, cds_unique_identifier, sus_record_type, procedure_seq_nbr)
);

create table procedure_cds_latest
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
    date_of_birth                  datetime COMMENT 'from PersonBirthDate',
    consultant_code                varchar(20) COMMENT 'GMC number of consultant, from ConsultantCode',
    procedure_date                 date         NOT NULL COMMENT 'from PrimaryProcedureDate, SecondaryProcedureDate etc. - note this file has no time component',
    procedure_opcs_code            varchar(5)   NOT NULL COMMENT 'opcs-4 code in format ANN.N from PrimaryProcedureOPCS, SecondaryProcedureOPCS etc.',
    procedure_seq_nbr              smallint     NOT NULL COMMENT 'number of this procedure in the CDS record',
    primary_procedure_opcs_code    varchar(5) COMMENT 'opcs-4 code in format ANN.N from PrimaryProcedureOPCS - will be null if this record is for the primary procedure',
    lookup_procedure_opcs_term     varchar(255) NOT NULL COMMENT 'term for above opcs-4 code, looked up using TRUD',
    lookup_person_id               int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_cds_latest PRIMARY KEY (cds_unique_identifier, sus_record_type, procedure_seq_nbr)
);

CREATE INDEX ix_procedure_cds_latest_join_helper on procedure_cds_latest (exchange_id, cds_unique_identifier, sus_record_type, procedure_seq_nbr);

-- records from sus inpatient, outpatient and emergency tail files are all written to this table with sus_record_type telling us which is which
create table procedure_cds_tail
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
    responsible_hcp_personnel_id int COMMENT 'from Responsible_HCP_Personal_ID',
    audit_json                   mediumtext  null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_cds_tail PRIMARY KEY (exchange_id, cds_unique_identifier, sus_record_type)
);

create table procedure_cds_tail_latest
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
    audit_json                   mediumtext  null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_cds_tail_latest PRIMARY KEY (cds_unique_identifier, sus_record_type)
);


-- records from the fixed-width Procedure file
create table procedure_procedure
(
    exchange_id                     char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                     datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                 bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    mrn                             varchar(10) NOT NULL COMMENT 'from MRN',
    nhs_number                      varchar(10) NOT NULL COMMENT 'from NHS_No',
    date_of_birth                   datetime    NOT NULL COMMENT 'from DOB',
    encounter_id                    int         NOT NULL COMMENT 'from Encntr_Id, but standardised to remove trailing .00',
    consultant                      varchar(100) NOT NULL COMMENT 'free-text consultant name from Consultant',
    proc_dt_tm                      datetime NOT NULL COMMENT 'from Proc_Dt_Tm',
    updated_by                      varchar(100) NOT NULL COMMENT 'free-text person name of who updated this record, from Updt_By',
    freetext_comment                mediumtext NOT NULL COMMENT 'from Comment',
    create_dt_tm                    datetime NOT NULL COMMENT 'from Create_Dt_Tm',
    proc_cd_type                    varchar(50) NOT NULL COMMENT 'opcs or snomed',
    proc_cd                         varchar(50) NOT NULL COMMENT 'opcs-4 or snomed code, opcs-4 code in format ANN.N, from Proc_Cd',
    proc_term                       varchar(255) NOT NULL COMMENT 'corresponding term for the above code, looked up via TRUD',
    person_id                       int NOT NULL COMMENT 'pre-looked up from encntr_id',
    ward                            varchar(50) NOT NULL COMMENT 'from Ward',
    site                            varchar(50) NOT NULL COMMENT 'from Site',
    lookup_person_id                int COMMENT 'person ID looked up via Encounter ID',
    lookup_consultant_personnel_id  int COMMENT 'pre-looked up from consultant',
    lookup_recorded_by_personnel_id int COMMENT 'pre-looked up from updt_by',
    audit_json                      mediumtext  null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_procedure PRIMARY KEY (exchange_id, encounter_id, proc_dt_tm, proc_cd)
);

create table procedure_procedure_latest
(
    exchange_id                     char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                     datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                 bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    mrn                             varchar(10) NOT NULL COMMENT 'from MRN',
    nhs_number                      varchar(10) NOT NULL COMMENT 'from NHS_No',
    date_of_birth                   datetime    NOT NULL COMMENT 'from DOB',
    encounter_id                    int         NOT NULL COMMENT 'from Encntr_Id, but standardised to remove trailing .00',
    consultant                      varchar(100) COMMENT 'free-text consultant name from Consultant',
    proc_dt_tm                      datetime COMMENT 'from Proc_Dt_Tm',
    updated_by                      varchar(100) COMMENT 'free-text person name of who updated this record, from Updt_By',
    freetext_comment                mediumtext COMMENT 'from Comment',
    create_dt_tm                    datetime COMMENT 'from Create_Dt_Tm',
    proc_cd_type                    varchar(50) NOT NULL COMMENT 'opcs or snomed',
    proc_cd                         varchar(50) NOT NULL COMMENT 'opcs-4 or snomed code, opcs-4 code in format ANN.N, from Proc_Cd',
    proc_term                       varchar(255) COMMENT 'corresponding term for the above code, looked up via TRUD',
    person_id                       int COMMENT 'pre-looked up from encntr_id',
    ward                            varchar(50) COMMENT 'from Ward',
    site                            varchar(50) COMMENT 'from Site',
    lookup_person_id                int COMMENT 'person ID looked up via Encounter ID',
    lookup_consultant_personnel_id  int COMMENT 'pre-looked up from consultant',
    lookup_recorded_by_personnel_id int COMMENT 'pre-looked up from updt_by',
    audit_json                      mediumtext  null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_procedure_latest PRIMARY KEY (encounter_id, proc_dt_tm, proc_cd)
);

-- records from PROCE (UKRWH_CDE_PROCEDURE)
create table procedure_PROCE
(
    exchange_id          char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received          datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum      bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    procedure_id         int        NOT NULL COMMENT 'from PROCEDURE_ID',
    active_ind           bool       NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    encounter_id         int COMMENT 'from ENCNTR_ID',
    encounter_slice_id   int COMMENT 'from ENCNTR_SLICE_ID',
    procedure_dt_tm      datetime COMMENT 'from PROCEDURE_DT_TM',
    procedure_type       varchar(50) COMMENT 'opcs or snomed, derived from CONCEPT_CKI_IDENT',
    procedure_code       varchar(50) COMMENT 'opcs-4 or snomed code, opcs-4 code in format ANN.N, derived from CONCEPT_CKI_IDENT',
    procedure_term       varchar(255) COMMENT 'corresponding term for the above code, looked up via TRUD',
    procedure_seq_nbr    int COMMENT 'from PROCEDURE_SEQ_NBR',
    lookup_person_id     int COMMENT 'pre-looked up via ENCNTR_ID',
    lookup_mrn           varchar(10) COMMENT 'looked up via ENCNTR_ID',
    lookup_nhs_number    varchar(10) COMMENT 'patient NHS number, looked up via ENCNTR_ID',
    lookup_date_of_birth datetime COMMENT 'patient NHS number, looked up via ENCNTR_ID',
    audit_json           mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_PROCE PRIMARY KEY (exchange_id, procedure_id)
);

create table procedure_PROCE_latest
(
    exchange_id          char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received          datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum      bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    procedure_id         int        NOT NULL COMMENT 'from PROCEDURE_ID',
    active_ind           bool       NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    encounter_id         int COMMENT 'from ENCNTR_ID',
    encounter_slice_id   int COMMENT 'from ENCNTR_SLICE_ID',
    procedure_dt_tm      datetime COMMENT 'from PROCEDURE_DT_TM',
    procedure_type       varchar(50) COMMENT 'opcs or snomed, derived from CONCEPT_CKI_IDENT',
    procedure_code       varchar(50) COMMENT 'opcs-4 or snomed code, opcs-4 code in format ANN.N, derived from CONCEPT_CKI_IDENT',
    procedure_term       varchar(255) COMMENT 'corresponding term for the above code, looked up via TRUD',
    procedure_seq_nbr    int COMMENT 'from PROCEDURE_SEQ_NBR',
    lookup_person_id     int COMMENT 'pre-looked up via ENCNTR_ID',
    lookup_mrn           varchar(10) COMMENT 'looked up via ENCNTR_ID',
    lookup_nhs_number    varchar(10) COMMENT 'patient NHS number, looked up via ENCNTR_ID',
    lookup_date_of_birth datetime COMMENT 'patient NHS number, looked up via ENCNTR_ID',
    audit_json           mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_PROCE_latest PRIMARY KEY (procedure_id)
);

CREATE INDEX ix_procedure_procedure_join_helper ON procedure_PROCE_latest (exchange_id, encounter_id, procedure_dt_tm, procedure_code);

CREATE INDEX ix_procedure_procedure_parent_helper ON procedure_PROCE_latest (lookup_person_id, encounter_id, procedure_seq_nbr, encounter_slice_id);

-- records from SURCC (UKRWH_CDE_SURGICAL_CASE)
create table procedure_SURCC
(
    exchange_id         char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received         datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum     bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    surgical_case_id    int        NOT NULL COMMENT 'from SURGICAL_CASE_ID',
    dt_extract          datetime   NOT NULL COMMENT 'from EXTRACT_DT_TM',
    active_ind          bool       NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    person_id           int COMMENT 'from PERSON_ID',
    encounter_id        int COMMENT 'from ENCNTR_ID',
    dt_cancelled        datetime COMMENT 'from CANCELLED_DT_TM',
    institution_code    varchar(50) COMMENT 'from INSTITUTION_CD',
    department_code     varchar(50) COMMENT 'from DEPT_CD',
    surgical_area_code  varchar(50) COMMENT 'from SURGICAL_AREA_CD',
    theatre_number_code varchar(50) COMMENT 'from THEATRE_NBR_CD',
    audit_json          mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_SURCC PRIMARY KEY (exchange_id, surgical_case_id)
);

create table procedure_SURCC_latest
(
    exchange_id         char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received         datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum     bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    surgical_case_id    int        NOT NULL COMMENT 'from SURGICAL_CASE_ID',
    dt_extract          datetime   NOT NULL COMMENT 'from EXTRACT_DT_TM',
    active_ind          bool       NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    person_id           int COMMENT 'from PERSON_ID',
    encounter_id        int COMMENT 'from ENCNTR_ID',
    dt_cancelled        datetime COMMENT 'from CANCELLED_DT_TM',
    institution_code    varchar(50) COMMENT 'from INSTITUTION_CD',
    department_code     varchar(50) COMMENT 'from DEPT_CD',
    surgical_area_code  varchar(50) COMMENT 'from SURGICAL_AREA_CD',
    theatre_number_code varchar(50) COMMENT 'from THEATRE_NBR_CD',
    audit_json          mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_SURCC_latest PRIMARY KEY (surgical_case_id)
);

-- records from SURCP (UKRWH_CDE_SURGICAL_CASE_PROCEDURE)
create table procedure_SURCP
(
    exchange_id                 char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                 datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum             bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    surgical_case_procedure_id  int        NOT NULL COMMENT 'from SURGICAL_CASE_PROC_ID',
    surgical_case_id            int COMMENT 'from SURGICAL_CASE_ID',
    dt_extract                  datetime   NOT NULL COMMENT 'from EXTRACT_DT_TM',
    active_ind                  bool       NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    procedure_code              int COMMENT 'from PROC_CD, relates to CVREF - not an OPCS or Snomed code',
    procedure_text              mediumtext COMMENT 'from PROC_TXT',
    modifier_text               varchar(255) COMMENT 'from MODIFIER_TXT',
    primary_procedure_indicator int COMMENT 'from PRIMARY_PROC_IND',
    surgeon_personnel_id        int COMMENT 'from PRIMARY_SURGEON_PRSNL_ID',
    dt_start                    datetime COMMENT 'from PROC_START_DT_TM',
    dt_stop                     datetime COMMENT 'from PROC_STOP_DT_TM',
    wound_class_code            varchar(25) COMMENT 'from WOUND_CLASS_CD',
    lookup_procedure_code_term  varchar(255),
    audit_json                  mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_SURCP PRIMARY KEY (exchange_id, surgical_case_procedure_id)
);

create table procedure_SURCP_latest
(
    exchange_id                 char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                 datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum             bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    surgical_case_procedure_id  int        NOT NULL COMMENT 'from SURGICAL_CASE_PROC_ID',
    surgical_case_id            int COMMENT 'from SURGICAL_CASE_ID',
    dt_extract                  datetime   NOT NULL COMMENT 'from EXTRACT_DT_TM',
    active_ind                  bool       NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    procedure_code              int COMMENT 'from PROC_CD, relates to CVREF - not an OPCS or Snomed code',
    procedure_text              mediumtext COMMENT 'from PROC_TXT',
    modifier_text               varchar(255) COMMENT 'from MODIFIER_TXT',
    primary_procedure_indicator int COMMENT 'from PRIMARY_PROC_IND',
    surgeon_personnel_id        int COMMENT 'from PRIMARY_SURGEON_PRSNL_ID',
    dt_start                    datetime COMMENT 'from PROC_START_DT_TM',
    dt_stop                     datetime COMMENT 'from PROC_STOP_DT_TM',
    wound_class_code            varchar(25) COMMENT 'from WOUND_CLASS_CD',
    lookup_procedure_code_term  varchar(255),
    audit_json                  mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_SURCP_latest PRIMARY KEY (surgical_case_procedure_id)
);

CREATE INDEX ix_procedure_SURCP_latest_join_helper ON procedure_SURCP_latest (exchange_id, surgical_case_id);

-- CREATE INDEX ix_procedure_SURCP_surgical_case_procedure_id
--   ON procedure_SURCP (surgical_case_procedure_id);

/*   commented out for now
-- still no idea whether we should be looking at ECDS or not! ECDS replaced SusEmergency, so I think it should
create table procedure_ecds (
	exchange_id char(36) NOT NULL COMMENT ''links to audit.exchange table (but on a different server)'',
    dt_received datetime NOT NULL COMMENT ''date time this record was received into Discovery'',
    record_checksum bigint NOT NULL COMMENT ''checksum of the columns below to easily spot duplicates'',
	???,
	audit_json mediumtext null comment ''Used for Audit Purposes'',
);

-- still no idea whether we should be looking at ECDS or not! ECDS replaced SusEmergency, so I think it should
create table procedure_ecds_tail (
	exchange_id char(36) NOT NULL COMMENT ''links to audit.exchange table (but on a different server)'',
    dt_received datetime NOT NULL COMMENT ''date time this record was received into Discovery'',
    record_checksum bigint NOT NULL COMMENT ''checksum of the columns below to easily spot duplicates'',
	???,
	audit_json mediumtext null comment ''Used for Audit Purposes'',
);
*/

-- target table for the above tables to populate, cleared down for each exchange
create table procedure_target
(
    exchange_id                char(36)     NOT NULL COMMENT ' links to audit.exchange table (but on a different server)',
    unique_id                  varchar(255) NOT NULL COMMENT ' unique ID derived from source IDs ',
    is_delete                  bool         NOT NULL COMMENT ' if this procedure should be deleted or upserted ',
    person_id                  int COMMENT ' person ID for the procedure ',
    encounter_id               int COMMENT ' encounter ID for the procedure ',
    performer_personnel_id     int,
    dt_performed               datetime,
    free_text                  mediumtext,
    recorded_by_personnel_id   int,
    dt_recorded                datetime,
    procedure_type             varchar(50) COMMENT ' opcs, snomed or cerner code - tells us the schema of the below code ',
    procedure_code             varchar(50),
    procedure_term             varchar(255),
    sequence_number            int,
    parent_procedure_unique_id varchar(255),
    qualifier                  varchar(255),
    location                   varchar(255) COMMENT ' this will be updated on the linked Encounter resource ',
    specialty                  varchar(255) COMMENT ' this will be updated on the linked Encounter resource ',
    audit_json                 mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_target PRIMARY KEY (exchange_id, unique_id)
);


-- latest version of every record that is in the archive table
create table procedure_target_latest
(
    exchange_id                char(36)     NOT NULL COMMENT ' links to audit.exchange table (but on a different server)',
    unique_id                  varchar(255) NOT NULL COMMENT ' unique ID derived from source IDs ',
    is_delete                  bool         NOT NULL COMMENT ' if this procedure should be deleted or upserted ',
    person_id                  int COMMENT ' person ID for the procedure ',
    encounter_id               int COMMENT ' encounter ID for the procedure ',
    performer_personnel_id     int,
    dt_performed               datetime,
    free_text                  mediumtext,
    recorded_by_personnel_id   int,
    dt_recorded                datetime,
    procedure_type             varchar(50) COMMENT ' opcs, snomed or cerner code - tells us the schema of the below code ',
    procedure_code             varchar(50),
    procedure_term             varchar(255),
    sequence_number            int,
    parent_procedure_unique_id varchar(255),
    qualifier                  varchar(255),
    location                   varchar(255) COMMENT ' this will be updated on the linked Encounter resource ',
    specialty                  varchar(255) COMMENT ' this will be updated on the linked Encounter resource ',
    audit_json                 mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_procedure_target PRIMARY KEY (unique_id)
);


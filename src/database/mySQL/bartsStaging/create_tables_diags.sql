use staging_barts;

drop table if exists diagnosis_cds;
drop table if exists diagnosis_cds_latest;
drop table if exists diagnosis_cds_tail;
drop table if exists diagnosis_cds_tail_latest;
drop table if exists diagnosis_diagnosis;
drop table if exists diagnosis_diagnosis_latest;
drop table if exists diagnosis_DIAGN;
drop table if exists diagnosis_DIAGN_latest;
drop table if exists diagnosis_problem;
drop table if exists diagnosis_problem_latest;

drop table if exists diagnosis_target;
drop table if exists diagnosis_target_latest;

-- records from sus inpatient, outpatient and emergency files are written to this table, with a record PER diagnosis
-- NOTE: there is no diagnosis_date so cds_activity_date is used
create table diagnosis_cds
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                    datetime     NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                bigint       NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date              datetime     NOT NULL COMMENT 'Date common to all sus files. NOTE: use for diagnosis date',
    sus_record_type                varchar(10)  NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier          varchar(50)  NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type                int          NOT NULL COMMENT 'from CDSUpdateType',
    mrn                            varchar(10)  NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                     varchar(10)  NOT NULL COMMENT 'from NHSNumber',
    date_of_birth                  date     COMMENT 'from PersonBirthDate',
    consultant_code                varchar(20) NOT NULL COMMENT 'GMC number of consultant, from ConsultantCode',
    diagnosis_icd_code             varchar(5)   NOT NULL COMMENT 'icd-10 code PrimaryDiagnosisICD, SecondaryDiagnosisICD etc.',
    diagnosis_seq_nbr              smallint     NOT NULL COMMENT 'number of this diagnosis in the CDS record',
    primary_diagnosis_icd_code     varchar(5) COMMENT 'ics-10 code from PrimaryDiagnosisICD - will be null if this record is for the primary diagnosis',
    lookup_diagnosis_icd_term      varchar(255) NOT NULL COMMENT 'term for above icd-10 code, looked up using TRUD',
    lookup_person_id               int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_diagnosis_cds PRIMARY KEY (exchange_id, cds_unique_identifier, sus_record_type, diagnosis_seq_nbr)
);

create table diagnosis_cds_latest
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                    datetime     NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                bigint       NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    cds_activity_date              datetime     NOT NULL COMMENT 'Date common to all sus files. NOTE: use for diagnosis date',
    sus_record_type                varchar(10)  NOT NULL COMMENT 'one of inpatient, outpatient, emergency',
    cds_unique_identifier          varchar(50)  NOT NULL COMMENT 'from CDSUniqueIdentifier',
    cds_update_type                int          NOT NULL COMMENT 'from CDSUpdateType',
    mrn                            varchar(10)  NOT NULL COMMENT 'patient MRN from LocalPatientID field',
    nhs_number                     varchar(10)  NOT NULL COMMENT 'from NHSNumber',
    date_of_birth                  date COMMENT 'from PersonBirthDate',
    consultant_code                varchar(20) COMMENT 'GMC number of consultant, from ConsultantCode',
    diagnosis_icd_code             varchar(5)   NOT NULL COMMENT 'icd-10 code PrimaryDiagnosisICD, SecondaryDiagnosisICD etc.',
    diagnosis_seq_nbr              smallint     NOT NULL COMMENT 'number of this diagnosis in the CDS record',
    primary_diagnosis_icd_code     varchar(5) COMMENT 'ics-10 code from PrimaryDiagnosisICD - will be null if this record is for the primary diagnosis',
    lookup_diagnosis_icd_term      varchar(255) NOT NULL COMMENT 'term for above icd-10 code, looked up using TRUD',
    lookup_person_id               int COMMENT 'person ID looked up using NHS number, DoB and MRN',
    lookup_consultant_personnel_id int COMMENT 'personnel ID looked up using consultant code',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_diagnosis_cds_latest PRIMARY KEY (cds_unique_identifier, sus_record_type, diagnosis_seq_nbr)
);

CREATE INDEX ix_diagnosis_cds_latest_join_helper on diagnosis_cds_latest (exchange_id, cds_unique_identifier, sus_record_type, diagnosis_seq_nbr);

-- records from sus inpatient, outpatient and emergency tail files are all written to this table with sus_record_type telling us which is which
create table diagnosis_cds_tail
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
    CONSTRAINT pk_diagnosis_cds_tail PRIMARY KEY (exchange_id, cds_unique_identifier, sus_record_type)
);

create table diagnosis_cds_tail_latest
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
    CONSTRAINT pk_diagnosis_cds_tail_latest PRIMARY KEY (cds_unique_identifier, sus_record_type)
);

-- records from the fixed-width Diagnosis file
create table diagnosis_diagnosis
(
    exchange_id                     char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                     datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                 bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    diagnosis_id                    int         NOT NULL COMMENT 'from diagnosis_id, but standardised to remove trailing .00. Joins to diagnosis_DIAGN.disagnosis_id',
    person_id                       int         NOT NULL COMMENT 'from person_id',
    active_ind                      bool        NOT NULL COMMENT 'whether an active record or not (deleted), from active_ind',
    mrn                             varchar(10) NOT NULL COMMENT 'from MRN',
    encounter_id                    int         NOT NULL COMMENT 'from encntr_id, but standardised to remove trailing .00',
    diag_dt_tm                      datetime    NOT NULL COMMENT 'from diag_dt. The date of the diagnosis',
    diag_type                       varchar(255) COMMENT ' text based diagnosis type',
    diag_prnsl                      varchar(255) COMMENT ' text based diagnosis performer',
    diag_code                       varchar(50) NOT NULL COMMENT 'diagnosis code of type described by vocab',
    confirmation                    varchar(50) COMMENT 'diagnosis confirmation text. Use to update the verification status',
    vocab                           varchar(50) NOT NULL COMMENT 'diagnosis code type, either SNOMED CT or UK ED Subset (Snomed)',
    location                        varchar(255) COMMENT ' text based location details',
    audit_json                      mediumtext  null comment 'Used for Audit Purposes',
    CONSTRAINT pk_diagnosis_diagnosis PRIMARY KEY (exchange_id, diagnosis_id)
);

create table diagnosis_diagnosis_latest
(
    exchange_id                     char(36)    NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                     datetime    NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                 bigint      NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    diagnosis_id                    int         NOT NULL COMMENT 'from diagnosis_id, but standardised to remove trailing .00. Joins to diagnosis_DIAGN.disagnosis_id',
    person_id                       int         NOT NULL COMMENT 'from person_id',
    active_ind                      bool        NOT NULL COMMENT 'whether an active record or not (deleted), from active_ind',
    mrn                             varchar(10) NOT NULL COMMENT 'from MRN',
    encounter_id                    int         NOT NULL COMMENT 'from encntr_id, but standardised to remove trailing .00',
    diag_dt_tm                      datetime    NOT NULL COMMENT 'from diag_dt',
    diag_type                       varchar(255) COMMENT ' text based diagnosis type',
    diag_prnsl                      varchar(255) COMMENT ' text based diagnosis performer',
    diag_code                       varchar(50) NOT NULL COMMENT 'diagnosis code of type described by vocab',
    confirmation                    varchar(50) COMMENT 'diagnosis confirmation text. Use to update the verification status',
    vocab                           varchar(50) NOT NULL COMMENT 'diagnosis code type, either SNOMED CT or UK ED Subset (Snomed)',
    location                        varchar(255) COMMENT ' text based location details',
    audit_json                      mediumtext  null comment 'Used for Audit Purposes',
    CONSTRAINT pk_diagnosis_diagnosis_latest PRIMARY KEY (diagnosis_id)
);

-- records from DIAGN (UKRWH_CDE_DIAGNOSIS)
create table diagnosis_DIAGN
(
    exchange_id          char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received          datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum      bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    diagnosis_id         int        NOT NULL COMMENT 'from DIAGNOSIS_ID.  Joins to diagnosis_dianosis.diagnosis_id',
    active_ind           bool       NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    encounter_id         int COMMENT 'from ENCNTR_ID',
    encounter_slice_id   int COMMENT 'from ENCNTR_SLICE_ID',
    diagnosis_dt_tm      datetime COMMENT 'from DIAGNOSIS_DT_TM',
    diagnosis_code_type  varchar(50) COMMENT 'icd-10 or snomed/SNMUKEMED, derived from CONCEPT_CKI_IDENT. format is: type!code',
    diagnosis_code       varchar(50) COMMENT 'icd-10 or snomed/SNMUKEMED code derived from CONCEPT_CKI_IDENT. format is: type!code',
    diagnosis_term       varchar(255) COMMENT 'corresponding term for the above code, looked up via TRUD',
    diagnosis_notes      varchar(255) COMMENT 'free text notes from DIAGNOSIS_TXT',
    diagnosis_type_cd    varchar(50) COMMENT 'from DIAGNOSIS_TYPE_CD, Cerner code set nbr = 17',
    diagnosis_seq_nbr    int COMMENT 'from DIAGNOSIS_SEQ_NBR',
    diag_personnel_id    int COMMENT 'the Id of the person making the diagnosis, from DIAG_HCP_PRSNL_ID',
    lookup_person_id     int COMMENT 'pre-looked up via ENCNTR_ID',
    lookup_mrn           varchar(10) COMMENT 'looked up via ENCNTR_ID',
    audit_json           mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_DIAGN PRIMARY KEY (exchange_id, diagnosis_id)
);

create table diagnosis_DIAGN_latest
(
    exchange_id          char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received          datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum      bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    diagnosis_id         int        NOT NULL COMMENT 'from DIAGNOSIS_ID.  Joins to diagnosis_dianosis.diagnosis_id',
    active_ind           bool       NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    encounter_id         int COMMENT 'from ENCNTR_ID',
    encounter_slice_id   int COMMENT 'from ENCNTR_SLICE_ID',
    diagnosis_dt_tm      datetime COMMENT 'from DIAGNOSIS_DT_TM',
    diagnosis_code_type  varchar(50) COMMENT 'icd-10 or snomed/SNMUKEMED, derived from CONCEPT_CKI_IDENT. format is: type!code',
    diagnosis_code       varchar(50) COMMENT 'icd-10 or snomed/SNMUKEMED code derived from CONCEPT_CKI_IDENT. format is: type!code',
    diagnosis_term       varchar(255) COMMENT 'corresponding term for the above code, looked up via TRUD',
    diagnosis_notes      varchar(255) COMMENT 'free text notes from DIAGNOSIS_TXT',
    diagnosis_type_cd    varchar(50) COMMENT 'from DIAGNOSIS_TYPE_CD, Cerner code set nbr = 17',
    diagnosis_seq_nbr    int COMMENT 'from DIAGNOSIS_SEQ_NBR',
    diag_personnel_id    int COMMENT 'the Id of the person making the diagnosis, from DIAG_HCP_PRSNL_ID',
    lookup_person_id     int COMMENT 'pre-looked up via ENCNTR_ID',
    lookup_mrn           varchar(10) COMMENT 'looked up via ENCNTR_ID',
    audit_json           mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_DIAGN_latest PRIMARY KEY (diagnosis_id)
);

-- TODO - create additional indexes? need to understand table relationships
-- CREATE INDEX ix_procedure_procedure_join_helper ON procedure_PROCE_latest (exchange_id, encounter_id, procedure_dt_tm, procedure_code);

-- CREATE INDEX ix_procedure_procedure_parent_helper ON procedure_PROCE_latest (lookup_person_id, encounter_id, procedure_seq_nbr, encounter_slice_id);

create table diagnosis_problem
(
    exchange_id          char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received          datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum      bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    problem_id           int        NOT NULL COMMENT 'unique problem ID',
    mrn                  varchar(10) NOT NULL COMMENT 'from MRN',
    onset_date           datetime COMMENT 'on-set date of problem',
    updated_by           varchar(50) COMMENT 'Clinician updating the record. Text, so map to Id',
    problem_code         varchar(50) COMMENT 'snomed description Id',
    problem_term         varchar(255) COMMENT 'problem raw term (not looked up on TRUD)',
    problem_txt          varchar(255) COMMENT 'problem free text, usually the same as the term, annotated_disp',
    qualifier            varchar(50) COMMENT 'problem qualifier to add to notes',
    classification       varchar(50) COMMENT 'problem classification text to add to notes',
    confirmation         varchar(50) COMMENT 'problem confirmation text. Use to update the verification status',
    persistence          varchar(50) COMMENT 'problem persistence text, i.e. type: Acute, Chronic',
    prognosis            varchar(50) COMMENT 'problem prognosis text to add to notes',
    vocab                varchar(50) COMMENT 'problem code type, either SNOMED CT, ICD-10, Cerner, UK ED Subset (Snomed Description Id),OPCS4,Patient Care',
    status               varchar(50) COMMENT 'problem status such as Active, Resolved, Inactive. From Status_Lifecycle',
    status_date          datetime COMMENT 'the date of the current status',
    location             varchar(255) COMMENT ' text based location details from org_name',
    audit_json           mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_diagnosis_problem PRIMARY KEY (exchange_id, problem_id)
);

create table diagnosis_problem_latest
(
    exchange_id          char(36)   NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received          datetime   NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum      bigint     NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    problem_id           int        NOT NULL COMMENT 'unique problem ID',
    mrn                  varchar(10) NOT NULL COMMENT 'from MRN',
    onset_date           datetime COMMENT 'on-set date of problem',
    updated_by           varchar(50) COMMENT 'Clinician updating the record. Text, so map to Id',
    problem_code         varchar(50) COMMENT 'snomed description Id',
    problem_term         varchar(255) COMMENT 'problem raw term (not looked up on TRUD)',
    problem_txt          varchar(255) COMMENT 'problem free text, usually the same as the term, from annotated_disp',
    qualifier            varchar(50) COMMENT 'problem qualifier to add to notes',
    classification       varchar(50) COMMENT 'problem classification text to add to notes',
    confirmation         varchar(50) COMMENT 'problem confirmation text. Use to update the verification status',
    persistence          varchar(50) COMMENT 'problem persistence text, i.e. type: Acute, Chronic',
    prognosis            varchar(50) COMMENT 'problem prognosis text to add to notes',
    vocab                varchar(50) COMMENT 'problem code type, either SNOMED CT, ICD-10, Cerner, UK ED Subset (Snomed Description Id),OPCS4,Patient Care',
    status               varchar(50) COMMENT 'problem status such as Active, Resolved, Inactive. From Status_Lifecycle',
    status_date          datetime COMMENT 'the date of the current status',
    location             varchar(255) COMMENT ' text based location details from org_name',
    audit_json           mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_diagnosis_problem PRIMARY KEY (problem_id)
);

-- target table for the above tables to populate, cleared down for each exchange
create table diagnosis_target
(
    exchange_id                char(36)     NOT NULL COMMENT ' links to audit.exchange table (but on a different server)',
    unique_id                  varchar(255) NOT NULL COMMENT ' unique ID derived from source IDs ',
    is_delete                  bool         NOT NULL COMMENT ' if this diagnosis should be deleted or upserted ',
    person_id                  int COMMENT ' person ID for the diagnosis ',
    encounter_id               int COMMENT ' encounter ID for the disagnosis ',
    performer_personnel_id     int COMMENT ' performer ID for the disagnosis, i.e. who diagnosed? ',
    dt_performed               datetime,
    free_text                  mediumtext,
    recorded_by_personnel_id   int,
    dt_recorded                datetime,
    diagnosis_code_type        varchar(50) COMMENT ' icd-10 or snomed or UK ED Subset (Snomed) - tells us the schema of the below code ',
    diagnosis_code             varchar(50),
    diagnosis_term             varchar(255),
    diagnosis_type             varchar(50) COMMENT 'The type of Diagnosis, either text or coded from Cerner code_set nbr = 17',
    sequence_number            int,
    location                   varchar(255) COMMENT ' text based location details',
    audit_json                 mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_diagnosis_target PRIMARY KEY (exchange_id, unique_id)
);

create index ix_duplication_helper on diagnosis_target (person_id, encounter_id, dt_performed, diagnosis_code, unique_id);


-- latest version of every record that is in the archive table
create table diagnosis_target_latest
(
    exchange_id                char(36)     NOT NULL COMMENT ' links to audit.exchange table (but on a different server)',
    unique_id                  varchar(255) NOT NULL COMMENT ' unique ID derived from source IDs ',
    is_delete                  bool         NOT NULL COMMENT ' if this diagnosis should be deleted or upserted ',
    person_id                  int COMMENT ' person ID for the diagnosis ',
    encounter_id               int COMMENT ' encounter ID for the disagnosis ',
    performer_personnel_id     int COMMENT ' performer ID for the disagnosis, i.e. who diagnosed? ',
    dt_performed               datetime,
    free_text                  mediumtext,
    recorded_by_personnel_id   int,
    dt_recorded                datetime,
    diagnosis_code_type        varchar(50) COMMENT ' icd-10 or snomed or UK ED Subset (Snomed) - tells us the schema of the below code ',
    diagnosis_code             varchar(50),
    diagnosis_term             varchar(255),
    diagnosis_type             varchar(50) COMMENT 'The type of Diagnosis, either text or coded from Cerner code_set nbr = 17',
    sequence_number            int,
    location                   varchar(255) COMMENT ' text based location details',
    audit_json                 mediumtext null comment 'Used for Audit Purposes',
    CONSTRAINT pk_diagnosis_target PRIMARY KEY (unique_id)
);


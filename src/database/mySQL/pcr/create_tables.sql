DROP DATABASE pcr;

CREATE DATABASE IF NOT EXISTS pcr;

USE pcr;

CREATE TABLE address
(
  id                       int NOT NULL,
  address_line_1           varchar(255),
  address_line_2           varchar(255),
  address_line_3           varchar(255),
  address_line_4           varchar(255),
  postcode                 varchar(10),
  uprn                     bigint COMMENT 'OS identifier for a property or place',
  approximation_concept_id bigint COMMENT 'Approximation qualifier for the UPRN',
  property_type_concept_id bigint COMMENT 'refers to information model to give the property type (e.g. prison)',
  PRIMARY KEY (id)
);

CREATE TABLE organisation
(
  id                     int      NOT NULL,
  service_id             varchar(36) NOT NULL COMMENT 'refers to admin database',
  system_id              varchar(36) NOT NULL COMMENT 'refers to admin database',
  ods_code               varchar(255),
  name                   varchar(255),
  is_active              boolean,
  parent_organisation_id int COMMENT 'refers back to this table',
  type_concept_id        bigint COMMENT 'organisation type, stored as a concept',
  main_location_id       int COMMENT 'refers to location table, giving main address etc for this org',
  PRIMARY KEY (id),
  CONSTRAINT organisation_parent_organisation_id
    FOREIGN KEY (parent_organisation_id)
      REFERENCES organisation (id)
) ;

CREATE TABLE location
(
  id                 int NOT NULL,
  organisation_id    int NOT NULL,
  name               varchar(255),
  type_concept_id    bigint COMMENT 'refers to information model to give the location type (e.g. branch surgery)',
  address_id         int,
  start_date         datetime,
  end_date           datetime,
  is_active          boolean,
  parent_location_id int COMMENT 'refers back to this table to give location hierarchy',
  PRIMARY KEY (id),
  CONSTRAINT location_address_id
    FOREIGN KEY (address_id)
      REFERENCES address (id),
  CONSTRAINT location_organisation_id
    FOREIGN KEY (organisation_id)
      REFERENCES organisation (id),
  CONSTRAINT location_parent_location_id
    FOREIGN KEY (parent_location_id)
      REFERENCES location (id)
)  COMMENT 'represents a physical location belonging to a healthcare organisation (e.g. GP practice main site or branch site)';

CREATE TABLE location_contact
(
  id                         int          NOT NULL AUTO_INCREMENT ,
  location_id                int          NOT NULL,
  type_concept_id            bigint       NOT NULL COMMENT 'type of contact (e.g. home phone, mobile phone, email)',
  value                      varchar(255) NOT NULL COMMENT 'the actual phone number or email address',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  PRIMARY KEY (id),
  CONSTRAINT location_contact_location_id
    FOREIGN KEY (location_id)
      REFERENCES location (id)
)AUTO_INCREMENT =1 COMMENT 'stores contact details (e.g. phone number) for a location';

CREATE TABLE practitioner
(
  id                         int NOT NULL,
  organisation_id            int NOT NULL,
  title                      varchar(255),
  first_name                 varchar(255),
  middle_names               varchar(255),
  last_name                  varchar(255),
  gender_concept_id          bigint,
  date_of_birth              datetime,
  is_active                  boolean,
  role_concept_id            bigint COMMENT 'refers to staff role, stored as a concept',
  speciality_concept_id      bigint COMMENT 'secondary care healthcare specialty (e.g. cardiology)',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  PRIMARY KEY (id),
  CONSTRAINT practitioner_organisation_id
    FOREIGN KEY (organisation_id)
      REFERENCES organisation (id)
) COMMENT 'represents a clinician (e.g. doctor or nurse) or non-clinician involved in healthcare';

CREATE TABLE practitioner_contact
(
  id                         int          NOT NULL AUTO_INCREMENT ,
  practitioner_id            int          NOT NULL,
  type_concept_id            bigint       NOT NULL COMMENT 'type of contact (e.g. home phone, mobile phone, email)',
  value                      varchar(255) NOT NULL COMMENT 'the actual phone number or email address',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  PRIMARY KEY (id),
  CONSTRAINT practitioner_contact_practitioner_id
    FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id)
) AUTO_INCREMENT =1 COMMENT 'stores contact details (e.g. phone) for practitioners';

CREATE TABLE practitioner_identifier
(
  practitioner_id            int          NOT NULL COMMENT 'refers to the practitioner whose ID this is',
  type_concept_id            bigint       NOT NULL COMMENT 'refers to identifier type (e.g. Prescribing ID, GMC#, NMC#, Smartcard ID, local code) stored in information model',
  value                      varchar(255) NOT NULL COMMENT 'the actual identifier value',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  PRIMARY KEY (practitioner_id, type_concept_id),
  CONSTRAINT practitioner_identifier_practitioner_id
    FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id)
) COMMENT 'stores various IDs for a practitioner (e.g. GMC number, prescribing code)';

CREATE TABLE patient
(
  id                                 int NOT NULL,
  organisation_id                    int NOT NULL,
  nhs_number                         varchar(10),
  nhs_number_verification_concept_id bigint COMMENT 'refers to information model to give status of NHS number',
  date_of_birth                      datetime,
  date_of_death                      datetime,
  gender_concept_id                  bigint COMMENT 'reference to the information model',
  usual_practitioner_id              int COMMENT 'refers to practitioner table to give usual GP/clinician',
  care_provider_id                   int COMMENT 'Refers to org table for systems that may not have usual practitioner',
  entered_by_practitioner_id         int COMMENT 'id in practitioner table. ',
  title                              varchar(255),
  first_name                         varchar(255),
  middle_names                       varchar(255),
  last_name                          varchar(255),
  previous_last_name                 varchar(255),
  home_address_id                    int COMMENT 'refers to the patient_address table',
  is_spine_sensitive                 boolean,
  ethnic_code                        char(1),
  PRIMARY KEY (id)
) COMMENT 'represents the patient demographics at an organisational level';

CREATE TABLE patient_address
(
  id                         int NOT NULL AUTO_INCREMENT,
  patient_id                 int NOT NULL,
  type_concept_id            bigint COMMENT 'refers to information model to give the address type (e.g. home, temporary, correspondence)',
  address_id                 int,
  start_date                 datetime,
  end_date                   datetime,
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT patient_address_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id),
  CONSTRAINT patient_address_address_id
    FOREIGN KEY (address_id)
      REFERENCES address (id)
) AUTO_INCREMENT = 1 ;

CREATE TABLE patient_contact
(
  id                         int     AUTO_INCREMENT     NOT NULL,
  patient_id                 int          NOT NULL,
  type_concept_id            bigint       NOT NULL COMMENT 'type of contact (e.g. home phone, mobile phone, email)',
  value                      varchar(255) NOT NULL COMMENT 'the actual phone number or email address',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT patient_contact_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id)
)AUTO_INCREMENT=1 COMMENT 'stores contact details about a patient (e.g. phone number, email address)';

CREATE TABLE patient_identifier
(
  id                         int NOT NULL AUTO_INCREMENT,
  patient_id                 int NOT NULL,
  type_concept_id            bigint COMMENT 'refers to information model to give the type of idenifier (e.g. hospital number, patient number)',
  identifier                 varchar(255) COMMENT 'the actual idenfifier value',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
   PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
   CONSTRAINT patient_identifier_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id)
) AUTO_INCREMENT=1;

CREATE TABLE additional_attribute
(
  patient_id                int    NOT NULL,
  item_type                 tinyint NOT NULL COMMENT 'valueset telling us which table type the item_id refers to (observation, procedure, referral, medication etc.)',
  item_id                   bigint  NOT NULL,
  concept_id                bigint COMMENT 'information model concept for the field/attribute label',
  attribute_value           double   DEFAULT NULL,
  attribute_date            datetime DEFAULT NULL,
  attribute_text            text     DEFAULT NULL,
  attribute_text_concept_id bigint(20) DEFAULT NULL COMMENT 'information model concept for the codeable value concept/text',
  is_consent                boolean NOT NULL COMMENT 'whether consent or dissent',
  PRIMARY KEY (patient_id, item_type, item_id, concept_id),
  CONSTRAINT additional_attribute_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id)
)  COMMENT 'Extension table to store additional attributes for tables that dont have a specific column to go in';

CREATE TABLE additional_relationship
(
  id                           int  AUTO_INCREMENT  NOT NULL,
  source_table                 varchar(255) COMMENT 'the name of the source table',
  related_table                varchar(255) COMMENT 'the name of the related table',
  relationship_type_concept_id bigint NOT NULL COMMENT 'information model concept for the relationship',
  source_item_id               bigint NOT NULL COMMENT 'id of the source table row',
  related_item_id              bigint NOT NULL COMMENT 'id of the related table row',
  PRIMARY KEY (id)
) AUTO_INCREMENT=1 COMMENT 'additional attributes that are relationships between one table and another';

CREATE TABLE data_entry_prompt
(
  id              int NOT NULL AUTO_INCREMENT ,
  organisation_id int NOT NULL,
  prompt_text     varchar(255) COMMENT 'the text of the prompt (e.g. a question)',
  PRIMARY KEY (id),
  CONSTRAINT data_entry_prompt_organisation_id
    FOREIGN KEY (organisation_id)
      REFERENCES organisation (id)
) AUTO_INCREMENT =1 COMMENT 'stores the prompts from user-defined forms/questionnaires/templates that are used to record observations based on textual questions';

CREATE TABLE free_text
(
  id                         bigint NOT NULL AUTO_INCREMENT ,
  patient_id                 int    NOT NULL,
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  free_text                  text   NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT free_text_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id)
) AUTO_INCREMENT=1 COMMENT 'free text blob table to keep large volumes of textual data separate from the main observations table, to avoid table overload';

CREATE TABLE appointment_schedule
(
  id                    int NOT NULL,
  organisation_id       int NOT NULL,
  location_id           int NOT NULL,
  description           varchar(255),
  type_concept_id       bigint COMMENT 'refers to session type (Emis category name and TPP rota type) stored in information model',
  speciality_concept_id bigint COMMENT 'speciality associated with the schedule, used in secondary care',
  schedule_start        datetime COMMENT 'MySQL default datetime is 8 bytes at sec precision',
  schedule_end          datetime,
  PRIMARY KEY (id),
  CONSTRAINT appointment_schedule_organisation_id
    FOREIGN KEY (organisation_id)
      REFERENCES organisation (id),
  CONSTRAINT appointment_schedule_location_id
    FOREIGN KEY (location_id)
      REFERENCES location (id)
)  COMMENT 'represents an appointments schedule/session/rota';

CREATE TABLE appointment_schedule_practitioner
(
  appointment_schedule_id    int  NOT NULL,
  practitioner_id            int     NOT NULL,
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  is_main_practitioner       boolean NOT NULL COMMENT 'in the event of multiple practitioners for a schedule, this tells us who is the main one',
  PRIMARY KEY (appointment_schedule_id, practitioner_id),
  CONSTRAINT appointment_schedule_practitioner_appointment_schedule_id
    FOREIGN KEY (appointment_schedule_id)
      REFERENCES appointment_schedule (id),
  CONSTRAINT appointment_schedule_practitioner_practitioner_id
    FOREIGN KEY (practitioner_id)
      REFERENCES practitioner (id)
)  COMMENT 'stores the practitioners linked to an appointments schedule';

CREATE TABLE appointment_slot
(
  id                       int NOT NULL AUTO_INCREMENT ,
  appointment_schedule_id  int NOT NULL,
  slot_start               datetime COMMENT 'MySQL default datetime is 8 bytes at sec precision',
  slot_end                 datetime,
  planned_duration_minutes int COMMENT 'for non-timed appointments all we may know is the duration',
  type_concept_id          bigint COMMENT 'appointment type (e.g. routine, urgent)',
  interaction_concept_id   bigint COMMENT 'concept stating how the interaction will take place (e.g. face to face, telephone)',
  PRIMARY KEY (id),
  CONSTRAINT appointment_slot_appointment_schedule_id
    FOREIGN KEY (appointment_schedule_id)
      REFERENCES appointment_schedule (id)
) AUTO_INCREMENT =1 COMMENT 'represents a slot within an appointments schedule';

CREATE TABLE appointment_booking
(
  appointment_slot_id        int  NOT NULL,
  booking_time               datetime NOT NULL COMMENT 'when this booking event took place',
  patient_id                 int COMMENT 'patient booked into the slot, or null if not patient related',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  booking_concept_id         bigint   NOT NULL COMMENT 'concept stating the state of the slot (e.g. booked, reserved, free)',
  reason                     varchar(255) COMMENT 'patient''s reason for booking the appointment',
  PRIMARY KEY (appointment_slot_id, booking_time),
  CONSTRAINT appointment_booking_appointment_slot_id
    FOREIGN KEY (appointment_slot_id)
      REFERENCES appointment_slot (id),
  CONSTRAINT appointment_booking_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id)
)  COMMENT 'represents the history of bookings into an appointment_slot';

CREATE TABLE appointment_attendance
(
  appointment_slot_id        int NOT NULL  ,
  patient_id                 int NOT NULL,
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  actual_start_time          datetime,
  actual_end_time            datetime,
  status_concept_id          bigint COMMENT 'refers to information model to give the appointment status (e.g. finished, DNA)',
  PRIMARY KEY (appointment_slot_id),
  CONSTRAINT appointment_attendance_appointment_slot_id
    FOREIGN KEY (appointment_slot_id)
      REFERENCES appointment_slot (id),
  CONSTRAINT appointment_attendance_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id)
) COMMENT 'stores data created during an appointment';

CREATE TABLE appointment_attendance_event
(
  appointment_slot_id        int NOT NULL,
  event_time                 datetime NOT NULL,
  event_concept_id           bigint   NOT NULL COMMENT 'concept to give the status of the attendance (e.g. arrived, sent in)',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  PRIMARY KEY (appointment_slot_id, event_time),
  CONSTRAINT appointment_attendance_event_appointment_slot_id
    FOREIGN KEY (appointment_slot_id)
      REFERENCES appointment_slot (id)
)   COMMENT 'records the various timestamps during an appointment (e.g. arrival, sent in, finished)';

CREATE TABLE gp_registration
(
  id                                    int   AUTO_INCREMENT    NOT NULL,
  patient_id                            int   NOT NULL,
  owning_organisation_id                int NOT NULL COMMENT 'refers to the organisation that owns/manages the GP registration',
  start_date                        	datetime NOT NULL,
  end_date                              datetime,
  PRIMARY KEY (id),
  UNIQUE KEY ix_patient_id (patient_id, id),
    CONSTRAINT gp_registration_patient_id
    FOREIGN KEY (patient_id)
    REFERENCES patient (id)
)AUTO_INCREMENT=1 COMMENT 'stores the regular patient registration';

CREATE TABLE gp_registration_history
(
  id                                    int   AUTO_INCREMENT    NOT NULL,
  patient_id                            int      NOT NULL,
  owning_organisation_id                int NOT NULL COMMENT 'refers to the organisation that owns/manages the GP registration',
  effective_date                        datetime NOT NULL COMMENT 'clinically significant date and time',
  patient_type				        	int NOT NULL COMMENT 'patient registration type (i.e. regular, temporary)',
  patient_status					    int NOT NULL COMMENT 'registration status e.g. (currently registered, deducted)',
    PRIMARY KEY (id),
  	UNIQUE KEY ix_patient_id (patient_id, id),
    CONSTRAINT gp_registration_history_patient_id
    FOREIGN KEY (patient_id)
    REFERENCES patient (id)
)AUTO_INCREMENT=1 COMMENT 'stores registration history for a GP registration';

CREATE TABLE care_episode
(
  id                            bigint   AUTO_INCREMENT  NOT NULL,
  patient_id                    int      NOT NULL,
  owning_organisation_id        int NOT NULL COMMENT 'refers to the organisation that owns/manages the care episode',
  start_date                    datetime,
  end_date                      datetime,
  service_id         			int COMMENT 'identifier for the speciality service of the responsible health care provider',
  service_type         			varchar(255) COMMENT 'identifier for the speciality service type of the responsible health care provider',
  referral_request_id           int COMMENT 'links to a referral that initiated this care episode',
   PRIMARY KEY (id),
  UNIQUE KEY patient_id (patient_id, id),
   CONSTRAINT care_episode_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id)
)AUTO_INCREMENT=1;

CREATE TABLE encounter
(
  id                         bigint   AUTO_INCREMENT  NOT NULL,
  patient_id                 int      NOT NULL,
  owning_organisation_id     int 	  COMMENT 'refers to the organisation that owns/manages the encounter',
  care_episode_id            int      NOT NULL COMMENT 'identifier of an episode associated with this encounter',
  parent_encounter_id        bigint COMMENT 'groups encounters for things like hospital spells',
  start_date             	 datetime NOT NULL,
  end_date                   datetime,
  event_type          	 	 varchar(255) COMMENT 'type of encounter (i.e. GP Consultation, GP Face to face, A&E attendance, transfer, admission, discharge)',
  state          	 		 varchar(255) COMMENT 'state of encounter, INPATIENT etc.',
  practitioner_id  int 	  	 COMMENT 'responsible practitioner for the care of the patient during the encounter',
  original_encounter_ref     varchar(255) COMMENT 'publishing system encounter reference i.e. Barts uses a FIN number to link care episodes',
  appointment_slot_id        int COMMENT 'refers to appointment table, giving the appointment this encounter took place in',
  additional_data            text COMMENT 'additional data stored as key-value pairs',
  location_id                int COMMENT 'refers to location table, stating where this encounter took place',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT encounter_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id),
  CONSTRAINT encounter_location_id
    FOREIGN KEY (location_id)
      REFERENCES location (id)
)AUTO_INCREMENT=1;

CREATE TABLE event_relationship
(
  item_id                             bigint  NOT NULL,
  item_type                           tinyint NOT NULL COMMENT 'valueset telling us whether linked_item_id refers to an observation, an allergy, medication etc.',
  linked_item_id                      bigint  NOT NULL,
  linked_item_relationship_concept_id bigint COMMENT 'refers to information model to define how this event relates to another event (e.g. child of, reason for, related complication, related result, related reaction, cause of, grouped with, evolved from)',
  PRIMARY KEY (item_id, linked_item_id)
)  COMMENT 'defines how a clinical event relates to another event (e.g. child of, reason for, related complication, related result, related reaction, cause of, grouped with, evolved from)';

CREATE TABLE observation
(
  id                               bigint   NOT NULL,
  patient_id                       int      NOT NULL,
  concept_id                       bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                   datetime COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint  COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  care_activity_id                 bigint,
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  original_code                    varchar(20) COLLATE utf8_bin DEFAULT NULL,
  original_term                    varchar(1000)                DEFAULT NULL,
  original_code_scheme             tinyint                      DEFAULT NULL COMMENT 'Original code scheme - 0=READ2, 1=READ3, 2=SnomedCT, 3=ICD-10, 4=OPCS-4, 5=MILLENNIUM',
  original_system                  tinyint                      DEFAULT NULL COMMENT 'Original system - 0=EMIS, 1=TPP, 2=VISION, 3=CERNER',
  episodicity_concept_id           bigint COMMENT 'refers to information model, giving episode/review (e.g. new episode, review)',
  free_text_id                     bigint COMMENT 'refers to free text table where comments are stored',
  data_entry_prompt_id             int COMMENT 'links to the table giving the free-text prompt used to enter this observation',
  significance_concept_id          bigint COMMENT 'refers to information model to define the significance, severity, normality or priority (e.g. minor, significant, abnormal, urgent, severe, normal)',
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT observation_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id),
  CONSTRAINT observation_free_text_id
    FOREIGN KEY (patient_id, free_text_id)
      REFERENCES free_text (patient_id, id),
  CONSTRAINT observation_data_entry_prompt_id
    FOREIGN KEY (data_entry_prompt_id)
      REFERENCES data_entry_prompt (id)
) ;

CREATE TABLE flag
(
  id                               bigint  NOT NULL,
  patient_id                       int      NOT NULL,
  type_concept_id                  bigint   NOT NULL COMMENT 'concept to describe the flag (e.g. Do not stop taking this medication without professional advice)',
  effective_date                   datetime  COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint  COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  end_date                         datetime COMMENT 'the clinical date and time for the end of this event',
  care_activity_id                 bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  free_text_id                     bigint COMMENT 'links to the table giving the actual text of this flag',
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT flag_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id)
)  COMMENT 'store alerts/warnings/flags (things that show warnings in patient records)';

CREATE TABLE problem
(
  id                          bigint NOT NULL,
  patient_id                  int    NOT NULL,
  observation_id              bigint NOT NULL,
  type_concept_id             bigint COMMENT 'refers to information model for problem type (e.g. problem, issue, health admin)',
  significance_concept_id     bigint COMMENT 'refers to information model to define the significance (e.g. minor, significant)',
  expected_duration_days      int,
  last_review_date            date,
  last_review_practitioner_id int,
  entered_by_practitioner_id  int COMMENT 'id in practitioner table. ',
   PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT problem_last_review_practitioner_id
    FOREIGN KEY (last_review_practitioner_id)
      REFERENCES practitioner (id),
  CONSTRAINT problem_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id),
  CONSTRAINT problem_observation_id
    FOREIGN KEY (patient_id, observation_id)
      REFERENCES observation (patient_id, id)
) ;

CREATE TABLE procedure_request
(
  id                               bigint   NOT NULL ,
  patient_id                       int      NOT NULL,
  concept_id                       bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                   datetime NOT NULL COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint  NOT NULL COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  care_activity_id                 bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  original_code                    varchar(20) COLLATE utf8_bin DEFAULT NULL,
  original_term                    varchar(1000)                DEFAULT NULL,
  original_code_scheme             tinyint                      DEFAULT NULL COMMENT 'Original code scheme - 0=READ2, 1=READ3, 2=SnomedCT, 3=ICD-10, 4=OPCS-4, 5=MILLENNIUM',
  original_system                  tinyint                      DEFAULT NULL COMMENT 'Original system - 0=EMIS, 1=TPP, 2=VISION, 3=CERNER',
  priority_concept_id              bigint COMMENT 'concept giving the priority of this request',
  recipient_organisation_id        int COMMENT 'to whom the request was made',
  request_identifier               varchar(255) COMMENT 'local identifier for the request (e.g. order number)',
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT procedure_request_recipient_organisation_id
    FOREIGN KEY (recipient_organisation_id)
      REFERENCES organisation (id)

)  COMMENT 'stores order (e.g. lab tests) and procedure (e.g. operation) requests';

CREATE TABLE `procedure`
(
  id                               bigint    NOT NULL,
  patient_id                       int      NOT NULL,
  concept_id                       bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                   datetime COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint  COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  end_date                         datetime COMMENT 'the clinical date and time for the end of this event',
  usual_practitioner_id            int COMMENT 'refers to practitioner table to give usual GP/clinician',
  care_activity_id                 bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  original_code                    varchar(20) COLLATE utf8_bin DEFAULT NULL,
  original_term                    varchar(1000)                DEFAULT NULL,
  original_code_scheme             tinyint                      DEFAULT NULL COMMENT 'Original code scheme - 0=READ2, 1=READ3, 2=SnomedCT, 3=ICD-10, 4=OPCS-4, 5=MILLENNIUM',
  original_system                  tinyint                      DEFAULT NULL COMMENT 'Original system - 0=EMIS, 1=TPP, 2=VISION, 3=CERNER',
  outcome_concept_id               bigint   NOT NULL,
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
  PRIMARY KEY (id)
);

CREATE TABLE device
(
  id                          int AUTO_INCREMENT    NOT NULL,
  organisation_id             int    NOT NULL,
  type_concept_id             bigint NOT NULL COMMENT 'concept for the nature of the device (e.g. cardiac pacemaker)',
  serial_number               varchar(255),
  device_name                 varchar(255),
  manufacturer                varchar(255),
  human_readable_identifier   varchar(255) COMMENT 'human readable bar code identifier',
  machine_readable_identifier varbinary(255) COMMENT 'machine readable bar code',
  version                     varchar(255) COMMENT 'version of the device (e.g. software version if the device is software)',
  entered_by_practitioner_id  int COMMENT 'id in practitioner table. ',
  PRIMARY KEY (id),
  CONSTRAINT device_organisation_id
    FOREIGN KEY (organisation_id)
      REFERENCES organisation (id)
)AUTO_INCREMENT =1 COMMENT 'stores a device (physical or softwre) used in a procedure';

CREATE TABLE procedure_device
(
  id           bigint NOT NULL AUTO_INCREMENT ,
  procedure_id bigint NOT NULL,
  device_id    int    NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT procedure_device_device_id
    FOREIGN KEY (device_id)
      REFERENCES device (id),
  CONSTRAINT procedure_device_procedure_id
    FOREIGN KEY (procedure_id)
      REFERENCES `procedure` (id)
) AUTO_INCREMENT =1 COMMENT 'links a procedure to the devices used during it';

CREATE TABLE observation_value
(
  patient_id                 int    NOT NULL,
  observation_id             bigint NOT NULL COMMENT 'refers to the observation this belongs to',
  operator_concept_id        bigint COMMENT 'refers to information model, giving operator (e.g. =, <, <=, >, >=)',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  result_value               double,
  result_value_units         varchar(255),
  result_date                datetime DEFAULT NULL,
  result_text                text     DEFAULT NULL,
  result_concept_id          bigint(20) DEFAULT NULL,
  reference_range_id         bigint COMMENT 'refers to reference_range table in information model',
  PRIMARY KEY (patient_id, observation_id),
  CONSTRAINT observation_value_patient_id_observation_id
    FOREIGN KEY (patient_id, observation_id)
      REFERENCES observation (patient_id, id)
) COMMENT 'provides columns for an observation value';

CREATE TABLE immunisation
(
  id                               bigint   NOT NULL,
  patient_id                       int      NOT NULL,
  concept_id                       bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                   datetime COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint  COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  care_activity_id                 bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  original_code                    varchar(20) COLLATE utf8_bin DEFAULT NULL,
  original_term                    varchar(1000)                DEFAULT NULL,
  original_code_scheme             tinyint                      DEFAULT NULL COMMENT 'Original code scheme - 0=READ2, 1=READ3, 2=SnomedCT, 3=ICD-10, 4=OPCS-4, 5=MILLENNIUM',
  original_system                  tinyint                      DEFAULT NULL COMMENT 'Original system - 0=EMIS, 1=TPP, 2=VISION, 3=CERNER',
  dose                             varchar(255),
  body_location_concept_id         bigint COMMENT 'refers to the information model to give the bodily location of the immunisation (e.g. arm)',
  method_concept_id                bigint COMMENT 'refers to the information model to give the method of immunisation (e.g. intramuscular)',
  batch_number                     varchar(255),
  expiry_date                      date,
  manufacturer                     varchar(255),
  dose_ordinal                     int COMMENT 'number of this immunisation within a series',
  doses_required                   int COMMENT 'number of doses of this immunisation required',
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id)
)   COMMENT 'provide supplementary immunisation information';

CREATE TABLE allergy
(
  id                               bigint   NOT NULL,
  patient_id                       int      NOT NULL,
  concept_id                       bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                   datetime COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint  COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  care_activity_id                 bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  original_code                    varchar(20) COLLATE utf8_bin DEFAULT NULL,
  original_term                    varchar(1000)                DEFAULT NULL,
  original_code_scheme             tinyint                      DEFAULT NULL COMMENT 'Original code scheme - 0=READ2, 1=READ3, 2=SnomedCT, 3=ICD-10, 4=OPCS-4, 5=MILLENNIUM',
  original_system                  tinyint                      DEFAULT NULL COMMENT 'Original system - 0=EMIS, 1=TPP, 2=VISION, 3=CERNER',
  substance_concept_id             bigint COMMENT 'concept representing the substance (as opposed to the allergy code, which is on the observation table)',
  manifestation_concept_id         bigint COMMENT 'concept stating how this allergy manifests itself (e.g. rash, anaphylactic shock)',
  manifestation_free_text_id       bigint COMMENT 'links to free text record to give textual description of the manifestation',
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT allergy_manifestation_free_text_id
    FOREIGN KEY (patient_id, manifestation_free_text_id)
      REFERENCES free_text (patient_id, id)
)  COMMENT 'table to provide allergy information';

CREATE TABLE referral
(
  id                               bigint   NOT NULL,
  patient_id                       int      NOT NULL,
  concept_id                       bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                   datetime COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint  COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  care_activity_id                 bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  original_code                    varchar(20) COLLATE utf8_bin DEFAULT NULL,
  original_term                    varchar(1000)                DEFAULT NULL,
  original_code_scheme             tinyint                      DEFAULT NULL COMMENT 'Original code scheme - 0=READ2, 1=READ3, 2=SnomedCT, 3=ICD-10, 4=OPCS-4, 5=MILLENNIUM',
  original_system                  tinyint                      DEFAULT NULL COMMENT 'Original system - 0=EMIS, 1=TPP, 2=VISION, 3=CERNER',
  ubrn                             varchar(255) COMMENT 'e-RS booking number',
  priority_concept_id              bigint COMMENT 'refers to information model to give the priority (e.g. urgent, routing, 2-week wait)',
  sender_organisation_id           int,
  recipient_organisation_id        int,
  mode_concept_id                  bigint COMMENT 'refers to information model to give the referral mode (e.g. e-RS, written, telephone)',
  source_concept_id                bigint COMMENT 'concept defining the source of the referral (e.g. self-referral, GP referral)',
  service_requested_concept_id     bigint COMMENT 'concept giving the service being requested (e.g. colonoscopy)',
  reason_for_referral_free_text_id bigint COMMENT 'additional free text for the actual reason for referral',
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
   PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
   CONSTRAINT referral_sender_organisation_id
    FOREIGN KEY (sender_organisation_id)
      REFERENCES organisation (id),
  CONSTRAINT referral_recipient_organisation_id
    FOREIGN KEY (recipient_organisation_id)
      REFERENCES organisation (id),
  CONSTRAINT referral_reason_for_referral_free_text_id
    FOREIGN KEY (patient_id, reason_for_referral_free_text_id)
      REFERENCES free_text (patient_id, id)
)  COMMENT 'table to provide referral information';

CREATE TABLE medication_amount
(
  id                         bigint NOT NULL,
  patient_id                 int    NOT NULL,
  dose                       varchar(500),
  quantity_value             double,
  quantity_units             varchar(255),
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT medication_amount_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id)
)   COMMENT 'Table to store medication dose and quantity information';

CREATE TABLE medication_statement
(
  id                                 bigint   NOT NULL,
  patient_id                         int      NOT NULL,
  drug_concept_id                    bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                     datetime  COMMENT 'clinically significant date and time',
  effective_date_precision           tinyint   COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id          int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id         int COMMENT 'id in practitioner table. ',
  care_activity_id                   bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id   bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id             int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                  bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                    boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  original_code                      varchar(20) COLLATE utf8_bin DEFAULT NULL,
  original_term                      varchar(1000)                DEFAULT NULL,
  original_code_scheme               tinyint                      DEFAULT NULL COMMENT 'Original code scheme - 0=READ2, 1=READ3, 2=SnomedCT, 3=ICD-10, 4=OPCS-4, 5=MILLENNIUM',
  original_system                    tinyint                      DEFAULT NULL COMMENT 'Original system - 0=EMIS, 1=TPP, 2=VISION, 3=CERNER',
  type_concept_id                    bigint   NOT NULL COMMENT 'refers to information model to give the prescription type (e.g. Acute, Repeat, RepeatDispensing)',
  medication_amount_id               bigint COMMENT 'refers to the medication_amount table for the dose and quantity',
  issues_authorised                  int COMMENT 'total number of issues allowed before review, for acutes this value will be 1',
  review_date                        date COMMENT 'date medication needs to be reviewed',
  course_length_per_issue_days       int COMMENT 'number of days each issue of this medication is expected to last',
  patient_instructions_free_text_id  bigint COMMENT 'links to free text entry giving additional patient instructions',
  pharmacy_instructions_free_text_id bigint COMMENT 'links to free text entry giving additional pharmacist instructions',
  is_active                          boolean,
  end_date                           date COMMENT 'date medication was stopped',
  end_reason_concept_id              bigint COMMENT 'reason for ending this medication',
  end_reason_free_text_id            bigint COMMENT 'links to free text entry giving detail on why this was ended',
  issues                             int COMMENT 'number of issues received',
  is_consent                         boolean  NOT NULL COMMENT 'whether consent or dissent',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT medication_statement_patient_instructions_free_text_id
    FOREIGN KEY (patient_id, patient_instructions_free_text_id)
      REFERENCES free_text (patient_id, id),
  CONSTRAINT medication_statement_pharmacy_instructions_free_text_id
    FOREIGN KEY (patient_id, pharmacy_instructions_free_text_id)
      REFERENCES free_text (patient_id, id),
  CONSTRAINT medication_statement_end_reason_free_text_id
    FOREIGN KEY (patient_id, end_reason_free_text_id)
      REFERENCES free_text (patient_id, id),
  CONSTRAINT medication_statement_medication_amount_id
    FOREIGN KEY (patient_id, medication_amount_id)
      REFERENCES medication_amount (patient_id, id)

)  COMMENT 'stores the prescribed medications/authorisations';

CREATE TABLE medication_order
(
  id                                 bigint   NOT NULL,
  patient_id                         int      NOT NULL,
  drug_concept_id                    bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                     datetime  COMMENT 'clinically significant date and time',
  effective_date_precision           tinyint   COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id          int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id         int COMMENT 'id in practitioner table. ',
  care_activity_id                   bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id   bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id             int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                  bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                    boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  original_code                      varchar(20) COLLATE utf8_bin DEFAULT NULL,
  original_term                      varchar(1000)                DEFAULT NULL,
  original_code_scheme               tinyint                      DEFAULT NULL COMMENT 'Original code scheme - 0=READ2, 1=READ3, 2=SnomedCT, 3=ICD-10, 4=OPCS-4, 5=MILLENNIUM',
  original_system                    tinyint                      DEFAULT NULL COMMENT 'Original system - 0=EMIS, 1=TPP, 2=VISION, 3=CERNER',
  type_concept_id                    bigint   NOT NULL COMMENT 'refers to information model to give the prescription type (e.g. Acute, Repeat, RepeatDispensing)',
  medication_statement_id            bigint COMMENT 'refers to the medication_statement table',
  medication_amount_id               bigint COMMENT 'refers to the medication_amount table for the dose and quantity',
  patient_instructions_free_text_id  bigint COMMENT 'links to free text entry giving additional patient instructions',
  pharmacy_instructions_free_text_id bigint COMMENT 'links to free text entry giving additional pharmacist instructions',
  estimated_cost                     double,
  is_active                          boolean,
  duration_days                      int,
  is_consent                         boolean  NOT NULL COMMENT 'whether consent or dissent',
   PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
   CONSTRAINT medication_order_patient_instructions_free_text_id
    FOREIGN KEY (patient_id, patient_instructions_free_text_id)
      REFERENCES free_text (patient_id, id),
  CONSTRAINT medication_order_pharmacy_instructions_free_text_id
    FOREIGN KEY (patient_id, pharmacy_instructions_free_text_id)
      REFERENCES free_text (patient_id, id),
  CONSTRAINT medication_order_medication_statement_id
    FOREIGN KEY (patient_id, medication_statement_id)
      REFERENCES medication_statement (patient_id, id),
  CONSTRAINT medication_order_medication_amount_id
    FOREIGN KEY (patient_id, medication_amount_id)
      REFERENCES medication_amount (patient_id, id)

)  COMMENT 'stores the medication actually issued to the patient';

CREATE TABLE related_person
(
  id                         bigint NOT NULL,
  patient_id                 int    NOT NULL,
  title                      varchar(255),
  first_name                 varchar(255),
  middle_names               varchar(255),
  last_name                  varchar(255),
  date_of_birth              datetime,
  is_active                  boolean,
  type_concept_id            bigint COMMENT 'refers to information model to give the address type (e.g. home address)',
  address_id                 int,
  start_date                 datetime,
  end_date                   datetime,
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT related_person_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id),
  CONSTRAINT related_person_address_id
    FOREIGN KEY (address_id)
      REFERENCES address (id)

)  COMMENT 'stores details of a patients family and carers';

CREATE TABLE related_person_contact
(
  patient_id                 int          NOT NULL,
  related_person_id          bigint       NOT NULL,
  type_concept_id            bigint       NOT NULL COMMENT 'type of contact (e.g. home phone, mobile phone, email)',
  value                      varchar(255) NOT NULL COMMENT 'the actual phone number or email address',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  PRIMARY KEY (patient_id, related_person_id, type_concept_id),
  CONSTRAINT related_person_contact_patient_id_related_person_id
    FOREIGN KEY (patient_id, related_person_id)
      REFERENCES related_person (patient_id, id)
)   COMMENT 'stores contact method(s) for a related person (e.g. phone number)';

CREATE TABLE related_person_relationship
(
  patient_id                 int    NOT NULL,
  related_person_id          bigint NOT NULL,
  type_concept_id            bigint NOT NULL COMMENT 'refers to the information model, to give the type of relationship (e.g. brother, friend)',
  entered_by_practitioner_id int COMMENT 'id in practitioner table. ',
  PRIMARY KEY (patient_id, related_person_id, type_concept_id),
  CONSTRAINT related_person_relationship_patient_id
    FOREIGN KEY (patient_id)
      REFERENCES patient (id),
  CONSTRAINT related_person_relationship_related_person_id
    FOREIGN KEY (patient_id, related_person_id)
      REFERENCES related_person (patient_id, id)
) COMMENT 'Table to provide the type(s) of relationship between a person and related_person (e.g. carer, brother, next of kin, key holder)';

CREATE TABLE care_plan
(
  id                               bigint   NOT NULL  AUTO_INCREMENT,
  patient_id                       int      NOT NULL,
  concept_id                       bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                   datetime  COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint   COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  end_date                         datetime COMMENT 'the clinical date and time for the end of this event',
  care_activity_id                 bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  description_free_text_id         bigint COMMENT 'links to the free text table to give a textual description of this care plan',
  performance_frequency_value      int COMMENT 'the numeric component of the care plan performance frequency',
  performance_frequency_unit       tinyint COMMENT 'valueset giving the unit associated with the performance frequency (e.g. days, weeks)',
  performance_location_concept_id  bigint COMMENT 'concept giving the type of location where this is to be performance (e.g. patient home)',
  parent_care_plan                 int COMMENT 'optionally links to a parent care plan that this one is part of',
  follow_up_event_id               int COMMENT 'links this care plan to an event if this care plan is a follow up to a procedure',
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id)
) AUTO_INCREMENT=1 COMMENT 'stores patient care plans, recalls, and Emis diary events';

CREATE TABLE care_plan_activity
(
  id                               int      NOT NULL AUTO_INCREMENT ,
  care_plan_id                     bigint   NOT NULL,
  patient_id                       int      NOT NULL,
  concept_id                       bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                   datetime  COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint  COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  end_date                         datetime COMMENT 'the clinical date and time for the end of this event',
  care_activity_id                 bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  goal_concept_id                  bigint COMMENT 'concept describing the goal of this activity',
  outcome_concept_id               bigint COMMENT 'concept giving the outcome of the goal',
  outcome_date                     datetime,
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
    PRIMARY KEY (id),
  UNIQUE KEY ix_patidid (patient_id, id),
  CONSTRAINT care_plan_activity_patient_id_care_plan_id
    FOREIGN KEY (patient_id, care_plan_id)
      REFERENCES care_plan (patient_id, id)
) AUTO_INCREMENT=1  COMMENT 'stores the activities to be performed in a care plan';

CREATE TABLE care_plan_activity_target
(
  id                               int      NOT NULL AUTO_INCREMENT ,
  care_plan_activity_id            int      NOT NULL,
  patient_id                       int      NOT NULL,
  concept_id                       bigint   NOT NULL COMMENT 'refers to information model, giving the clinical concept of the event',
  effective_date                   datetime  COMMENT 'clinically significant date and time',
  effective_date_precision         tinyint   COMMENT 'qualifies the effective_date for display purposes',
  effective_practitioner_id        int COMMENT 'refers to the practitioner table for who is said to have done the event',
  entered_by_practitioner_id       int COMMENT 'id in practitioner table. ',
  end_date                         datetime COMMENT 'the clinical date and time for the end of this event',
  care_activity_id                 bigint COMMENT 'by having this here, we don''t need an care_activity_id on the observation, referral, allergy table etc.',
  care_activity_heading_concept_id bigint   NOT NULL COMMENT 'information model concept describing the care activity heading type (e.g. examination, history)',
  owning_organisation_id           int COMMENT 'refers to the organisation that owns/manages the event',
  status_concept_id                bigint   NOT NULL COMMENT 'refers to information model, giving the event status (e.g. active, final, pending, amended, corrected, deleted)',
  is_confidential                  boolean  NOT NULL COMMENT 'indicates this is a confidential event',
  target_concept_id                bigint   NOT NULL COMMENT 'Target nature of target in relation to the goal',
  target_date                      datetime,
  outcome_concept_id               bigint,
  outcome_date                     datetime,
  is_consent                       boolean  NOT NULL COMMENT 'whether consent or dissent',
  PRIMARY KEY (id),
  UNIQUE KEY ixpatcareplanid (patient_id, care_plan_activity_id, id)
  ) AUTO_INCREMENT=1 ;

CREATE TABLE pcr_tables
(
  id         tinyint      NOT NULL AUTO_INCREMENT ,
  table_name varchar(255) NOT NULL,
  PRIMARY KEY (id)
) AUTO_INCREMENT =1 COMMENT 'lookup of all the table names in this database';

CREATE TABLE event_log
(
  id                         bigint   NOT NULL AUTO_INCREMENT,
  organisation_id            int      NOT NULL,
  entry_date                 datetime NOT NULL,
  entered_by_practitioner_id int COMMENT 'who actually recorded the entry into the host system',
  device_id                  int COMMENT 'refers to the device that may have recorded the data',
  entry_mode                 tinyint  NOT NULL COMMENT 'entry mode i.e. 0-upsert, 1-delete',
  table_id                   tinyint  NOT NULL COMMENT 'the table ID relevant to the entry',
  item_id                    bigint   NOT NULL COMMENT 'the item identifier in the table relevant to to entry',
  PRIMARY KEY (id),
  CONSTRAINT event_log_entered_practitioner_id
  FOREIGN KEY (entered_by_practitioner_id)
  REFERENCES practitioner (id),
  CONSTRAINT event_log_table_id
  FOREIGN KEY (table_id)
  REFERENCES pcr_tables (id)
) AUTO_INCREMENT=1 COMMENT 'represents the transaction log of all core table entries';

# CREATE TABLE pcr_id_map - MOVED TO SUBSCRIBER_TRANSFORM
# (
#   id           int          NOT NULL COMMENT 'ID in PCR',
#   resourceId   varchar(255) NOT NULL COMMENT 'resourceId from source',
#   resourceType varchar(255) NOT NULL COMMENT 'resource type from source',
#   sourceDb     int          NOT NULL COMMENT 'Pointer to pcr_db_map reference to source db'
# ) COMMENT 'To track PCR data back to source';

CREATE TABLE pcr_db_map
(
  id              int   AUTO_INCREMENT        NOT NULL,
  discoveryDb     varchar(255) NOT NULL COMMENT 'eg db URL',
  discoverySchema varchar(255) COMMENT 'Allow for possible future schema-less DB',
   PRIMARY KEY (id)
) AUTO_INCREMENT =1 COMMENT 'Sources of data ';

CREATE TABLE code_scheme_lookup
(
  id          int     AUTO_INCREMENT      NOT NULL,
  code_schema VARCHAR(255) NOT NULL COMMENT 'URI of schema eg CSV3, READ2 etc',
   PRIMARY KEY (id)
) AUTO_INCREMENT =1;
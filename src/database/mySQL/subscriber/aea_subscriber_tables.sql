drop if exists aea_attendance;
drop if exists aea_diagnosis;
drop if exists aea_investigation;
drop if exists aea_mental_health_classification;
drop if exists aea_referral_to_service;
drop if exists aea_safeguarding_concern;
drop if exists aea_treatment;


CREATE TABLE aea_attendance(
	id bigint NOT NULL,
	patient_id bigint NOT NULL,
	organisation_id bigint NOT NULL,
	cds_unique_id varchar(50) NOT NULL,
	location_id bigint NOT NULL,
	department_type_concept_id int NOT NULL,
	ambulance_incident_number varchar(50) NOT NULL,
	ambulance_trust_organisation_id bigint NOT NULL,
	attendance_id varchar(50) NOT NULL,
	arrival_mode_concept_id int NOT NULL,
	category_concept_id int NOT NULL,
	source_concept_id int NOT NULL,
	arrival_datetime datetime NOT NULL,
	initial_assessment_datetime datetime NOT NULL,
	chief_complaint_concept_id int NOT NULL,
	treatment_datetime datetime NOT NULL,
	decided_to_admin_datetime datetime NULL,
	treatment_function_concept_id int NOT NULL,
	discharge_status_concept_id int NOT NULL,
	conclusion_datetime datetime NOT NULL,
	departure_datetime datetime NOT NULL,
	discharge_destination_concept_id int NOT NULL,
	CONSTRAINT PK_aea_attendance PRIMARY KEY (id)
);

CREATE TABLE aea_diagnosis(
	id bigint NOT NULL,
	patient_id bigint NOT NULL,
	organisation_id bigint NOT NULL,
	aea_attendance_id bigint NOT NULL,
	diagnosis_concept_id int NOT NULL,
	sequence_number tinyint NOT NULL,
	CONSTRAINT PK_aea_diagnosis PRIMARY KEY (id),
	CONSTRAINT fk_aea_diagnosis_id FOREIGN KEY (aea_attendance_id)
	REFERENCES aea_attendance (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE TABLE aea_investigation(
	id bigint NOT NULL,
	patient_id bigint NOT NULL,
	organisation_id bigint NOT NULL,
	aea_attendance_id bigint NOT NULL,
	investigation_concept_id int NOT NULL,
	sequence_number tinyint NOT NULL,
	performed_datetime datetime NOT NULL,
	CONSTRAINT PK_aea_investigation PRIMARY KEY (id),
	CONSTRAINT fk_aea_investigation_id FOREIGN KEY (aea_attendance_id)
	REFERENCES aea_attendance (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE TABLE aea_mental_health_classification(
	id bigint NOT NULL,
	patient_id bigint NOT NULL,
	organisation_id bigint NOT NULL,
	aea_attendance_id bigint NOT NULL,
	classification_concept_id int NOT NULL,
	start_datetime datetime NOT NULL,
	end_datetime datetime NOT NULL,
	sequence_number tinyint NOT NULL,
	CONSTRAINT PK_aea_mental_health_classification PRIMARY KEY (id),
	CONSTRAINT fk_aea_mental_health_classification_id FOREIGN KEY (aea_attendance_id)
	REFERENCES aea_attendance (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE TABLE aea_referral_to_service(
	id bigint NOT NULL,
	patient_id bigint NOT NULL,
	organisation_id bigint NOT NULL,
	aea_attendance_id bigint NOT NULL,
	referral_concept_id int NOT NULL,
	request_datetime datetime NOT NULL,
	assessment_datetime datetime NOT NULL,
	sequence_number tinyint NOT NULL,
	CONSTRAINT PK_aea_referral_to_service PRIMARY KEY (id),
	CONSTRAINT fk_aea_referral_to_service_id FOREIGN KEY (aea_attendance_id)
	REFERENCES aea_attendance (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE aea_safeguarding_concern(
	id bigint NOT NULL,
	patient_id bigint NOT NULL,
	organisation_id bigint NOT NULL,
	aea_attendance_id bigint NOT NULL,
	concern_concept_id int NOT NULL,
	sequence_number tinyint NOT NULL,
	CONSTRAINT PK_aea_safeguarding_concern PRIMARY KEY (id),
	CONSTRAINT fk_aea_safeguarding_concern_id FOREIGN KEY (aea_attendance_id)
	REFERENCES aea_attendance (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE aea_treatment(
	id bigint NOT NULL,
	patint_id bigint NOT NULL,
	organisation_id bigint NOT NULL,
	aea_attendance_id bigint NOT NULL,
	treatment_concept_id int NOT NULL,
	treatment_datetime datetime NOT NULL,
	sequence_number tinyint NOT NULL,
	CONSTRAINT PK_aea_treatment PRIMARY KEY (id),
	CONSTRAINT fk_aea_treatment_id FOREIGN KEY (aea_attendance_id)
	REFERENCES aea_attendance (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION
);
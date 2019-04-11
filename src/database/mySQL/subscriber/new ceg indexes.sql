

CREATE INDEX person_id ON observation
(
	person_id ASC
);

CREATE INDEX organisation_id ON observation
(
	organisation_id ASC
);

CREATE INDEX person_id_organisation_id_snomed_concept_id_clinical_effective_date_id ON observation
(
	person_id ASC,
    organization_id ASC, 
    snomed_concept_id ASC,
    clinical_effective_date ASC, 
    id ASC
);

CREATE INDEX snomed_concept_id_clinical_effective_date_patient_id ON encounter
(
	snomed_concept_id ASC,
	clinical_effective_date ASC,
	patient_id ASC
);

CREATE INDEX patient_id_clinical_effective_date_snomed_concept_id ON encounter
(
	patient_id ASC,
	clinical_effective_date ASC,
	snomed_concept_id ASC
);

CREATE INDEX encounter_snomed_concept_id ON encounter
(
	snomed_concept_id ASC
);

CREATE INDEX registration_type_id_patient_id_date_registered_date_registered_end ON episode_of_care
(
	registration_type_id ASC,
	patient_id ASC,
	date_registered ASC,
	date_registered_end ASC
);

CREATE INDEX snomed_concept_id_patient_id_clinical_effective_date ON observation
(
	snomed_concept_id ASC,
	patient_id ASC,
	clinical_effective_date ASC
);

CREATE INDEX observation_clinical_effective_date ON observation
(
	clinical_effective_date DESC
);

CREATE INDEX organization_id_parent_organization_id ON organization
(
	id ASC,
	parent_organization_id ASC
);

CREATE INDEX patient_id_organization_id ON patient
(
	id ASC,
	organization_id ASC
);

CREATE INDEX organization_id_date_of_death_id ON patient
(
	organization_id ASC,
	date_of_death ASC,
	id ASC
);

CREATE INDEX patient_date_of_death ON patient
(
	date_of_death ASC
);




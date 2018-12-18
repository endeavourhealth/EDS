use pcr;

DROP PROCEDURE IF EXISTS `patient_demographics`;
DROP PROCEDURE IF EXISTS `patient_observations`;
DROP PROCEDURE IF EXISTS `patient_immunisations`;
DROP PROCEDURE IF EXISTS `patient_medications`;
DROP PROCEDURE IF EXISTS `patient_medication_orders`;
DROP PROCEDURE IF EXISTS `patient_allergies`;
DROP PROCEDURE IF EXISTS `practice_diabetics_count_over_12s`;
DROP PROCEDURE IF EXISTS `practice_asthma_count`;
DROP PROCEDURE IF EXISTS `practice_pcr_data_count`;
DROP PROCEDURE IF EXISTS `practice_currently_registered_patients`;

DELIMITER $$
CREATE PROCEDURE `patient_demographics`(
    IN _nhsno varchar(36)
)
    BEGIN

        -- all demographics for a patient by NHS number
        select
            p.id, p.nhs_number, p.date_of_birth, p.gender_concept_id,
            p.title, p.first_name, p.middle_names, p.last_name,
            a.address_line_1, a.address_line_2, a.address_line_3, a.address_line_4,
            a.postcode, a.uprn
        from
            pcr.patient p
        left join
            address a on a.id = p.home_address_id
        where
            p.nhs_number = _nhsno;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `patient_observations`(
    IN _nhsno varchar(36)
)
    BEGIN

        -- all observations for a patient by NHS number
        select
            o.patient_id, o.effective_date, o.original_code, o.original_term, o.original_code_scheme,
            ov.result_value, ov.result_value_units, ov.result_date, ov.result_text,
            pr.title as 'Clincian Title', pr.first_name as 'Clinician First Name', pr.last_name as 'Clinician Last Name'
        from
            pcr.observation o
        left join
            pcr.observation_value ov on o.id = ov.observation_id and ov.patient_id = o.patient_id
        left join
            pcr.practitioner pr on pr.id = o.effective_practitioner_id
        join
            pcr.patient p on p.id = o.patient_id
        where
            p.nhs_number = _nhsno;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `patient_immunisations`(
    IN _nhsno varchar(36)
)
    BEGIN

        -- all immunisations for a patient by NHS number
        select
            i.patient_id, i.effective_date, i.original_code, i.original_term, i.original_code_scheme,
            pr.title as 'Clincian Title', pr.first_name as 'Clinician First Name', pr.last_name as 'Clinician Last Name'
        from
            pcr.immunisation i
        left join
            pcr.practitioner pr on pr.id = i.effective_practitioner_id
        join
            pcr.patient p on p.id = i.patient_id
        where
            p.nhs_number = _nhsno;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `patient_medications`(
    IN _nhsno varchar(36)
)
    BEGIN

        -- all medications for a patient by NHS number
        select
            ms.patient_id, ms.effective_date, ms.original_code, ms.original_term,ms.original_code_scheme,
            mo.dose, mo.quantity_value, mo.quantity_units,
            pr.title as 'Clincian Title', pr.first_name as 'Clinician First Name', pr.last_name as 'Clinician Last Name'
        from
            pcr.medication_statement ms
        join
            pcr.medication_amount mo on ms.medication_amount_id = mo.id
        left join
            pcr.practitioner pr on pr.id = ms.effective_practitioner_id
        join
            pcr.patient p on p.id = ms.patient_id
        where
            p.nhs_number = _nhsno;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `patient_medication_orders`(
    IN _nhsno varchar(36)
)
    BEGIN

        -- all medication orders/issues for a patient by NHS number
        select
            m.patient_id, m.effective_date, m.original_code, m.original_term,m.original_code_scheme,
            mo.dose, mo.quantity_value, mo.quantity_units,
            pr.title as 'Clincian Title', pr.first_name as 'Clinician First Name', pr.last_name as 'Clinician Last Name'
        from
            pcr.medication_order m
            join
            pcr.medication_amount mo on m.medication_amount_id = mo.id
            left join
            pcr.practitioner pr on pr.id = m.effective_practitioner_id
            join
            pcr.patient p on p.id = m.patient_id
        where
            p.nhs_number = _nhsno;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `patient_allergies`(
    IN _nhsno varchar(36)
)
    BEGIN

        -- all allergies for a patient by NHS number
        select
            a.patient_id, a.effective_date, a.original_code, a.original_term, a.original_code_scheme,
            pr.title as 'Clincian Title', pr.first_name as 'Clinician First Name', pr.last_name as 'Clinician Last Name'
        from
            pcr.allergy a
        left join
            pcr.practitioner pr on pr.id = a.effective_practitioner_id
        join
            pcr.patient p on p.id = a.patient_id
        where
            p.nhs_number = _nhsno;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `practice_diabetics_count_over_12s`(
    IN _odscode varchar(10)
)
    BEGIN

        -- all registered patients who have a Diabetes code (3,4,5) and are 12 years or older
        select count(distinct(p.nhs_number)) as 'Diabetics 12+ count' from pcr.observation o
            join pcr.organisation org on org.id = o.owning_organisation_id
            join pcr.patient p on p.id = o.patient_id
            join pcr.gp_registration_status reg on reg.patient_id = p.id
        where
            org.ods_code = _odscode
        and
            o.original_code in (select read2_concept_id from subscriber_transform_pcr.code_set_codes where code_set_id in (3,4,5))
        and
            p.date_of_birth <= DATE(NOW() - INTERVAL 12 year)
        and
            (reg.gp_registration_status_concept_id = 2 and reg.is_current = true)
        and
            p.date_of_death is null;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `practice_asthma_count`(
    IN _odscode varchar(10)
)
    BEGIN

        -- all registered patients who have an Asthma diagnosis code (68) and have been given Asthma medication (69) during the last 12 months
        select count(distinct(p.nhs_number)) as 'Asthma patients' from pcr.observation o
            join pcr.organisation org on org.id = o.owning_organisation_id
            join pcr.patient p on p.id = o.patient_id
            join pcr.medication_statement ms on ms.patient_id = p.id
            join pcr.medication_order mo on mo.medication_statement_id = ms.id
            join pcr.gp_registration_status reg on reg.patient_id = p.id
            join subscriber_transform_pcr.code_set_codes csc1 on csc1.read2_concept_id = o.original_code
            join subscriber_transform_pcr.code_set_codes csc2 on csc2.sct_concept_id = ms.original_code
        where
            org.ods_code =  _odscode
        and
            (reg.gp_registration_status_concept_id = 2 and reg.is_current = true)
        and
            p.date_of_death is null
        and
            (csc1.code_set_id = 68 and csc2.code_set_id = 69)
        and
            mo.effective_date >= DATE(NOW() - INTERVAL 12 MONTH);

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `practice_currently_registered_patients`(
    IN _odscode varchar(10)
)
    BEGIN

        -- all registered patients
        select count(distinct(p.nhs_number)) as 'Currently registered patients' from pcr.patient p
            join pcr.organisation org on org.id = p.organisation_id
            join pcr.gp_registration_status reg on reg.patient_id = p.id
        and
            org.ods_code = _odscode
        and
            (reg.gp_registration_status_concept_id = 2 and reg.is_current = true)
        and
            p.date_of_death is null;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `practice_pcr_data_count`(
    IN _odscode varchar(10)
)
    BEGIN

        -- all PCR data tables counts
        select
            count(*) as 'Count', '' 'pcr.allergy' from pcr.allergy a
            join pcr.organisation org on org.id = a.owning_organisation_id
        where org.ods_code = _odscode
        union
        select
            count(*) as 'Count' , 'pcr.problem' from pcr.problem pr
            join pcr.observation o on o.id = pr.observation_id
            join pcr.organisation org on org.id = o.owning_organisation_id
        where org.ods_code = _odscode
        union
        select
            count(*) as 'Count', 'pcr.immunisation' from pcr.immunisation i
            join pcr.organisation org on org.id = i.owning_organisation_id
        where org.ods_code = _odscode
        union
        select
            count(*)  as 'Count', 'pcr.medication_order' from pcr.medication_order mo
            join pcr.organisation org on org.id = mo.owning_organisation_id
        where org.ods_code = _odscode
        union
        select
            count(*)  as 'Count', 'pcr.medication_statement' from pcr.medication_statement ms
            join pcr.organisation org on org.id = ms.owning_organisation_id
        where org.ods_code = _odscode
        union
        select
            count(*)  as 'Count', 'pcr.observation' from pcr.observation o
            join pcr.organisation org on org.id = o.owning_organisation_id
        where org.ods_code = _odscode
        union
        select
            count(*)  as 'Count', 'pcr.patient' from pcr.patient p
            join pcr.organisation org on org.id = p.organisation_id
        where org.ods_code = _odscode;

    END$$
DELIMITER ;
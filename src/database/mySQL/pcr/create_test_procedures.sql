use pcr;

DROP PROCEDURE IF EXISTS `patient_demographics`;
DROP PROCEDURE IF EXISTS `patient_observations`;
DROP PROCEDURE IF EXISTS `patient_immunisations`;
DROP PROCEDURE IF EXISTS `patient_medications`;
DROP PROCEDURE IF EXISTS `patient_allergies`;
DROP PROCEDURE IF EXISTS `practice_diabetics_count_over_12s`;
DROP PROCEDURE IF EXISTS `practice_asthma_count`;

DELIMITER $$
CREATE PROCEDURE `patient_demographics`(
    IN _nhsno varchar(36)
)
    BEGIN

        select
            p.id, p.nhs_number, p.date_of_birth, p.gender_concept_id,
            p.title, p.first_name, p.middle_names, p.last_name,
            a.address_line_1, a.address_line_2, a.address_line_3, a.address_line_4,
            a.postcode, a.uprn
        from
            pcr.patient p
            left join
            patient_address pa on pa.patient_id = p.id
            left join
            address a on a.id = pa.address_id
        where
            p.nhs_number = _nhsno;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `patient_observations`(
    IN _nhsno varchar(36)
)
    BEGIN

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
CREATE PROCEDURE `patient_allergies`(
    IN _nhsno varchar(36)
)
    BEGIN

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

        select count(distinct(patient_id)) as 'Diabetics 12+ count' from pcr.observation o
            join pcr.organisation org on org.id = o.owning_organisation_id
            join pcr.patient p on p.id = o.patient_id
        where o.original_code in (select read2_concept_id from subscriber_transform_pcr.code_set_codes where code_set_id in (3,5))
              and org.ods_code = _odscode
              and p.date_of_birth < DATE(NOW()-INTERVAL 12 year)
              and p.date_of_death is null;

    END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE `practice_asthma_count`(
    IN _odscode varchar(10)
)
    BEGIN

        select count(distinct(patient_id)) as 'Asthma patients' from pcr.observation o
            join pcr.organisation org on org.id = o.owning_organisation_id
            join pcr.patient p on p.id = o.patient_id
        where o.original_code in (select read2_concept_id from subscriber_transform_pcr.code_set_codes where code_set_id in (0))
              and org.ods_code = _odscode
              and p.date_of_death is null;

    END$$
DELIMITER ;

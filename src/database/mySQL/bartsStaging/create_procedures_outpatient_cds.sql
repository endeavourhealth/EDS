USE staging_barts;

DROP PROCEDURE IF EXISTS `process_outpatient_cds_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_outpatient_cds_staging_exchange`(
    IN _exchange_id char(36)
)
BEGIN

    -- create helper table to get latest CDS tail records for the exchange
    insert into cds_tail_latest
    select
        exchange_id,
        dt_received,
        record_checksum,
        sus_record_type,
        cds_unique_identifier,
        cds_update_type,
        mrn,
        nhs_number,
        person_id,
        encounter_id,
        episode_id,
        responsible_hcp_personnel_id,
        treatment_function_code,
        audit_json
    from
        cds_tail
    where
        exchange_id = _exchange_id
    ON DUPLICATE KEY UPDATE
         exchange_id = values(exchange_id),
         dt_received = values(dt_received),
         record_checksum = values(record_checksum),
         cds_update_type = values(cds_update_type),
         mrn = values(mrn),
         nhs_number = values(nhs_number),
         person_id = values(person_id),
         encounter_id = values(encounter_id),
         episode_id = values(episode_id),
         responsible_hcp_personnel_id = values(responsible_hcp_personnel_id),
         treatment_function_code = values(treatment_function_code),
         audit_json = values(audit_json);

    -- create helper table to get latest outpatient cds records
    insert into cds_outpatient_latest
    select
        exchange_id,
        dt_received,
        record_checksum,
        cds_activity_date,
        cds_unique_identifier,
        cds_update_type,
        mrn,
        nhs_number,
        withheld,
        date_of_birth,
        consultant_code,
        patient_pathway_identifier,
        appt_attendance_identifier,
        appt_attended_code,
        appt_outcome_code,
        appt_date,
        appt_site_code,

        -- store any diagnosis and procedure data
        primary_diagnosis_ICD,
        secondary_diagnosis_ICD,
        other_diagnosis_ICD,
        primary_procedure_OPCS,
        primary_procedure_date,
        secondary_procedure_OPCS,
        secondary_procedure_date,
        other_procedures_OPCS,

        lookup_person_id,
        lookup_consultant_personnel_id,
        audit_json
    from
        cds_outpatient
    where
        exchange_id = _exchange_id
    ON DUPLICATE KEY UPDATE
         exchange_id = VALUES(exchange_id),
         dt_received = VALUES(dt_received),
         record_checksum = VALUES(record_checksum),
         cds_activity_date=VALUES(cds_activity_date),
         cds_unique_identifier = VALUES(cds_unique_identifier),
         cds_update_type = VALUES(cds_update_type),
         mrn = VALUES(mrn),
         nhs_number = VALUES(nhs_number),
         withheld = VALUES(withheld),
         date_of_birth = VALUES(date_of_birth),
         consultant_code = VALUES(consultant_code),
         patient_pathway_identifier = VALUES(patient_pathway_identifier),
         appt_attendance_identifier = VALUES(appt_attendance_identifier),
         appt_attended_code = VALUES(appt_attended_code),
         appt_outcome_code = VALUES(appt_outcome_code),
         appt_date = VALUES(appt_date),
         appt_site_code = VALUES(appt_site_code),
         primary_diagnosis_ICD = VALUES(primary_diagnosis_ICD),
         secondary_diagnosis_ICD = VALUES(secondary_diagnosis_ICD),
         other_diagnosis_ICD = VALUES(other_diagnosis_ICD),
         primary_procedure_OPCS = VALUES(primary_procedure_OPCS),
         primary_procedure_date = VALUES(primary_procedure_date),
         secondary_procedure_OPCS = VALUES(secondary_procedure_OPCS),
         secondary_procedure_date = VALUES(secondary_procedure_date),
         other_procedures_OPCS = VALUES(other_procedures_OPCS),
         lookup_person_id = VALUES(lookup_person_id),
         lookup_consultant_personnel_id = VALUES(lookup_consultant_personnel_id),
         audit_json = VALUES(audit_json);


    -- clear down cds_outpatient_target for the exchange
    delete from cds_outpatient_target where exchange_id = _exchange_id;

    insert into cds_outpatient_target
    select
        _exchange_id as exchange_id,
        concat('OPCDS-', cdsop.cds_unique_identifier) as unique_id,
        if (cdsop.cds_update_type = 1, true, false) as is_delete,
        coalesce(cdsop.lookup_person_id, tail.person_id) as person_id,
        tail.encounter_id as encounter_id,
        tail.episode_id as episode_id,
        coalesce(cdsop.lookup_consultant_personnel_id, tail.responsible_hcp_personnel_id) as performer_personnel_id,
        cdsop.patient_pathway_identifier,
        cdsop.appt_attendance_identifier,
        cdsop.appt_attended_code,
        cdsop.appt_outcome_code,
        cdsop.appt_date,
        cdsop.appt_site_code,
        cdsop.primary_diagnosis_ICD,
        cdsop.secondary_diagnosis_ICD,
        cdsop.other_diagnosis_ICD,
        cdsop.primary_procedure_OPCS,
        cdsop.primary_procedure_date,
        cdsop.secondary_procedure_OPCS,
        cdsop.secondary_procedure_date,
        cdsop.other_procedures_OPCS,
        concat_ws('&', cdsop.audit_json, tail.audit_json) as audit_json,
        cdsop.withheld as is_confidential
    from
        cds_outpatient_latest cdsop
            left join
        cds_tail_latest tail
        on cdsop.cds_unique_identifier = tail.cds_unique_identifier
    where
        cdsop.exchange_id = _exchange_id;

    insert into cds_outpatient_target_latest
    select
        exchange_id,
        unique_id,
        is_delete,
        person_id,
        encounter_id,
        episode_id,
        performer_personnel_id,
        patient_pathway_identifier,
        appt_attendance_identifier,
        appt_attended_code,
        appt_outcome_code,
        appt_date,
        appt_site_code,

        -- store any diagnosis and procedure data
        primary_diagnosis_ICD,
        secondary_diagnosis_ICD,
        other_diagnosis_ICD,
        primary_procedure_OPCS,
        primary_procedure_date,
        secondary_procedure_OPCS,
        secondary_procedure_date,
        other_procedures_OPCS,

        audit_json,
        is_confidential
    FROM
        cds_outpatient_target
    WHERE
        exchange_id = _exchange_id
    ON DUPLICATE KEY UPDATE
         exchange_id = values(exchange_id),
         -- unique_id = values(unique_id), -- part of primary key
         is_delete = values(is_delete),
         person_id = values(person_id),
         encounter_id = values(encounter_id),
         episode_id = values(episode_id),
         performer_personnel_id = values(performer_personnel_id),
         patient_pathway_identifier = VALUES(patient_pathway_identifier),
         appt_attendance_identifier = VALUES(appt_attendance_identifier),
         appt_attended_code = VALUES(appt_attended_code),
         appt_outcome_code = VALUES(appt_outcome_code),
         appt_date = VALUES(appt_date),
         appt_site_code = VALUES(appt_site_code),
         primary_diagnosis_ICD = VALUES(primary_diagnosis_ICD),
         secondary_diagnosis_ICD = VALUES(secondary_diagnosis_ICD),
         other_diagnosis_ICD = VALUES(other_diagnosis_ICD),
         primary_procedure_OPCS = VALUES(primary_procedure_OPCS),
         primary_procedure_date = VALUES(primary_procedure_date),
         secondary_procedure_OPCS = VALUES(secondary_procedure_OPCS),
         secondary_procedure_date = VALUES(secondary_procedure_date),
         other_procedures_OPCS = VALUES(other_procedures_OPCS),
         audit_json = values(audit_json),
         is_confidential = values(is_confidential);

END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `get_target_outpatient_cds_exchange`;

DELIMITER $$
CREATE PROCEDURE `get_target_outpatient_cds_exchange`(
    IN _exchange_id char(36)
)
BEGIN

    select
        ot.unique_id,
        ot.is_delete,
        ot.person_id,
        ot.encounter_id,
        ot.episode_id,
        ot.performer_personnel_id,
        ot.patient_pathway_identifier,
        ot.appt_attendance_identifier,
        ot.appt_attended_code,
        ot.appt_outcome_code,
        ot.appt_date,
        ot.appt_site_code,
        ot.primary_diagnosis_ICD,
        ot.secondary_diagnosis_ICD,
        ot.other_diagnosis_ICD,
        ot.primary_procedure_OPCS,
        ot.primary_procedure_date,
        ot.secondary_procedure_OPCS,
        ot.secondary_procedure_date,
        ot.other_procedures_OPCS,
        ot.audit_json,
        ot.is_confidential
    from
        cds_outpatient_target ot
    where
        ot.exchange_id = _exchange_id;

END$$
DELIMITER ;
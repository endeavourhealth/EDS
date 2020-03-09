USE staging_barts;

DROP PROCEDURE IF EXISTS `process_inpatient_cds_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_inpatient_cds_staging_exchange`(
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

    -- create helper table to get latest inpatient cds records
    insert into cds_inpatient_latest
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
        spell_number,
        administrative_category_code,
        admission_method_code,
        admission_source_code,
        patient_classification,
        spell_start_date,
        episode_number,
        episode_start_site_code,
        episode_start_ward_code,
        episode_start_date,
        episode_end_site_code,
        episode_end_ward_code,
        episode_end_date,
        discharge_date,
        discharge_destination_code,
        discharge_method,

        -- store any maternity data
        maternity_data_birth,
        maternity_data_delivery,

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
        cds_inpatient
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
         spell_number = VALUES(spell_number),
         administrative_category_code = VALUES(administrative_category_code),
         admission_method_code = VALUES(admission_method_code),
         admission_source_code = VALUES(admission_source_code),
         patient_classification = VALUES(patient_classification),
         spell_start_date = VALUES(spell_start_date),
         episode_number = VALUES(episode_number),
         episode_start_site_code = VALUES(episode_start_site_code),
         episode_start_ward_code = VALUES(episode_start_ward_code),
         episode_start_date = VALUES(episode_start_date),
         episode_end_site_code = VALUES(episode_end_site_code),
         episode_end_ward_code = VALUES(episode_end_ward_code),
         episode_end_date = VALUES(episode_end_date),
         discharge_date = VALUES(discharge_date),
         discharge_destination_code = VALUES(discharge_destination_code),
         discharge_method = VALUES(discharge_method),
         maternity_data_birth = VALUES(maternity_data_birth),
         maternity_data_delivery = VALUES(maternity_data_delivery),
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


    -- clear down cds_inpatient_target for the exchange
    delete from cds_inpatient_target where exchange_id = _exchange_id;

    insert into cds_inpatient_target
    select
        _exchange_id as exchange_id,
        concat('IPCDS-', cdsip.cds_unique_identifier) as unique_id,
        if (cdsip.cds_update_type = 1, true, false) as is_delete,
        coalesce(cdsip.lookup_person_id, tail.person_id) as person_id,
        tail.encounter_id as encounter_id,
        tail.episode_id as episode_id,
        coalesce(cdsip.lookup_consultant_personnel_id, tail.responsible_hcp_personnel_id) as performer_personnel_id,
        cdsip.patient_pathway_identifier,
        cdsip.spell_number,
        cdsip.administrative_category_code,
        cdsip.admission_method_code,
        cdsip.admission_source_code,
        cdsip.patient_classification,
        cdsip.spell_start_date,
        cdsip.episode_number,
        cdsip.episode_start_site_code,
        cdsip.episode_start_ward_code,
        cdsip.episode_start_date,
        cdsip.episode_end_site_code,
        cdsip.episode_end_ward_code,
        cdsip.episode_end_date,
        cdsip.discharge_date,
        cdsip.discharge_destination_code,
        cdsip.discharge_method,
        cdsip.maternity_data_birth,
        cdsip.maternity_data_delivery,
        cdsip.primary_diagnosis_ICD,
        cdsip.secondary_diagnosis_ICD,
        cdsip.other_diagnosis_ICD,
        cdsip.primary_procedure_OPCS,
        cdsip.primary_procedure_date,
        cdsip.secondary_procedure_OPCS,
        cdsip.secondary_procedure_date,
        cdsip.other_procedures_OPCS,
        concat_ws('&', cdsip.audit_json, tail.audit_json) as audit_json,
        cdsip.withheld as is_confidential
    from
        cds_inpatient_latest cdsip
            left join
        cds_tail_latest tail
        on cdsip.cds_unique_identifier = tail.cds_unique_identifier
    where
        cdsip.exchange_id = _exchange_id;

    insert into cds_inpatient_target_latest
    select
        exchange_id,
        unique_id,
        is_delete,
        person_id,
        encounter_id,
        episode_id,
        performer_personnel_id,
        patient_pathway_identifier,
        spell_number,
        administrative_category_code,
        admission_method_code,
        admission_source_code,
        patient_classification,
        spell_start_date,
        episode_number,
        episode_start_site_code,
        episode_start_ward_code,
        episode_start_date,
        episode_end_site_code,
        episode_end_ward_code,
        episode_end_date,
        discharge_date,
        discharge_destination_code,
        discharge_method,
        maternity_data_birth,
        maternity_data_delivery,
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
        cds_inpatient_target
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
         spell_number = VALUES(spell_number),
         administrative_category_code = VALUES(administrative_category_code),
         admission_method_code = VALUES(admission_method_code),
         admission_source_code = VALUES(admission_source_code),
         patient_classification = VALUES(patient_classification),
         spell_start_date = VALUES(spell_start_date),
         episode_number = VALUES(episode_number),
         episode_start_site_code = VALUES(episode_start_site_code),
         episode_start_ward_code = VALUES(episode_start_ward_code),
         episode_start_date = VALUES(episode_start_date),
         episode_end_site_code = VALUES(episode_end_site_code),
         episode_end_ward_code = VALUES(episode_end_ward_code),
         episode_end_date = VALUES(episode_end_date),
         discharge_date = VALUES(discharge_date),
         discharge_destination_code = VALUES(discharge_destination_code),
         discharge_method = VALUES(discharge_method),
         maternity_data_birth = VALUES(maternity_data_birth),
         maternity_data_delivery = VALUES(maternity_data_delivery),
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

DROP PROCEDURE IF EXISTS `get_target_inpatient_cds_exchange`;

DELIMITER $$
CREATE PROCEDURE `get_target_inpatient_cds_exchange`(
    IN _exchange_id char(36)
)
BEGIN

    select
        it.unique_id,
        it.is_delete,
        it.person_id,
        it.encounter_id,   -- derive from lookup if null using spell_number on IPEPI
        it.episode_id,
        it.performer_personnel_id,
        it.patient_pathway_identifier,
        it.spell_number,
        it.administrative_category_code,
        it.admission_method_code,
        it.admission_source_code,
        it.patient_classification,
        it.spell_start_date,
        it.episode_number,
        it.episode_start_site_code,
        it.episode_start_ward_code,
        it.episode_start_date,
        it.episode_end_site_code,
        it.episode_end_ward_code,
        it.episode_end_date,
        it.discharge_date,
        it.discharge_destination_code,
        it.discharge_method,
        it.maternity_data_birth,
        it.maternity_data_delivery,
        it.primary_diagnosis_ICD,
        it.secondary_diagnosis_ICD,
        it.other_diagnosis_ICD,
        it.primary_procedure_OPCS,
        it.primary_procedure_date,
        it.secondary_procedure_OPCS,
        it.secondary_procedure_date,
        it.other_procedures_OPCS,
        it.audit_json,
        it.is_confidential
    from
        cds_inpatient_target it
    where
        it.exchange_id = _exchange_id;

END$$
DELIMITER ;

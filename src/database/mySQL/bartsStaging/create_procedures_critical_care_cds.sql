USE staging_barts;

DROP PROCEDURE IF EXISTS `process_critical_care_cds_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_critical_care_cds_staging_exchange`(
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

    -- create helper table to get latest critical care cds records
    insert into cds_critical_care_latest
    select
        exchange_id,
        dt_received,
        record_checksum,
        cds_unique_identifier,
        mrn,
        nhs_number,
        critical_care_type_id,
        spell_number,
        episode_number,
        critical_care_identifier,
        care_start_date,
        care_unit_function,
        admission_source_code,
        admission_type_code,
        admission_location,
        gestation_length_at_delivery,
        advanced_respiratory_support_days,
        basic_respiratory_supports_days,
        advanced_cardiovascular_support_days,
        basic_cardiovascular_support_days,
        renal_support_days,
        neurological_support_days,
        gastro_intestinal_support_days,
        dermatological_support_days,
        liver_support_days,
        organ_support_maximum,
        critical_care_level2_days,
        critical_care_level3_days,
        discharge_date,
        discharge_ready_date,
        discharge_status_code,
        discharge_destination,
        discharge_location,
        care_activity_1,
        care_activity_2100,
        lookup_person_id,
        audit_json
    from
        cds_critical_care
    where
        exchange_id = _exchange_id
    ON DUPLICATE KEY UPDATE
         exchange_id = VALUES(exchange_id),
         dt_received = VALUES(dt_received),
         record_checksum = VALUES(record_checksum),
         cds_unique_identifier = VALUES(cds_unique_identifier),
         mrn = VALUES(mrn),
         nhs_number = VALUES(nhs_number),
         critical_care_type_id = VALUES(critical_care_type_id),
         spell_number = VALUES(spell_number),
         episode_number = VALUES(episode_number),
         critical_care_identifier = VALUES(critical_care_identifier),
         care_start_date = VALUES(care_start_date),
         care_unit_function = VALUES(care_unit_function),
         admission_source_code = VALUES(admission_source_code),
         admission_type_code = VALUES(admission_type_code),
         admission_location = VALUES(admission_location),
         gestation_length_at_delivery = VALUES(gestation_length_at_delivery),
         advanced_respiratory_support_days = VALUES(advanced_respiratory_support_days),
         basic_respiratory_supports_days = VALUES(basic_respiratory_supports_days),
         advanced_cardiovascular_support_days = VALUES(advanced_cardiovascular_support_days),
         basic_cardiovascular_support_days = VALUES(basic_cardiovascular_support_days),
         renal_support_days = VALUES(renal_support_days),
         neurological_support_days = VALUES(neurological_support_days),
         gastro_intestinal_support_days = VALUES(gastro_intestinal_support_days),
         dermatological_support_days = VALUES(dermatological_support_days),
         liver_support_days = VALUES(liver_support_days),
         organ_support_maximum = VALUES(organ_support_maximum),
         critical_care_level2_days = VALUES(critical_care_level2_days),
         critical_care_level3_days = VALUES(critical_care_level3_days),
         discharge_date = VALUES(discharge_date),
         discharge_ready_date = VALUES(discharge_ready_date),
         discharge_status_code = VALUES(discharge_status_code),
         discharge_destination = VALUES(discharge_destination),
         discharge_location = VALUES(discharge_location),
         care_activity_1 = VALUES(care_activity_1),
         care_activity_2100 = VALUES(care_activity_2100),
         lookup_person_id = VALUES(lookup_person_id),
         audit_json = VALUES(audit_json);

    -- clear down cds_critical_care for the exchange
    delete from cds_critical_care_target where exchange_id = _exchange_id;

    insert into cds_critical_care_target
    select
        _exchange_id as exchange_id,
        concat('CCCDS-', cdscc.cds_unique_identifier) as unique_id,
        if (cdsip.cds_update_type = 1, true, false) as is_delete,
        cdscc.lookup_person_id as person_id,
        cdsip.lookup_consultant_personnel_id as performer_personnel_id,
        cdsip.episode_start_site_code as organisation_code,
        cdscc.critical_care_type_id,
        cdscc.spell_number,
        cdscc.episode_number,
        cdscc.critical_care_identifier,
        cdscc.care_start_date,
        cdscc.care_unit_function,
        cdscc.admission_source_code,
        cdscc.admission_type_code,
        cdscc.admission_location,
        cdscc.gestation_length_at_delivery,
        cdscc.advanced_respiratory_support_days,
        cdscc.basic_respiratory_supports_days,
        cdscc.advanced_cardiovascular_support_days,
        cdscc.basic_cardiovascular_support_days,
        cdscc.renal_support_days,
        cdscc.neurological_support_days,
        cdscc.gastro_intestinal_support_days,
        cdscc.dermatological_support_days,
        cdscc.liver_support_days,
        cdscc.organ_support_maximum,
        cdscc.critical_care_level2_days,
        cdscc.critical_care_level3_days,
        cdscc.discharge_date,
        cdscc.discharge_ready_date,
        cdscc.discharge_status_code,
        cdscc.discharge_destination,
        cdscc.discharge_location,
        cdscc.care_activity_1,
        cdscc.care_activity_2100,
        concat_ws('&', cdscc.audit_json, cdsip.audit_json) as audit_json,
        cdsip.withheld as is_confidential
    from
        cds_critical_care_latest cdscc
            join
        cds_inpatient_latest cdsip
            on cdsip.cds_unique_identifier = cdscc.cds_unique_identifier
    where
        cdscc.exchange_id = _exchange_id;

#   ?? target_latest table needed ??

END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `get_target_critical_care_cds_exchange`;

DELIMITER $$
CREATE PROCEDURE `get_target_critical_care_cds_exchange`(
    IN _exchange_id char(36)
)
BEGIN

    select
        cct.unique_id,
        cct.is_delete,
        cct.person_id,
        cct.performer_personnel_id,
        cct.organisation_code,
        cct.critical_care_type_id,
        cct.spell_number,
        cct.episode_number,
        cct.critical_care_identifier,
        cct.care_start_date,
        cct.care_unit_function,
        cct.admission_source_code,
        cct.admission_type_code,
        cct.admission_location,
        cct.gestation_length_at_delivery,
        cct.advanced_respiratory_support_days,
        cct.basic_respiratory_supports_days,
        cct.advanced_cardiovascular_support_days,
        cct.basic_cardiovascular_support_days,
        cct.renal_support_days,
        cct.neurological_support_days,
        cct.gastro_intestinal_support_days,
        cct.dermatological_support_days,
        cct.liver_support_days,
        cct.organ_support_maximum,
        cct.critical_care_level2_days,
        cct.critical_care_level3_days,
        cct.discharge_date,
        cct.discharge_ready_date,
        cct.discharge_status_code,
        cct.discharge_destination,
        cct.discharge_location,
        cct.care_activity_1,
        cct.care_activity_2100,
        cct.audit_json,
        cct.is_confidential
    from
        cds_critical_care_target cct
    where
        cct.exchange_id = _exchange_id;

END$$
DELIMITER ;

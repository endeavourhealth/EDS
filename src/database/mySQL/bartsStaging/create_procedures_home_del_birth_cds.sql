USE staging_barts;

DROP PROCEDURE IF EXISTS `process_home_del_birth_cds_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_home_del_birth_cds_staging_exchange`(
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

    -- create helper table to get latest home delivery birth cds records
    insert into cds_home_del_birth_latest
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
        birth_weight,
        live_or_still_birth_indicator,
        total_previous_pregnancies,
        number_of_babies,
        first_antenatal_assessment_date,
        antenatal_care_practitioner,
        antenatal_care_practice,
        delivery_place_intended,
        delivery_place_change_reason_code,
        gestation_length_labour_onset,
        delivery_date,
        delivery_place_actual,
        delivery_method,
        mother_nhs_number,
        lookup_person_id,
        audit_json
    from
        cds_home_del_birth
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
         birth_weight = VALUES(birth_weight),
         live_or_still_birth_indicator = VALUES(live_or_still_birth_indicator),
         total_previous_pregnancies = VALUES(total_previous_pregnancies),
         number_of_babies = VALUES(number_of_babies),
         first_antenatal_assessment_date = VALUES(first_antenatal_assessment_date),
         antenatal_care_practitioner = VALUES(antenatal_care_practitioner),
         antenatal_care_practice = VALUES(antenatal_care_practice),
         delivery_place_intended = VALUES(delivery_place_intended),
         delivery_place_change_reason_code = VALUES(delivery_place_change_reason_code),
         gestation_length_labour_onset = VALUES(gestation_length_labour_onset),
         delivery_date = VALUES(delivery_date),
         delivery_place_actual = VALUES(delivery_place_actual),
         delivery_method = VALUES(delivery_method),
         mother_nhs_number = VALUES(mother_nhs_number),
         lookup_person_id = VALUES(lookup_person_id),
         audit_json = VALUES(audit_json);


    -- TODO: determine if to progress with Target tables after analysis

END$$
DELIMITER ;

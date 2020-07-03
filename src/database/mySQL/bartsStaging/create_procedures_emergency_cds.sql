USE staging_barts;

DROP PROCEDURE IF EXISTS `process_emergency_cds_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_emergency_cds_staging_exchange`(
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

    -- create helper table to get latest emergency cds records
    insert into cds_emergency_latest
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
        patient_pathway_identifier,
        department_type,
        ambulance_incident_number,
        treatment_organisation_code,
        attendance_identifier,
        arrival_mode,
        attendance_category,
        attendance_source,
        arrival_date,
        initial_assessment_date,
        chief_complaint,
        seen_for_treatment_date,
        decided_to_admit_date,
        discharge_status,
        discharge_destination,
        discharge_destination_site_id,
        discharge_follow_up,
        conclusion_date,
        departure_date,
        mh_classifications,
        diagnosis,
        investigations,
        treatments,
        referred_to_services,
        safeguarding_concerns,
        lookup_person_id,
        audit_json
    from
        cds_emergency
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
         patient_pathway_identifier = VALUES(patient_pathway_identifier),
         department_type = VALUES(department_type),
         ambulance_incident_number = VALUES(ambulance_incident_number),
         treatment_organisation_code = VALUES(treatment_organisation_code),
         attendance_identifier = VALUES(attendance_identifier),
         arrival_mode = VALUES(arrival_mode),
         attendance_category = VALUES(attendance_category),
         attendance_source = VALUES(attendance_source),
         arrival_date = VALUES(arrival_date),
         initial_assessment_date = VALUES(initial_assessment_date),
         chief_complaint = VALUES(chief_complaint),
         seen_for_treatment_date = VALUES(seen_for_treatment_date),
         decided_to_admit_date = VALUES(decided_to_admit_date),
         discharge_status = VALUES(discharge_status),
         discharge_destination = VALUES(discharge_destination),
         discharge_follow_up = VALUES(discharge_follow_up),
         conclusion_date = VALUES(conclusion_date),
         departure_date = VALUES(departure_date),
         mh_classifications = VALUES(mh_classifications),
         diagnosis = VALUES(diagnosis),
         investigations = VALUES(investigations),
         treatments = VALUES(treatments),
         referred_to_services = VALUES(referred_to_services),
         safeguarding_concerns = VALUES(safeguarding_concerns),
         lookup_person_id = VALUES(lookup_person_id),
         audit_json = VALUES(audit_json);


    -- clear down cds_emergency_target for the exchange
    delete from cds_emergency_target where exchange_id = _exchange_id;

    insert into cds_emergency_target
    select
        _exchange_id as exchange_id,
        concat('ECDS-', cdse.cds_unique_identifier) as unique_id,
        if (cdse.cds_update_type = 1, true, false) as is_delete,
        coalesce(cdse.lookup_person_id, tail.person_id) as person_id,
        tail.encounter_id as encounter_id,
        tail.episode_id as episode_id,
        tail.responsible_hcp_personnel_id as performer_personnel_id,
        cdse.department_type,
        cdse.ambulance_incident_number as ambulance_no,
        cdse.treatment_organisation_code as organisation_code,
        cdse.attendance_identifier as attendance_id,
        cdse.arrival_mode,
        cdse.attendance_category,
        cdse.attendance_source,
        cdse.arrival_date,
        cdse.initial_assessment_date,
        cdse.chief_complaint,
        cdse.seen_for_treatment_date,
        cdse.decided_to_admit_date,
        tail.treatment_function_code,
        cdse.discharge_status,
        cdse.discharge_destination,
        cdse.discharge_follow_up,
        cdse.conclusion_date,
        cdse.departure_date,
        cdse.mh_classifications,
        cdse.diagnosis,
        cdse.investigations,
        cdse.treatments,
        cdse.referred_to_services,
        cdse.safeguarding_concerns,
        concat_ws('&', cdse.audit_json, tail.audit_json) as audit_json,
        cdse.withheld as is_confidential
    from
        cds_emergency_latest cdse
            left join
        cds_tail_latest tail
        on cdse.cds_unique_identifier = tail.cds_unique_identifier
    where
        cdse.exchange_id = _exchange_id;

    insert into cds_emergency_target_latest
    select
        exchange_id,
        unique_id,
        is_delete,
        person_id,
        encounter_id,
        episode_id,
        performer_personnel_id,
        department_type,
        ambulance_no,
        organisation_code,
        attendance_id,
        arrival_mode,
        attendance_category,
        attendance_source,
        arrival_date,
        initial_assessment_date,
        chief_complaint,
        seen_for_treatment_date,
        decided_to_admit_date,
        treatment_function_code,
        discharge_status,
        discharge_destination,
        discharge_follow_up,
        conclusion_date,
        departure_date,
        mh_classifications,
        diagnosis,
        investigations,
        treatments,
        referred_to_services,
        safeguarding_concerns,
        audit_json,
        is_confidential
    FROM
        cds_emergency_target
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
         department_type  = values(department_type),
         ambulance_no  = values(ambulance_no),
         organisation_code  = values(organisation_code),
         attendance_id  = values(attendance_id),
         arrival_mode  = values(arrival_mode),
         attendance_category = values(attendance_category),
         attendance_source = values(attendance_source),
         arrival_date  = values(arrival_date),
         initial_assessment_date = values(initial_assessment_date),
         chief_complaint = values(chief_complaint),
         seen_for_treatment_date = values(seen_for_treatment_date),
         decided_to_admit_date = values(decided_to_admit_date),
         treatment_function_code = values(treatment_function_code),
         discharge_status = values(discharge_status),
         discharge_destination = values(discharge_destination),
         discharge_follow_up = values(discharge_follow_up),
         conclusion_date = values(conclusion_date),
         departure_date = values(departure_date),
         mh_classifications = values(mh_classifications),
         diagnosis = values(diagnosis),
         investigations = values(investigations),
         treatments = values(treatments),
         referred_to_services = values(referred_to_services),
         safeguarding_concerns = values(safeguarding_concerns),
         audit_json = values(audit_json),
         is_confidential = values(is_confidential);

END$$
DELIMITER ;


DROP PROCEDURE IF EXISTS `get_target_emergency_cds_exchange`;

DELIMITER $$
CREATE PROCEDURE `get_target_emergency_cds_exchange`(
    IN _exchange_id char(36)
)
BEGIN

    select
        et.unique_id,
        et.is_delete,
        et.person_id,
        et.encounter_id,
        et.episode_id,
        et.performer_personnel_id,
        et.department_type,
        et.ambulance_no,
        et.organisation_code,
        et.attendance_id,
        et.arrival_mode,
        et.attendance_category,
        et.attendance_source,
        et.arrival_date,
        et.initial_assessment_date,
        et.chief_complaint,
        et.seen_for_treatment_date,
        et.decided_to_admit_date,
        et.treatment_function_code,
        et.discharge_status,
        et.discharge_destination,
        et.discharge_follow_up,
        et.conclusion_date,
        et.departure_date,
        et.diagnosis,
        et.investigations,
        et.treatments,
        et.referred_to_services,
        et.safeguarding_concerns,
        et.audit_json,
        et.is_confidential
    from
        cds_emergency_target et
    where
        et.exchange_id = _exchange_id;

END$$
DELIMITER ;
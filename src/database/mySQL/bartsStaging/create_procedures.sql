USE staging_barts;

DROP PROCEDURE IF EXISTS `process_procedure_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_procedure_staging_exchange`(
  IN _exchange_id char(36)
)
BEGIN

	-- create helper table to get latest CDS records
    insert into procedure_cds_latest
    select
        exchange_id,
		dt_received,
		record_checksum,
		cds_activity_date,
		sus_record_type,
		cds_unique_identifier,
		cds_update_type,
		mrn,
		nhs_number,
		date_of_birth,
		consultant_code,
		procedure_date,
		procedure_opcs_code,
		procedure_seq_nbr,
		primary_procedure_opcs_code,
		lookup_procedure_opcs_term,
		lookup_person_id,
		lookup_consultant_personnel_id,
		audit_json
	from
		procedure_cds
	where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
		exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		cds_activity_date = values(cds_activity_date),
		-- sus_record_type = values(sus_record_type), -- part of primary key
		-- cds_unique_identifier = values(cds_unique_identifier), -- part of primary key
		cds_update_type = values(cds_update_type),
		mrn = values(mrn),
		nhs_number = values(nhs_number),
		date_of_birth = values(date_of_birth),
		consultant_code = values(consultant_code),
		procedure_date = values(procedure_date),
		procedure_opcs_code = values(procedure_opcs_code),
		-- procedure_seq_nbr = values(procedure_seq_nbr), -- part of primary key
		primary_procedure_opcs_code = values(primary_procedure_opcs_code),
		lookup_procedure_opcs_term = values(lookup_procedure_opcs_term),
		lookup_person_id = values(lookup_person_id),
		lookup_consultant_personnel_id = values(lookup_consultant_personnel_id),
		audit_json = values(audit_json);


	-- create helper table to get latest CDS tail records
    insert into procedure_cds_tail_latest
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
		responsible_hcp_personnel_id,
		audit_json
    from
		procedure_cds_tail
    where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
		exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		-- sus_record_type = values(sus_record_type), -- part of primary key
		-- cds_unique_identifier = values(cds_unique_identifier), -- part of primary key
		cds_update_type = values(cds_update_type),
		mrn = values(mrn),
		nhs_number = values(nhs_number),
		person_id = values(person_id),
		encounter_id = values(encounter_id),
		responsible_hcp_personnel_id = values(responsible_hcp_personnel_id),
		audit_json = values(audit_json);


	-- create helper table to get latest procedure records
    insert into procedure_procedure_latest
    select
        exchange_id,
		dt_received,
		record_checksum,
		mrn,
		nhs_number,
		date_of_birth,
		encounter_id,
		consultant,
		proc_dt_tm,
		updated_by,
		freetext_comment,
		create_dt_tm,
		proc_cd_type,
		proc_cd,
		proc_term,
		person_id,
		ward,
		site,
		lookup_person_id,
		lookup_consultant_personnel_id,
		lookup_recorded_by_personnel_id,
		audit_json
    from
		procedure_procedure
	where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
        exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		mrn = values(mrn),
		nhs_number = values(nhs_number),
		date_of_birth = values(date_of_birth),
		-- encounter_id = values(encounter_id), -- part of primary key
		consultant = values(consultant),
		-- proc_dt_tm = values(proc_dt_tm), -- part of primary key
		updated_by = values(updated_by),
		freetext_comment = values(freetext_comment),
		create_dt_tm = values(create_dt_tm),
		proc_cd_type = values(proc_cd_type),
		-- proc_cd = values(proc_cd), -- part of primary key
		proc_term = values(proc_term),
		person_id = values(person_id),
		ward = values(ward),
		site = values(site),
		lookup_person_id = values(lookup_person_id),
		lookup_consultant_personnel_id = values(lookup_consultant_personnel_id),
		lookup_recorded_by_personnel_id = values(lookup_recorded_by_personnel_id),
		audit_json = values(audit_json);



	-- create helper table to get latest PROCE records
	insert into procedure_PROCE_latest
    select
        exchange_id,
		dt_received,
		record_checksum,
		procedure_id,
		active_ind,
		encounter_id,
        encounter_slice_id,
		procedure_dt_tm,
		procedure_type,
		procedure_code,
		procedure_term,
		procedure_seq_nbr,
		lookup_person_id,
		lookup_mrn,
		lookup_nhs_number,
		lookup_date_of_birth,
		audit_json
	from
		procedure_PROCE
	where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
        exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		-- procedure_id = values(procedure_id), -- part of primary key
		active_ind = values(active_ind),
		encounter_id = values(encounter_id),
        encounter_slice_id = values(encounter_slice_id),
		procedure_dt_tm = values(procedure_dt_tm),
		procedure_type = values(procedure_type),
		procedure_code = values(procedure_code),
		procedure_term = values(procedure_term),
		procedure_seq_nbr = values(procedure_seq_nbr),
		lookup_person_id = values(lookup_person_id),
		lookup_mrn = values(lookup_mrn),
		lookup_nhs_number = values(lookup_nhs_number),
		lookup_date_of_birth = values(lookup_date_of_birth),
		audit_json = values(audit_json);


	-- create helper table to get latest SURCC records
	insert into procedure_SURCC_latest
    select
        exchange_id,
		dt_received,
		record_checksum,
		surgical_case_id,
		dt_extract,
		active_ind,
		person_id,
		encounter_id,
		dt_cancelled,
		institution_code,
		department_code,
		surgical_area_code,
		theatre_number_code,
    specialty_code,
		audit_json
    from
		procedure_SURCC
	where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
		exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		-- surgical_case_id = values(surgical_case_id), -- part of primary key
		dt_extract = values(dt_extract),
		active_ind = values(active_ind),
		person_id = values(person_id),
		encounter_id = values(encounter_id),
		dt_cancelled = values(dt_cancelled),
		institution_code = values(institution_code),
		department_code = values(department_code),
		surgical_area_code = values(surgical_area_code),
		theatre_number_code = values(theatre_number_code),
		specialty_code = values(specialty_code),
		audit_json = values(audit_json);

	-- create helper table to get latest SURCC records
    insert into procedure_SURCP_latest
    select
        exchange_id,
		dt_received,
		record_checksum,
		surgical_case_procedure_id,
		surgical_case_id,
		dt_extract,
		active_ind,
		procedure_code,
		procedure_text,
		modifier_text,
		primary_procedure_indicator,
		surgeon_personnel_id,
		dt_start,
		dt_stop,
		wound_class_code,
		lookup_procedure_code_term,
		audit_json
    from
		procedure_SURCP
	where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
        exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		-- surgical_case_procedure_id = values(surgical_case_procedure_id), -- part of primary key
		surgical_case_id = values(surgical_case_id),
		dt_extract = values(dt_extract),
		active_ind = values(active_ind),
		procedure_code = values(procedure_code),
		procedure_text = values(procedure_text),
		modifier_text = values(modifier_text),
		primary_procedure_indicator = values(primary_procedure_indicator),
		surgeon_personnel_id = values(surgeon_personnel_id),
		dt_start = values(dt_start),
		dt_stop = values(dt_stop),
		wound_class_code = values(wound_class_code),
		lookup_procedure_code_term = values(lookup_procedure_code_term),
		audit_json = values(audit_json);


	-- first, clear down procedure_target for the exchange
	delete from procedure_target where exchange_id = _exchange_id;


	-- CDS and CDS Tail (left join, get all records including where tail record is null)
	insert into procedure_target
	select
		cds.exchange_id as exchange_id,
		concat('CDS-', cds.cds_unique_identifier, '-', cds.procedure_seq_nbr) as unique_id,
		if (cds.cds_update_type = 1, true, false) as is_delete,
		coalesce(tail.person_id, cds.lookup_person_id) as person_id,
		tail.encounter_id as encounter_id,
		coalesce(tail.responsible_hcp_personnel_id, cds.lookup_consultant_personnel_id) as performer_personnel_id,
		coalesce(cds.procedure_date, cds.cds_activity_date) as dt_performed,
		null as free_text,    -- data not available
		null as recorded_by_personnel_id, -- data not available
		null as dt_recorded,  -- data not available
		'OPCS4' as procedure_type,
		cds.procedure_opcs_code as procedure_code,
		cds.lookup_procedure_opcs_term as procedure_term,
		cds.procedure_seq_nbr as sequence_number,
		if (
		  cds.procedure_seq_nbr = 1,
		  null,
		  concat('CDS-', cds.cds_unique_identifier, '-', 1)
		) as parent_procedure_unique_id, -- if sequence number > 1 then parent procedure ID is the same unique ID but with seq 1
		null as qualifier,   -- data not available
		null as location,    -- data not available
		null as speciality,  -- data not available
		cds.audit_json
	from
		procedure_cds_latest cds
	left join
		procedure_cds_tail_latest tail
        on cds.cds_unique_identifier = tail.cds_unique_identifier
        and cds.sus_record_type = tail.sus_record_type
	where
		cds.exchange_id = _exchange_id;

	-- Procedure and PROCE (left join to get all)
	insert into procedure_target
	select
		proce.exchange_id as exchange_id,
		concat('PROCE-', proce.procedure_id) as unique_id,
		if (proce.active_ind = 0, true, false) as is_delete,
		coalesce(proc.person_id, proce.lookup_person_id) as person_id,
		coalesce(proc.encounter_id, proce.encounter_id) as encounter_id,
		proc.lookup_consultant_personnel_id as performer_personnel_id,
		proce.procedure_dt_tm as dt_performed,
		proc.freetext_comment as free_text,
		proc.lookup_recorded_by_personnel_id as recorded_by_personnel_id,
		null as dt_recorded,  -- data not available
		coalesce(proc.proc_cd_type, proce.procedure_type) as procedure_type,
		coalesce(proc.proc_cd, proce.procedure_code) as procedure_code,
		coalesce(proc.proc_term, proce.procedure_term) as procedure_term,
		proce.procedure_seq_nbr as sequence_number,
		if (
		  proce.procedure_seq_nbr is null or proce.procedure_seq_nbr = 1,
		  null,
		  concat('PROCE-', parent_proce.procedure_id)
    ) as parent_procedure_unique_id, -- if sequence number > 1 then parent procedure ID is the same unique ID but with seq 1
		null as qualifier,   -- data not available
		coalesce(proc.site, proc.ward) as location,
		null as speciality,  -- data not available
		proce.audit_json
	from
		procedure_PROCE_latest proce
	left join
		procedure_procedure_latest proc
        on proce.encounter_id = proc.encounter_id
		and proce.procedure_dt_tm = proc.proc_dt_tm
		and proce.procedure_code = proc.proc_cd
	left join
		procedure_PROCE_latest parent_proce
		on parent_proce.lookup_person_id = proce.lookup_person_id
        and parent_proce.encounter_id = proce.encounter_id
        and parent_proce.encounter_slice_id = proce.encounter_slice_id
        and parent_proce.procedure_seq_nbr = 1
		and proce.procedure_seq_nbr is not null
        and proce.procedure_seq_nbr > 1
	where
		proce.exchange_id = _exchange_id;


	-- SURCC and SURCP
	insert into procedure_target
	select
		cp.exchange_id as exchange_id,
		concat('SURG-',cp.surgical_case_procedure_id) as unique_id,
		if (cp.active_ind = 0, true, false) as is_delete,
		cc.person_id as person_id,
		cc.encounter_id as encounter_id,
		cp.surgeon_personnel_id as performer_personnel_id,
		cp.dt_start as dt_performed,
		if (cp.wound_class_code > 0, concat(cp.procedure_text,'. Wound class:',cp.wound_class_code),cp.procedure_text) as free_text,
		null as recorded_by_personnel_id,  -- data not available
		null as dt_recorded,  -- data not available
		'CERNER' as procedure_type,
		cp.procedure_code as procedure_code,
		cp.lookup_procedure_code_term as procedure_term,
		cp.primary_procedure_indicator as sequence_number,   -- the only data available is either 0 or 1
		null as parent_procedure_unique_id,  -- data not applicable
		cp.modifier_text as qualifier,
		coalesce(cc.institution_code, cc.department_code, cc.surgical_area_code, cc.theatre_number_code) as location,
		cc.specialty_code as specialty,
		cp.audit_json
	from
		procedure_SURCP_latest cp
	left join procedure_SURCC_latest cc
		on cp.surgical_case_id = cc.surgical_case_id
	where
		cp.exchange_id = _exchange_id;

		-- the above will have picked up CDS records where we don't have a patient, so delete them out of the target table
	  delete from procedure_target
	  where exchange_id = _exchange_id
	  and person_id is null;



	-- carry over to the target_latest table so we can see the latest state of everything
    INSERT INTO procedure_target_latest
    SELECT
		exchange_id,
		unique_id,
		is_delete,
		person_id,
		encounter_id,
		performer_personnel_id,
		dt_performed,
		free_text,
		recorded_by_personnel_id,
		dt_recorded,
		procedure_type,
		procedure_code,
		procedure_term,
		sequence_number,
		parent_procedure_unique_id,
		qualifier,
		location,
		specialty,
		audit_json
	FROM
		procedure_target
	WHERE
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
    	exchange_id = values(exchange_id),
		-- unique_id = values(unique_id), -- part of primary key
		is_delete = values(is_delete),
		person_id = values(person_id),
		encounter_id = values(encounter_id),
		performer_personnel_id = values(performer_personnel_id),
		dt_performed = values(dt_performed),
		free_text = values(free_text),
		recorded_by_personnel_id = values(recorded_by_personnel_id),
		dt_recorded = values(dt_recorded),
		procedure_type = values(procedure_type),
		procedure_code = values(procedure_code),
		procedure_term = values(procedure_term),
		sequence_number = values(sequence_number),
		parent_procedure_unique_id = values(parent_procedure_unique_id),
		qualifier = values(qualifier),
		location = values(location),
		specialty = values(specialty),
		audit_json = values(audit_json);


END$$
DELIMITER ;

 -- CALL process_procedure_staging_exchange('<exchange id>');
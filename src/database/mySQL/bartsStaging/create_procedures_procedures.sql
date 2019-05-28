USE staging_barts;

DROP PROCEDURE IF EXISTS `process_procedure_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_procedure_staging_exchange`(
  IN _exchange_id char(36)
)
BEGIN

  -- work out if any CDS records now have fewer procedures than before
  CREATE TEMPORARY TABLE procedure_cds_count_changed AS
  SELECT new_count.*, previous_count.procedure_count AS old_count
  FROM procedure_cds_count new_count
  INNER JOIN procedure_cds_count_latest previous_count
    ON new_count.cds_unique_identifier = previous_count.cds_unique_identifier
    AND new_count.sus_record_type = previous_count.sus_record_type
  WHERE
    new_count.exchange_id = _exchange_id
    AND new_count.procedure_count < previous_count.procedure_count;

  -- update CDS count latest table with new counts
  INSERT INTO procedure_cds_count_latest
  SELECT
    exchange_id,
    dt_received,
    record_checksum,
    sus_record_type,
    cds_unique_identifier,
    procedure_count
  FROM procedure_cds_count
  WHERE
    exchange_id = _exchange_id
  ON DUPLICATE KEY UPDATE
    exchange_id = VALUES(exchange_id),
    dt_received = VALUES(dt_received),
    record_checksum = VALUES(record_checksum),
    -- sus_record_type = VALUES(sus_record_type), -- part of primary key
    -- cds_unique_identifier = VALUES(cds_unique_identifier), -- part of primary key
    procedure_count = VALUES(procedure_count);

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
        withheld,
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
    withheld = values(withheld),
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
		encounter_id = values(encounter_id),
		consultant = values(consultant),
		-- proc_dt_tm = values(proc_dt_tm), -- part of primary key
		updated_by = values(updated_by),
		freetext_comment = values(freetext_comment),
		create_dt_tm = values(create_dt_tm),
		proc_cd_type = values(proc_cd_type),
		-- proc_cd = values(proc_cd), -- part of primary key
		proc_term = values(proc_term),
		ward = values(ward),
		site = values(site),
		-- lookup_person_id = values(lookup_person_id), -- part of primary key
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
		lookup_responsible_personnel_id,
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
		lookup_responsible_personnel_id = values(lookup_responsible_personnel_id),
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
        dt_start,
        dt_stop,
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
	    dt_start = values(dt_start),
	    dt_stop = values(dt_stop),
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

	-- generate deletes for any CDS procedures where the procedure count has gone down
    BEGIN -- need a begin because you need this to be able to declare a variable here
		DECLARE sequenceNumber int default 0;
		SET sequenceNumber = (SELECT MAX(old_count) FROM procedure_cds_count_changed);

		WHILE (sequenceNumber > 0) DO

			INSERT INTO procedure_target
			SELECT
				_exchange_id as exchange_id,
				concat('CDS-', cds_count_changed.cds_unique_identifier, '-', sequenceNumber) as unique_id,
				true as is_delete,
				null as person_id,
				null as encounter_id,
				null as performer_personnel_id,
				null as dt_performed,
				null as dt_ended, -- no end dates for these
				null as free_text,    -- data not available
				null as recorded_by_personnel_id, -- data not available
				null as dt_recorded,  -- data not available
				null as procedure_type,
				null as procedure_code,
				null as procedure_term,
				null as sequence_number,
				null as parent_procedure_unique_id,
				null as qualifier,
				null as location,
				null as speciality,
				null -- audit
			FROM
				procedure_cds_count_changed cds_count_changed
			WHERE
				old_count >= sequenceNumber
				and procedure_count < sequenceNumber;

			-- decrement and loop
			SET sequenceNumber = sequenceNumber - 1;
		END WHILE;
	END;

	-- CDS and CDS Tail (left join, get all records including where tail record is null)
    -- CDS must be done before PROCE, as PROCE refers back to CDS to avoid duplicates
	insert into procedure_target
	select
		_exchange_id as exchange_id,
		concat('CDS-', cds.cds_unique_identifier, '-', cds.procedure_seq_nbr) as unique_id,
		if (cds.cds_update_type = 1, true, false) as is_delete,
		coalesce(tail.person_id, cds.lookup_person_id) as person_id,
		tail.encounter_id as encounter_id,
		coalesce(tail.responsible_hcp_personnel_id, cds.lookup_consultant_personnel_id) as performer_personnel_id,
		coalesce(cds.procedure_date, cds.cds_activity_date) as dt_performed,
		null as dt_ended, -- no end dates for these
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

	-- the above will have picked up CDS records where we don't have a patient, so delete them out of the target table
	delete from procedure_target
	where
		exchange_id = _exchange_id
		and person_id is null
		and is_delete = 0;

	-- Procedure and PROCE (left join to get all)
	insert into procedure_target
	select
		_exchange_id as exchange_id,
		concat('PROCE-', proce.procedure_id) as unique_id,
		if (proce.active_ind = 0, true, false) as is_delete,
		coalesce(proc.lookup_person_id, proce.lookup_person_id) as person_id,
		coalesce(proc.encounter_id, proce.encounter_id) as encounter_id,
		coalesce(proce.lookup_responsible_personnel_id, proc.lookup_consultant_personnel_id) as performer_personnel_id,  -- DAB-121 fix
		proce.procedure_dt_tm as dt_performed,
		null as dt_ended, -- no end dates for these
		proc.freetext_comment as free_text,
		proc.lookup_recorded_by_personnel_id as recorded_by_personnel_id,
		null as dt_recorded,  -- data not available
		coalesce(proc.proc_cd_type, proce.procedure_type) as procedure_type,
		coalesce(proc.proc_cd, proce.procedure_code) as procedure_code,
		coalesce(proc.proc_term, proce.procedure_term) as procedure_term,
		proce.procedure_seq_nbr as sequence_number,
		if (
		  proce.procedure_seq_nbr is null
			or proce.procedure_seq_nbr = 1
            or parent_proce.procedure_id is null,
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
    on proce.lookup_person_id = proc.lookup_person_id -- DAB-122 - should join on person ID, rather than encounter ID
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
        and proce.dt_received >= parent_proce.dt_received -- only join to parent proce that were added before, so we don't join to future ones if re-running data
	where
		proce.exchange_id = _exchange_id
		or (proc.exchange_id is not null and proc.exchange_id = _exchange_id); -- DAB-95 fix - we need to pick up procedure records that have changed w/o a PROCE change

	-- all procedures from CDS are also in PROCE, so we need to de-duplicate those
    -- DAB-115 - need to handle CDS files received at a later date to the PROCE file, and send
    -- through a is_delete = true rather than just delete from the target table
    update procedure_target target_proce
	inner join procedure_target target_cds
		on target_proce.person_id = target_cds.person_id
		and target_proce.encounter_id = target_cds.encounter_id
		and date(target_proce.dt_performed) = target_cds.dt_performed -- CDS procs don't have times
		and target_proce.procedure_code = target_cds.procedure_code
		and target_proce.unique_id != target_cds.unique_id
	set
		target_proce.is_delete = 1,
		target_proce.person_id = null,
		target_proce.encounter_id = null,
		target_proce.performer_personnel_id = null,
		target_proce.dt_performed = null,
		target_proce.dt_ended = null,
		target_proce.free_text = null,
		target_proce.recorded_by_personnel_id = null,
		target_proce.dt_recorded = null,
		target_proce.procedure_type = null,
		target_proce.procedure_code = null,
		target_proce.procedure_term = null,
		target_proce.sequence_number = null,
		target_proce.parent_procedure_unique_id = null,
		target_proce.qualifier = null,
		target_proce.location = null,
		target_proce.specialty = null,
		target_proce.audit_json = null
	where
		(target_proce.exchange_id = _exchange_id
			or target_cds.exchange_id = _exchange_id)
		and target_proce.unique_id like 'PROCE-%'
		and target_cds.unique_id like 'CDS-%';

	-- SURCC and SURCP
	insert into procedure_target
	select
		_exchange_id as exchange_id,
		concat('SURG-',cp.surgical_case_procedure_id) as unique_id,
		if (cp.active_ind = 0, true, false) as is_delete,
		cc.person_id as person_id, -- note this may be null if we're deleting a SURCP record - we won't have joined to a SURCC record
		cc.encounter_id as encounter_id,
		cp.surgeon_personnel_id as performer_personnel_id,
		coalesce(cp.dt_start, cc.dt_start) as dt_performed,
		coalesce(cp.dt_stop, cc.dt_stop) as dt_ended,
		if (
		  cp.wound_class_code > 0,
		  concat(cp.procedure_text, '. Wound class:', cp.wound_class_code),
		  cp.procedure_text
		) as free_text,
		null as recorded_by_personnel_id,  -- data not available
		null as dt_recorded,  -- data not available
		'CERNER' as procedure_type,
		cp.procedure_code as procedure_code,
		cp.lookup_procedure_code_term as procedure_term,
        if (
			cp.primary_procedure_indicator is null,
            null,
			if (
				cp.primary_procedure_indicator = 1,
                1,
                2
			)
		) as sequence_number,   -- DAB-101 fix
		if (cp.primary_procedure_indicator is null
			or cp.primary_procedure_indicator = 1
            or parent_cp.surgical_case_procedure_id is null,
		  null,
		  concat('SURG-', parent_cp.surgical_case_procedure_id)
		) as parent_procedure_unique_id, -- if sequence number > 1 then parent procedure ID is the same unique ID but with seq 1
		cp.modifier_text as qualifier,
		coalesce(cc.institution_code, cc.department_code, cc.surgical_area_code, cc.theatre_number_code) as location,
		cc.specialty_code as specialty,
		cp.audit_json
	from
		procedure_SURCP_latest cp
	left join procedure_SURCC_latest cc
		on cp.surgical_case_id = cc.surgical_case_id
	left join procedure_SURCP_latest parent_cp -- DAB-110 - need to join to parent SURCP record so we can work out the parent ID
		on cp.surgical_case_id = parent_cp.surgical_case_id
        and parent_cp.primary_procedure_indicator = 1
        and parent_cp.dt_start is not null -- sometimes there are multiple primaries, but only one is done (e.g. 76082621)
        and cp.primary_procedure_indicator = 0
        and cp.dt_received >= parent_cp.dt_received -- only join to parent proce that were added before, so we don't join to future ones if re-running data
	where
		(cp.exchange_id = _exchange_id
		  or (cc.exchange_id is not null and cc.exchange_id = _exchange_id)) -- DAB-103 fix - we need to pick up SURCC records that have changed w/o a SURCP change
		and (cp.active_ind = 0 -- DAB-104 added active_ind check to pick up deletes as they will always have null dt_starts
			or cp.dt_start is not null
            or cc.dt_start is not null) -- DAB-103 - check both CC and CP for non-null start
		and (cp.active_ind = 0
			or cp.procedure_code is not null); -- DAB-103 - exclude ones without procedure codes

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
		dt_ended,
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
		dt_ended = values(dt_ended),
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

DROP TEMPORARY TABLE procedure_cds_count_changed;

END$$
DELIMITER ;


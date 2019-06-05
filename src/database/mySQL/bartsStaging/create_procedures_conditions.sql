USE staging_barts;

DROP PROCEDURE IF EXISTS `process_condition_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_condition_staging_exchange`(
  IN _exchange_id char(36)
)
BEGIN

	-- work out if any CDS records now have fewer conditions than before
	DROP TEMPORARY TABLE IF EXISTS condition_cds_count_changed;

	CREATE TEMPORARY TABLE condition_cds_count_changed AS
	SELECT
		new_count.*,
		previous_count.condition_count AS old_count
	FROM condition_cds_count new_count
				 INNER JOIN condition_cds_count_latest previous_count
										ON new_count.cds_unique_identifier = previous_count.cds_unique_identifier
											AND new_count.sus_record_type = previous_count.sus_record_type
	WHERE
			new_count.exchange_id = _exchange_id
		AND new_count.condition_count < previous_count.condition_count;

	-- update CDS count latest table with new counts
	INSERT INTO condition_cds_count_latest
	SELECT
		exchange_id,
		dt_received,
		record_checksum,
		sus_record_type,
		cds_unique_identifier,
		condition_count,
		audit_json
	FROM
	  condition_cds_count
	WHERE
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
		exchange_id = VALUES(exchange_id),
		dt_received = VALUES(dt_received),
		record_checksum = VALUES(record_checksum),
		-- sus_record_type = VALUES(sus_record_type), -- part of primary key
		-- cds_unique_identifier = VALUES(cds_unique_identifier), -- part of primary key
		condition_count = VALUES(condition_count),
		audit_json = VALUES(audit_json);

	-- create helper table to get latest CDS records
	insert into condition_cds_latest
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
		diagnosis_icd_code,
		diagnosis_seq_nbr,
		primary_diagnosis_icd_code,
		lookup_diagnosis_icd_term,
		lookup_person_id,
		lookup_consultant_personnel_id,
		audit_json
	from
		condition_cds
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
  	diagnosis_icd_code = values(diagnosis_icd_code),
		-- diagnosis_seq_nbr = values(diagnosis_seq_nbr), -- part of primary key
		primary_diagnosis_icd_code = values(primary_diagnosis_icd_code),
		lookup_diagnosis_icd_term = values(lookup_diagnosis_icd_term),
		lookup_person_id = values(lookup_person_id),
		lookup_consultant_personnel_id = values(lookup_consultant_personnel_id),
		audit_json = values(audit_json);

	-- create helper table to get latest CDS tail records
	insert into condition_cds_tail_latest
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
		condition_cds_tail
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

	-- create helper table to get latest diagnosis records
	insert into condition_diagnosis_latest
	select
		exchange_id,
		dt_received,
		record_checksum,
		diagnosis_id,
		person_id,
		active_ind,
		mrn,
		encounter_id,
		diag_dt_tm,
		diag_type,
		diag_prnsl,
		vocab,
		diag_code,
		diag_term,
		diag_notes,
		classification,
		ranking,
		confirmation,
		axis,
		location,
		lookup_consultant_personnel_id,
		audit_json
	from
		condition_diagnosis
	where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
		exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		-- diagnosis_id = values(diagnosis_id), -- part of primary key
		person_id = values(person_id),
		active_ind = values(active_ind),
		mrn = values(mrn),
		encounter_id = values(encounter_id),
		diag_dt_tm = values(diag_dt_tm),
		diag_type = values(diag_type),
		diag_prnsl = values(diag_prnsl),
		vocab = values(vocab),
		diag_code = values(diag_code),
		diag_term = values(diag_term),
		diag_notes = values(diag_notes),
		classification = values(classification),
		ranking = values(ranking),
		confirmation = values(confirmation),
		axis = values(axis),
		location = values(location),
		lookup_consultant_personnel_id = values(lookup_consultant_personnel_id),
		audit_json = values(audit_json);


	-- create helper table to get latest DIAGN records
	insert into condition_DIAGN_latest
	select
		exchange_id,
		dt_received,
		record_checksum,
		diagnosis_id,
		active_ind,
		encounter_id,
		encounter_slice_id,
		diagnosis_dt_tm,
		diagnosis_code_type,
		diagnosis_code,
		diagnosis_term,
		diagnosis_notes,
		diagnosis_type_cd,
		diagnosis_seq_nbr,
		diag_personnel_id,
	  lookup_person_id,
		lookup_mrn,
		audit_json
	from
		condition_DIAGN
	where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
		exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		-- diagnosis_id = values(diagnosis_id), -- part of primary key
		active_ind = values(active_ind),
		encounter_id = values(encounter_id),
		encounter_slice_id = values(encounter_slice_id),
		diagnosis_dt_tm = values(diagnosis_dt_tm),
		diagnosis_code_type = values(diagnosis_code_type),
		diagnosis_code = values(diagnosis_code),
		diagnosis_term = values(diagnosis_term),
		diagnosis_notes = values(diagnosis_notes),
		diagnosis_type_cd  = values(diagnosis_type_cd),
		diagnosis_seq_nbr = values(diagnosis_seq_nbr),
		diag_personnel_id = values(diag_personnel_id),
		lookup_person_id = values(lookup_person_id),
		lookup_mrn = values(lookup_mrn),
		audit_json = values(audit_json);


	-- create helper table to get latest Problem records
	insert into condition_problem_latest
	select
		exchange_id,
		dt_received,
		record_checksum,
		problem_id,
		person_id,
		mrn,
		onset_date,
		updated_by,
		vocab,
		problem_code,
		problem_term,
		problem_txt,
		classification,
		confirmation,
		ranking,
		axis,
		problem_status,
		problem_status_date,
		location,
		lookup_consultant_personnel_id,
		audit_json
	from
	  condition_problem
	where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
		exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		-- problem_id = values(problem_id), -- part of primary key
		person_id = values(person_id),
		mrn = values(mrn),
		onset_date = values(onset_date),
		updated_by = values(updated_by),
		vocab = values(vocab),
		problem_code = values(problem_code),
		problem_term = values(problem_term),
		problem_txt = values(problem_txt),
		classification = values(classification),
		confirmation = values(confirmation),
		ranking = values(ranking),
		axis = values(axis),
		problem_status = values(problem_status),
		problem_status_date = values(problem_status_date),
		location = values(location),
		lookup_consultant_personnel_id = values(lookup_consultant_personnel_id),
		audit_json = values(audit_json);


	-- first, clear down condition_target for the exchange
	delete from condition_target where exchange_id = _exchange_id;

	-- generate deletes for any CDS diagnosis where the disgnosis count has gone down
	BEGIN -- need a begin because you need this to be able to declare a variable here
	DECLARE sequenceNumber int default 0;
	SET sequenceNumber = (SELECT MAX(old_count) FROM condition_cds_count_changed);

	WHILE (sequenceNumber > 0) DO

	INSERT INTO condition_target
	SELECT
		_exchange_id as exchange_id,
		concat('CDS-', cds_count_changed.cds_unique_identifier, '-', sequenceNumber) as unique_id,
		true as is_delete,
		null as person_id,
		null as encounter_id,
		null as performer_personnel_id,
		null as dt_performed,
		null as condition_code_type,
		null as condition_code,
		null as condition_term,
		null as condition_type,
		null as free_text,
		null as sequence_number,
		null as parent_condition_unique_id,
		null as classification,
		null as confirmation,
		null as problem_status,
		null as problem_status_date,
		null as ranking,
		null as axis,
		null as location,
		cds_count_changed.audit_json as audit_json,
		null as is_confidential
	FROM
		condition_cds_count_changed cds_count_changed
	WHERE
			old_count >= sequenceNumber
		and condition_count < sequenceNumber;

	-- decrement and loop
	SET sequenceNumber = sequenceNumber - 1;
	END WHILE;
	END;


	-- CDS and CDS Tail (left join, get all records including where tail record is null)
	-- CDS must be done before DIAGN, as DIAGN refers back to CDS to avoid duplicates
	insert into condition_target
	select
		_exchange_id as exchange_id,
		concat('CDS-', cds.cds_unique_identifier, '-', cds.condition_seq_nbr) as unique_id,
		if (cds.cds_update_type = 1, true, false) as is_delete,
		coalesce(tail.person_id, cds.lookup_person_id) as person_id,
		tail.encounter_id as encounter_id,
		coalesce(tail.responsible_hcp_personnel_id, cds.lookup_consultant_personnel_id) as performer_personnel_id,
		cds.cds_activity_date as dt_performed,  -- NOTE - only cds activity date available
		'OPCS4' as condition_code_type,
		cds.diagnosis_icd_code as condition_code,
		cds.lookup_diagnosis_icd_term as condition_term,
		null as condition_type,	    -- data not available
		null as free_text,    			-- data not available
		cds.diagnosis_seq_nbr as sequence_number,
		if (
					cds.diagnosis_seq_nbr = 1,
					null,
					concat('CDS-', cds.cds_unique_identifier, '-', 1)
			) as parent_condition_unique_id, -- if sequence number > 1 then parent condition ID is the same unique ID but with seq 1
		null as classification,   -- data not available
		null as confirmation,     -- data not available
		null as problem_status,   -- data not available
		null as problem_status_date,  -- data not available
		null as ranking,					-- data not available
		null as axis,							-- data not available
		null as location,					-- data not available
		concat_ws('&', cds.audit_json, tail.audit_json) as audit_json,
		withheld as is_confidential
	from
		condition_cds_latest cds
	left join
		condition_cds_tail_latest tail
	on
	  cds.cds_unique_identifier = tail.cds_unique_identifier
	and
	  cds.sus_record_type = tail.sus_record_type
	where
		cds.exchange_id = _exchange_id;

	-- the above will have picked up CDS records where we don't have a patient, so delete them out of the target table
	delete from condition_target
	where
		exchange_id = _exchange_id
	and
	  person_id is null
	and
	  is_delete = 0;

	-- Diagnosis and DIAGN (left join to get all using the diagnosis_id)
	insert into condition_target
	select
		_exchange_id as exchange_id,
		concat('DIAGN-', diagn.diagnosis_id) as unique_id,
		if (diagn.active_ind = 0, true, false) as is_delete,
		coalesce(diag.person_id, diagn.lookup_person_id) as person_id,
		coalesce(diag.encounter_id, diagn.encounter_id) as encounter_id,
		coalesce(diagn.diag_personnel_id, diag.lookup_consultant_personnel_id) as performer_personnel_id,
		coalesce(diag.diag_dt_tm, diagn.diagnosis_dt_tm) as dt_performed,
		coalesce(diag.vocab, diagn.diagnosis_code_type) as condition_code_type,
		coalesce(diag.diag_code, diagn.diagnosis_code) as condition_code,
		coalesce(diag.diag_term, diagn.diagnosis_term) as condition_term,
		coalesce(diag.diag_type, diagn.diagnosis_type_cd) as condition_type,
		coalesce(diag.diag_notes, diagn.diagnosis_notes) as free_text,
		diagn.diagnosis_seq_nbr as sequence_number,
		if (
					diagn.diagnosis_seq_nbr is null
					or diagn.diagnosis_seq_nbr = 1
					or parent_diagn.diagnosis_id is null,
					null,
					concat('DIAGN-', parent_diagn.diagnosis_id)
			) as parent_condition_unique_id, -- if sequence number > 1 then parent condition ID is the same unique ID but with seq 1

		diag.classification as classification,
		diag.confirmation as confirmation,
		null as problem_status,  			-- data not available
		null as problem_status_date,  -- data not available
		diag.ranking as ranking,
		diag.axis as axis,
		diag.location as location,
		concat_ws('&', diagn.audit_json, diag.audit_json, parent_diagn.audit_json) as audit_json,
		null as is_confidential
	from
			condition_DIAGN_latest diagn
		left join
			condition_diagnosis_latest diag
		on
		  diagn.diagnosis_id = diag.diagnosis_id
		left join
			condition_DIAGN_latest parent_diagn
		on parent_diagn.lookup_person_id = diagn.lookup_person_id
			and parent_diagn.encounter_id = diagn.encounter_id
			and parent_diagn.encounter_slice_id = diagn.encounter_slice_id
			and parent_diagn.diagnosis_seq_nbr = 1
			and diagn.diagnosis_seq_nbr is not null
			and diagn.diagnosis_seq_nbr > 1
			and diagn.dt_received >= parent_diagn.dt_received -- only join to parent diagn that were added before, so we don't join to future ones if re-running data
	where
			diagn.exchange_id = _exchange_id
		 or (diag.exchange_id is not null and diag.exchange_id = _exchange_id); -- see DAB-95 fix for procedures - we need to pick up diagnosis records that have changed w/o a DIAGN change

	-- all diagnosis from CDS are also in DIAGN, so we need to de-duplicate those
	-- also need to handle CDS files received at a later date (see DAB-115) to the DIAGN file, and send
	-- through a is_delete = true rather than just delete from the target table
	DROP TEMPORARY TABLE IF EXISTS condition_diag_duplicates;

	CREATE TABLE condition_diag_duplicates as
	SELECT
		target_cond.unique_id,
		min(target_cds.audit_json) as cds_audit_json    -- group by and min(..) so that we just get ONE cds match per DIAGN
	FROM
		condition_target target_cond
			INNER JOIN condition_target target_cds
								 on target_cond.person_id = target_cds.person_id
									 and date(target_cond.dt_performed) = target_cds.dt_performed -- CDS diags don't have times
									 and target_cond.condition_code = target_cds.condition_code
									 and target_cond.unique_id != target_cds.unique_id
	WHERE
		(target_cond.exchange_id = _exchange_id
			or target_cds.exchange_id = _exchange_id)
		and target_cond.unique_id like 'DIAGN-%'
		and target_cds.unique_id like 'CDS-%'
	GROUP BY target_cond.unique_id;

	-- remove anything out of our target table for our current exchange
	DELETE target_cond
	FROM
		condition_target target_cond
			INNER JOIN
		condition_diag_duplicates duplicates
		ON duplicates.unique_id = target_cond.unique_id
	WHERE target_cond.exchange_id = _exchange_id;

	-- insert a "delete" for any duplicate with today's exchange ID
	INSERT INTO condition_target
	SELECT
		_exchange_id,
		duplicates.unique_id,
		1 as is_delete,
		null as person_id,
		null as encounter_id,
		null as performer_personnel_id,
		null as dt_performed,
		null as condition_code_type,
		null as condition_code,
		null as condition_term,
		null as condition_type,
		null as free_text,
		null as sequence_number,
		null as parent_condition_unique_id,
		null as classification,
		null as confirmation,
		null as problem_status,
		null as problem_status_date,
		null as ranking,
		null as axis,
	  null as location,
		cds_audit_json as audit_json,
		null as is_confidential
	FROM
		condition_diag_duplicates duplicates;

	-- Problems
	insert into condition_target
	select
		_exchange_id as exchange_id,
		concat('PROB-', p.problem_id) as unique_id,
		false as is_delete,    		-- no other data received for problems except status, which is handled in the transform
		p.person_id as person_id,
		null as encounter_id,			-- no encounter date for problems
		p.lookup_consultant_personnel_id as performer_personnel_id,
	  p.onset_dt_tm as dt_performed,
	  p.vocab as condition_code_type,
	  p.problem_code as condition_code,
	  p.problem_term as condition_term,
	  null as condition_type,   					-- data not available
	  p.problem_txt as free_text,
		1 as sequence_number,								-- set Problems with sequence_number = 1
		null as parent_condition_unique_id,	-- data not available
		p.classification as classification,
	  p.confirmation as confirmation,
	  p.problem_status as problem_status,
		p.problem_status_date as problem_status_date,
		p.ranking as ranking,
	  p.axis as axis,
	  p.location as location,
		concat_ws('&', p.audit_json) as audit_json,
		null as is_confidential
	from
		condition_problem p
	where
		p.exchange_id = _exchange_id;


	-- carry over to the target_latest table so we can see the latest state of everything
	INSERT INTO condition_target_latest
	SELECT
		exchange_id,
		unique_id,
		is_delete,
		person_id,
		encounter_id,
		performer_personnel_id,
		dt_performed,
		condition_code_type,
		condition_code,
		condition_term,
		condition_type,
		free_text,
		sequence_number,
		parent_condition_unique_id,
		classification,
		confirmation,
		problem_status,
		problem_status_date,
		ranking,
		axis,
		location,
		audit_json,
		is_confidential
	FROM
		condition_target
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
		condition_code_type = values(condition_code_type),
		condition_code = values(condition_code),
		condition_term = values(condition_term),
		condition_type = values(condition_type),
		free_text = values(free_text),
		sequence_number = values(sequence_number),
  	parent_condition_unique_id = values(parent_condition_unique_id),
		classification = values(classification),
		confirmation = values(confirmation),
		problem_status = values(problem_status),
		problem_status_date = values(problem_status_date),
	  ranking = valuse(ranking),
		axis = values(axis),
		location = values(location),
		audit_json = values(audit_json),
		is_confidential = values(is_confidential);


	DROP TEMPORARY TABLE IF EXISTS condition_cds_count_changed;
	DROP TEMPORARY TABLE IF EXISTS condition_diag_duplicates;

END$$
DELIMITER ;
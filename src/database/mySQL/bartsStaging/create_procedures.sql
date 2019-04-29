USE staging_barts;

DROP PROCEDURE IF EXISTS `process_procedure_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_procedure_staging_exchange`(
  IN _exchange_id varchar(36)
)
BEGIN

  -- first, clear down procedure_target for the exchange
  delete from procedure_target where exchange_id = _exchange_id;

  -- CDS and CDS Tail (left join, get all records)
  insert into procedure_target
  select
    _exchange_id as exchange_id,
    concat('CDS-',cds.cds_unique_identifier,'-',cds.procedure_seq_nbr) as unique_id,
    if (cds.cds_update_type = 1, true, false) as isDeleted,
    coalesce(tail.person_id, cds.lookup_person_id) as person_id,
    tail.encounter_id as encounter_id,
    coalesce(tail.responsible_hcp_personnel_id, cds.lookup_consultant_personnel_id) as performer_personnel_id,
    cds.procedure_date as dt_performed,
    null as free_text, -- comment?
    null as recorded_by_personnel_id, -- check this
    null as dt_recorded,  -- recorded date - where from?
    'OPCS4' as procedure_type,
    cds.procedure_opcs_code as procedure_code,
    cds.lookup_procedure_opcs_term as procedure_term,
    cds.procedure_seq_nbr as sequence_number,
    null as parent_procedure_unique_id,  -- whats this?
    null as qualifier,
    null as location, -- location?
    null as speciality, -- speciality?
    cds.audit_json
  from
    procedure_cds cds
      left join
    procedure_cds_tail tail on cds.cds_unique_identifier = tail.cds_unique_identifier
  where
      cds.exchange_id = _exchange_id or tail.exchange_id = _exchange_id;


  -- Procedure and PROCE (left join to get all)
  insert into procedure_target
  select
    _exchange_id as exchange_id,
    concat('PROCE-',proce.procedure_id,'-',proce.procedure_seq_nbr) as unique_id,
    if (proce.active_ind = 0, true, false) as isDeleted,
    coalesce(proc.person_id, proce.lookup_person_id) as person_id,
    coalesce(proc.encounter_id, proce.encounter_id) as encounter_id,
    proc.lookup_consultant_personnel_id as performer_personnel_id,
    proce.procedure_dt_tm as dt_performed,
    proc.freetext_comment as free_text,
    proc.lookup_recorded_by_personnel_id as recorded_by_personnel_id,
    null as dt_recorded,  -- recorded date - where from?
    coalesce(proc.proc_cd_type, proce.procedure_type) as procedure_type,
    coalesce(proc.proc_cd, proce.procedure_code) as procedure_code,
    coalesce(proc.proc_term, proce.procedure_term) as procedure_term,
    proce.procedure_seq_nbr as sequence_number,
    null as parent_procedure_unique_id,  -- whats this?
    null as qualifier,
    coalesce(proc.site, proc.ward) as location,
    null as speciality, -- speciality?
    proce.audit_json
  from
    procedure_PROCE proce
      left join
    procedure_procedure proc on proce.encounter_id = proc.encounter_id
      and
                                proce.procedure_dt_tm = proc.proc_dt_tm
      and
                                proce.procedure_code = proc.proc_cd
  where
      proce.exchange_id = _exchange_id or proc.exchange_id = _exchange_id;


  -- SURCC and SURCP
  insert into procedure_target
  select
    _exchange_id as exchange_id,
    concat('SURG-',cp.surgical_case_procedure_id) as unique_id,
    if (cp.active_ind = 0, true, false) as isDeleted,
    cc.person_id as person_id,
    cc.encounter_id as encounter_id,
    cp.surgeon_personnel_id as performer_personnel_id,
    cp.dt_start as dt_performed,
    if (cp.wound_class_code > 0, concat(cp.procedure_text,'. Wound class:',cp.wound_class_code),cp.procedure_text) as free_text,
    null as recorded_by_personnel_id,
    null as dt_recorded,  -- recorded date - where from?
    'CERNER' as procedure_type,
    cp.procedure_code as procedure_code,
    cp.lookup_procedure_code_term as procedure_term,
    cp.primary_procedure_indicator as sequence_number,   -- the only data available, either 0 or 1
    null as parent_procedure_unique_id,  -- whats this?
    cp.modifier_text as qualifier,
    coalesce(cc.institution_code, cc.department_code, cc.surgical_area_code, cc.theatre_number_code) as location,
    null as speciality, -- speciality?
    cp.audit_json
  from
    procedure_SURCP cp
      left join
    procedure_SURCC cc on cp.surgical_case_id = cc.surgical_case_id
  where
      cp.exchange_id = _exchange_id or cc.exchange_id = _exchange_id;


END$$
DELIMITER ;
USE staging_barts;

DROP PROCEDURE IF EXISTS `process_clinical_events_staging_exchange`;

DELIMITER $$
CREATE PROCEDURE `process_clinical_events_staging_exchange`(
  IN _exchange_id char(36)
)
BEGIN
	-- create helper table to get latest procedure records
    insert into clinical_event_latest
    select
        exchange_id,
		dt_received,
		record_checksum,
		event_id,
		active_ind,
		person_id,
		encounter_id,
		order_id,
		parent_event_id,
		event_cd,
		lookup_event_code,
		lookup_event_term,
		event_start_dt_tm,
		event_end_dt_tm,
		clinically_significant_dt_tm,
		event_class_cd,
		lookup_event_class,
		event_result_status_cd,
		lookup_event_result_status,
		event_result_txt,
		event_result_nbr,
		processed_numeric_result,
		comparator,
		event_result_dt,
		normalcy_cd,
		lookup_normalcy_code,
		normal_range_low_txt,
		normal_range_low_value,
		normal_range_high_txt,
		normal_range_high_value,
		event_performed_dt_tm,
		event_performed_prsnl_id,
		event_tag,
		event_title_txt,
		event_result_units_cd,
		lookup_result_units_code,
		record_status_cd,
		lookup_record_status_code,
		lookup_mrn,
		audit_json
    from
		clinical_event
	where
		exchange_id = _exchange_id
	ON DUPLICATE KEY UPDATE
        exchange_id = values(exchange_id),
		dt_received = values(dt_received),
		record_checksum = values(record_checksum),
		event_id = values(event_id),
		active_ind = values(active_ind),
		person_id = values(person_id),
		encounter_id = values(encounter_id),
		order_id = values(order_id),
		parent_event_id = values(parent_event_id),
		event_cd = values(event_cd),
		lookup_event_code = values(lookup_event_code),
		lookup_event_term = values(lookup_event_term),
		event_start_dt_tm = values(event_start_dt_tm),
		event_end_dt_tm = values(event_end_dt_tm),
		clinically_significant_dt_tm = values(clinically_significant_dt_tm),
		event_class_cd = values(event_class_cd),
		lookup_event_class = values(lookup_event_class),
		event_result_status_cd = values(event_result_status_cd),
		lookup_event_result_status = values(lookup_event_result_status),
		event_result_txt = values(event_result_txt),
		event_result_nbr = values(event_result_nbr),
		processed_numeric_result = values(processed_numeric_result),
		comparator = values(comparator),
		event_result_dt = values(event_result_dt),
		normalcy_cd = values(normalcy_cd),
		lookup_normalcy_code = values(lookup_normalcy_code),
		normal_range_low_txt = values(normal_range_low_txt),
		normal_range_low_value = values(normal_range_low_value),
		normal_range_high_txt = values(normal_range_high_txt),
		normal_range_high_value = values(normal_range_high_value),
		event_performed_dt_tm = values(event_performed_dt_tm),
		event_performed_prsnl_id = values(event_performed_prsnl_id),
		event_tag = values(event_tag),
		event_title_txt = values(event_title_txt),
		event_result_units_cd = values(event_result_units_cd),
		lookup_result_units_code = values(lookup_result_units_code),
		record_status_cd = values(record_status_cd),
		lookup_record_status_code = values(lookup_record_status_code),
		lookup_mrn = values(lookup_mrn),
		audit_json = values(audit_json);

	-- first, clear down procedure_target for the exchange
	delete from clinical_event_target where exchange_id = _exchange_id;

	-- Procedure and PROCE (left join to get all)
	insert into clinical_event_target
	select
		_exchange_id as exchange_id,
		concat('CLEVE-', cleve.event_id) as unique_id,
		if (cleve.active_ind = 0, true, false) as is_delete,
        cleve.event_id as event_id,
		cleve.person_id as person_id,
		cleve.encounter_id as encounter_id,
        cleve.order_id as order_id,
        cleve.parent_event_id as parent_event_id,
		cleve.event_cd as event_cd,
		cleve.lookup_event_code as lookup_event_code,
		cleve.lookup_event_term as lookup_event_term,
		cleve.event_start_dt_tm as event_start_dt_tm,
		cleve.event_end_dt_tm as event_end_dt_tm, 
		cleve.clinically_significant_dt_tm as clinically_significant_dt_tm,
		cleve.event_class_cd as event_class_cd,
		cleve.lookup_event_class as lookup_event_class,
		cleve.event_result_status_cd as event_result_status_cd,
		cleve.lookup_event_result_status as lookup_event_result_status, 
		cleve.event_result_txt as event_result_txt,
		cleve.event_result_nbr as event_result_nbr,
		cleve.processed_numeric_result as processed_numeric_result,
		cleve.comparator as comparator,
		cleve.event_result_dt as event_result_dt,
		cleve.normalcy_cd as normalcy_cd,
		cleve.lookup_normalcy_code as lookup_normalcy_code,
		cleve.normal_range_low_txt as normal_range_low_txt,
		cleve.normal_range_low_value as normal_range_low_value,
		cleve.normal_range_high_txt as normal_range_high_txt,
		cleve.normal_range_high_value as normal_range_high_value,
		cleve.event_performed_dt_tm as event_performed_dt_tm,
		cleve.event_performed_prsnl_id as event_performed_prsnl_id,
		cleve.event_tag as event_tag,
		cleve.event_title_txt as event_title_txt,
		cleve.event_result_units_cd as event_result_units_cd,
		cleve.lookup_result_units_code as lookup_result_units_code,
		cleve.record_status_cd as record_status_cd,
		cleve.lookup_record_status_code as lookup_record_status_code,
		cleve.lookup_mrn as lookup_mrn,
		cleve.audit_json as audit_json,
		null as is_confidential
	from
		clinical_event_latest cleve
	where
		cleve.exchange_id = _exchange_id;


END$$
DELIMITER ;

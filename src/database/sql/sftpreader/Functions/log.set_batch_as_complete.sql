
create or replace function log.set_batch_as_complete
(
	_batch_id integer,
	_sequence_number integer
)
returns void
as $$

	update log.batch
	set
		sequence_number = _sequence_number,
		is_complete = true,
		complete_date = date_trunc('second', now()::timestamp)
	where batch_id = _batch_id
	and is_complete = false;
	
$$ language sql;

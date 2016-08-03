
create or replace function sftpreader.set_batch_as_complete
(
	_batch_id integer,
	_sequence_number integer
)
returns void
as $$

	update sftpreader.batch
	set
		sequence_number = _sequence_number,
		is_complete = true,
		complete_date = now()
	where batch_id = _batch_id
	and is_complete = false;

$$ language sql;

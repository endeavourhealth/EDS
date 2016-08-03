
create or replace function sftpreader.set_batch_as_notified
(
	_batch_id integer
)
returns void
as $$

	update sftpreader.batch
	set
		have_notified = true,
		notification_date = now()
	where batch_id = _batch_id
	and have_notified = false;

$$ language sql;

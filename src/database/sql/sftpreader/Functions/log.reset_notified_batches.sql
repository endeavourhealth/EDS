
create or replace function log.reset_notified_batches
()
returns void
as $$

	update log.batch_split
	set
		have_notified = false, 
		notification_date = null;
	
$$ language sql;

create or replace function log.reset_all_content
()
returns void
as $$

	delete from log.unknown_file;
	delete from log.notification_message;
	delete from log.batch_split;
	delete from log.batch_file;
	delete from log.batch;
	
$$ language sql;

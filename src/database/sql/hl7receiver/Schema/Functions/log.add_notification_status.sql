
create or replace function log.add_notification_status
(
	_message_id integer
)
returns void
as $$
begin

	insert into log.message_notification_status
	(
		message_id
	)
	values
	(
		_message_id
	);
	
end;
$$ language plpgsql;

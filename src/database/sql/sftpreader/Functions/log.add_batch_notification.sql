
create or replace function log.add_batch_notification
(
	_batch_id integer,
	_batch_split_id integer,
	_instance_id varchar,
	_message_uuid uuid,
	_outbound_message varchar,
	_inbound_message varchar,
	_was_success boolean,
	_error_text varchar
)
returns void
as $$
declare
	_timestamp timestamp;
begin

	_timestamp = date_trunc('second', now()::timestamp);
	
	insert into log.notification_message
	(
		batch_id,
		batch_split_id,
		instance_id,
		message_uuid,
		timestamp,
		outbound,
		inbound,
		was_success,
		error_text
	)
	values
	(
		_batch_id,
		_batch_split_id,
		_instance_id,
		_message_uuid,
		_timestamp,
		_outbound_message,
		_inbound_message,
		_was_success,
		_error_text
	);

	if (_was_success)
	then

		if exists
		(
			select *
			from log.batch_split
			where batch_split_id = _batch_split_id
			and have_notified = true
		)
		then
			raise exception 'batch has already been notified';
		end if;

		update log.batch_split
		set
			have_notified = true,
			notification_date = _timestamp
		where batch_split_id = _batch_split_id
		and have_notified = false;

	end if;

end;
$$ language plpgsql;


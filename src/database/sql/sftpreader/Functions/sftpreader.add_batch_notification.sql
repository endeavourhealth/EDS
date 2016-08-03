
create or replace function sftpreader.add_batch_notification
(
	_batch_id integer,
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
	
	insert into sftpreader.notification_message
	(
		batch_id,
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
			from sftpreader.batch
			where batch_id = _batch_id
			and have_notified = true
		)
		then
			raise exception 'batch has already been notified';
		end if;

		update sftpreader.batch
		set
			have_notified = true,
			notification_date = _timestamp
		where batch_id = _batch_id
		and have_notified = false;

	end if;

end;
$$ language plpgsql;


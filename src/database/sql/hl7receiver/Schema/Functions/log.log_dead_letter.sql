
create or replace function log.log_dead_letter
(
	_connection_id integer,
	_local_port integer,
	_remote_host varchar(100),
	_remote_port integer,
	_channel_id integer,
	_sending_application varchar(100),
	_sending_facility varchar(100),
	_recipient_application varchar(100),
	_recipient_facility varchar(100),
	_inbound_payload text,
	_outbound_payload text
)
returns integer
as $$
declare 
	_dead_letter_id integer;
begin

	insert into log.dead_letter
	(
		log_date,
		connection_id,
		local_port,
		remote_host,
		remote_port,
		channel_id,
		sending_application,
		sending_facility,
		receipient_application,
		receipient_facility,
		inbound_payload,
		outbound_payload
	)
	values
	(
		now(),
		_connection_id,
		_local_port,
		_remote_host,
		_channel_id,
		_sending_application,
		_sending_facility,
		_recipient_application,
		_recipient_facility,
		_inbound_payload,
		_outbound_payload
	)
	returning dead_letter_id into _dead_letter_id;
	
	return _dead_letter_id;
	
end;
$$ language plpgsql;

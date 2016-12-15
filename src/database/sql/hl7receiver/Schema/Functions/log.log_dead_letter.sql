
create or replace function log.log_dead_letter
(
	_channel_id integer,
	_connection_id integer,
	_local_port integer,
	_remote_host varchar(100),
	_remote_port integer,
	_sending_application varchar(100),
	_sending_facility varchar(100),
	_receiving_application varchar(100),
	_receiving_facility varchar(100),
	_message_control_id varchar(100),
	_inbound_message_type varchar(100),
	_inbound_payload text,
	_outbound_message_type varchar(100),
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
		channel_id,
		connection_id,
		local_port,
		remote_host,
		remote_port,
		sending_application,
		sending_facility,
		receiving_application,
		receiving_facility,
		message_control_id,
		inbound_message_type,
		inbound_payload,
		outbound_message_type,
		outbound_payload
	)
	values
	(
		now(),
		_channel_id,
		_connection_id,
		_local_port,
		_remote_host,
		_remote_port,
		_sending_application,
		_sending_facility,
		_receiving_application,
		_receiving_facility,
		_message_control_id,
		_inbound_message_type,
		_inbound_payload,
		_outbound_message_type,
		_outbound_payload
	)
	returning dead_letter_id into _dead_letter_id;
	
	return _dead_letter_id;
	
end;
$$ language plpgsql;

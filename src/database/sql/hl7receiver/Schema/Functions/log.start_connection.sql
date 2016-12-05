
create or replace function log.start_connection
(
	_instance_name varchar(100),
	_channel_name varchar(100),
	_host varchar(100)	
)
returns integer
as $$
declare
	_instance_id integer;
	_channel_id integer;
	_connection_id integer;
begin

	select
		instance_id into _instance_id
	from configuration.instance
	where instance_name = _instance_name;

	select
		channel_id into _channel_id
	from configuration.channel
	where channel_name = _channel_name;

	insert into log.connection
	(
		instance_id,
		channel_id,
		host,
		connected
	)
	values
	(
		_instance_id,
		_channel_id,
		_host,
		now()
	)
	returning connection_id into _connection_id;
	
	return _connection_id;
	
end;
$$ language plpgsql;

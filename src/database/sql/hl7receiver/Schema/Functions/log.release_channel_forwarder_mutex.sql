
create or replace function log.release_channel_forwarder_mutex
(
	_channel_id integer,
	_instance_id integer
)
returns void
as $$
begin

	lock table log.channel_forwarder_mutex in access exclusive mode;

	delete from log.channel_forwarder_mutex
	where channel_id = _channel_id
	and instance_id = _instance_id;
	
end;
$$ language plpgsql;


create or replace function log.claim_channel_forwarder_mutex
(
	_channel_id integer,
	_instance_id integer
)
returns boolean
as $$
declare
	_channel_claimed boolean;
	_current_instance_id integer;
	_last_heartbeat_date timestamp;
begin

	_channel_claimed = false;

	lock table log.channel_forwarder in access exclusive mode;

	--
	-- get the last channel forwarder for this channel
	--
	select
		instance_id, heartbeat_date into _current_instance_id, _last_heartbeat_date
	from log.channel_forwarder
	where channel_id = _channel_id
	order by heartbeat_date desc
	limit 1;

	if (_current_instance_id = _instance_id)
	then
		--
		-- if its on our instance, update the heartbeat
		--
		update log.channel_forwarder
		set heartbeat_date = now()
		where channel_id = _channel_id
		and instance_id = _instance_id
		and heartbeat_date = _last_heartbeat_date;
		
		_channel_claimed = true;
	else
		--
		-- if its not on our instance
		--
		if ((_current_instance_id is null) or (_current_instance_id != _instance_id and _last_heartbeat_date <= (now() - interval '5 minute')))
		then
			--
			-- if the channel has never been assigned an instance
			-- or if last heartbeat was more than 5 minutes, take over the channel
			--
			insert into log.channel_forwarder
			(
				channel_id,
				instance_id,
				heartbeat_date
			)
			values
			(
				_channel_id,
				_instance_id,
				now()
			);
		
			_channel_claimed = true;

		end if;
	end if;

	return _channel_claimed;
	
end;
$$ language plpgsql;

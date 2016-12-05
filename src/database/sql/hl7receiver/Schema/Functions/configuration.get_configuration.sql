
create or replace function configuration.get_configuration
(
	_instance_name varchar(100)
)
returns setof refcursor
as $$
declare
	configuration_instance refcursor;
	configuration_channel refcursor;
begin

	------------------------------------------------------
	configuration_instance = 'configuration_instance';

	open configuration_instance for
	select
		i.instance_id,
		i.instance_name,
		i.description
	from configuration.instance i
	where i.instance_name = _instance_name;
	
	return next configuration_instance;

	------------------------------------------------------
	configuration_channel = 'configuration_channel';

	open configuration_channel for
	select 
		c.channel_id,
		c.channel_name,
		c.port_number,
		c.is_active,
		c.use_tls,
		c.remote_application,
		c.remote_facility,
		c.local_application,
		c.local_facility,
		c.port_number,
		c.use_acks,
		c.notes
	from configuration.channel c;
	
	return next configuration_channel;
	
end;
$$ language plpgsql;


create or replace function configuration.get_configuration
(
	_instance_id varchar(100)
)
returns setof refcursor
as $$
declare
	configuration_instance refcursor;
	configuration_port refcursor;
	configuration_channel refcursor;
begin

	------------------------------------------------------
	configuration_instance = 'configuration_instance';

	open configuration_instance for
	select
		i.instance_id,
		i.description
	from configuration.instance i
	where i.instance_id = _instance_id;
	
	return next configuration_instance;

	------------------------------------------------------
	configuration_port = 'configuration_port';
	
	open configuration_port for
	select
		p.port_number,
		p.notes
	from configuration.port p;
	
	return next configuration_port;
		
	------------------------------------------------------
	configuration_channel = 'configuration_channel';

	open configuration_channel for
	select 
		c.channel_id,
		c.channel_name,
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


create or replace function configuration.get_configuration
(
	_instance_name varchar(100)
)
returns setof refcursor
as $$
declare
	configuration_instance refcursor;
	configuration_channel refcursor;
	configuration_channel_message_type refcursor;
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
		c.sending_application,
		c.sending_facility,
		c.receiving_application,
		c.receiving_facility,
		c.port_number,
		c.notes
	from configuration.channel c;
	
	return next configuration_channel;
	
	------------------------------------------------------
	configuration_channel_message_type = 'configuration_channel_message_type';

	open configuration_channel_message_type for
	select
		t.channel_id,
		t.message_type,
		t.is_allowed		
	from configuration.channel_message_type t;
	
	return next configuration_channel_message_type;
	
end;
$$ language plpgsql;

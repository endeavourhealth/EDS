
create or replace function configuration.add_configuration_kvp
(
	_instance_id varchar,
	_key varchar,
	_value varchar
)
returns void
as $$
begin
	
	insert into configuration.configuration_kvp
	(
		instance_id,
		key,
		value
	)
	values
	(
		_instance_id,
		_key,
		_value
	);

end;
$$ language plpgsql;



create or replace function configuration.get_configuration_kvp
(
	_instance_id varchar(100)
)
returns table
(
	key varchar,
	value varchar
)
as $$

	select
		kvp.key,
		kvp.value
	from configuration.configuration_kvp kvp
	where kvp.instance_id = _instance_id;

$$ language sql;

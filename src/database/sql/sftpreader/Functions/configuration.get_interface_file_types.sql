
create or replace function configuration.get_interface_file_types
(
	_instance_id varchar
)
returns table
(
	file_type_identifier varchar
)
as $$

	select
		ift.file_type_identifier
	from configuration.configuration c
	inner join configuration.interface_type it on c.interface_type_id = it.interface_type_id
	inner join configuration.interface_file_type ift on ift.interface_type_id = it.interface_type_id
	where c.instance_id = _instance_id

$$ language sql;
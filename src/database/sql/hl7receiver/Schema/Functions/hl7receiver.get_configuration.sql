
create or replace function hl7receiver.get_configuration
(
	_instance_id varchar(100)
)
returns table
(
	instance_id varchar,
	instance_description varchar
)
as $$

	select
		i.instance_id,
		i.description as instance_description	from hl7receiver.instance i
	where i.instance_id = _instance_id;

$$ language sql;


create or replace function sftpreader.is_file_type_identifier_valid
(
	_instance_id varchar,
	_file_type_identifier varchar
)
returns boolean
as $$
declare 
	_is_valid boolean;
begin

	_is_valid = false;

	if exists
	(
		select
			*	
		from sftpreader.configuration c
		inner join sftpreader.interface_type it on c.interface_type_id = it.interface_type_id
		inner join sftpreader.interface_file_type ift on ift.interface_type_id = it.interface_type_id
		where c.instance_id = _instance_id
		and file_type_identifier = _file_type_identifier
	)
	then
		_is_valid = true;
	end if;

	return _is_valid;	

end;
$$ language plpgsql;

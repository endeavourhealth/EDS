
create or replace function sftpreader.is_batch_file_type_valid
(
	_instance_id varchar,
	_remote_file_type_identifier varchar
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
		inner join sftpreader.batch_type bt on c.batch_type_id = bt.batch_type_id
		inner join sftpreader.batch_file_type bft on bft.batch_type_id = bt.batch_type_id
		where c.instance_id = _instance_id
		and remote_file_type_identifier = _remote_file_type_identifier
	)
	then
		_is_valid = true;
	end if;

	return _is_valid;	

end;
$$ language plpgsql;

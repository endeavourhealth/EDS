
create or replace function configuration.add_emis_organisation_map
(
    _guid varchar,
    _name varchar,
    _ods_code varchar
)
returns void as
$$

	delete from configuration.emis_organisation_map
	where guid = _guid;

	insert into configuration.emis_organisation_map
	(
		guid,
		name,
		ods_code
	)
	values
	(
		_guid,
		_name,
		_ods_code
	);

$$ language sql;

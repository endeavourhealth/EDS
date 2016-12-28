
create or replace function sftpreader.add_emis_organisation_map
(
    _guid varchar,
    _name varchar,
    _ods_code varchar
)
returns void as
$$

	delete from sftpreader.emis_organisation_map
	where guid = _guid;

	insert into sftpreader.emis_organisation_map
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

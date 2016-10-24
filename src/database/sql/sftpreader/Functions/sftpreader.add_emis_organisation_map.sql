-- Function: sftpreader.add_emis_organisation_map(character varying, character varying, character varying)

-- DROP FUNCTION sftpreader.add_emis_organisation_map(character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION sftpreader.add_emis_organisation_map(
    _guid character varying,
    _name character varying,
    _ods_code character varying)
  RETURNS void AS
$BODY$


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

$BODY$
  LANGUAGE sql VOLATILE
  COST 100;
ALTER FUNCTION sftpreader.add_emis_organisation_map(character varying, character varying, character varying)
  OWNER TO postgres;

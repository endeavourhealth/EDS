-- Function: sftpreader.get_emis_organisation_map(character varying)

-- DROP FUNCTION sftpreader.get_emis_organisation_map(character varying);

CREATE OR REPLACE FUNCTION sftpreader.get_emis_organisation_map(IN _guid character varying)
  RETURNS TABLE(guid character varying, name character varying, ods_code character varying) AS
$BODY$

	select guid,
		name,
		ods_code
	from sftpreader.emis_organisation_map
	where guid = _guid;

$BODY$
  LANGUAGE sql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION sftpreader.get_emis_organisation_map(character varying)
  OWNER TO postgres;

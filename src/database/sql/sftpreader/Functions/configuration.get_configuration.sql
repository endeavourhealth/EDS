
create or replace function configuration.get_configuration
(
	_instance_id varchar(100)
)
returns table
(
	instance_id varchar,
	instance_description varchar,
	interface_type_name varchar,
	poll_frequency_seconds integer,
	local_root_path varchar,
	hostname varchar,
	port integer,
	remote_path varchar,
	username varchar,
	client_private_key varchar,
	client_private_key_password varchar,
	host_public_key varchar,
	pgp_file_extension_filter varchar,
	pgp_sender_public_key varchar,
	pgp_recipient_public_key varchar,
	pgp_recipient_private_key varchar,
	pgp_recipient_private_key_password varchar,
	eds_url varchar,
	eds_service_identifier varchar,
	software_name varchar,
	software_version varchar,
	envelope_content_type varchar,
	use_keycloak boolean,
	keycloak_token_uri varchar,
	keycloak_realm varchar,
	keycloak_username varchar,
	keycloak_password varchar,
	keycloak_clientid varchar
)
as $$

	select
		i.instance_id,
		i.description as instance_description,
		it.interface_type_name,
		c.poll_frequency_seconds,	
		c.local_root_path,
		cs.hostname,
		cs.port,
		cs.remote_path,
		cs.username,
		cs.client_private_key,
		cs.client_private_key_password,
		cs.host_public_key,
		cp.file_extension_filter as pgp_file_extension_filter,
		cp.sender_public_key as pgp_sender_public_key,
		cp.recipient_public_key as pgp_recipient_public_key,
		cp.recipient_private_key as pgp_recipient_private_key,
		cp.recipient_private_key_password as pgp_private_key_password,
		ce.eds_url,
		ce.eds_service_identifier,
		ce.software_name,
		ce.software_version,
		ce.envelope_content_type,
		ce.use_keycloak,
		ce.keycloak_token_uri,
		ce.keycloak_realm,
		ce.keycloak_username,
		ce.keycloak_password,
		ce.keycloak_clientid
	from configuration.instance i
	inner join configuration.configuration c on i.instance_id = c.instance_id
	inner join configuration.interface_type it on c.interface_type_id = it.interface_type_id
	inner join configuration.configuration_sftp cs on c.instance_id = cs.instance_id
	inner join configuration.configuration_eds ce on c.instance_id = ce.instance_id
	left outer join configuration.configuration_pgp cp on c.instance_id = cp.instance_id
	where i.instance_id = _instance_id;

$$ language sql;

/*
	insert test data
*/

insert into sftpreader.instance
(
	instance_id
)
values
(
	'EMIS001'
);

insert into sftpreader.configuration
(
	instance_id,
	local_root_path,
	poll_frequency_seconds
)
values
(
	'EMIS001',
	'/Users/jonny/Code/Local/sftp',
	10
);

insert into sftpreader.configuration_sftp
(
	instance_id,
	hostname,
	port,
	remote_path,
	username,
	client_public_key,
	client_private_key,
	client_private_key_password,
	host_public_key
)
values
(
	'EMIS001',
	'endeavour-sftp.cloudapp.net',
	22,
	'/gpg',
	'test-endeavour',
	'test-ssh-client-public.pub',
	'test-ssh-client-private.key',
	'password',
	'test-ssh-client-public.pub'
);

insert into sftpreader.configuration_pgp
(
	instance_id,
	file_extension_filter,
	sender_public_key,
	recipient_public_key,
	recipient_private_key,
	recipient_private_key_password
)
values
(
	'EMIS001',
	'.gpg',
	'test-pgp-endeavour-public.asc',
	'test-pgp-endeavour-private.asc',
	'test-pgp-emis-public.asc',
	'password'
);

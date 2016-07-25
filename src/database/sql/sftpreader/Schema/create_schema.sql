/* 
	create schema
*/

create schema sftpreader;

create table sftpreader.instance
(
	instance_id varchar(100) not null,

	constraint sftpreader_instance_instanceid_pk primary key (instance_id),
	constraint sftpreader_instance_instanceid_ck check (char_length(trim(instance_id)) > 0)
);

create table sftpreader.configuration
(
	instance_id varchar(100) not null,
	poll_frequency_seconds integer not null,
	local_root_path varchar(1000) not null,

	constraint sftpreader_configuration_instanceid_pk primary key (instance_id),
	constraint sftpreader_configuration_instanceid_fk foreign key (instance_id) references sftpreader.instance (instance_id),
	constraint sftpreader_configuration_pollfrequencyseconds check (poll_frequency_seconds >= 0),
	constraint sftpreader_configuration_localrootpath_ck check (char_length(trim(local_root_path)) > 0)
);

create table sftpreader.configuration_sftp
(
	instance_id varchar(100) not null,
	hostname varchar(100) not null,
	port integer not null,
	remote_path varchar(1000) not null,
	username varchar(100) not null,
	client_public_key varchar(8000) not null,
	client_private_key varchar(8000) not null,
	client_private_key_password varchar(1000) not null,
	host_public_key varchar(8000) not null,

	constraint sftpreader_configurationsftp_instanceid_pk primary key (instance_id),
	constraint sftpreader_configurationsftp_instanceid_fk foreign key (instance_id) references sftpreader.instance (instance_id),
	constraint sftpreader_configurationsftp_port_ck check (port > 0),
	constraint sftpreader_configurationsftp_remotepath_ck check (char_length(trim(remote_path)) > 0),
	constraint sftpreader_configurationsftp_username_ck check (char_length(trim(username)) > 0)
);

create table sftpreader.configuration_pgp
(
	instance_id varchar(100) not null,
	file_extension_filter varchar(100) not null,
	sender_public_key varchar(8000) not null,
	recipient_public_key varchar(8000) not null,
	recipient_private_key varchar(8000) not null,
	recipient_private_key_password varchar(1000) not null,

	constraint sftpreader_configurationpgp_intsanceid_pk primary key (instance_id),
	constraint sftpreader_configurationpgp_instanceid_fk foreign key (instance_id) references sftpreader.instance (instance_id)
);
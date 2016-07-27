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
	client_public_key varchar not null,
	client_private_key varchar not null,
	client_private_key_password varchar(1000) not null,
	host_public_key varchar not null,

	constraint sftpreader_configurationsftp_instanceid_pk primary key (instance_id),
	constraint sftpreader_configurationsftp_instanceid_fk foreign key (instance_id) references sftpreader.configuration (instance_id),
	constraint sftpreader_configurationsftp_port_ck check (port > 0),
	constraint sftpreader_configurationsftp_remotepath_ck check (char_length(trim(remote_path)) > 0),
	constraint sftpreader_configurationsftp_username_ck check (char_length(trim(username)) > 0)
);

create table sftpreader.configuration_pgp
(
	instance_id varchar(100) not null,
	file_extension_filter varchar(100) not null,
	sender_public_key varchar not null,
	recipient_public_key varchar not null,
	recipient_private_key varchar not null,
	recipient_private_key_password varchar(1000) not null,

	constraint sftpreader_configurationpgp_intsanceid_pk primary key (instance_id),
	constraint sftpreader_configurationpgp_instanceid_fk foreign key (instance_id) references sftpreader.configuration (instance_id)
);

create table sftpreader.file_set
(
	file_set_id serial not null,
	instance_id varchar(100) not null,
	local_identifier varchar(500) not null,
	create_date timestamp not null,
	is_complete boolean not null default false,
	complete_date timestamp null,
	have_notified boolean not null default false,
	notification_date timestamp null,

	constraint sftpreader_fileset_filesetid_pk primary key (file_set_id),
	constraint sftpreader_fileset_instanceid_fk foreign key (instance_id) references sftpreader.instance (instance_id),
	constraint sftpreader_fileset_instanceid_filesetid_uq unique (instance_id, file_set_id),
	constraint sftpreader_fileset_instanceid_localidentifier_uq unique (instance_id, local_identifier),
	constraint sftpreader_fileset_localidentifier_ck check (char_length(trim(local_identifier)) > 0),
	constraint sftpreader_fileset_createdate_completedate_ck check ((complete_date is null) or (create_date <= complete_date)),
	constraint sftpreader_fileset_completedate_notificationdate_ck check ((complete_date is null or notification_date is null) or (complete_date <= notification_date)),
	constraint sftpreader_fileset_iscomplete_completedate_ck check ((is_complete and complete_date is not null) or ((not is_complete) and complete_date is null)),
	constraint sftpreader_fileset_havenotified_notificationdate_ck check ((have_notified and notification_date is not null) or ((not have_notified) and notification_date is null)),
	constraint sftpreader_fileset_iscomplete_havenotified_ck check (is_complete or (not have_notified))
);

create table sftpreader.file
(
	file_id serial not null,
	instance_id varchar(100) not null,
	file_set_id integer not null,
	file_name varchar(1000) not null,
	file_path varchar(1000) not null,
	file_size_bytes bigint not null,
	download_date timestamp not null,

	constraint sftpreader_file_fileid_pk primary key (file_id),
	constraint sftpreader_file_instanceid_filesetid_fk foreign key (instance_id, file_set_id) references sftpreader.file_set (instance_id, file_set_id),
	constraint sftpreader_file_instanceid_filename_uq unique (instance_id, file_name),
	constraint sftpreader_file_filename_ck check (char_length(trim(file_name)) > 0),
	constraint sftpreader_file_filesizebytes check (file_size_bytes >= 0)
);

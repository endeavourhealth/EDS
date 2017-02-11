/* 
	create schema
*/

create schema configuration;
create schema log;

create table configuration.instance
(
	instance_id varchar(100) not null,
	description varchar(1000) not null,

	constraint configuration_instance_instanceid_pk primary key (instance_id),
	constraint configuration_instance_instanceid_ck check (char_length(trim(instance_id)) > 0),
	constraint configuration_instance_description_uq unique (description),
	constraint configuration_instance_description_ck check (char_length(trim(description)) > 0)
);

create table configuration.interface_type
(
	interface_type_id integer not null,
	interface_type_name varchar(1000) not null,

	constraint configuration_interfacetype_interfacetypeid_pk primary key (interface_type_id),
	constraint configuration_interfacetype_interfacetypename_uq unique (interface_type_name),
	constraint configuration_interfacetype_interfacetypename_ck check (char_length(trim(interface_type_name)) > 0)
);

insert into configuration.interface_type
(
	interface_type_id,
	interface_type_name
)
values
(
	1,
	'EMIS-EXTRACT-SERVICE-5-1'
);

create table configuration.interface_file_type
(
	interface_type_id integer not null,
	file_type_identifier varchar(1000) not null,

	constraint configuration_interfacefiletype_interfacetypeid_filetypeidentifier_pk primary key (interface_type_id, file_type_identifier),
	constraint configuration_interfacefiletype_interfacetypeid_fk foreign key (interface_type_id) references configuration.interface_type (interface_type_id),
	constraint configuration_interfacefiletype_filetypeidentifier_ck check (char_length(trim(file_type_identifier)) > 0)
);

insert into configuration.interface_file_type
(
	interface_type_id,
	file_type_identifier
)
values
(1, 'Admin_Location'),
(1, 'Admin_Organisation'),
(1, 'Admin_OrganisationLocation'),
(1, 'Admin_Patient'),
(1, 'Admin_UserInRole'),
(1, 'Agreements_SharingOrganisation'),
(1, 'Appointment_Session'),
(1, 'Appointment_SessionUser'),
(1, 'Appointment_Slot'),
(1, 'Audit_RegistrationAudit'),
(1, 'Audit_PatientAudit'),
--
-- temporarily remove as test pack does not contain these files
--
--(1, 'PatientAdmin_ListEntry'),
--
(1, 'CareRecord_Consultation'),
(1, 'CareRecord_Diary'),
(1, 'CareRecord_Observation'),
(1, 'CareRecord_ObservationReferral'),
(1, 'CareRecord_Problem'),
(1, 'Coding_ClinicalCode'),
(1, 'Coding_DrugCode'),
(1, 'Prescribing_DrugRecord'),
(1, 'Prescribing_IssueRecord');

create table configuration.configuration
(
	instance_id varchar(100) not null,
	interface_type_id integer not null,
	poll_frequency_seconds integer not null,
	local_root_path varchar(1000) not null,

	constraint configuration_configuration_instanceid_pk primary key (instance_id),
	constraint configuration_configuration_instanceid_fk foreign key (instance_id) references configuration.instance (instance_id),
	constraint configuration_configuration_interfacetypeid_fk foreign key (interface_type_id) references configuration.interface_type (interface_type_id),
	constraint configuration_configuration_instanceid_interfacetypeid_uq unique (instance_id, interface_type_id),
	constraint configuration_configuration_pollfrequencyseconds_ck check (poll_frequency_seconds >= 0),
	constraint configuration_configuration_localrootpath_uq unique (local_root_path),
	constraint configuration_configuration_localrootpath_ck check (char_length(trim(local_root_path)) > 0)
);

create table configuration.configuration_sftp
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

	constraint configuration_configurationsftp_instanceid_pk primary key (instance_id),
	constraint configuration_configurationsftp_instanceid_fk foreign key (instance_id) references configuration.configuration (instance_id),
	constraint configuration_configurationsftp_port_ck check (port > 0),
	constraint configuration_configurationsftp_remotepath_ck check (char_length(trim(remote_path)) > 0),
	constraint configuration_configurationsftp_username_ck check (char_length(trim(username)) > 0)
);

create table configuration.configuration_pgp
(
	instance_id varchar(100) not null,
	file_extension_filter varchar(100) not null,
	sender_public_key varchar not null,
	recipient_public_key varchar not null,
	recipient_private_key varchar not null,
	recipient_private_key_password varchar(1000) not null,

	constraint configuration_configurationpgp_intsanceid_pk primary key (instance_id),
	constraint configuration_configurationpgp_instanceid_fk foreign key (instance_id) references configuration.configuration (instance_id)
);

create table configuration.configuration_eds
(
	instance_id varchar(100) not null,
	eds_url varchar(1000) not null,
	eds_service_identifier varchar(100) not null,
	software_name varchar(100) not null,
	software_version varchar(100) not null,
	envelope_content_type varchar(100) not null,
	use_keycloak boolean not null,
	keycloak_token_uri varchar(500) null,
	keycloak_realm varchar(100) null,
	keycloak_username varchar(100) null,
	keycloak_password varchar(100) null,
	keycloak_clientid varchar(100) null,

	constraint configuration_configurationeds_instanceid_pk primary key (instance_id),
	constraint configuration_configurationeds_instanceid_fk foreign key (instance_id) references configuration.configuration (instance_id),
	constraint configuration_configurationeds_edsurl_ck check (char_length(trim(eds_url)) > 0),
	constraint configuration_configurationeds_edsserviceidentifier_uq unique (eds_service_identifier),
	constraint configuration_configurationeds_edsserviceidentifier_ck check (char_length(trim(eds_service_identifier)) > 0),
	constraint configuration_configurationeds_softwarename_ck check (char_length(trim(software_name)) > 0),
	constraint configuration_configurationeds_softwareversion_ck check (char_length(trim(software_version)) > 0),
	constraint configuration_configurationeds_envelopecontenttype_ck check (char_length(trim(envelope_content_type)) > 0),
	constraint configuration_configurationeds_usekeycloak_keycloaktokenuri_keycloakrealm_keycloakusername_keycloakpassword_keycloakclientid_ck check ((not use_keycloak) or (keycloak_token_uri is not null and keycloak_realm is not null and keycloak_username is not null and keycloak_password is not null and keycloak_clientid is not null)),
	constraint configuration_configurationeds_keycloaktokenuri_ck check (keycloak_token_uri is null or (char_length(trim(keycloak_token_uri)) > 0)),
	constraint configuration_configurationeds_keycloakrealm_ck check (keycloak_realm is null or (char_length(trim(keycloak_realm)) > 0)),
	constraint configuration_configurationeds_keycloakusername_ck check (keycloak_username is null or (char_length(trim(keycloak_username)) > 0)),
	constraint configuration_configurationeds_keycloakpassword_ck check (keycloak_password is null or (char_length(trim(keycloak_password)) > 0)),
	constraint configuration_configurationeds_keycloakclientid_ck check (keycloak_clientid is null or (char_length(trim(keycloak_clientid)) > 0))
);	

create table configuration.configuration_kvp
(
	instance_id varchar(100) not null,
	key varchar(100) not null,
	value varchar(1000) not null,

	constraint configuration_configuration_instanceid_key_pk primary key (instance_id, key),
	constraint configuration_configuration_instanceid_fk foreign key (instance_id) references configuration.configuration (instance_id),
	constraint configuration_configuration_key_ck check (char_length(trim(key)) > 0)
);

create table configuration.emis_organisation_map
(
	guid varchar not null,
	name varchar not null,
	ods_code varchar not null,

	constraint configuration_emisorganisationmap_guid_pk primary key (guid)
);

create index configuration_emisorganisationmap_guid_ix
on configuration.emis_organisation_map
using btree
(
	guid collate pg_catalog."default"
);

create table log.batch
(
	batch_id serial not null,
	instance_id varchar(100) not null,
	interface_type_id integer not null,
	batch_identifier varchar(500) not null,
	local_relative_path varchar(1000) not null,
	insert_date timestamp not null default (date_trunc('second', now()::timestamp)),
	sequence_number integer null,
	is_complete boolean not null default false,
	complete_date timestamp null,

	constraint log_batch_filesetid_pk primary key (batch_id),
	constraint log_batch_instanceid_interfacetypeid_fk foreign key (instance_id, interface_type_id) references configuration.configuration (instance_id, interface_type_id),
	constraint log_batch_instanceid_batchid_uq unique (instance_id, batch_id),
	constraint log_batch_instanceid_batchidentifier_uq unique (instance_id, batch_identifier),
	constraint log_batch_batchidentifier_ck check (char_length(trim(batch_identifier)) > 0),
	constraint log_batch_insertdate_completedate_ck check ((complete_date is null) or (insert_date <= complete_date)),
	constraint log_batch_sequencenumber_ck check (sequence_number is null or (sequence_number > 0)),
	constraint log_batch_instanceid_sequencenumber_uq unique (instance_id, sequence_number),
	constraint log_batch_iscomplete_completedate_sequencenumber_ck check ((is_complete and complete_date is not null and sequence_number is not null) or ((not is_complete) and complete_date is null and sequence_number is null))
);

create table log.batch_file
(
	batch_file_id serial not null,
	batch_id integer not null,
	interface_type_id integer not null,
	file_type_identifier varchar(1000) not null,
	insert_date timestamp not null default (date_trunc('second', now()::timestamp)),
	filename varchar(1000) not null,
	remote_created_date timestamp not null,
	remote_size_bytes bigint not null,
	is_downloaded boolean not null default (false),
	download_date timestamp null,
	local_size_bytes bigint null,
	requires_decryption boolean not null,
	is_decrypted boolean null,
	decrypt_date timestamp null,
	decrypted_filename varchar(1000) null,
	decrypted_size_bytes bigint null,

	constraint log_batchfile_batchfileid_pk primary key (batch_file_id),
	constraint log_batchfile_batchid_fk foreign key (batch_id) references log.batch (batch_id),
	constraint log_batchfile_interfacetypeid_filetypeidentifier_fk foreign key (interface_type_id, file_type_identifier) references configuration.interface_file_type (interface_type_id, file_type_identifier),
	constraint log_batchfile_batchid_filetypeidentifier_uq unique (batch_id, file_type_identifier),
	constraint log_batchfile_batchid_filename_uq unique (batch_id, filename),
	constraint log_batchfile_filename_ck check (char_length(trim(filename)) > 0),
	constraint log_batchfile_remotesizebytes_ck check (remote_size_bytes >= 0),
	constraint log_batchfile_isdownloaded_downloaddate_localfilesizebytes_ck check ((is_downloaded and download_date is not null and local_size_bytes is not null) or ((not is_downloaded) and download_date is null and local_size_bytes is null)),
	constraint log_batchfile_requiresdecryption_isdecrypted_ck check (((not requires_decryption) and is_decrypted is null) or (requires_decryption and is_decrypted is not null)),
	constraint log_batchfile_isdecrypted_decryptdate_decryptedfilename_decryptedsizebytes_ck check ((is_decrypted is not null and is_decrypted and decrypt_date is not null and decrypted_filename is not null and decrypted_size_bytes is not null) or (((is_decrypted is null) or (not is_decrypted)) and decrypt_date is null and decrypted_filename is null and decrypted_size_bytes is null)),
	constraint log_batchfile_localsizebytes_ck check (local_size_bytes >= 0),
	constraint log_batchfile_isdownloaded_isdecrypted_ck check ((not is_decrypted) or is_downloaded),
	constraint log_batchfile_batchid_decryptedfilename_uq unique (batch_id, decrypted_filename),
	constraint log_batchfile_decryptedfilename_ck check (decrypted_filename is null or (char_length(trim(decrypted_filename)) > 0)),
	constraint log_batchfile_decryptedsizebytes_ck check (decrypted_size_bytes >= 0),
	constraint log_batchfile_insertdate_downloaddate_ck check (download_date is null or (insert_date <= download_date)),
	constraint log_batchfile_downloaddate_decryptdate_ck check ((download_date is null or decrypt_date is null) or (download_date <= decrypt_date))
);

create table log.batch_split
(
	batch_split_id serial not null,
	batch_id integer not null,
	instance_id varchar(100) not null,
	local_relative_path varchar(1000) not null,
	organisation_id varchar(100) not null,
	have_notified boolean not null default false,
	notification_date timestamp null,

	constraint log_batchsplit_batchsplitid_pk primary key (batch_split_id),
	constraint log_batchsplit_instanceid_interfacetypeid_fk foreign key (batch_id, instance_id) references log.batch (batch_id, instance_id),
	constraint log_batchsplit_instanceid_batchid_uq unique (instance_id, batch_id, batch_split_id)
);


create table log.unknown_file
(
	unknown_file_id serial not null, 
	instance_id varchar(100) not null,
	insert_date timestamp not null default (date_trunc('second', now()::timestamp)),
	filename varchar(1000) not null,
	remote_created_date timestamp not null,
	remote_size_bytes bigint not null,

	constraint log_unknownfile_unknownfileid_pk primary key (unknown_file_id),
	constraint log_unknownfile_instanceid_fk foreign key (instance_id) references configuration.configuration (instance_id),
	constraint log_unknownfile_filename_ck check (char_length(trim(filename)) > 0),
	constraint log_unknownfile_remotesizebytes_ck check (remote_size_bytes >= 0)
);

create table log.notification_message
(
	notification_message_id serial not null,
	batch_id integer not null,
	batch_split_id integer not null,
	instance_id varchar(100) not null,
	message_uuid uuid not null, 
	timestamp timestamp not null,
	outbound varchar not null,
	inbound varchar null,
	was_success boolean not null,
	error_text varchar null,

	constraint log_notificationmessage_notificationmessageid_pk primary key (notification_message_id),
	constraint log_notificationmessage_batchid_instanceid_fk foreign key (batch_id, batch_split_id, instance_id) references log.batch_split (batch_id, batch_split_id, instance_id),
	constraint log_notificationmessage_messageuuid_uq unique (message_uuid),
	constraint log_notificationmessage_outbound_ck check (char_length(trim(outbound)) > 0),
	constraint log_notificationmessage_inbound_wassuccess_ck check (inbound is not null or (not was_success)),
	constraint log_notificationmessage_wassuccess_errortext_ck check ((was_success and error_text is null) or ((not was_success) and error_text is not null))
);

create table log.error_digest
(
	error_digest_id integer not null,
	error_count integer not null,
	last_log_date timestamp not null,
	log_class varchar(1000) not null,
	log_method varchar(1000) not null,
	log_message varchar(1000) not null,
	exception varchar not null,
	
	constraint log_errordigest_errordigestid_pk primary key (error_digest_id),
	constraint log_errordigest_errorcount_ck check (error_count > 0),
	constraint log_errordigest_logclass_logmethod_logmessage_exception_uq unique (log_class, log_method, log_message, exception)
);

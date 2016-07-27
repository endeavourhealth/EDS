/* 
	create schema
*/

create schema sftpreader;

create table sftpreader.instance
(
	instance_id varchar(100) not null,
	description varchar(1000) not null,

	constraint sftpreader_instance_instanceid_pk primary key (instance_id),
	constraint sftpreader_instance_instanceid_ck check (char_length(trim(instance_id)) > 0),
	constraint sftpreader_instance_description_uq unique (description),
	constraint sftpreader_instance_description_ck check (char_length(trim(description)) > 0)
);

create table sftpreader.batch_type
(
	batch_type_id integer not null,
	description varchar(1000) not null,

	constraint sftpreader_batchtype_batchtypeid_pk primary key (batch_type_id),
	constraint sftpreader_batchtype_description_uq unique (description),
	constraint sftpreader_batchtype_description_ck check (char_length(trim(description)) > 0)
);

insert into sftpreader.batch_type
(
	batch_type_id,
	description
)
values
(
	1,
	'EMIS-EXTRACT-SERVICE-5-1'
);

create table sftpreader.batch_file_type
(
	batch_type_id integer not null,
	local_file_type_identifier varchar(1000),

	constraint sftpreader_batchfiletype_batchtypeid_localfiletypeidentifier_pk primary key (batch_type_id, local_file_type_identifier),
	constraint sftpreader_batchfiletype_batchtypeid_fk foreign key (batch_type_id) references sftpreader.batch_type (batch_type_id),
	constraint sftpreader_batchfiletype_localfiletypeidentifier_ck check (char_length(trim(local_file_type_identifier)) > 0)
);

insert into sftpreader.batch_file_type
(
	batch_type_id,
	local_file_type_identifier
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
(1, 'CareRecord_Consultation'),
(1, 'CareRecord_Diary'),
(1, 'CareRecord_Observation'),
(1, 'CareRecord_ObservationReferral'),
(1, 'CareRecord_Problem'),
(1, 'Coding_ClinicalCode'),
(1, 'Coding_DrugCode'),
(1, 'PatientAdmin_ListEntry'),
(1, 'Prescribing_DrugRecord'),
(1, 'Prescribing_IssueRecord');

create table sftpreader.configuration
(
	instance_id varchar(100) not null,
	batch_type_id integer not null,
	poll_frequency_seconds integer not null,
	local_root_path varchar(1000) not null,

	constraint sftpreader_configuration_instanceid_pk primary key (instance_id),
	constraint sftpreader_configuration_instanceid_fk foreign key (instance_id) references sftpreader.instance (instance_id),
	constraint sftpreader_configuration_batchtypeid_fk foreign key (batch_type_id) references sftpreader.batch_type (batch_type_id),
	constraint sftpreader_configuration_instanceid_batchtypeid_uq unique (instance_id, batch_type_id),
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

create table sftpreader.batch
(
	batch_id serial not null,
	instance_id varchar(100) not null,
	batch_type_id integer not null,
	local_batch_identifier varchar(500) not null,
	create_date timestamp not null default now(),
	is_complete boolean not null default false,
	complete_date timestamp null,
	have_notified boolean not null default false,
	notification_date timestamp null,

	constraint sftpreader_batch_filesetid_pk primary key (batch_id),
	constraint sftpreader_batch_instanceid_batchtypeid_fk foreign key (instance_id, batch_type_id) references sftpreader.configuration (instance_id, batch_type_id),
	constraint sftpreader_batch_instanceid_batchid_uq unique (instance_id, batch_id),
	constraint sftpreader_batch_instanceid_localbatchidentifier_uq unique (instance_id, local_batch_identifier),
	constraint sftpreader_batch_localbatchidentifier_ck check (char_length(trim(local_batch_identifier)) > 0),
	constraint sftpreader_batch_createdate_completedate_ck check ((complete_date is null) or (create_date <= complete_date)),
	constraint sftpreader_batch_completedate_notificationdate_ck check ((complete_date is null or notification_date is null) or (complete_date <= notification_date)),
	constraint sftpreader_batch_iscomplete_completedate_ck check ((is_complete and complete_date is not null) or ((not is_complete) and complete_date is null)),
	constraint sftpreader_batch_havenotified_notificationdate_ck check ((have_notified and notification_date is not null) or ((not have_notified) and notification_date is null)),
	constraint sftpreader_batch_iscomplete_havenotified_ck check (is_complete or (not have_notified))
);

create table sftpreader.batch_file
(
	batch_file_id serial not null,
	batch_id integer not null,
	batch_type_id integer not null,
	local_file_type_identifier varchar(1000) not null,
	file_name varchar(1000) not null,
	file_path varchar(1000) not null,
	file_size_bytes bigint not null,
	download_date timestamp not null default now(),

	constraint sftpreader_batchfile_batchfileid_pk primary key (batch_file_id),
	constraint sftpreader_batchfile_batchid_fk foreign key (batch_id) references sftpreader.batch (batch_id),
	constraint sftpreader_batchfile_batchtypeid_localfiletypeidentifier_fk foreign key (batch_type_id, local_file_type_identifier) references sftpreader.batch_file_type (batch_type_id, local_file_type_identifier),
	constraint sftpreader_batchfile_batchid_localfiletypeidentifier_uq unique (batch_id, local_file_type_identifier),
	constraint sftpreader_batchfile_batchid_filename_uq unique (batch_id, file_name),
	constraint sftpreader_batchfile_filename_ck check (char_length(trim(file_name)) > 0),
	constraint sftpreader_batchfile_filesizebytes check (file_size_bytes >= 0)
);


/*
	insert test data
*/

insert into configuration.instance
(
	instance_id, 
	instance_name,
	description
)
values
(
	1,
	'TEST001', 
	'TESTING'
);

insert into configuration.channel 
(
	channel_id, 
	channel_name, 
	port_number, 
	is_active,
	use_tls, 
	remote_application, 
	remote_facility,
	local_application, 
	local_facility, 
	use_acks, 
	notes
)
values
(1, 'HOMERTON', 8900, true, false, 'HOMERTON_TIE', 'HOMERTON', 'EDS', 'ENDEAVOUR', false, ''),
(2, 'BARTS', 8901, true, false, 'BLT_TIE', 'BLT', 'EDS', 'ENDEAVOUR', false, '');

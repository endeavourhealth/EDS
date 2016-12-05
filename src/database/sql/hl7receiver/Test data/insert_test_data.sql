/*
	insert test data
*/

insert into configuration.instance
(
	instance_id, 
	description
)
values
(
	'TEST001', 
	'TESTING'
);

insert into configuration.channel 
(
	channel_id, 
	channel_name, 
	port_number, 
	use_tls, 
	remote_application, 
	remote_facility,
	local_application, 
	local_facility, 
	use_acks, 
	notes
)
values
(1, 'HOMERTON', 8900, false, 'HOMERTON_TIE', 'HOMERTON', 'EDS', 'ENDEAVOUR', false, ''),
(2, 'BARTS', 8901, false, 'BLT_TIE', 'BLT', 'EDS', 'ENDEAVOUR', false, '');

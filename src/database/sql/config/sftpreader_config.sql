insert into config
(
	app_id, 
	config_id, 
	config_data
)
values
('sftpreader', 'postgres-url', 'jdbc:postgresql://localhost:5432/hl7receiver'),
('sftpreader', 'postgres-username', 'postgres'),
('sftpreader', 'postgres-password', '');

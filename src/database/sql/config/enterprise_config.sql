
--to use postgreSQL
INSERT INTO config
(app_id, config_id, config_data)
VALUES
('enterprise', 'patient_database', '{
  "driverClass" : "org.postgresql.Driver",
	"enterprise_username" : "postgres",
	"enterprise_password" : "",
	"enterprise_url"	: "jdbc:postgresql://localhost:5432/enterprise_data",
	"pseudonymised": false,
	"transform_username" : "postgres",
	"transform_password" : "",
	"transform_url"	: "jdbc:postgresql://localhost:5432/transform_enterprise_pi"
}' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('enterprise', 'admin_database', '{
  "driverClass" : "org.postgresql.Driver",
	"username" : "postgres",
	"password" : "",
	"url"	: "jdbc:postgresql://localhost:5432/enterprise_admin",
	"pseudonymised": false
}' );


--to use mySQL
INSERT INTO config
(app_id, config_id, config_data)
VALUES
('enterprise', 'patient_database', '{
  "driverClass" : "com.mysql.jdbc.Driver",
	"enterprise_username" : "root",
	"enterprise_password" : "",
	"enterprise_url"	: "jdbc:mysql://localhost:3306/enterprise_data_pseudonymised?useSSL=false",
	"pseudonymised": false,
	"transform_username" : "postgres",
	"transform_password" : "",
	"transform_url"	: "jdbc:postgresql://localhost:5432/transform_enterprise_pi"
}' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('enterprise', 'admin_database', '{
  "driverClass" : "com.mysql.jdbc.Driver",
	"username" : "root",
	"password" : "",
	"url"	: "jdbc:mysql://localhost:3306/enterprise_admin?useSSL=false",
	"pseudonymised": false
}' );



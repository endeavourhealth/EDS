
--to use postgreSQL
INSERT INTO config
(app_id, config_id, config_data)
VALUES
('enterprise', 'patient_database', '{
  "driverClass" : "org.postgresql.Driver",
	"username" : "postgres",
	"password" : "",
	"url"	: "jdbc:postgresql://localhost:5432/enterprise_data",
	"pseudonymised": false
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
  "driverClass" : "com.mysql.cj.jdbc.Driver",
	"username" : "root",
	"password" : "",
	"url"	: "jdbc:mysql://localhost:3306/enterprise_data?useSSL=false",
	"pseudonymised": false
}' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('enterprise', 'admin_database', '{
  "driverClass" : "com.mysql.cj.jdbc.Driver",
	"username" : "root",
	"password" : "",
	"url"	: "jdbc:mysql://localhost:3306/enterprise_admin?useSSL=false",
	"pseudonymised": false
}' );



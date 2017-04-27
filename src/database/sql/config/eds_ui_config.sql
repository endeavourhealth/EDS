INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'keycloak', '{
  "realm": "endeavour",
  "realm-public-key": "<REALM PUBLIC KEY>",
  "auth-server-url": "<AUTH SERVER URL>",
  "ssl-required": "external",
  "resource": "eds-ui",
  "public-client": true
}' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'keycloak_proxy', '{
 "user" : "<USERNAME>",
 "pass" : "<PASSWORD>",
 "url" : "https://devauth.endeavourhealth.net/auth"
}');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'application', '{ "appUrl" : "http://localhost:8080" }');


/*INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'rePostMessageToExchangeConfig', '{
	"username" : "<USERNAME>",
	"password" : "<PASSWORD>",
	"nodes" : ["localhost:5672"],
	"exchange" : "EdsInbound",
	"routingHeader" : "SenderLocalIdentifier"
}');*/

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'logbackDb','{
   "url" : "jdbc:postgresql://localhost:5432/logback",
   "username" : "<USERNAME>",
   "password" : "<PASSWORD>"
}');


INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'OrganisationManagerDB','{
   "url" : "jdbc:mysql://URL_OF_SERVER/OrganisationManager",
   "username" : "<USERNAME>",
   "password" : "<PASSWORD>"
}');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('eds-ui', 'GoogleMapsAPI','{
   "url" : "https://maps.googleapis.com/maps/api/geocode/json?address=",
   "apiKey" : "API_KEY"
}');


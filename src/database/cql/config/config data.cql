USE configuration;

INSERT INTO configuration.configuration_resource (configuration_id,configuration_data,configuration_name) VALUES (
7b9dcacf-50b8-4224-8bcc-b93524fc9ae9,'1.rabbitmq.eds.c.healthforge.io:15672,2.rabbitmq.eds.c.healthforge.io:15672,3.rabbitmq.eds.c.healthforge.io:15672','RabbitNodes');
INSERT INTO configuration.configuration_resource (configuration_id,configuration_data,configuration_name) VALUES (
b9b14e26-5a52-4f36-ad89-f01e465c1361,'[{
	"uuid": "a5813d55-4264-443d-9550-c85712f44a74",
	"regex": "[A-M].*",
	"routeKey": "A-M",
	"name": "Default A-M",
	"description": "Default fallback group, initial character A-M"
},
{
	"uuid": "a0e84d9b-3f17-4954-ba7a-ceb34564cb84",
	"regex": "[N-Z].*",
	"routeKey": "N-Z",
	"name": "Default A-Z",
	"description": "Default fallback group, initial character N-Z"
},
{
	"uuid": "b94aac35-b667-45b7-b7c8-d9e8109f9da5",
	"regex": "[0-9].*",
	"routeKey": "0-9",
	"name": "Default 0-9",
	"description": "Default fallback group, initial character 0-9"
}]','RouteGroupings');
INSERT INTO configuration.configuration_resource (configuration_id,configuration_data,configuration_name) VALUES (
c865eb28-58dd-4ec4-9ca9-fda1273566bf,'{
  "realm": "endeavour",
  "realm-public-key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlnMTOPmUhfZVgd/qCV1svJe0AWFTm6hs3zaas2HNNHhR1rIGH02YBXeDazrbMHvJz1Q/weXn1j0tyfTNf53cwH4KQy3+OXjnh1vXOlR26XzmjdkvG+Hoy5L3+JbpIV5ktflFRxstml1CU3p8jZSXMyjLcCn1I1IbCWG/YsO1ST34ZOSI0K+11Y3N/fYZnsZW7OIPTc6zTpUIq0/jOySgSD1xwOS9q/MPJ6gq8B2LyDDYDR+pJPzlxQ3JPk2gbvqYYTSQcpwZwNZiyVhatiX4lxutDXzZd0FFMF2WVv9uFUcQkt1GkKxs9mN9u5EdbaWTb2noznBvBvdvbdePuSJdpQIDAQAB",
  "auth-server-url": "https://keycloak.eds.c.healthforge.io/auth",
  "ssl-required": "external",
  "resource": "eds-ui",
  "public-client": true
}','KeycloakConfig');
INSERT INTO configuration.configuration_resource (configuration_id,configuration_data,configuration_name) VALUES (
f48c8bf3-a03d-411e-baf1-8712569ed67f,'{
  "realm": "endeavour",
  "realm-public-key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlnMTOPmUhfZVgd/qCV1svJe0AWFTm6hs3zaas2HNNHhR1rIGH02YBXeDazrbMHvJz1Q/weXn1j0tyfTNf53cwH4KQy3+OXjnh1vXOlR26XzmjdkvG+Hoy5L3+JbpIV5ktflFRxstml1CU3p8jZSXMyjLcCn1I1IbCWG/YsO1ST34ZOSI0K+11Y3N/fYZnsZW7OIPTc6zTpUIq0/jOySgSD1xwOS9q/MPJ6gq8B2LyDDYDR+pJPzlxQ3JPk2gbvqYYTSQcpwZwNZiyVhatiX4lxutDXzZd0FFMF2WVv9uFUcQkt1GkKxs9mN9u5EdbaWTb2noznBvBvdvbdePuSJdpQIDAQAB",
  "auth-server-url": "https://keycloak.eds.c.healthforge.io/auth",
  "ssl-required": "external",
  "resource": "eds-sftpreader",
  "public-client": true
}','KeycloakConfig-SFTPReader');

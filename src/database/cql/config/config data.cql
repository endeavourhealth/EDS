USE configuration;

INSERT INTO configuration_resource
  ( 
	configuration_id,
	configuration_name,
    configuration_data
  )
values
 (
 	b9b14e26-5a52-4f36-ad89-f01e465c1361,
 	'RouteGroupings',
 	'[
{
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
	"uuid": "1fa471a6-4ec8-4a89-8702-a5bae11f5e69",
	"regex": ".*",
	"routeKey": "Fallback",
	"name": "Default Fallback",
	"description": "Default fallback group, all remaining"
}
]'
);

INSERT INTO configuration_resource
( 
	configuration_id,
	configuration_name,
	configuration_data
)
VALUES
(
	7b9dcacf-50b8-4224-8bcc-b93524fc9ae9,
	'RabbitNodes',
	'localhost:15672,localhost:15673,localhost:15674'
);
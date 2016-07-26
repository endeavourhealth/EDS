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
 	'[{
	"uuid": "1fa471a6-4ec8-4a89-8702-a5bae11f5e69",
	"regex": "[A-H,J-N,P,W,Y][0-9][0-9][0-9][0-9][0-9]",
	"routeKey": "GPs_Eng_Wls",
	"name": "GPs Eng & Wls",
	"description": "GP organisations in England and Wales"
},
{
	"uuid": "ac6f8842-2e68-4391-a3b3-f3b12ae56af2",
	"regex": "S[0-9][0-9][0-9][0-9][0-9]",
	"routeKey": "GPs_Sct",
	"name": "GPs Scot",
	"description": "GP organisations in Scotland"
},
{
	"uuid": "7e2c7d15-9b93-4880-9898-07b3ddfdd1cd",
	"regex": "V[0-9][0-9][0-9][0-9][0-9]",
	"routeKey": "Dental",
	"name": "Dental",
	"description": "Dental organisations"
},
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
	"uuid": "b94aac35-b667-45b7-b7c8-d9e8109f9da5",
	"regex": "[0-9].*",
	"routeKey": "0-9",
	"name": "Default 0-9",
	"description": "Default fallback group, initial character 0-9"
}]'
);

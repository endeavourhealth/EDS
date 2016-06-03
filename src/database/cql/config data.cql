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
	"regex": "[A-H,J-N,P,W,Y][0-9][0-9][0-9][0-9][0-9]",
	"routeKey": "GPs_Eng_Wls",
	"name": "GPs Eng & Wls",
	"description": "GP organisations in England and Wales"
},
{
	"regex": "S[0-9][0-9][0-9][0-9][0-9]",
	"routeKey": "GPs_Sct",
	"name": "GPs Scot",
	"description": "GP organisations in Scotland"
},
{
	"regex": "V[0-9][0-9][0-9][0-9][0-9]",
	"routeKey": "Dental",
	"name": "Dental",
	"description": "Dental organisations"
},
{
	"regex": "[A-M].*",
	"routeKey": "A-M",
	"name": "Default A-M",
	"description": "Default fallback group, initial character A-M"
},
{
	"regex": "[N-Z].*",
	"routeKey": "N-Z",
	"name": "Default A-Z",
	"description": "Default fallback group, initial character N-Z"
},
{
	"regex": "[0-9].*",
	"routeKey": "0-9",
	"name": "Default 0-9",
	"description": "Default fallback group, initial character 0-9"
}]'
 )
 
DROP TABLE IF EXISTS [abp_address_v2];
CREATE TABLE [abp_address_v2] (
  [id] bigint NOT NULL IDENTITY,
  [property_id] bigint NOT NULL,
  [flat] varchar(255) DEFAULT NULL,
  [building] varchar(255) DEFAULT NULL,
  [number] varchar(255) DEFAULT NULL,
  [dependent_thoroughfare] varchar(255) DEFAULT NULL,
  [street] varchar(255) DEFAULT NULL,
  [dependent_locality] varchar(255) DEFAULT NULL,
  [locality] varchar(255) DEFAULT NULL,
  [town] varchar(255) DEFAULT NULL,
  [postcode] varchar(255) DEFAULT NULL,
  [abp_organisation] varchar(255) DEFAULT NULL,
  [classification_id] bigint DEFAULT NULL,
  PRIMARY KEY ([id],[property_id])
)
GO

DROP TABLE IF EXISTS [address_v2];
CREATE TABLE [address_v2] (
  [id] bigint NOT NULL IDENTITY,
  [line_1] varchar(255) DEFAULT NULL,
  [line_2] varchar(255) DEFAULT NULL,
  [line_3] varchar(255) DEFAULT NULL,
  [city] varchar(255) DEFAULT NULL,
  [county] varchar(255) DEFAULT NULL,
  [postcode] varchar(255) DEFAULT NULL,
  [location_id] bigint DEFAULT NULL,
  PRIMARY KEY ([id])
)
GO

CREATE INDEX [idx_addv2_location_id] ON [address_v2] ([location_id])
GO

DROP TABLE IF EXISTS [location_v2];
CREATE TABLE [location_v2] (
  [id] bigint NOT NULL IDENTITY,
  [name] varchar(255) DEFAULT NULL,
  [type_code] varchar(50) DEFAULT NULL,
  [type_desc] varchar(255) DEFAULT NULL,
  [postcode] varchar(10) DEFAULT NULL,
  [managing_organization_id] bigint DEFAULT NULL,
  [uprn] varchar(255) DEFAULT NULL,
  [uprn_ralf00] varchar(255) DEFAULT NULL,
  [latitude] float DEFAULT NULL,
  [longitude] float DEFAULT NULL,
  [xcoordinate] float DEFAULT NULL,
  [ycoordinate] float DEFAULT NULL,
  [lsoa_code] varchar(9) DEFAULT NULL,
  [msoa_code] varchar(9) DEFAULT NULL,
  [imp_code] varchar(9) DEFAULT NULL,
  PRIMARY KEY ([id])
)
GO

CREATE INDEX [Index_location_id] ON [location_v2] ([id])
GO

CREATE INDEX [index_managing_organization] ON [location_v2] ([managing_organization_id])
GO

DROP TABLE IF EXISTS [organization_additional];
CREATE TABLE [organization_additional] (
  [id] bigint NOT NULL ,
  [property_id] bigint NOT NULL ,
  [value_id] bigint DEFAULT NULL ,
  [json_value] text DEFAULT NULL ,
  [value] varchar(255) DEFAULT NULL,
  [name] varchar(255) DEFAULT NULL,
  PRIMARY KEY ([id],[property_id])
)
GO

DROP TABLE IF EXISTS [organization_contact_v2];
CREATE TABLE [organization_contact_v2] (
  [id] bigint NOT NULL IDENTITY,
  [organization_id] bigint DEFAULT NULL,
  [contact_type] varchar(255) DEFAULT NULL,
  [value] varchar(255) DEFAULT NULL ,
  PRIMARY KEY ([id])
)
GO

DROP TABLE IF EXISTS [organization_v2];
CREATE TABLE [organization_v2] (
  [id] bigint NOT NULL,
  [ods_code] varchar(50) DEFAULT NULL,
  [name] varchar(255) DEFAULT NULL,
  [type_code] varchar(50) DEFAULT NULL,
  [type_desc] varchar(255) DEFAULT NULL,
  [postcode] varchar(10) DEFAULT NULL,
  [parent_organization_id] bigint DEFAULT NULL,
  [location_id] bigint DEFAULT NULL,
  PRIMARY KEY ([id])
)
GO

CREATE INDEX [index_organization_id] ON [organization_v2] ([id])
GO

DROP TABLE IF EXISTS [property_v2];
CREATE TABLE [property_v2] (
  [id] bigint NOT NULL IDENTITY,
  [location_id] bigint NOT NULL,
  [project_ralf_id] bigint DEFAULT NULL,
  PRIMARY KEY ([id],[location_id])
)
GO

CREATE INDEX [idx_property_location_idx] ON [property_v2] ([location_id])
GO

CREATE INDEX [idx_project_ralf_idx] ON [property_v2] ([project_ralf_id])
GO

DROP TABLE IF EXISTS [uprn_match_event_v2];
CREATE TABLE [uprn_match_event_v2] (
  [id] bigint NOT NULL IDENTITY,
  [uprn] varchar(255) DEFAULT NULL,
  [uprn_ralf00] varchar(255) DEFAULT NULL,
  [location_id] bigint DEFAULT NULL,
  [patient_address_id] bigint DEFAULT NULL,
  [latitude] float DEFAULT NULL,
  [longitude] float DEFAULT NULL,
  [xcoordinate] float DEFAULT NULL,
  [ycoordinate] float DEFAULT NULL,
  [qualifier] varchar(255) DEFAULT NULL,
  [algorithm] varchar(512) DEFAULT NULL,
  [match_date] datetime2 DEFAULT NULL,
  [abp_address_id] bigint DEFAULT NULL,
  [match_pattern_postcode] varchar(255) DEFAULT NULL,
  [match_pattern_street] varchar(255) DEFAULT NULL,
  [match_pattern_number] varchar(255) DEFAULT NULL,
  [match_pattern_building] varchar(255) DEFAULT NULL,
  [match_pattern_flat] varchar(255) DEFAULT NULL,
  [algorithm_version] varchar(255) DEFAULT NULL,
  [epoch] varchar(255) DEFAULT NULL,
  [previous_address] varchar(512) DEFAULT NULL,
  PRIMARY KEY ([id])
)
GO

CREATE INDEX [idx_location_id] ON [uprn_match_event_v2] ([location_id])
GO

DROP TABLE IF EXISTS [abp_classification_v2];
CREATE TABLE [abp_classification_v2] (
  [id] bigint NOT NULL,
  [code] varchar(255) NOT NULL,
  [term] varchar(255) DEFAULT NULL,
  PRIMARY KEY ([id],[code])
)
GO

INSERT INTO [abp_classification_v2] VALUES (1,'C','Commercial'),(2,'CA','Agricultural'),(3,'CA01','Farm / Non-Residential Associated Building'),(4,'CA02','Fishery'),(5,'CA02FF','Fish Farming'),(6,'CA02FH','Fish Hatchery'),(7,'CA02FP','Fish Processing'),(8,'CA02OY','Oyster / Mussel Bed'),(9,'CA03','Horticulture'),(10,'CA03SH','Smallholding'),(11,'CA03VY','Vineyard'),(12,'CA03WB','Watercress Bed'),(13,'CA04','Slaughter House / Abattoir'),(14,'CB','Ancillary Building'),(15,'CC','Community Services'),(16,'CC02','Law Court'),(17,'CC03','Prison'),(18,'CC03HD','HM Detention Centre'),(19,'CC03PR','HM Prison Service'),(20,'CC03SC','Secure Residential Accommodation'),(21,'CC04','Public / Village Hall / Other Community Facility'),(22,'CC04YR','Youth Recreational / Social Club'),(23,'CC05','Public Convenience'),(24,'CC06','Cemetery / Crematorium / Graveyard. In Current Use.'),(25,'CC06CB','Columbarium'),(26,'CC06CN','Crematorium'),(27,'CC06CR','Chapel Of Rest'),(28,'CC06CY','Cemetery'),(29,'CC06MC','Military Cemetery'),(30,'CC06MY','Mortuary'),(31,'CC07','Church Hall / Religious Meeting Place / Hall'),(32,'CC08','Community Service Centre / Office'),(33,'CC09','Public Household Waste Recycling Centre (HWRC)'),(34,'CC10','Recycling Site'),(35,'CC11','CCTV'),(36,'CC12','Job Centre'),(37,'CE','Education'),(38,'CE01','College'),(39,'CE01FE','Further Education'),(40,'CE01HE','Higher Education'),(41,'CE02','Childrens Nursery / Crche'),(42,'CE03','Preparatory / First / Primary / Infant / Junior / Middle School'),(43,'CE03FS','First School'),(44,'CE03IS','Infant School'),(45,'CE03JS','Junior School'),(46,'CE03MS','Middle School'),(47,'CE03NP','Non State Primary / Preparatory School'),(48,'CE03PS','Primary School'),(49,'CE04','Secondary / High School'),(50,'CE04NS','Non State Secondary School'),(51,'CE04SS','Secondary School'),(52,'CE05','University'),(53,'CE06','Special Needs Establishment.'),(54,'CE07','Other Educational Establishment'),(55,'CH','Hotel / Motel / Boarding / Guest House'),(56,'CH01','Boarding / Guest House / Bed And Breakfast / Youth Hostel'),(57,'CH01YH','Youth Hostel'),(58,'CH02','Holiday Let/Accomodation/Short-Term Let Other Than CH01'),(59,'CH03','Hotel/Motel'),(60,'CI','\"Industrial Applicable to manufacturing, engineering, maintenance, storage / wholesale distribution and extraction sites\"'),(61,'CI01','Factory/Manufacturing'),(62,'CI01AW','Aircraft Works'),(63,'CI01BB','Boat Building'),(64,'CI01BR','Brick Works'),(65,'CI01BW','Brewery'),(66,'CI01CD','Cider Manufacture'),(67,'CI01CM','Chemical Works'),(68,'CI01CW','Cement Works'),(69,'CI01DA','Dairy Processing'),(70,'CI01DY','Distillery'),(71,'CI01FL','Flour Mill'),(72,'CI01FO','Food Processing'),(73,'CI01GW','Glassworks'),(74,'CI01MG','Manufacturing'),(75,'CI01OH','Oast House'),(76,'CI01OR','Oil Refining'),(77,'CI01PG','Pottery Manufacturing'),(78,'CI01PM','Paper Mill'),(79,'CI01PW','Printing Works'),(80,'CI01SR','Sugar Refinery'),(81,'CI01SW','Steel Works'),(82,'CI01TL','Timber Mill'),(83,'CI01WN','Winery'),(84,'CI01YD','Shipyard'),(85,'CI02','Mineral / Ore Working / Quarry / Mine'),(86,'CI02MA','Mineral Mining / Active'),(87,'CI02MD','Mineral Distribution / Storage'),(88,'CI02MP','Mineral Processing'),(89,'CI02OA','Oil / Gas Extraction / Active'),(90,'CI02QA','Mineral Quarrying / Open Extraction / Active'),(91,'CI03','Workshop / Light Industrial'),(92,'CI03GA','Servicing Garage'),(93,'CI04','Warehouse / Store / Storage Depot'),(94,'CI04CS','Crop Handling / Storage'),(95,'CI04PL','Postal Sorting / Distribution'),(96,'CI04SO','Solid Fuel Storage'),(97,'CI04TS','Timber Storage'),(98,'CI05','Wholesale Distribution'),(99,'CI05SF','Solid Fuel Distribution'),(100,'CI05TD','Timber Distribution'),(101,'CI06','Recycling Plant'),(102,'CI07','Incinerator / Waste Transfer Station'),(103,'CI08','Maintenance Depot'),(104,'CL','Leisure - Applicable to recreational sites and enterprises'),(105,'CL01','Amusements'),(106,'CL01LP','Leisure Pier'),(107,'CL02','Holiday / Campsite'),(108,'CL02CG','Camping'),(109,'CL02CV','Caravanning'),(110,'CL02HA','Holiday Accommodation'),(111,'CL02HO','Holiday Centre'),(112,'CL02YC','Youth Organisation Camp'),(113,'CL03','Library'),(114,'CL03RR','Reading Room'),(115,'CL04','Museum / Gallery'),(116,'CL04AC','Art Centre / Gallery'),(117,'CL04AM','Aviation Museum'),(118,'CL04HG','Heritage Centre'),(119,'CL04IM','Industrial Museum'),(120,'CL04MM','Military Museum'),(121,'CL04NM','Maritime Museum'),(122,'CL04SM','Science Museum'),(123,'CL04TM','Transport Museum'),(124,'CL06','Indoor / Outdoor Leisure / Sporting Activity / Centre'),(125,'CL06AH','Athletics Facility'),(126,'CL06BF','Bowls Facility'),(127,'CL06CK','Cricket Facility'),(128,'CL06CU','Curling Facility'),(129,'CL06DS','Diving / Swimming Facility'),(130,'CL06EQ','Equestrian Sports Facility'),(131,'CL06FB','Football Facility'),(132,'CL06FI','Fishing / Angling Facility'),(133,'CL06GF','Golf Facility'),(134,'CL06GL','Gliding Facility'),(135,'CL06GR','Greyhound Racing Facility'),(136,'CL06HF','Hockey Facility'),(137,'CL06HR','Horse Racing Facility'),(138,'CL06HV','Historic Vessel / Aircraft / Vehicle'),(139,'CL06LS','Activity / Leisure / Sports Centre'),(140,'CL06ME','Model Sports Facility'),(141,'CL06MF','Motor Sports Facility'),(142,'CL06PF','Playing Field'),(143,'CL06QS','Racquet Sports Facility'),(144,'CL06RF','Rugby Facility'),(145,'CL06RG','Recreation Ground'),(146,'CL06SI','Shinty Facility'),(147,'CL06SK','Skateboarding Facility'),(148,'CL06SX','Civilian Firing Facility'),(149,'CL06TB','Tenpin Bowling Facility'),(150,'CL06TN','Public Tennis Court'),(151,'CL06WA','Water Sports Facility'),(152,'CL06WP','Winter Sports Facility'),(153,'CL06WY','Wildlife Sports Facility'),(154,'CL06YF','Cycling Sports Facility'),(155,'CL07','Bingo Hall / Cinema / Conference / Exhibition Centre / Theatre / Concert Hall'),(156,'CL07CI','Cinema'),(157,'CL07EN','Entertainment Complex'),(158,'CL07EX','Conference / Exhibition Centre'),(159,'CL07TH','Theatre'),(160,'CL08','Zoo / Theme Park'),(161,'CL08AK','Amusement Park'),(162,'CL08AQ','Aquatic Attraction'),(163,'CL08MX','Model Village Site'),(164,'CL08WZ','Wildlife / Zoological Park'),(165,'CL09','\"Beach Hut (Recreational, Non-Residential Use Only)\"'),(166,'CL10','Licensed Private Members Club'),(167,'CL10RE','Recreational / Social Club'),(168,'CL11','Arena / Stadium'),(169,'CL11SD','Stadium'),(170,'CL11SJ','Showground'),(171,'CM','Medical'),(172,'CM01','Dentist'),(173,'CM02','General Practice Surgery / Clinic'),(174,'CM02HC','Health Centre'),(175,'CM02HL','Health Care Services'),(176,'CM03','Hospital / Hospice'),(177,'CM03HI','Hospice'),(178,'CM03HP','Hospital'),(179,'CM04','Medical / Testing / Research Laboratory'),(180,'CM05','Professional Medical Service'),(181,'CM05ZS','Assessment / Development Services'),(182,'CN','Animal Centre'),(183,'CN01','Cattery / Kennel'),(184,'CN02','Animal Services'),(185,'CN02AX','Animal Quarantining'),(186,'CN03','Equestrian'),(187,'CN03HB','Horse Racing / Breeding Stable'),(188,'CN03SB','Commercial Stabling / Riding'),(189,'CN04','Vet / Animal Medical Treatment'),(190,'CN05','Animal / Bird / Marine Sanctuary'),(191,'CN05AN','Animal Sanctuary'),(192,'CN05MR','Marine Sanctuary'),(193,'CO','Office'),(194,'CO01','Office / Work Studio'),(195,'CO01EM','\"Embassy /, High Commission / Consulate\"'),(196,'CO01FM','Film Studio'),(197,'CO01GV','Central Government Service'),(198,'CO01LG','Local Government Service'),(199,'CO02','Broadcasting (TV / Radio)'),(200,'CR','Retail'),(201,'CR01','Bank / Financial Service'),(202,'CR02','Retail Service Agent'),(203,'CR02PO','Post Office'),(204,'CR04','Market (Indoor / Outdoor)'),(205,'CR04FK','Fish Market'),(206,'CR04FV','Fruit / Vegetable Market'),(207,'CR04LV','Livestock Market'),(208,'CR05','Petrol Filling Station'),(209,'CR06','Public House / Bar / Nightclub'),(210,'CR07','Restaurant / Cafeteria'),(211,'CR08','Shop / Showroom'),(212,'CR08GC','Garden Centre'),(213,'CR09','Other Licensed Premise / Vendor'),(214,'CR10','Fast Food Outlet / Takeaway (Hot / Cold)'),(215,'CR11','Automated Teller Machine (ATM)'),(216,'CS','Storage Land'),(217,'CS01','General Storage Land'),(218,'CS02','Builders Yard'),(219,'CT','Transport'),(220,'CT01','Airfield / Airstrip / Airport / Air Transport Infrastructure Facility'),(221,'CT01AF','Airfield'),(222,'CT01AI','Air Transport Infrastructure Services'),(223,'CT01AP','Airport'),(224,'CT01AY','Air Passenger Terminal'),(225,'CT01HS','Helicopter Station'),(226,'CT01HT','Heliport / Helipad'),(227,'CT02','Bus Shelter'),(228,'CT03','Car / Coach / Commercial Vehicle / Taxi Parking / Park And Ride Site'),(229,'CT03PK','Public Park And Ride'),(230,'CT03PP','Public Car Parking'),(231,'CT03PU','Public Coach Parking'),(232,'CT03VP','Public Commercial Vehicle Parking'),(233,'CT04','Goods Freight Handling / Terminal'),(234,'CT04AE','Air Freight Terminal'),(235,'CT04CF','Container Freight'),(236,'CT04RH','Road Freight Transport'),(237,'CT04RT','Rail Freight Transport'),(238,'CT05','Marina'),(239,'CT06','Mooring'),(240,'CT07','Railway Asset'),(241,'CT08','Station / Interchange / Terminal / Halt'),(242,'CT08BC','Bus / Coach Station'),(243,'CT08RS','Railway Station'),(244,'CT08VH','Vehicular Rail Terminal'),(245,'CT09','Transport Track / Way'),(246,'CT09CL','Cliff Railway'),(247,'CT09CX','Chair Lift / Cable Car / Ski Tow'),(248,'CT09MO','Monorail'),(249,'CT10','Vehicle Storage'),(250,'CT10BG','Boat Storage'),(251,'CT10BU','Bus / Coach Depot'),(252,'CT11','Transport Related Infrastructure'),(253,'CT11AD','Aqueduct'),(254,'CT11LK','Lock'),(255,'CT11WE','Weir'),(256,'CT11WG','Weighbridge / Load Gauge'),(257,'CT12','Overnight Lorry Park'),(258,'CT13','Harbour / Port / Dock / Dockyard / Slipway / Landing Stage / Pier / Jetty / Pontoon / Terminal / Berthing / Quay'),(259,'CT13FR','Passenger Ferry Terminal'),(260,'CT13NB','Non-Tanker Nautical Berthing'),(261,'CT13NF','Nautical Refuelling Facility'),(262,'CT13SA','Slipway'),(263,'CT13SP','Ship Passenger Terminal'),(264,'CT13TK','Tanker Berthing'),(265,'CT13VF','Vehicular Ferry Terminal'),(266,'CU','Utility'),(267,'CU01','Electricity Sub-Station'),(268,'CU02','Landfill'),(269,'CU03','Power Station / Energy Production'),(270,'CU03ED','Electricity Distribution Facility'),(271,'CU03EP','Electricity Production Facility'),(272,'CU03WF','Wind Farm'),(273,'CU03WU','Wind Turbine'),(274,'CU04','Pump House / Pumping Station / Water Tower'),(275,'CU04WC','Water Controlling / Pumping'),(276,'CU04WD','Water Distribution / Pumping'),(277,'CU04WM','Water Quality Monitoring'),(278,'CU04WS','Water Storage'),(279,'CU04WW','Waste Water Distribution / Pumping'),(280,'CU06','Telecommunication'),(281,'CU06TE','Telecommunications Mast'),(282,'CU06TX','Telephone Exchange'),(283,'CU07','Water / Waste Water / Sewage Treatment Works'),(284,'CU07WR','Waste Water Treatment'),(285,'CU07WT','Water Treatment'),(286,'CU08','Gas / Oil Storage / Distribution'),(287,'CU08GG','Gas Governor'),(288,'CU08GH','Gas Holder'),(289,'CU08OT','Oil Terminal'),(290,'CU09','Other Utility Use'),(291,'CU09CQ','Cable Terminal Station'),(292,'CU09OV','Observatory'),(293,'CU09RA','Radar Station'),(294,'CU09SE','Satellite Earth Station'),(295,'CU10','Waste Management'),(296,'CU11','Telephone Box'),(297,'CU11OP','Other Public Telephones'),(298,'CU12','Dam'),(299,'CX','Emergency / Rescue Service'),(300,'CX01','Police / Transport Police / Station'),(301,'CX01PT','Police Training'),(302,'CX02','Fire Station'),(303,'CX02FT','Fire Service Training'),(304,'CX03','Ambulance Station'),(305,'CX03AA','Air Sea Rescue / Air Ambulance'),(306,'CX04','Lifeboat Services / Station'),(307,'CX05','Coastguard Rescue / Lookout / Station'),(308,'CX06','Mountain Rescue Station'),(309,'CX07','Lighthouse'),(310,'CX08','Police Box / Kiosk'),(311,'CZ','Information'),(312,'CZ01','Advertising Hoarding'),(313,'CZ02','Tourist Information Signage'),(314,'CZ02VI','Visitor Information'),(315,'CZ03','Traffic Information Signage'),(316,'L','Land'),(317,'LA','Agricultural - Applicable to land in farm ownership and not run as a separate business enterprise'),(318,'LA01','Grazing Land'),(319,'LA02','Permanent Crop / Crop Rotation'),(320,'LA02OC','Orchard'),(321,'LB','Ancillary Building'),(322,'LB99AV','Aviary / Dovecot / Cage'),(323,'LB99BD','Bandstand'),(324,'LB99PI','Pavilion / Changing Room'),(325,'LB99SV','Sports Viewing Structure'),(326,'LC','Burial Ground'),(327,'LC01','Historic / Disused Cemetery / Graveyard'),(328,'LD','Development'),(329,'LD01','Development Site'),(330,'LD01CC','Commercial Construction Site'),(331,'LD01CO','Community Construction Site'),(332,'LD01RN','Residential Construction Site'),(333,'LD01TC','Transport Construction Site'),(334,'LF','Forestry'),(335,'LF02','Forest / Arboretum / Pinetum (Managed / Unmanaged)'),(336,'LF02AU','Arboretum'),(337,'LF03','Woodland'),(338,'LL','Allotment'),(339,'LM','Amenity - Open areas not attracting visitors'),(340,'LM01','Landscaped Roundabout'),(341,'LM02','Verge / Central Reservation'),(342,'LM02NV','Natural Central Reservation'),(343,'LM02VE','Natural Verge'),(344,'LM03','Maintained Amenity Land'),(345,'LM04','Maintained Surfaced Area'),(346,'LM04MV','Made Central Reservation'),(347,'LM04PV','Pavement'),(348,'LO','Open Space'),(349,'LO01','Heath / Moorland'),(350,'LP','Park'),(351,'LP01','Public Park / Garden'),(352,'LP02','Public Open Space / Nature Reserve'),(353,'LP03','Playground'),(354,'LP03PA','Play Area'),(355,'LP03PD','Paddling Pool'),(356,'LP04','Private Park / Garden'),(357,'LU','Unused Land'),(358,'LU01','Vacant / Derelict Land'),(359,'LW','Water'),(360,'LW01','Lake / Reservoir'),(361,'LW01BP','Balancing Pond'),(362,'LW01BV','Buried Reservoir'),(363,'LW02','Named Pond'),(364,'LW02DE','Dew Pond'),(365,'LW02DP','Decoy Pond'),(366,'LW02IW','Static Water'),(367,'LW03','Waterway'),(368,'LW03DR','Drain'),(369,'LW03LR','Leats / Races'),(370,'M','Military'),(371,'MA','Army'),(372,'MA99AG','Army Military Storage'),(373,'MA99AR','Army Military Range'),(374,'MA99AS','Army Site'),(375,'MA99AT','Army Military Training'),(376,'MB','Ancillary Building'),(377,'MB99TG','Military Target'),(378,'MF','Air Force'),(379,'MF99UG','Air Force Military Storage'),(380,'MF99UR','Air Force Military Range'),(381,'MF99US','Air Force Site'),(382,'MF99UT','Air Force Military Training'),(383,'MG','Defence Estates'),(384,'MN','Navy'),(385,'MN99VG','Naval Military Storage'),(386,'MN99VR','Naval Military Range'),(387,'MN99VS','Naval Site'),(388,'MN99VT','Naval Military Training'),(389,'O','Other (Ordnance Survey Only)'),(390,'OA','Aid To Navigation'),(391,'OA01','Aid To Aeronautical Navigation'),(392,'OA01AL','Aeronautical Navigation Beacon / Light'),(393,'OA01LL','Landing Light'),(394,'OA01SQ','Signal Square'),(395,'OA01WK','Wind Sock / Wind Tee'),(396,'OA02','Aid To Nautical Navigation'),(397,'OA02DM','Daymark'),(398,'OA02FG','Fog Horn Warning'),(399,'OA02NL','Nautical Navigation Beacon / Light'),(400,'OA03','Aid To Road Navigation'),(401,'OA03GP','Guide Post'),(402,'OC','Coastal Protection / Flood Prevention'),(403,'OC01','Boulder Wall / Sea Wall'),(404,'OC02','Flood Gate / Flood Sluice Gate / Flood Valve'),(405,'OC03','Groyne'),(406,'OC04','Rip-Rap'),(407,'OE','Emergency Support'),(408,'OE01','Beach Office / First Aid Facility'),(409,'OE02','Emergency Telephone (Non Motorway)'),(410,'OE03','Fire Alarm Structure / Fire Observation Tower / Fire Beater Facility'),(411,'OE04','Emergency Equipment Point / Emergency Siren / Warning Flag'),(412,'OE05','Lifeguard Facility'),(413,'OE06','LIfe / Belt / Buoy / Float / Jacket / Safety Rope'),(414,'OF','Street Furniture'),(415,'OG','Agricultural Support Objects'),(416,'OG01','Fish Ladder / Lock / Pen / Trap'),(417,'OG02','Livestock Pen / Dip'),(418,'OG03','Currick'),(419,'OG04','Slurry Bed / Pit'),(420,'OH','Historical Site / Object'),(421,'OH01','Historic Structure / Object'),(422,'OI','Industrial Support'),(423,'OI01','Adit / Incline / Level'),(424,'OI02','Caisson / Dry Dock / Grid'),(425,'OI03','Channel / Conveyor / Conduit / Pipe'),(426,'OI04','Chimney / Flue'),(427,'OI05','Crane / Hoist / Winch / Material Elevator'),(428,'OI06','Flare Stack'),(429,'OI07','Hopper / Silo / Cistern / Tank'),(430,'OI08','Grab / Skip / Other Industrial Waste Machinery / Discharging'),(431,'OI09','Kiln / Oven / Smelter'),(432,'OI10','Manhole / Shaft'),(433,'OI11','Industrial Overflow / Sluice / Valve / Valve Housing'),(434,'OI12','Cooling Tower'),(435,'OI13','Solar Panel / Waterwheel'),(436,'OI14','Telephone Pole / Post'),(437,'OI15','Electricity Distribution Pole / Pylon'),(438,'ON','Significant Natural Object'),(439,'ON01','Boundary / Significant / Historic Tree / Pollard'),(440,'ON02','Boundary / Significant Rock / Boulder'),(441,'ON03','Natural Hole (Blow / Shake / Swallow)'),(442,'OO','Ornamental / Cultural Object'),(443,'OO02','Mausoleum / Tomb / Grave'),(444,'OO03','Simple Ornamental Object'),(445,'OO04','Maze'),(446,'OP','Sport / Leisure Support'),(447,'OP01','Butt / Hide'),(448,'OP02','Gallop / Ride'),(449,'OP03','Miniature Railway'),(450,'OR','Royal Mail Infrastructure'),(451,'OR01','Postal Box'),(452,'OR02','Postal Delivery Box / Pouch'),(453,'OR03','PO Box'),(454,'OR04','Additional Mail / Packet Addressee'),(455,'OS','Scientific / Observation Support'),(456,'OS01','Meteorological Station / Equipment'),(457,'OS02','Radar / Satellite Infrastructure'),(458,'OS03','Telescope / Observation Infrastructure / Astronomy'),(459,'OT','Transport Support'),(460,'OT01','Cattle Grid / Ford'),(461,'OT02','Elevator / Escalator / Steps'),(462,'OT03','Footbridge / Walkway'),(463,'OT04','Pole / Post / Bollard (Restricting Vehicular Access)'),(464,'OT05','Subway / Underpass'),(465,'OT06','Customs Inspection Facility'),(466,'OT07','Lay-By'),(467,'OT08','Level Crossing'),(468,'OT09','Mail Pick Up'),(469,'OT10','Railway Pedestrian Crossing'),(470,'OT11','Railway Buffer'),(471,'OT12','Rail Drag'),(472,'OT13','Rail Infrastructure Services'),(473,'OT14','Rail Kilometre Distance Marker'),(474,'OT15','Railway Lighting'),(475,'OT16','Rail Mile Distance Marker'),(476,'OT17','Railway Turntable'),(477,'OT18','Rail Weighbridge'),(478,'OT19','Rail Signalling'),(479,'OT20','Railway Traverse'),(480,'OT21','Goods Tramway'),(481,'OT22','Road Drag'),(482,'OT23','Vehicle Dip'),(483,'OT24','Road Turntable'),(484,'OT25','Road Mile Distance Marker'),(485,'OT26','Road Kilometre Distance Marker'),(486,'OT27','Road Infrastructure Services'),(487,'OU','Unsupported Site'),(488,'OU01','Cycle Parking Facility'),(489,'OU04','Picnic / Barbeque Site'),(490,'OU05','Travelling Persons Site'),(491,'OU08','Shelter (Not Including Bus Shelter)'),(492,'P','Parent Shell'),(493,'PP','Property Shell'),(494,'PS','Street Record'),(495,'R','Residential'),(496,'RB','Ancillary Building'),(497,'RC','Car Park Space'),(498,'RC01','Allocated Parking'),(499,'RD','Dwelling'),(500,'RD01','Caravan'),(501,'RD02','Detached'),(502,'RD03','Semi-Detached'),(503,'RD04','Terraced'),(504,'RD06','Self Contained Flat (Includes Maisonette / Apartment)'),(505,'RD07','House Boat'),(506,'RD08','Sheltered Accommodation'),(507,'RD10','Privately Owned Holiday Caravan / Chalet'),(508,'RG','Garage'),(509,'RG02','Lock-Up Garage / Garage Court'),(510,'RH','House In Multiple Occupation'),(511,'RH01','HMO Parent'),(512,'RH02','HMO Bedsit / Other Non Self Contained Accommodation'),(513,'RH03','HMO Not Further Divided'),(514,'RI','Residential Institution'),(515,'RI01','Care / Nursing Home'),(516,'RI02','Communal Residence'),(517,'RI02NC','Non-Commercial Lodgings'),(518,'RI02RC','Religious Community'),(519,'RI03','Residential Education'),(520,'U','Unclassified'),(521,'UC','Awaiting Classification'),(522,'UP','Pending Internal Investigation'),(523,'X','Dual Use'),(524,'Z','Object of Interest'),(525,'ZA','Archaeological Dig Site'),(526,'ZM','Monument'),(527,'ZM01','Obelisk / Milestone / Standing Stone'),(528,'ZM01OB','Obelisk'),(529,'ZM01ST','Standing Stone'),(530,'ZM02','Memorial / Market Cross'),(531,'ZM03','Statue'),(532,'ZM04','Castle / Historic Ruin'),(533,'ZM05','Other Structure'),(534,'ZM05BS','Boundary Stone'),(535,'ZM05CE','Cascade / Fountain'),(536,'ZM05PN','Permanent Art Display / Sculpture'),(537,'ZM05WI','Windmill (Inactive)'),(538,'ZS','Stately Home'),(539,'ZU','Underground Feature'),(540,'ZU01','Cave'),(541,'ZU04','Pothole / Natural Hole'),(542,'ZV','Other Underground Feature'),(543,'ZV01','Cellar'),(544,'ZV02','Disused Mine'),(545,'ZV02MI','Mineral Mining / Inactive'),(546,'ZV02OI','Oil And / Gas Extraction/ Inactive'),(547,'ZV02QI','Mineral Quarrying And / Open Extraction / Inactive'),(548,'ZV03','Well / Spring'),(549,'ZV03SG','Spring'),(550,'ZV03WL','Well'),(551,'ZW','Place Of Worship'),(552,'ZW99AB','Abbey'),(553,'ZW99CA','Cathedral'),(554,'ZW99CH','Church'),(555,'ZW99CP','Chapel'),(556,'ZW99GU','Gurdwara'),(557,'ZW99KH','Kingdom Hall'),(558,'ZW99LG','Lych Gate'),(559,'ZW99MQ','Mosque'),(560,'ZW99MT','Minster'),(561,'ZW99SU','Stupa'),(562,'ZW99SY','Synagogue'),(563,'ZW99TP','Temple');
GO
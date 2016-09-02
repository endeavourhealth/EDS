// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS admin;
CREATE KEYSPACE admin WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

USE admin;

CREATE TABLE item 
  ( 
    id UUID,
    audit_id UUID,
    xml_content TEXT,
    title TEXT,
    description TEXT,
    is_deleted BOOLEAN,
    PRIMARY KEY (id, audit_id) 
  );
  
CREATE TABLE item_dependency 
  ( 
    item_id UUID,
    audit_id UUID,
    dependent_item_id UUID,
    dependency_type_id INT,
    PRIMARY KEY (item_id, audit_id, dependent_item_id, dependency_type_id) 
  );
  
CREATE MATERIALIZED VIEW item_dependency_by_type AS
SELECT item_id, audit_id, dependent_item_id, dependency_type_id
FROM  item_dependency
WHERE item_id IS NOT NULL
  AND audit_id IS NOT NULL
  AND dependent_item_id IS NOT NULL
  AND dependency_type_id IS NOT NULL
PRIMARY KEY((item_id), audit_id, dependency_type_id, dependent_item_id)
WITH CLUSTERING ORDER BY (item_id ASC);

CREATE MATERIALIZED VIEW item_dependency_by_dependent_item_id AS
SELECT item_id, audit_id, dependent_item_id, dependency_type_id
FROM  item_dependency
WHERE item_id IS NOT NULL
  AND audit_id IS NOT NULL
  AND dependent_item_id IS NOT NULL
  AND dependency_type_id IS NOT NULL
PRIMARY KEY(dependent_item_id, dependency_type_id, item_id, audit_id)
WITH CLUSTERING ORDER BY (dependent_item_id ASC);

CREATE TABLE active_item 
  ( 
    id UUID,
    organisation_id UUID,
    item_id UUID,
    audit_id UUID,
    item_type_id INT,
    is_deleted BOOLEAN,
    PRIMARY KEY (id, organisation_id, item_type_id) 
  );
  
CREATE MATERIALIZED VIEW active_item_by_item_id AS
SELECT id, organisation_id, item_id, audit_id, item_type_id, is_deleted
FROM  active_item
WHERE id IS NOT NULL
  AND organisation_id IS NOT NULL
  AND item_id IS NOT NULL
  AND audit_id IS NOT NULL
  AND item_type_id IS NOT NULL
  AND is_deleted IS NOT NULL
PRIMARY KEY((item_id), id, organisation_id, item_type_id)
WITH CLUSTERING ORDER BY (item_id ASC);

CREATE MATERIALIZED VIEW active_item_by_org_and_type AS
SELECT id, organisation_id, item_id, audit_id, item_type_id, is_deleted
FROM  active_item
WHERE id IS NOT NULL
  AND organisation_id IS NOT NULL
  AND item_id IS NOT NULL
  AND audit_id IS NOT NULL
  AND item_type_id IS NOT NULL
  AND is_deleted IS NOT NULL
PRIMARY KEY((organisation_id), item_type_id, is_deleted, id)
WITH CLUSTERING ORDER BY (organisation_id ASC);

CREATE MATERIALIZED VIEW active_item_by_type AS
SELECT id, item_id, audit_id, item_type_id, is_deleted, organisation_id
FROM  active_item
WHERE id IS NOT NULL
  AND item_id IS NOT NULL
  AND audit_id IS NOT NULL
  AND item_type_id IS NOT NULL
  AND is_deleted IS NOT NULL
  AND organisation_id IS NOT NULL
PRIMARY KEY((item_type_id), is_deleted, id, organisation_id)
WITH CLUSTERING ORDER BY (item_type_id ASC);

CREATE MATERIALIZED VIEW active_item_by_org AS
SELECT id, organisation_id, item_id, audit_id, item_type_id, is_deleted
FROM  active_item
WHERE id IS NOT NULL
  AND organisation_id IS NOT NULL
  AND item_id IS NOT NULL
  AND audit_id IS NOT NULL
  AND item_type_id IS NOT NULL
  AND is_deleted IS NOT NULL
PRIMARY KEY((organisation_id), is_deleted, item_type_id, id);
  
CREATE MATERIALIZED VIEW active_item_by_audit_id AS
SELECT id, organisation_id, item_id, audit_id, item_type_id, is_deleted
FROM  active_item
WHERE id IS NOT NULL
  AND organisation_id IS NOT NULL
  AND item_id IS NOT NULL
  AND audit_id IS NOT NULL
  AND item_type_id IS NOT NULL
  AND is_deleted IS NOT NULL
PRIMARY KEY((audit_id), id, organisation_id, item_type_id)
WITH CLUSTERING ORDER BY (audit_id ASC);
  
CREATE TABLE audit 
  ( 
    id UUID,
    end_user_id UUID,
    time_stamp timestamp,
    audit_version INT,
    organisation_id UUID,
    PRIMARY KEY (id, organisation_id, time_stamp) 
  );
  
CREATE MATERIALIZED VIEW audit_by_org_and_date_desc AS
SELECT id, end_user_id, time_stamp, audit_version, organisation_id
FROM  audit
WHERE id IS NOT NULL
  AND end_user_id IS NOT NULL
  AND time_stamp IS NOT NULL
  AND audit_version IS NOT NULL
  AND organisation_id IS NOT NULL
PRIMARY KEY(organisation_id, time_stamp, id)
WITH CLUSTERING ORDER BY (time_stamp DESC);
  
CREATE TABLE dependency_type 
  ( 
    id INT,
    description TEXT,
    PRIMARY KEY (id) 
  );

CREATE TABLE item_type 
  ( 
    id INT,
    description TEXT,
    PRIMARY KEY (id) 
  ); 
  
CREATE TABLE end_user 
  ( 
    id UUID,
    title TEXT,
    forename TEXT,
    surname TEXT,
    email TEXT,
    is_super_user BOOLEAN,
    PRIMARY KEY (id) 
  );
  
CREATE MATERIALIZED VIEW end_user_by_email AS
SELECT id, title, forename, surname, email, is_super_user
FROM  end_user
WHERE id IS NOT NULL
  AND title IS NOT NULL
  AND forename IS NOT NULL
  AND surname IS NOT NULL
  AND email IS NOT NULL
  AND is_super_user IS NOT NULL
PRIMARY KEY((email), id)
WITH CLUSTERING ORDER BY (email ASC);

CREATE TABLE end_user_email_invite 
  ( 
    id UUID,
    end_user_id UUID,
    unique_token TEXT,
    dt_completed timestamp,
    PRIMARY KEY (id) 
  );
  
CREATE TABLE end_user_pwd
  ( 
    id UUID,
    end_user_id UUID,
    pwd_hash TEXT,
    dt_expired timestamp,
    failed_attempts INT,
    is_one_time_use BOOLEAN,
    PRIMARY KEY (id) 
  );
  
CREATE MATERIALIZED VIEW end_user_pwd_by_user_id AS
SELECT id, end_user_id, pwd_hash, dt_expired, failed_attempts, is_one_time_use
FROM  end_user_pwd
WHERE id IS NOT NULL
  AND end_user_id IS NOT NULL
  AND pwd_hash IS NOT NULL
  AND dt_expired IS NOT NULL
  AND failed_attempts IS NOT NULL
  AND is_one_time_use IS NOT NULL
PRIMARY KEY((end_user_id), id)
WITH CLUSTERING ORDER BY (end_user_id ASC);

create table service
(
	id UUID,
	name TEXT,
	local_identifier TEXT,
	endpoints TEXT,
	organisations map<UUID, TEXT>,
	primary key (id)
);

CREATE MATERIALIZED VIEW service_by_local_identifier AS
SELECT local_identifier, id
FROM service
WHERE local_identifier IS NOT NULL
AND id IS NOT NULL
PRIMARY KEY ((local_identifier), id)
WITH CLUSTERING ORDER BY (local_identifier ASC);

CREATE TABLE organisation 
  ( 
    id UUID,
    name TEXT,
    national_id TEXT,
	services map<UUID, TEXT>,
    PRIMARY KEY (id) 
  );

CREATE TABLE organisation_end_user_link
  ( 
    id UUID,
    organisation_id UUID,
    end_user_id UUID,
    is_admin BOOLEAN,
    dt_expired timestamp,
    PRIMARY KEY (id) 
  );
  
CREATE MATERIALIZED VIEW organisation_end_user_link_by_user_id AS
SELECT id, organisation_id, end_user_id, is_admin, dt_expired
FROM  organisation_end_user_link
WHERE id IS NOT NULL
  AND organisation_id IS NOT NULL
  AND end_user_id IS NOT NULL
  AND is_admin IS NOT NULL
  AND dt_expired IS NOT NULL
PRIMARY KEY((end_user_id), id)
WITH CLUSTERING ORDER BY (end_user_id ASC);

CREATE MATERIALIZED VIEW organisation_by_national_id AS
SELECT id, name, national_id, services
FROM organisation
WHERE id IS NOT NULL
  AND name IS NOT NULL
  AND national_id IS NOT NULL
  AND services IS NOT NULL
PRIMARY KEY((national_id), id)
WITH CLUSTERING ORDER BY (national_id ASC);

CREATE TABLE queuedMessage
(
	id UUID,
	messageBody TEXT,
	PRIMARY KEY (id)
);

INSERT INTO dependency_type
(id, description)
VALUES 
(0, 'IsChildOf');
	
INSERT INTO dependency_type
(id, description)
VALUES 
(1, 'IsContainedWithin');
	
INSERT INTO dependency_type
(id, description)
VALUES 
(2, 'Uses');
		
INSERT INTO item_type
(id, description)
VALUES 
(2, 'Cohort');
	
INSERT INTO item_type
(id, description)
VALUES 
(3, 'Test');
	
INSERT INTO item_type
(id, description)
VALUES 
(4, 'Resource');
	
INSERT INTO item_type
(id, description)
VALUES 
(5, 'CodeSet');
	
INSERT INTO item_type
(id, description)
VALUES 
(6, 'DataSet');
	
INSERT INTO item_type
(id, description)
VALUES 
(7, 'LibraryFolder');
	
INSERT INTO item_type
(id, description)
VALUES 
(8, 'Protocol');
	
INSERT INTO item_type
(id, description)
VALUES 
(9, 'System');
	
INSERT INTO end_user
(id, title, forename, surname, email, is_super_user)
VALUES
(B5D86DA5-5E57-422E-B2C5-7E9C6F3DEA32, 'Mr', 'Regular', 'User', 'regular@email', true);

INSERT INTO end_user_pwd
(id, end_user_id, pwd_hash, dt_expired, failed_attempts, is_one_time_use)
VALUES 
(A9B3F8BE-ADF8-4923-832C-BD0AB7E484DB, B5D86DA5-5E57-422E-B2C5-7E9C6F3DEA32, '1000:1d049ba2ce1cbc28d76fed47e9699b21885252496d58b58b:c4d1df46d3ba5867349bec73fafba63468803ba5fe0d8edd', null, 0, false);
	
INSERT INTO admin.organisation (id,name,national_id,services) VALUES (
cafdfb56-f5b8-495e-bb35-0eb1ec3de4a0,'EMIS 50003','A00003',{a3f63eb0-8baf-42d1-89f8-70060de16e2c:'EMIS 50003'});
INSERT INTO admin.organisation (id,name,national_id,services) VALUES (
b6362185-98fe-4493-a27b-71227c8eab8d,'EMIS 50002','A00002',{70f931e8-366b-4128-8672-aa4ce28eee33:'EMIS 50002'});
INSERT INTO admin.organisation (id,name,national_id,services) VALUES (
06e178b7-b599-4acf-b0f1-1c2aa728cedd,'EMIS 50004','A00004',{01e9a3f1-633e-40f5-9f1a-fdbd033ac04b:'EMIS 50004'});
INSERT INTO admin.organisation (id,name,national_id,services) VALUES (
1986d191-5f3d-4c33-9c98-d88bd7816199,'EMIS 50005','A00005',{db7eba14-4a89-4090-abf8-af6c60742cb1:'EMIS 50005'});
INSERT INTO admin.organisation (id,name,national_id,services) VALUES (
b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,'Test Organisation','12345',{});
	
INSERT INTO admin.service (id,endpoints,local_identifier,name,organisations) VALUES (
01e9a3f1-633e-40f5-9f1a-fdbd033ac04b,'[{"systemUuid":"db8fa60e-08ff-4b61-ba4c-6170e6cb8df7","technicalInterfaceUuid":"58701738-5cae-4aaf-a375-31d4f4fdede9","endpoint":"http://"}]','EMIS 50004','EMIS 50004',{06e178b7-b599-4acf-b0f1-1c2aa728cedd:'EMIS 50004'});
INSERT INTO admin.service (id,endpoints,local_identifier,name,organisations) VALUES (
70f931e8-366b-4128-8672-aa4ce28eee33,'[{"systemUuid":"db8fa60e-08ff-4b61-ba4c-6170e6cb8df7","technicalInterfaceUuid":"58701738-5cae-4aaf-a375-31d4f4fdede9","endpoint":"http://"}]','EMIS 50002','EMIS 50002',{b6362185-98fe-4493-a27b-71227c8eab8d:'EMIS 50002'});
INSERT INTO admin.service (id,endpoints,local_identifier,name,organisations) VALUES (
a3f63eb0-8baf-42d1-89f8-70060de16e2c,'[{"systemUuid":"db8fa60e-08ff-4b61-ba4c-6170e6cb8df7","technicalInterfaceUuid":"58701738-5cae-4aaf-a375-31d4f4fdede9","endpoint":"http://"}]','EMIS 50003','EMIS 50003',{cafdfb56-f5b8-495e-bb35-0eb1ec3de4a0:'EMIS 50003'});
INSERT INTO admin.service (id,endpoints,local_identifier,name,organisations) VALUES (
db7eba14-4a89-4090-abf8-af6c60742cb1,'[{"systemUuid":"db8fa60e-08ff-4b61-ba4c-6170e6cb8df7","technicalInterfaceUuid":"58701738-5cae-4aaf-a375-31d4f4fdede9","endpoint":"http://"}]','EMIS 50005','EMIS 50005',{1986d191-5f3d-4c33-9c98-d88bd7816199:'EMIS 50005'});

INSERT INTO organisation_end_user_link
(id, organisation_id, end_user_id, is_admin, dt_expired)
VALUES 
(6A82F368-DEC5-42F1-8C10-20AF183FC3FB, B6FF900D-8FCD-43D8-AF37-5DB3A87A6EF6, B5D86DA5-5E57-422E-B2C5-7E9C6F3DEA32, true, null);
	
INSERT INTO admin.item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
8aebd3b7-94de-4ab5-840b-5dbd3905a540,69ab901e-30d9-4a8a-ab2f-af764946a7ea,'',false,'Cohorts','');
INSERT INTO admin.item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
96448ff1-903b-4b1c-b306-4c04c7b3e0bc,0291238c-aa5d-4cf0-85cc-58c451db2fae,'',false,'Library','');
INSERT INTO admin.item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
82c7e37a-2d8d-4132-aca3-b25f3b497191,f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6,'',false,'Systems','');
INSERT INTO admin.item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
221dd197-2649-49fa-923b-36276e2d92f0,eb6a6ee8-6eab-4fa6-8218-c6f83790ff48,'',false,'Data Sets','');
INSERT INTO admin.item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
8fcf222f-d648-4a76-b509-12f3ba7d8c1e,32b609bd-03c8-44d6-9f31-9c2219a14c93,'',false,'Code Sets','');
INSERT INTO admin.item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
db8fa60e-08ff-4b61-ba4c-6170e6cb8df7,d5ee1e7b-b6c3-4a10-bad5-aae24a732816,'EMIS Web Witness Test System',false,'EMIS Web Test','<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<libraryItem>
    <uuid>db8fa60e-08ff-4b61-ba4c-6170e6cb8df7</uuid>
    <name>EMIS Web Test</name>
    <description>EMIS Web Witness Test System</description>
    <folderUuid>82c7e37a-2d8d-4132-aca3-b25f3b497191</folderUuid>
    <system>
        <uuid>db8fa60e-08ff-4b61-ba4c-6170e6cb8df7</uuid>
        <name>EMIS Web Test</name>
        <technicalInterface>
            <uuid>58701738-5cae-4aaf-a375-31d4f4fdede9</uuid>
            <name>EmisExtractService</name>
            <frequency>Daily</frequency>
            <messageType>Patient Record</messageType>
            <messageFormat>EMISCSV</messageFormat>
            <messageFormatVersion>TestPack</messageFormatVersion>
        </technicalInterface>
    </system>
</libraryItem>
');
INSERT INTO admin.item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
c3e5f788-b44a-4a2f-8a6f-80a5b2023693,6226dbb5-ea0f-4e3a-b7db-0e08a485c6e4,'EMIS Test Protocol for Witness Testing',false,'EMIS Test Protocol','<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<libraryItem>
    <uuid>c3e5f788-b44a-4a2f-8a6f-80a5b2023693</uuid>
    <name>EMIS Test Protocol</name>
    <description>EMIS Test Protocol for Witness Testing</description>
    <folderUuid>76ce138a-1e61-4cf4-9340-faff5cb0c8c2</folderUuid>
    <protocol>
        <enabled>TRUE</enabled>
        <patientConsent>OPT-IN</patientConsent>
        <cohort>0</cohort>
        <dataSet>0</dataSet>
        <serviceContract>
            <type>PUBLISHER</type>
            <service>
                <uuid>70f931e8-366b-4128-8672-aa4ce28eee33</uuid>
                <name>EMIS 50002</name>
            </service>
            <system>
                <uuid>db8fa60e-08ff-4b61-ba4c-6170e6cb8df7</uuid>
                <name>EMIS Web Test</name>
            </system>
            <technicalInterface>
                <uuid>58701738-5cae-4aaf-a375-31d4f4fdede9</uuid>
                <name>EmisExtractService</name>
            </technicalInterface>
            <active>TRUE</active>
        </serviceContract>
        <serviceContract>
            <type>PUBLISHER</type>
            <service>
                <uuid>a3f63eb0-8baf-42d1-89f8-70060de16e2c</uuid>
                <name>EMIS 50003</name>
            </service>
            <system>
                <uuid>db8fa60e-08ff-4b61-ba4c-6170e6cb8df7</uuid>
                <name>EMIS Web Test</name>
            </system>
            <technicalInterface>
                <uuid>58701738-5cae-4aaf-a375-31d4f4fdede9</uuid>
                <name>EmisExtractService</name>
            </technicalInterface>
            <active>TRUE</active>
        </serviceContract>
        <serviceContract>
            <type>PUBLISHER</type>
            <service>
                <uuid>01e9a3f1-633e-40f5-9f1a-fdbd033ac04b</uuid>
                <name>EMIS 50004</name>
            </service>
            <system>
                <uuid>db8fa60e-08ff-4b61-ba4c-6170e6cb8df7</uuid>
                <name>EMIS Web Test</name>
            </system>
            <technicalInterface>
                <uuid>58701738-5cae-4aaf-a375-31d4f4fdede9</uuid>
                <name>EmisExtractService</name>
            </technicalInterface>
            <active>TRUE</active>
        </serviceContract>
        <serviceContract>
            <type>PUBLISHER</type>
            <service>
                <uuid>db7eba14-4a89-4090-abf8-af6c60742cb1</uuid>
                <name>EMIS 50005</name>
            </service>
            <system>
                <uuid>db8fa60e-08ff-4b61-ba4c-6170e6cb8df7</uuid>
                <name>EMIS Web Test</name>
            </system>
            <technicalInterface>
                <uuid>58701738-5cae-4aaf-a375-31d4f4fdede9</uuid>
                <name>EmisExtractService</name>
            </technicalInterface>
            <active>TRUE</active>
        </serviceContract>
    </protocol>
</libraryItem>
');

INSERT INTO admin.active_item (id,organisation_id,item_type_id,audit_id,is_deleted,item_id) VALUES (
acbf49b3-f9ce-485a-8df6-de8216e1d2b6,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,7,5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52,false,76ce138a-1e61-4cf4-9340-faff5cb0c8c2);
INSERT INTO admin.active_item (id,organisation_id,item_type_id,audit_id,is_deleted,item_id) VALUES (
675d9cc5-d06d-4d1a-af5d-a148345c8599,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,9,d5ee1e7b-b6c3-4a10-bad5-aae24a732816,false,db8fa60e-08ff-4b61-ba4c-6170e6cb8df7);
INSERT INTO admin.active_item (id,organisation_id,item_type_id,audit_id,is_deleted,item_id) VALUES (
7cc930b8-5c5b-4be4-8957-facbb0275f1b,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,7,69ab901e-30d9-4a8a-ab2f-af764946a7ea,false,8aebd3b7-94de-4ab5-840b-5dbd3905a540);
INSERT INTO admin.active_item (id,organisation_id,item_type_id,audit_id,is_deleted,item_id) VALUES (
99299154-e953-4f60-831d-291c276a2df5,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,7,32b609bd-03c8-44d6-9f31-9c2219a14c93,false,8fcf222f-d648-4a76-b509-12f3ba7d8c1e);
INSERT INTO admin.active_item (id,organisation_id,item_type_id,audit_id,is_deleted,item_id) VALUES (
6a071454-55d0-47e6-baaf-731e053f5207,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,7,f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6,false,82c7e37a-2d8d-4132-aca3-b25f3b497191);
INSERT INTO admin.active_item (id,organisation_id,item_type_id,audit_id,is_deleted,item_id) VALUES (
ab26da5a-47dd-447f-8a94-0a6b201949e5,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,7,eb6a6ee8-6eab-4fa6-8218-c6f83790ff48,false,221dd197-2649-49fa-923b-36276e2d92f0);
INSERT INTO admin.active_item (id,organisation_id,item_type_id,audit_id,is_deleted,item_id) VALUES (
825dfeb4-f6f9-4f9c-a1fb-cad20de26c29,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,8,6226dbb5-ea0f-4e3a-b7db-0e08a485c6e4,false,c3e5f788-b44a-4a2f-8a6f-80a5b2023693);
INSERT INTO admin.active_item (id,organisation_id,item_type_id,audit_id,is_deleted,item_id) VALUES (
922bad5b-7f1a-4435-929c-729929e0f660,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,7,0291238c-aa5d-4cf0-85cc-58c451db2fae,false,96448ff1-903b-4b1c-b306-4c04c7b3e0bc);

INSERT INTO admin.audit (id,organisation_id,time_stamp,audit_version,end_user_id) VALUES (
69ab901e-30d9-4a8a-ab2f-af764946a7ea,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,'2016-06-21 10:57:23',0,b5d86da5-5e57-422e-b2c5-7e9c6f3dea32);
INSERT INTO admin.audit (id,organisation_id,time_stamp,audit_version,end_user_id) VALUES (
f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,'2016-04-04 21:37:43',1,b5d86da5-5e57-422e-b2c5-7e9c6f3dea32);
INSERT INTO admin.audit (id,organisation_id,time_stamp,audit_version,end_user_id) VALUES (
6226dbb5-ea0f-4e3a-b7db-0e08a485c6e4,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,'2016-09-02 10:38:22',0,fbb470ed-64fb-4c50-9564-b15065d462e7);
INSERT INTO admin.audit (id,organisation_id,time_stamp,audit_version,end_user_id) VALUES (
5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,'2016-04-04 21:37:43',1,b5d86da5-5e57-422e-b2c5-7e9c6f3dea32);
INSERT INTO admin.audit (id,organisation_id,time_stamp,audit_version,end_user_id) VALUES (
0291238c-aa5d-4cf0-85cc-58c451db2fae,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,'2016-04-04 21:37:43',1,b5d86da5-5e57-422e-b2c5-7e9c6f3dea32);
INSERT INTO admin.audit (id,organisation_id,time_stamp,audit_version,end_user_id) VALUES (
d5ee1e7b-b6c3-4a10-bad5-aae24a732816,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,'2016-09-02 10:33:14',0,fbb470ed-64fb-4c50-9564-b15065d462e7);
INSERT INTO admin.audit (id,organisation_id,time_stamp,audit_version,end_user_id) VALUES (
eb6a6ee8-6eab-4fa6-8218-c6f83790ff48,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,'2016-06-21 10:57:08',0,b5d86da5-5e57-422e-b2c5-7e9c6f3dea32);
INSERT INTO admin.audit (id,organisation_id,time_stamp,audit_version,end_user_id) VALUES (
32b609bd-03c8-44d6-9f31-9c2219a14c93,b6ff900d-8fcd-43d8-af37-5db3a87a6ef6,'2016-06-21 10:57:54',0,b5d86da5-5e57-422e-b2c5-7e9c6f3dea32);

INSERT INTO admin.item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
8aebd3b7-94de-4ab5-840b-5dbd3905a540,69ab901e-30d9-4a8a-ab2f-af764946a7ea,96448ff1-903b-4b1c-b306-4c04c7b3e0bc,0);
INSERT INTO admin.item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
82c7e37a-2d8d-4132-aca3-b25f3b497191,f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6,96448ff1-903b-4b1c-b306-4c04c7b3e0bc,0);
INSERT INTO admin.item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
221dd197-2649-49fa-923b-36276e2d92f0,eb6a6ee8-6eab-4fa6-8218-c6f83790ff48,96448ff1-903b-4b1c-b306-4c04c7b3e0bc,0);
INSERT INTO admin.item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
8fcf222f-d648-4a76-b509-12f3ba7d8c1e,32b609bd-03c8-44d6-9f31-9c2219a14c93,96448ff1-903b-4b1c-b306-4c04c7b3e0bc,0);
INSERT INTO admin.item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
db8fa60e-08ff-4b61-ba4c-6170e6cb8df7,d5ee1e7b-b6c3-4a10-bad5-aae24a732816,82c7e37a-2d8d-4132-aca3-b25f3b497191,1);
INSERT INTO admin.item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
c3e5f788-b44a-4a2f-8a6f-80a5b2023693,6226dbb5-ea0f-4e3a-b7db-0e08a485c6e4,76ce138a-1e61-4cf4-9340-faff5cb0c8c2,1);
INSERT INTO admin.item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
76ce138a-1e61-4cf4-9340-faff5cb0c8c2,5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52,96448ff1-903b-4b1c-b306-4c04c7b3e0bc,0);
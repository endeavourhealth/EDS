USE admin;

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

INSERT INTO organisation (id,name,national_id,services) VALUES (
'cafdfb56-f5b8-495e-bb35-0eb1ec3de4a0','EMIS 50003','A00003','{a3f63eb0-8baf-42d1-89f8-70060de16e2c:''EMIS 50003''}');
INSERT INTO organisation (id,name,national_id,services) VALUES (
'b6362185-98fe-4493-a27b-71227c8eab8d','EMIS 50002','A00002','{70f931e8-366b-4128-8672-aa4ce28eee33:''EMIS 50002''})');
INSERT INTO organisation (id,name,national_id,services) VALUES (
'06e178b7-b599-4acf-b0f1-1c2aa728cedd','EMIS 50004','A00004','{01e9a3f1-633e-40f5-9f1a-fdbd033ac04b:''EMIS 50004''}');
INSERT INTO organisation (id,name,national_id,services) VALUES (
'1986d191-5f3d-4c33-9c98-d88bd7816199','EMIS 50005','A00005','{db7eba14-4a89-4090-abf8-af6c60742cb1:''EMIS 50005''}');
INSERT INTO organisation (id,name,national_id,services) VALUES (
'b6ff900d-8fcd-43d8-af37-5db3a87a6ef6','Test Organisation','12345','{}');

INSERT INTO service (id,endpoints,local_id,name,organisations) VALUES (
'01e9a3f1-633e-40f5-9f1a-fdbd033ac04b','[{"systemUuid":"db8fa60e-08ff-4b61-ba4c-6170e6cb8df7","technicalInterfaceUuid":"58701738-5cae-4aaf-a375-31d4f4fdede9","endpoint":"http://"}]','EMIS 50004','EMIS 50004','{06e178b7-b599-4acf-b0f1-1c2aa728cedd:''EMIS 50004''}');
INSERT INTO service (id,endpoints,local_id,name,organisations) VALUES (
'70f931e8-366b-4128-8672-aa4ce28eee33','[{"systemUuid":"db8fa60e-08ff-4b61-ba4c-6170e6cb8df7","technicalInterfaceUuid":"58701738-5cae-4aaf-a375-31d4f4fdede9","endpoint":"http://"}]','EMIS 50002','EMIS 50002','{b6362185-98fe-4493-a27b-71227c8eab8d:''EMIS 50002''}');
INSERT INTO service (id,endpoints,local_id,name,organisations) VALUES (
'a3f63eb0-8baf-42d1-89f8-70060de16e2c','[{"systemUuid":"db8fa60e-08ff-4b61-ba4c-6170e6cb8df7","technicalInterfaceUuid":"58701738-5cae-4aaf-a375-31d4f4fdede9","endpoint":"http://"}]','EMIS 50003','EMIS 50003','{cafdfb56-f5b8-495e-bb35-0eb1ec3de4a0:''EMIS 50003''}');
INSERT INTO service (id,endpoints,local_id,name,organisations) VALUES (
'db7eba14-4a89-4090-abf8-af6c60742cb1','[{"systemUuid":"db8fa60e-08ff-4b61-ba4c-6170e6cb8df7","technicalInterfaceUuid":"58701738-5cae-4aaf-a375-31d4f4fdede9","endpoint":"http://"}]','EMIS 50005','EMIS 50005','{1986d191-5f3d-4c33-9c98-d88bd7816199:''EMIS 50005''}');

INSERT INTO item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
'8aebd3b7-94de-4ab5-840b-5dbd3905a540','69ab901e-30d9-4a8a-ab2f-af764946a7ea','',false,'Cohorts','');
INSERT INTO item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
'96448ff1-903b-4b1c-b306-4c04c7b3e0bc','0291238c-aa5d-4cf0-85cc-58c451db2fae','',false,'Library','');
INSERT INTO item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
'82c7e37a-2d8d-4132-aca3-b25f3b497191','f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6','',false,'Systems','');
INSERT INTO item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
'221dd197-2649-49fa-923b-36276e2d92f0','eb6a6ee8-6eab-4fa6-8218-c6f83790ff48','',false,'Data Sets','');
INSERT INTO item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
'8fcf222f-d648-4a76-b509-12f3ba7d8c1e','32b609bd-03c8-44d6-9f31-9c2219a14c93','',false,'Code Sets','');
INSERT INTO item
(id, audit_id, description, is_deleted, title, xml_content)
VALUES('76ce138a-1e61-4cf4-9340-faff5cb0c8c2', '5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52', '', false, 'Data Protocols', '');
INSERT INTO item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
'db8fa60e-08ff-4b61-ba4c-6170e6cb8df7','d5ee1e7b-b6c3-4a10-bad5-aae24a732816','EMIS Web Witness Test System',false,'EMIS Web Test','<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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
INSERT INTO item (id,audit_id,description,is_deleted,title,xml_content) VALUES (
'c3e5f788-b44a-4a2f-8a6f-80a5b2023693','6226dbb5-ea0f-4e3a-b7db-0e08a485c6e4','EMIS Test Protocol for Witness Testing',false,'EMIS Test Protocol','<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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

INSERT INTO active_item (item_id, audit_id, item_type_id, is_deleted, organisation_id) VALUES (
'76ce138a-1e61-4cf4-9340-faff5cb0c8c2','5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52',7,false,'b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO active_item (item_id, audit_id, item_type_id, is_deleted, organisation_id) VALUES (
'db8fa60e-08ff-4b61-ba4c-6170e6cb8df7','d5ee1e7b-b6c3-4a10-bad5-aae24a732816',9,false,'b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO active_item (item_id, audit_id, item_type_id, is_deleted, organisation_id) VALUES (
'8aebd3b7-94de-4ab5-840b-5dbd3905a540','69ab901e-30d9-4a8a-ab2f-af764946a7ea',7,false,'b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO active_item (item_id, audit_id, item_type_id, is_deleted, organisation_id) VALUES (
'8fcf222f-d648-4a76-b509-12f3ba7d8c1e','32b609bd-03c8-44d6-9f31-9c2219a14c93',7,false,'b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO active_item (item_id, audit_id, item_type_id, is_deleted, organisation_id) VALUES (
'82c7e37a-2d8d-4132-aca3-b25f3b497191','f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6',7,false,'b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO active_item (item_id, audit_id, item_type_id, is_deleted, organisation_id) VALUES (
'221dd197-2649-49fa-923b-36276e2d92f0','eb6a6ee8-6eab-4fa6-8218-c6f83790ff48',7,false,'b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO active_item (item_id, audit_id, item_type_id, is_deleted, organisation_id) VALUES (
'c3e5f788-b44a-4a2f-8a6f-80a5b2023693','6226dbb5-ea0f-4e3a-b7db-0e08a485c6e4',8,false,'b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO active_item (item_id, audit_id, item_type_id, is_deleted, organisation_id) VALUES (
'96448ff1-903b-4b1c-b306-4c04c7b3e0bc','0291238c-aa5d-4cf0-85cc-58c451db2fae',7,false,'b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');


INSERT INTO audit (id,end_user_id,timestamp,organisation_id) VALUES (
'69ab901e-30d9-4a8a-ab2f-af764946a7ea','b5d86da5-5e57-422e-b2c5-7e9c6f3dea32','2016-04-04 21:37:43','b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO audit (id,end_user_id,timestamp,organisation_id) VALUES (
'f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6','b5d86da5-5e57-422e-b2c5-7e9c6f3dea32','2016-04-04 21:37:43','b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO audit (id,end_user_id,timestamp,organisation_id) VALUES (
'6226dbb5-ea0f-4e3a-b7db-0e08a485c6e4','b5d86da5-5e57-422e-b2c5-7e9c6f3dea32','2016-04-04 21:37:43','b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO audit (id,end_user_id,timestamp,organisation_id) VALUES (
'5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52','b5d86da5-5e57-422e-b2c5-7e9c6f3dea32','2016-04-04 21:37:43','b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO audit (id,end_user_id,timestamp,organisation_id) VALUES (
'0291238c-aa5d-4cf0-85cc-58c451db2fae','b5d86da5-5e57-422e-b2c5-7e9c6f3dea32','2016-04-04 21:37:43','b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO audit (id,end_user_id,timestamp,organisation_id) VALUES (
'd5ee1e7b-b6c3-4a10-bad5-aae24a732816','b5d86da5-5e57-422e-b2c5-7e9c6f3dea32','2016-04-04 21:37:43','b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO audit (id,end_user_id,timestamp,organisation_id) VALUES (
'eb6a6ee8-6eab-4fa6-8218-c6f83790ff48','b5d86da5-5e57-422e-b2c5-7e9c6f3dea32','2016-04-04 21:37:43','b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');
INSERT INTO audit (id,end_user_id,timestamp,organisation_id) VALUES (
'32b609bd-03c8-44d6-9f31-9c2219a14c93','b5d86da5-5e57-422e-b2c5-7e9c6f3dea32','2016-04-04 21:37:43','b6ff900d-8fcd-43d8-af37-5db3a87a6ef6');




INSERT INTO item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
'8aebd3b7-94de-4ab5-840b-5dbd3905a540','69ab901e-30d9-4a8a-ab2f-af764946a7ea','96448ff1-903b-4b1c-b306-4c04c7b3e0bc',0);
INSERT INTO item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
'82c7e37a-2d8d-4132-aca3-b25f3b497191','f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6','96448ff1-903b-4b1c-b306-4c04c7b3e0bc',0);
INSERT INTO item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
'221dd197-2649-49fa-923b-36276e2d92f0','eb6a6ee8-6eab-4fa6-8218-c6f83790ff48','96448ff1-903b-4b1c-b306-4c04c7b3e0bc',0);
INSERT INTO item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
'8fcf222f-d648-4a76-b509-12f3ba7d8c1e','32b609bd-03c8-44d6-9f31-9c2219a14c93','96448ff1-903b-4b1c-b306-4c04c7b3e0bc',0);
INSERT INTO item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
'db8fa60e-08ff-4b61-ba4c-6170e6cb8df7','d5ee1e7b-b6c3-4a10-bad5-aae24a732816','82c7e37a-2d8d-4132-aca3-b25f3b497191',1);
INSERT INTO item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
'c3e5f788-b44a-4a2f-8a6f-80a5b2023693','6226dbb5-ea0f-4e3a-b7db-0e08a485c6e4','76ce138a-1e61-4cf4-9340-faff5cb0c8c2',1);
INSERT INTO item_dependency (item_id,audit_id,dependent_item_id,dependency_type_id) VALUES (
'76ce138a-1e61-4cf4-9340-faff5cb0c8c2','5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52','96448ff1-903b-4b1c-b306-4c04c7b3e0bc',0);


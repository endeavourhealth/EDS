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
  
CREATE TABLE organisation 
  ( 
    id UUID,
    name TEXT,
    national_id TEXT,
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
	(0, 'ReportFolder');
	
INSERT INTO item_type
	(id, description)
VALUES 
	(1, 'Report');
	
INSERT INTO item_type
	(id, description)
VALUES 
	(2, 'Query');
	
INSERT INTO item_type
	(id, description)
VALUES 
	(3, 'Test');
	
INSERT INTO item_type
	(id, description)
VALUES 
	(4, 'DataSource');
	
INSERT INTO item_type
	(id, description)
VALUES 
	(5, 'CodeSet');
	
INSERT INTO item_type
	(id, description)
VALUES 
	(6, 'ListOutput');
	
INSERT INTO item_type
	(id, description)
VALUES 
	(7, 'LibraryFolder');
	
INSERT INTO item_type
	(id, description)
VALUES 
	(8, 'Protocol');
	
INSERT INTO end_user
	(id, title, forename, surname, email, is_super_user)
VALUES 
	(B5D86DA5-5E57-422E-B2C5-7E9C6F3DEA32, 'Mr', 'Regular', 'User', 'regular@email', true);

INSERT INTO end_user_pwd
	(id, end_user_id, pwd_hash, dt_expired, failed_attempts, is_one_time_use)
VALUES 
	(A9B3F8BE-ADF8-4923-832C-BD0AB7E484DB, B5D86DA5-5E57-422E-B2C5-7E9C6F3DEA32, '1000:1d049ba2ce1cbc28d76fed47e9699b21885252496d58b58b:c4d1df46d3ba5867349bec73fafba63468803ba5fe0d8edd', null, 0, false);
	
INSERT INTO organisation
	(id, name, national_id)
VALUES 
	(B6FF900D-8FCD-43D8-AF37-5DB3A87A6EF6, 'Test Organisation', '12345');
	
INSERT INTO organisation_end_user_link
	(id, organisation_id, end_user_id, is_admin, dt_expired)
VALUES 
	(6A82F368-DEC5-42F1-8C10-20AF183FC3FB, B6FF900D-8FCD-43D8-AF37-5DB3A87A6EF6, B5D86DA5-5E57-422E-B2C5-7E9C6F3DEA32, true, null);
	
INSERT INTO active_item
	(id, organisation_id, item_id, audit_id, item_type_id, is_deleted)
VALUES
	(922BAD5B-7F1A-4435-929C-729929E0F660, B6FF900D-8FCD-43D8-AF37-5DB3A87A6EF6, 96448FF1-903B-4B1C-B306-4C04C7B3E0BC, 0291238C-AA5D-4CF0-85CC-58C451DB2FAE, 7, false);	
  
INSERT INTO active_item
	(id, organisation_id, item_id, audit_id, item_type_id, is_deleted)
VALUES
	(acbf49b3-f9ce-485a-8df6-de8216e1d2b6, B6FF900D-8FCD-43D8-AF37-5DB3A87A6EF6, 76ce138a-1e61-4cf4-9340-faff5cb0c8c2, 5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52, 7, false);	

INSERT INTO active_item
	(id, organisation_id, item_id, audit_id, item_type_id, is_deleted)
VALUES
	(6a071454-55d0-47e6-baaf-731e053f5207, B6FF900D-8FCD-43D8-AF37-5DB3A87A6EF6, 82c7e37a-2d8d-4132-aca3-b25f3b497191, f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6, 7, false);	

INSERT INTO item
	(id, audit_id, xml_content, title, description, is_deleted)
VALUES
	(96448FF1-903B-4B1C-B306-4C04C7B3E0BC, 0291238C-AA5D-4CF0-85CC-58C451DB2FAE, '', 'Library', '', false);
	
INSERT INTO item
	(id, audit_id, xml_content, title, description, is_deleted)
VALUES
	(76ce138a-1e61-4cf4-9340-faff5cb0c8c2, 5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52, '', 'Data Protocols', '', false);

INSERT INTO item
	(id, audit_id, xml_content, title, description, is_deleted)
VALUES
	(82c7e37a-2d8d-4132-aca3-b25f3b497191, f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6, '', 'Population Queries', '', false);

INSERT INTO audit
	(id, end_user_id, time_stamp, audit_version, organisation_id)
VALUES
	(0291238C-AA5D-4CF0-85CC-58C451DB2FAE, B5D86DA5-5E57-422E-B2C5-7E9C6F3DEA32, '2016-04-04 21:37:43', 1, B6FF900D-8FCD-43D8-AF37-5DB3A87A6EF6);

INSERT INTO audit
	(id, end_user_id, time_stamp, audit_version, organisation_id)
VALUES
	(5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52, B5D86DA5-5E57-422E-B2C5-7E9C6F3DEA32, '2016-04-04 21:37:43', 1, B6FF900D-8FCD-43D8-AF37-5DB3A87A6EF6);

INSERT INTO audit
	(id, end_user_id, time_stamp, audit_version, organisation_id)
VALUES
	(f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6, B5D86DA5-5E57-422E-B2C5-7E9C6F3DEA32, '2016-04-04 21:37:43', 1, B6FF900D-8FCD-43D8-AF37-5DB3A87A6EF6);

INSERT INTO item_dependency
	(item_id, audit_id, dependent_item_id, dependency_type_id)
VALUES
	(76ce138a-1e61-4cf4-9340-faff5cb0c8c2, 5e1a9072-2559-41a4-bc9b-2fbf1e0e1b52, 96448FF1-903B-4B1C-B306-4C04C7B3E0BC, 0);

INSERT INTO item_dependency
	(item_id, audit_id, dependent_item_id, dependency_type_id)
VALUES
	(82c7e37a-2d8d-4132-aca3-b25f3b497191, f3c9183b-e9be-4ce5-b0ff-58e46bfd8da6, 96448FF1-903B-4B1C-B306-4C04C7B3E0BC, 0);
	

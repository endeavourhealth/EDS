// Drop and recreate the keyspace
DROP KEYSPACE IF EXISTS admin;
CREATE KEYSPACE admin WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE admin;

DROP TABLE IF EXISTS patient_cohort;
DROP TABLE IF EXISTS queuedMessage;

CREATE TABLE snomed_lookup (
    concept_id text,
    type_id text,
    term text,
    PRIMARY KEY (concept_id)
);

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

CREATE TABLE patient_cohort (
    protocol_id UUID,
    service_id UUID,
    nhs_number text,
    version TIMEUUID,
    inserted TIMESTAMP,
    in_cohort BOOLEAN,
    PRIMARY KEY ((protocol_id, service_id), nhs_number, version)
) WITH CLUSTERING ORDER BY (nhs_number ASC, version DESC);


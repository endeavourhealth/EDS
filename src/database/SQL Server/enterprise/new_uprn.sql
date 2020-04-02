USE enterprise_pseudo
GO

IF OBJECT_ID('dbo.patient_address_match', 'U') IS NOT NULL DROP TABLE dbo.patient_address_match
GO

CREATE TABLE patient_address_match (
  [id] bigint NOT NULL IDENTITY,
  [patient_address_id] bigint NOT NULL,
  [uprn] bigint NOT NULL,
  [status] smallint DEFAULT NULL,
  [classification] varchar(45) DEFAULT NULL,
  [latitude] float DEFAULT NULL,
  [longitude] float DEFAULT NULL,
  [xcoordinate] float DEFAULT NULL,
  [ycoordinate] float DEFAULT NULL,
  [qualifier] varchar(50) DEFAULT NULL,
  [algorithm] varchar(50) DEFAULT NULL,
  [match_date] datetime2(0) DEFAULT NULL,
  [abp_address_number] varchar(255) DEFAULT NULL,
  [abp_address_street] varchar(255) DEFAULT NULL,
  [abp_address_locality] varchar(255) DEFAULT NULL,
  [abp_address_town] varchar(255) DEFAULT NULL,
  [abp_address_postcode] varchar(10) DEFAULT NULL,
  [abp_address_organization] varchar(255) DEFAULT NULL,
  [match_pattern_postcode] varchar(20) DEFAULT NULL,
  [match_pattern_street] varchar(20) DEFAULT NULL,
  [match_pattern_number] varchar(20) DEFAULT NULL,
  [match_pattern_building] varchar(20) DEFAULT NULL,
  [match_pattern_flat] varchar(20) DEFAULT NULL,
  [algorithm_version] varchar(20) DEFAULT NULL,
  [epoc] varchar(20) DEFAULT NULL,
  PRIMARY KEY ([id]),
  CONSTRAINT [id_UNIQUE] UNIQUE  ([id]),
  CONSTRAINT [patient_address_uprn_patient_address_id_fk] FOREIGN KEY ([patient_address_id]) REFERENCES patient_address ([id])
)
GO
CREATE INDEX [patient_address_uprn_index] ON patient_address_match ([uprn])
GO
CREATE INDEX [patient_address_patient_address_id] ON patient_address_match ([patient_address_id])
GO

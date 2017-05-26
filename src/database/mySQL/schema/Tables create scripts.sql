
create schema OrganisationManager;

drop table if exists OrganisationManager.Region;

create table OrganisationManager.Region (
	Uuid char(36) NOT NULL primary key,
    name varchar(100) not null,
    description varchar(2000),    
    
    index (name asc) 
);

drop table if exists OrganisationManager.Organisation;

create table OrganisationManager.Organisation (
	uuid char(36) not null primary key,
    name varchar(100) not null,
    alternative_name varchar(100) null,
    ods_code varchar(10) null,
    ico_code varchar(10) null,
    ig_toolkit_status varchar(10) null,
    date_of_registration date null,
    registration_person char(36) null,  /*change to not null*/
    evidence_of_registration varchar(500) null, /*change to not null*/
    IsService boolean not null,
    BulkImported boolean not null,
    BulkItemUpdated boolean not null,
    BulkConflictedWith char(36) null,
    
    index (name asc, ods_code asc)
);

drop table if exists OrganisationManager.Address;

create table OrganisationManager.Address (
	Uuid char(36) not null primary key,
    OrganisationUuid char(36) not null,
    BuildingName varchar(100) null,
    NumberAndStreet varchar(100) null,
    Locality varchar(100) null,
    City varchar(100) null,
    County varchar(100) null,
    Postcode varchar(100) null,
    lat float(10,6) null,
    lng float(10,6) null,
    GeolocationReprocess tinyint(1),        
    
    foreign key (OrganisationUuid) references OrganisationManager.Organisation(uuid) on delete cascade
);

drop table if exists OrganisationManager.FlowDirection;

create table OrganisationManager.FlowDirection (
	id smallint not null primary key,
    Direction varchar(100) not null
    
);


drop table if exists OrganisationManager.FlowSchedule;

create table OrganisationManager.FlowSchedule (
	id smallint not null primary key,
    FlowSchedule varchar(100) not null
    
);

drop table if exists OrganisationManager.DataExchangeMethod;

create table OrganisationManager.DataExchangeMethod (
	id smallint not null primary key,
    DataExhangeMethod varchar(100) not null
    
);

drop table if exists OrganisationManager.FlowStatus;

create table OrganisationManager.FlowStatus (
	id smallint not null primary key,
    FlowStatus varchar(100) not null
    
);

drop table if exists OrganisationManager.SecurityInfrastructure;

create table OrganisationManager.SecurityInfrastructure (
	id smallint not null primary key,
    SecurityInfrastructure varchar(100) not null    
);

drop table if exists OrganisationManager.SecurityArchitecture;

create table OrganisationManager.SecurityArchitecture (
	id smallint not null primary key,
    SecurityArchitecture varchar(100) not null    
);

drop table if exists OrganisationManager.Cohort;

create table OrganisationManager.Cohort (
	Uuid char(36) not null primary key,
    Name varchar(100) not null,
    Nature varchar(100) null,
    PatientCohortInclusionConsentModel varchar(100) null,
    QueryDefinition varchar(100) null,
    RemovalPolicy varchar(100) null
    
);

drop table if exists OrganisationManager.Dataset;

create table OrganisationManager.Dataset (
	Uuid char(36) not null primary key,
    Name varchar(100) not null,
    Description varchar(100) null,
    Attributes text not null,
    QueryDefinition varchar(100) null

);

drop table if exists OrganisationManager.DataFlow;

create table OrganisationManager.DataFlow (
	Uuid char(36) not null primary key,
    Name varchar(100) not null,
    DirectionId smallint null, 
    FlowScheduleId smallint not null, 
    ApproximateVolume int not null,
    DataExchangeMethodId smallint not null,
    FlowStatusId smallint not null,    
    AdditionalDocumentation varchar(100) null,
    SignOff varchar(10),
    StorageProtocolId smallint not null,
    SecurityInfrastructureId smallint not null,
    SecurityArchitectureId smallint not null,
    
    foreign key (FlowScheduleId) references OrganisationManager.FlowSchedule(id),    
    foreign key (DataExchangeMethodId) references OrganisationManager.DataExchangeMethod(id),
    foreign key (FlowStatusId) references OrganisationManager.FlowStatus(id),
    foreign key (DirectionId) references OrganisationManager.FlowDirection(id),
    foreign key (StorageProtocolId) references OrganisationManager.StorageProtocol(id)
    
);


drop table if exists OrganisationManager.DSAStatus;

create table OrganisationManager.DSAStatus (
	id smallint not null primary key,
    DSAStatus varchar(100) not null
    
);

drop table if exists OrganisationManager.ConsentModel;

create table OrganisationManager.ConsentModel (
	id smallint not null primary key,
    ConsentModel varchar(100) not null
    
);

drop table if exists OrganisationManager.DataSharingAgreement;

create table OrganisationManager.DataSharingAgreement (
	Uuid char(36) not null primary key,
    Name varchar(100) not null,
    Description varchar(100) null,
    Derivation varchar(100) null, 
    DSAStatusId smallint not null,
    ConsentModelId smallint not null,
    StartDate Date null,
    EndDate Date null,    
    
    foreign key (DSAStatusId) references OrganisationManager.DSAStatus(id),
    foreign key (ConsentModelId) references OrganisationManager.ConsentModel(id)        
);

drop table if exists OrganisationManager.StorageProtocol;

create table OrganisationManager.StorageProtocol (
	id smallint not null primary key,
    StorageProtocol varchar(100) not null
    
);


drop table if exists OrganisationManager.DataProcessingAgreement;

create table OrganisationManager.DataProcessingAgreement (
	Uuid char(36) not null primary key,
    Name varchar(100) not null,
    Description varchar(100) null,
    Derivation varchar(100) null, 
    PublisherInformation varchar(100) null, 
    PublisherContractInformation varchar(100) null,
    PublisherDataSet char(36) null,
    DSAStatusId smallint not null,
    DataFlow char(36) null,
    ReturnToSenderPolicy varchar(100) null,
    StartDate Date null,
    EndDate Date null,
    
    foreign key (DSAStatusId) references OrganisationManager.DSAStatus(id)
);

drop table if exists OrganisationManager.NatureOfInformation;

create table OrganisationManager.NatureOfInformation (
	id smallint not null primary key,
    NatureOfInformation varchar(100) not null
    
);

drop table if exists OrganisationManager.FormatType;

create table OrganisationManager.FormatType (
	id smallint not null primary key,
    FormatType varchar(100) not null
    
);

drop table if exists OrganisationManager.DataSubjectType;

create table OrganisationManager.DataSubjectType (
	id smallint not null primary key,
    DataSubjectType varchar(100) not null
    
);

drop table if exists OrganisationManager.ReviewCycle;

create table OrganisationManager.ReviewCycle (
	id smallint not null primary key,
    ReviewCycle varchar(100) not null
    
);

drop table if exists OrganisationManager.DataSharingSummary;

create table OrganisationManager.DataSharingSummary (
	Uuid char(36) not null primary key,
    Name varchar(100) not null,
    Description varchar(100) null,
    Purpose varchar(100) null, 
    NatureOfInformationId smallint not null,
    Schedule2Condition varchar(100) null,
    BenefitToSharing varchar(200) null,
    OverviewOfDataItems varchar(100) null,
    FormatTypeId smallint not null,
    DataSubjectTypeId smallint not null,
    NatureOfPersonsAccessingData varchar(100) null,
    ReviewCycleId smallint not null,
    ReviewDate date null,
    StartDate date null,
    EvidenceOfAgreement varchar(200) null,
    
    foreign key (FormatTypeId) references OrganisationManager.FormatType(id),
    foreign key (NatureOfInformationId) references OrganisationManager.NatureOfInformation(id),
    foreign key (DataSubjectTypeId) references OrganisationManager.DataSubjectType(id),
    foreign key (ReviewCycleId) references OrganisationManager.ReviewCycle(id)
);

drop table if exists OrganisationManager.MapType;

create table OrganisationManager.MapType (
	id smallint not null primary key,
    MapType varchar(100) not null    
);

drop table if exists OrganisationManager.MasterMapping;

create table OrganisationManager.MasterMapping (
	ChildUuid char(36) not null,
    ChildMapTypeId smallint not null,
    ParentUUid char(36) not null,
    ParentMapTypeId smallint not null,
    IsDefault boolean not null,
    
    foreign key (ChildMapTypeId) references OrganisationManager.MapType(id),
    foreign key (ParentMapTypeId) references OrganisationManager.MapType(id),
    primary key (ChildUuid, ChildMapTypeId, ParentUUid, ParentMapTypeId)
);

drop table if exists OrganisationManager.Purpose;

create table OrganisationManager.Purpose (
	Uuid char(36) not null primary key,
    Title varchar(50) not null,
    Detail varchar(2000) not null
);

drop table if exists OrganisationManager.Benefit;
drop table if exists OrganisationManager.DataSharingAgreementBenefit;
drop table if exists OrganisationManager.DataSharingAgreementPurpose;

drop table if exists OrganisationManager.Documentation;

create table OrganisationManager.Documentation (
	Uuid char(36) not null primary key,
    Title varchar(50) not null,
    Filename varchar(50) not null,
    FileData mediumtext not null
);


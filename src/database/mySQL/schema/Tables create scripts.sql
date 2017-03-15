
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
    alternative_name varchar(100) not null,
    ods_code varchar(10) null,
    ico_code varchar(10) null,
    ig_toolkit_status varchar(10) null,
    date_of_registration date null,
    registration_person char(36) null,  /*change to not null*/
    evidence_of_registration varchar(500) null, /*change to not null*/
    
    index (name asc, ods_code asc)
);


alter table OrganisationManager.Organisation
modify date_of_registration date null;

drop table if exists OrganisationManager.RegionOrganisationMap;

create table OrganisationManager.RegionOrganisationMap (
	regionUuid char(36) not null,
    organisationUUid char(36) not null,
    
    primary key (regionUuid, organisationUuid),   
    
    
    foreign key (regionUuid) references OrganisationManager.Region(uuid) on delete cascade,
    foreign key (organisationUuid) references OrganisationManager.Organisation(uuid)  on delete cascade
);


drop table if exists OrganisationManager.SupraRegionMap;

create table OrganisationManager.SupraRegionMap (
	ParentRegionUuid char(36) not null,
    ChildRegionUUid char(36) not null,
    
    primary key (ParentRegionUuid, ChildRegionUUid),   
    
    
    foreign key (ParentRegionUuid) references OrganisationManager.Region(uuid) on delete cascade,
    foreign key (ChildRegionUUid) references OrganisationManager.Region(uuid)  on delete cascade
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
    Geolocation varchar(100) null,
    GeolocationReprocess tinyint(1),        
    
    foreign key (OrganisationUuid) references OrganisationManager.organisation(uuid) on delete cascade
);



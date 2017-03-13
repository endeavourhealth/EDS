
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
    date_of_registration datetime not null default now(),
    registration_person int null,  /*change to not null*/
    evidence_of_registration varchar(500) null, /*change to not null*/
    
    index (name asc, ods_code asc)
);

drop table if exists OrganisationManager.RegionOrganisationMap;

create table OrganisationManager.RegionOrganisationMap (
	regionUuid char(36) not null,
    organisationUUid char(36) not null,
    
    primary key (regionUuid, organisationUuid),   
    
    
    foreign key (regionUuid) references OrganisationManager.Region(uuid) on delete cascade,
    foreign key (organisationUuid) references OrganisationManager.Organisation(uuid)  on delete cascade
);


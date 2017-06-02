Alter table OrganisationManager.DataSharingSummary
modify column Description varchar(10000) Null;


Alter table OrganisationManager.DataSharingSummary
modify column Purpose varchar(10000) Null;

Alter table OrganisationManager.DataProcessingAgreement
modify column Description text Null;

Alter table OrganisationManager.DataProcessingAgreement
add column StartDate date null;

Alter table OrganisationManager.DataProcessingAgreement
add column EndDate date null;


insert into OrganisationManager.MapType (Id, MapType)
values (8, "Publisher");

insert into OrganisationManager.MapType (Id, MapType)
values (9, "Subscriber");

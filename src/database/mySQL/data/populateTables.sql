
/*OrganisationManager.FlowDirection*/
insert into OrganisationManager.FlowDirection (Id, Direction)
values (0, "Inbound");

insert into OrganisationManager.FlowDirection (Id, Direction)
values (1, "Outbound");

/*OrganisationManager.FlowSchedule*/
insert into OrganisationManager.FlowSchedule (Id, FlowSchedule)
values (0, "Daily");

insert into OrganisationManager.FlowSchedule (Id, FlowSchedule)
values (1, "On Demand");

/*OrganisationManager.DataExchangeMethod*/
insert into OrganisationManager.DataExchangeMethod (Id, DataExhangeMethod)
values (0, "Paper");

insert into OrganisationManager.DataExchangeMethod (Id, DataExhangeMethod)
values (1, "Electronic");

/*OrganisationManager.FlowStatus*/
insert into OrganisationManager.FlowStatus (Id, FlowStatus)
values (0, "In Development");

insert into OrganisationManager.FlowStatus (Id, FlowStatus)
values (1, "Live");

/*OrganisationManager.Cohort*/
insert into OrganisationManager.Cohort (Uuid, Name, Nature)
values ("db64e478-0a3d-11e7-bc48-80fa5b27a530", "All Patients", "Sharing Data");

insert into OrganisationManager.DataFlow (Uuid, Name, Status, DirectionId, FlowScheduleId, ApproximateVolume, DataExchangeMethodId, FlowStatusId)
values ("2ea68a0b-0a3e-11e7-bc48-80fa5b27a530", "Endeavour Data Flow", "Active DataFlow", 0, 1, 200000, 1, 0);
    
/*OrganisationManager.DSAStatus*/
insert into OrganisationManager.DSAStatus (Id, DSAStatus)
values (0, "Active");

insert into OrganisationManager.DSAStatus (Id, DSAStatus)
values (1, "Inactive");


/*OrganisationManager.DataSharingAgreement*/
insert into OrganisationManager.DataSharingAgreement (Uuid, Name, Description, DSAStatusId)
values ("e8340789-0a61-11e7-bc48-80fa5b27a530", "National Data Sharing Agreement", "Full country sharing agreement", 1);

/*OrganisationManager.StorageProtocol*/
insert into OrganisationManager.StorageProtocol (Id, StorageProtocol)
values (0, "Audit Only");

insert into OrganisationManager.StorageProtocol (Id, StorageProtocol)
values (1, "Temporary Store and Forward");

insert into OrganisationManager.StorageProtocol (Id, StorageProtocol)
values (2, "Permanent Record Store");

/*OrganisationManager.DataProcessingAgreement*/
insert into OrganisationManager.DataProcessingAgreement (Uuid, Name, Description, DSAStatusId, StorageProtocolId)
values ("0140a2f8-0a63-11e7-bc48-80fa5b27a530", "National Data Processing Agreement", "Full Country Processing Agreement", 0, 2);

/*OrganisationManager.NatureOfInformation*/
insert into OrganisationManager.NatureOfInformation (Id, NatureOfInformation)
values (0, "Personal");

insert into OrganisationManager.NatureOfInformation (Id, NatureOfInformation)
values (1, "Personal Sensitive");

insert into OrganisationManager.NatureOfInformation (Id, NatureOfInformation)
values (2, "Commercial");

/*OrganisationManager.FormatType*/
insert into OrganisationManager.FormatType (Id, FormatType)
values (0, "Removable Media");

insert into OrganisationManager.FormatType (Id, FormatType)
values (1, "Electronic Structured Data");

/*OrganisationManager.DataSubjectType*/
insert into OrganisationManager.DataSubjectType (Id, DataSubjectType)
values (0, "Patient");

/*OrganisationManager.ReviewCycle*/
insert into OrganisationManager.ReviewCycle (Id, ReviewCycle)
values (0, "Annually");

insert into OrganisationManager.ReviewCycle (Id, ReviewCycle)
values (1, "Monthly");

insert into OrganisationManager.ReviewCycle (Id, ReviewCycle)
values (2, "Weekly");

/*OrganisationManager.DataSharingSummary*/
insert into OrganisationManager.DataSharingSummary (Uuid, Name, Description, NatureOfInformationId, FormatTypeId, DataSubjectTypeId, ReviewCycleId)
values ("138024c9-0aee-11e7-926e-80fa5b27a530", "National Data Summary", "Sharing data for all patients", 0, 1, 0, 1);

/*OrganisationManager.MapType*/
insert into OrganisationManager.MapType (Id, MapType)
values (0, "Service");

insert into OrganisationManager.MapType (Id, MapType)
values (1, "Organisation");

insert into OrganisationManager.MapType (Id, MapType)
values (2, "Region");

insert into OrganisationManager.MapType (Id, MapType)
values (3, "Data Sharing Agreement");

insert into OrganisationManager.MapType (Id, MapType)
values (4, "Data Flow");

insert into OrganisationManager.MapType (Id, MapType)
values (5, "Data Processing Agreement");

insert into OrganisationManager.MapType (Id, MapType)
values (6, "Cohort");

insert into OrganisationManager.MapType (Id, MapType)
values (7, "Data Set");

insert into OrganisationManager.MapType (Id, MapType)
values (8, "Publisher");

insert into OrganisationManager.MapType (Id, MapType)
values (9, "Subscriber");

insert into OrganisationManager.MapType (Id, MapType)
values (10, "Purpose");

insert into OrganisationManager.MapType (Id, MapType)
values (11, "Benefit");

insert into OrganisationManager.MapType (Id, MapType)
values (12, "Document");


/*OrganisationManager.SecurityInfrastructure*/
insert into OrganisationManager.SecurityInfrastructure (Id, SecurityInfrastructure)
values (0, "N3");

insert into OrganisationManager.SecurityInfrastructure (Id, SecurityInfrastructure)
values (1, "PSN");

insert into OrganisationManager.SecurityInfrastructure (Id, SecurityInfrastructure)
values (2, "Internet");

/*OrganisationManager.SecurityArchitecture*/
insert into OrganisationManager.SecurityArchitecture (Id, SecurityArchitecture)
values (0, "TLS/MA");

insert into OrganisationManager.SecurityArchitecture (Id, SecurityArchitecture)
values (1, "Secure FTP");

/*OrganisationManager.ConsentModel*/
insert into OrganisationManager.ConsentModel (Id, ConsentModel)
values (0, "Explicit Consent");

insert into OrganisationManager.ConsentModel (Id, ConsentModel)
values (1, "Implied Consent");


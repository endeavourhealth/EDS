/*Regions*/
insert into OrganisationManager.Region (name, description, Uuid)
values ("East London", "East London Region", "2773b467-07dd-11e7-83f3-80fa5b27a530");

insert into OrganisationManager.Region (name, description, Uuid)
values ("North East Corner", "Newcastle and Cumbria", "2bd60c36-07dd-11e7-83f3-80fa5b27a530");

insert into OrganisationManager.Region (name, description, Uuid)
values ("Manchester", "M62 Corridor", "29f15c8e-08ad-11e7-a81d-80fa5b27a530");

insert into OrganisationManager.Region (name, description, Uuid)
values ("London", "London City Supra Region", "3bd58c4c-08ad-11e7-a81d-80fa5b27a530");

insert into OrganisationManager.Region (name, description, Uuid)
values ("England", "Whole country", "4e812e38-08ad-11e7-a81d-80fa5b27a530");

/*Organisations*/
insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid, IsService)
values ("Crisp Street", "Crispy Street", "A12354", "77f0ebd0-07dc-11e7-83f3-80fa5b27a530", 0);

insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid, IsService)
values ("North Street", "Northy Street", "A11111", "856e4efb-07dc-11e7-83f3-80fa5b27a530", 0);

insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid, IsService)
values ("East Street", "Easty Street", "B22222", "aefcb614-07dc-11e7-83f3-80fa5b27a530", 0);

insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid, IsService)
values ("West Street", "Westy Street", "C33333", "b61572e6-07dc-11e7-83f3-80fa5b27a530", 0);

insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid, IsService)
values ("West Street Minor Injuries Unit", "Westy Street MIU", "C33333", "d43a509a-0d3e-11e7-8387-80fa5b27a530", 1);

insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid, IsService)
values ("West Street GP Surgery Service", "Westy Street GP", "C33333", "ef71a067-0d3e-11e7-8387-80fa5b27a530", 1);

/*OrganisationManager.MasterMapping*/
/*
insert into OrganisationManager.MasterMapping (ChildUuid, ParentUUid, MapTypeId, IsDefault)
values ("d43a509a-0d3e-11e7-8387-80fa5b27a530", "b61572e6-07dc-11e7-83f3-80fa5b27a530", 0, 0);

insert into OrganisationManager.MasterMapping (ChildUuid, ParentUUid, MapTypeId, IsDefault)
values ("ef71a067-0d3e-11e7-8387-80fa5b27a530", "b61572e6-07dc-11e7-83f3-80fa5b27a530", 0, 1);

insert into OrganisationManager.MasterMapping (ChildUuid, ParentUUid, MapTypeId, IsDefault)
values ("77f0ebd0-07dc-11e7-83f3-80fa5b27a530", "2773b467-07dd-11e7-83f3-80fa5b27a530", 1, 1);

insert into OrganisationManager.MasterMapping (ChildUuid, ParentUUid, MapTypeId, IsDefault)
values ("856e4efb-07dc-11e7-83f3-80fa5b27a530", "2773b467-07dd-11e7-83f3-80fa5b27a530", 1, 1);

insert into OrganisationManager.MasterMapping (ChildUuid, ParentUUid, MapTypeId, IsDefault)
values ("aefcb614-07dc-11e7-83f3-80fa5b27a530", "2773b467-07dd-11e7-83f3-80fa5b27a530", 1, 1);

insert into OrganisationManager.MasterMapping (ChildUuid, ParentUUid, MapTypeId, IsDefault)
values ("b61572e6-07dc-11e7-83f3-80fa5b27a530", "2bd60c36-07dd-11e7-83f3-80fa5b27a530", 1, 1);
*/

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
values ("e8340789-0a61-11e7-bc48-80fa5b27a530", "National Data Sharing Agreement", "Full country sharing agreement", 1)

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


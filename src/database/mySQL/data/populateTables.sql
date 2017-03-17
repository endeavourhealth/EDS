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
insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid)
values ("Crisp Street", "Crispy Street", "A12354", "77f0ebd0-07dc-11e7-83f3-80fa5b27a530");

insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid)
values ("North Street", "Northy Street", "A11111", "856e4efb-07dc-11e7-83f3-80fa5b27a530");

insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid)
values ("East Street", "Easty Street", "B22222", "aefcb614-07dc-11e7-83f3-80fa5b27a530");

insert into OrganisationManager.Organisation (name, alternative_name, ods_code, uuid)
values ("West Street", "Westy Street", "C33333", "b61572e6-07dc-11e7-83f3-80fa5b27a530");

insert into organisationmanager.regionorganisationmap (RegionUuid, OrganisationUuid)
values ("2773b467-07dd-11e7-83f3-80fa5b27a530", "77f0ebd0-07dc-11e7-83f3-80fa5b27a530");

/*Region Organisation Maps*/
insert into organisationmanager.regionorganisationmap (RegionUuid, OrganisationUuid)
values ("2773b467-07dd-11e7-83f3-80fa5b27a530", "856e4efb-07dc-11e7-83f3-80fa5b27a530");

insert into organisationmanager.regionorganisationmap (RegionUuid, OrganisationUuid)
values ("2773b467-07dd-11e7-83f3-80fa5b27a530", "aefcb614-07dc-11e7-83f3-80fa5b27a530");

insert into organisationmanager.regionorganisationmap (RegionUuid, OrganisationUuid)
values ("2bd60c36-07dd-11e7-83f3-80fa5b27a530", "b61572e6-07dc-11e7-83f3-80fa5b27a530");

/*Supra Region Map*/

insert into organisationmanager.supraregionmap (ParentRegionUuid, ChildRegionUuid)
values ("4e812e38-08ad-11e7-a81d-80fa5b27a530", "3bd58c4c-08ad-11e7-a81d-80fa5b27a530");

insert into organisationmanager.supraregionmap (ParentRegionUuid, ChildRegionUuid)
values ("4e812e38-08ad-11e7-a81d-80fa5b27a530", "29f15c8e-08ad-11e7-a81d-80fa5b27a530");

insert into organisationmanager.supraregionmap (ParentRegionUuid, ChildRegionUuid)
values ("4e812e38-08ad-11e7-a81d-80fa5b27a530", "2bd60c36-07dd-11e7-83f3-80fa5b27a530");

insert into organisationmanager.supraregionmap (ParentRegionUuid, ChildRegionUuid)
values ("3bd58c4c-08ad-11e7-a81d-80fa5b27a530", "2773b467-07dd-11e7-83f3-80fa5b27a530");

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
    
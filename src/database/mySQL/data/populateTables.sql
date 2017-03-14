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


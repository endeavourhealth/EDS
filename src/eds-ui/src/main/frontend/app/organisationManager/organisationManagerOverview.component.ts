import {Component, Input} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AdminService} from "../administration/admin.service";
import {StateService} from "ui-router-ng2";
import {Organisation} from "./models/Organisation";
import {LoggerService} from "../common/logger.service";
import {OrganisationManagerService} from "./organisationManager.service";
import {Region} from "../region/models/Region";
import {OrganisationManagerStatistics} from "./models/OrganisationManagerStatistics";

@Component({
    template: require('./organisationManagerOverview.html')
})
export class OrganisationManagerOverviewComponent {
    organisations: Organisation[];
    regions: Region[] = [];
    services: Organisation[];
    private file : File;
    existingOrg : Organisation;
    newOrg : Organisation;

    conflictedOrgs : Organisation[];
    orgStats : OrganisationManagerStatistics[];
    serviceStats : OrganisationManagerStatistics[];

    constructor(private $modal: NgbModal,
                private organisationManagerService: OrganisationManagerService,
                private adminService : AdminService,
                private log: LoggerService,
                protected $state: StateService) {
        this.getOverview();
    }

    getOverview() {
        var vm = this;
        vm.getOrganisationStatistics();
        vm.getServiceStatistics();
        vm.getConflictingOrganisations();

    }

    getOrganisationStatistics() {
        var vm= this;
        vm.organisationManagerService.getOrganisationStatistics()
            .subscribe(result => {
                    vm.orgStats = result
                },
                error => vm.log.error('Failed to load organisation statistics', error, 'Load service statistics')
            );
    }

    getServiceStatistics() {
        var vm= this;
        vm.organisationManagerService.getServiceStatistics()
            .subscribe(result => {
                    vm.serviceStats = result
                },
                error => vm.log.error('Failed to load service statistics', error, 'Load service statistics')
            );
    }

    fileChange(event) {
        let fileList: FileList = event.target.files;
        if(fileList.length > 0)
            this.file = fileList[0];
        else
            this.file = null;
    }

    private uploadFile() {
        var vm = this;
        var myReader:FileReader = new FileReader();

        myReader.onloadend = function(e){
            // you can perform an action with readed data here
            vm.organisationManagerService.uploadCsv(myReader.result)
                    .subscribe(result => {
                        vm.log.success('Organisations uploaded successfully');
                        vm.getConflictingOrganisations();
                    },
                    error => vm.log.error('Failed to upload bulk organisations', error, 'Upload Bulk Organisations')
                );
            }


        myReader.readAsText(vm.file);
    }

    private getConflictingOrganisations() {
        var vm = this;
        vm.organisationManagerService.getConflictedOrganisations()
            .subscribe(result => vm.conflictedOrgs = result,
                    error => vm.log.error('Failed to get conflicted Organisations', error, 'Get Conflicting Organisations'))
    }

    ok() {
        this.uploadFile();
    }

    cancel() {
        this.file = null;
    }

    resolveDifferences(organisation : Organisation) {
        var vm = this;
        vm.newOrg = organisation;
        vm.organisationManagerService.getOrganisationAddresses(organisation.uuid)
            .subscribe(
                result => {vm.newOrg.addresses = result,
                console.log(result)},
                error => vm.log.error('Error getting address', error, 'Error')
            );

        vm.organisationManagerService.getOrganisation(organisation.bulkConflictedWith)
            .subscribe(result => {
                vm.existingOrg = result
                vm.organisationManagerService.getOrganisationAddresses(organisation.bulkConflictedWith)
                    .subscribe(
                        result => vm.existingOrg.addresses = result,
                        error => vm.log.error('Error getting address', error, 'Error')
                    );
                },
                error => vm.log.error('Error getting address', error, 'Error')

            );
    }

    saveConflict() {
        var vm = this;
        vm.organisationManagerService.saveOrganisation(vm.existingOrg)
                .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.removeConflict(vm.newOrg);
                },
                error => vm.log.error('Error saving', error, 'Error')
        );
    }

    cancelConflictResolution() {
        this.existingOrg = null;
    }

    removeConflict(org) {
        var vm = this;
        vm.organisationManagerService.deleteOrganisation(org.uuid)
            .subscribe(
                result => vm.log.success('Conflict Resolved', vm.existingOrg, 'Saved'),
                error => vm.log.error('Error deleting conflict', error, 'Error')
            )

        var index = vm.conflictedOrgs.indexOf(org, 0);
        if (index > -1)
            this.conflictedOrgs.splice(index, 1);
        this.newOrg = null;
        this.existingOrg = null;

    }

    goToOrganisations() {
        this.$state.go('app.organisationManager', {mode: 'organisations'});
    }

    goToServices() {
        this.$state.go('app.organisationManager', {mode: 'services'});
    }
}
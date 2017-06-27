import {Component, Input} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {Organisation} from "./models/Organisation";
import {AdminService, LoggerService} from "eds-common-js";
import {OrganisationManagerService} from "./organisationManager.service";
import {Region} from "../region/models/Region";
import {OrganisationManagerStatistics} from "./models/OrganisationManagerStatistics";
import {FileUpload} from "./models/FileUpload";

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
    filesToUpload: FileUpload[] = [];
    fileList: FileList;

    conflictedOrgs : Organisation[];
    orgStats : OrganisationManagerStatistics[];
    serviceStats : OrganisationManagerStatistics[];
    regionStats : OrganisationManagerStatistics[];

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
        vm.getRegionStatistics();
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

    getRegionStatistics() {
        var vm= this;
        vm.organisationManagerService.getRegionStatistics()
            .subscribe(result => {
                    vm.regionStats = result
                },
                error => vm.log.error('Failed to load region statistics', error, 'Load region statistics')
            );
    }

    fileChange(event) {
        var vm = this;
        vm.filesToUpload = [];

        vm.fileList = event.target.files;

        if(vm.fileList.length > 0) {
            this.file = vm.fileList[0];
            for (var i =0; i <= vm.fileList.length - 1; i++){
                this.filesToUpload.push(<FileUpload>{
                        name: vm.fileList[i].name,
                        file: vm.fileList[i]
                    }
                );
            }
        }
        else
            this.file = null;
    }

    private uploadFile(fileToUpload: FileUpload) {
        var vm = this;

        var myReader:FileReader = new FileReader();

        myReader.onloadend = function(e) {
            fileToUpload.fileData = myReader.result;
            fileToUpload.file = null;
            vm.log.success('Uploading File ' + fileToUpload.name, null, 'Upload');
            vm.sendToServer(fileToUpload);
        }

        myReader.readAsText(fileToUpload.file);
    }

    private getNextFileToUpload() {
        var vm = this;
        var allUploaded : boolean = true;
        for (let file of vm.filesToUpload) {
            if (file.success == null) {
                vm.uploadFile(file);
                allUploaded = false;
                break;
            }
        };

        if (allUploaded) {
            vm.log.success('All Uploaded Successfully', null, 'Upload');
            vm.log.success('Saving mappings now', null, 'Upload');
            vm.organisationManagerService.endUpload()
                .subscribe(
                    result => {
                        vm.log.success('Mappings saved Successfully ' , null, 'Success');
                        vm.log.success('All Organisations Uploaded Successfully ' , null, 'Success');
                        vm.getOrganisationStatistics();
                        vm.getServiceStatistics();
                        vm.getRegionStatistics();
                        vm.getConflictingOrganisations();
                    },
                    error => vm.log.error('Failed to save mappings', error, 'Upload Bulk Organisations')
                )
        }
    }

    private sendToServer(fileToUpload: FileUpload) {
        var vm = this;
        vm.log.success('Sending To Server', null, 'Upload');
        vm.organisationManagerService.uploadCsv(fileToUpload)
            .subscribe(result => {
                    fileToUpload.success = 1;
                    vm.log.success('File Uploaded Successfully ' + fileToUpload.name, null, 'Success');
                    vm.getNextFileToUpload();
                },
                error => vm.log.error('Failed to upload bulk organisations ' + fileToUpload.name, error, 'Upload Bulk Organisations')
            );
    };

    private getConflictingOrganisations() {
        var vm = this;
        vm.organisationManagerService.getConflictedOrganisations()
            .subscribe(result => vm.conflictedOrgs = result,
                    error => vm.log.error('Failed to get conflicted Organisations', error, 'Get Conflicting Organisations'))
    }

    ok() {
        this.uploadFiles();
    }

    private uploadFiles() {
        var vm = this;
        vm.organisationManagerService.startUpload()
            .subscribe(
                result => {
                    vm.getNextFileToUpload();
                },
                error => vm.log.error('Error starting upload', error, 'Error')
            );

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

    goToRegions() {
        this.$state.go('app.region');
    }
}
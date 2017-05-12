import {Component, Injectable} from "@angular/core";
import {Organisation} from "../organisationManager/models/Organisation";
import {RegionService} from "./region.service";
import {AdminService, LoggerService} from "eds-common-js";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Region} from "./models/Region";
import {OrganisationManagerPickerDialog} from "../organisationManager/organisationManagerPicker.dialog";
import {RegionPickerDialog} from "./regionPicker.dialog";
import {Marker} from "./models/Marker";
import {OrganisationManagerService} from "../organisationManager/organisationManager.service";
import {Dsa} from "../dsa/models/Dsa";
import {DsaPickerDialog} from "../dsa/dsaPicker.dialog";

@Component({
    template: require('./regionEditor.html')
})
export class RegionEditorComponent {
    public accordionClass: string = 'accordionClass';

    region : Region = <Region>{};
    organisations : Organisation[];
    parentRegions : Region[];
    childRegions : Region[];
    sharingAgreements : Dsa[];
    lat: number = 54.4347266;
    lng: number = -4.7194005;
    zoom: number = 6.01;
    markers: Marker[];
    editDisabled : boolean = false;

    orgDetailsToShow = new Organisation().getDisplayItems();
    regionDetailsToShow = new Region().getDisplayItems();
    sharingAgreementsDetailsToShow = new Dsa().getDisplayItems();

    constructor(private $modal: NgbModal,
                private state : StateService,
                private log:LoggerService,
                private organisationManagerService : OrganisationManagerService,
                private adminService : AdminService,
                private regionService : RegionService,
                private transition : Transition,
                protected $state : StateService
    ) {
        this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);
    }

    protected performAction(action:string, itemUuid:string) {
        switch (action) {
            case 'add':
                this.create(itemUuid);
                break;
            case 'edit':
                this.load(itemUuid);
                break;
        }
    }

    create(uuid : string) {
        this.region = {
            name : ''
        } as Region;
    }

    load(uuid : string) {
        var vm = this;
        vm.regionService.getRegion(uuid)
            .subscribe(result =>  {
                    vm.region = result;
                    vm.getRegionOrganisations();
                    vm.getParentRegions();
                    vm.getChildRegions();
                    vm.getOrganisationMarkers();
                    vm.getSharingAgreements();
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;

        // Populate organisations before save
        vm.region.organisations = {};
        for (var idx in this.organisations) {
            var organisation : Organisation = this.organisations[idx];
            this.region.organisations[organisation.uuid] = organisation.name;
        }

        //populate Parent Regions
        vm.region.parentRegions = {};
        for (var idx in this.parentRegions) {
            var region : Region = this.parentRegions[idx];
            this.region.parentRegions[region.uuid] = region.name;
        }

        //populate Parent Regions
        vm.region.childRegions = {};
        for (var idx in this.childRegions) {
            var region : Region = this.childRegions[idx];
            this.region.childRegions[region.uuid] = region.name;
        }

        //populate sharing agreements
        vm.region.sharingAgreements = {};
        for (var idx in this.sharingAgreements) {
            var dsa : Dsa = this.sharingAgreements[idx];
            this.region.sharingAgreements[dsa.uuid] = dsa.name;
        }

        vm.regionService.saveRegion(vm.region)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Item saved', vm.region, 'Saved');
                    if (close) { this.$state.go('app.organisationManagerOverview'); }
                },
                error => vm.log.error('Error saving', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        this.$state.go('app.organisationManagerOverview');
    }

    private getRegionOrganisations() {
        var vm = this;
        vm.regionService.getRegionOrganisations(vm.region.uuid)
            .subscribe(
                result => vm.organisations = result,
                error => vm.log.error('Failed to load region organisations', error, 'Load region organisation')
            );
    }

    private getParentRegions() {
        var vm = this;
        vm.regionService.getParentRegions(vm.region.uuid)
            .subscribe(
                result => vm.parentRegions = result,
                error => vm.log.error('Failed to load parent regions', error, 'Load parent regions')
            );
    }

    private getChildRegions() {
        var vm = this;
        vm.regionService.getChildRegions(vm.region.uuid)
            .subscribe(
                result => vm.childRegions = result,
                error => vm.log.error('Failed to load child regions', error, 'Load child regions')
            );
    }

    private getSharingAgreements() {
        var vm = this;
        vm.regionService.getSharingAgreements(vm.region.uuid)
            .subscribe(
                result => {
                    vm.sharingAgreements = result;
                },
                error => vm.log.error('Failed to load sharing agreements', error, 'Load sharing agreements')

            );
    }

    private getOrganisationMarkers() {
        var vm = this;
        vm.organisationManagerService.getOrganisationMarkers(vm.region.uuid)
            .subscribe(
                result => vm.markers = result,
                error => vm.log.error('Failed to load organisation markers', error, 'Load organisation Markers')
            );
    }

    private editOrganisations() {
        var vm = this;
        OrganisationManagerPickerDialog.open(vm.$modal, vm.organisations, 'organisations')
            .result.then(function (result : Organisation[]) {
            vm.organisations = result;
        });
    }

    private editParentRegions() {
        var vm = this;
        RegionPickerDialog.open(vm.$modal, vm.parentRegions)
            .result.then(function (result : Region[]) {
            vm.parentRegions = result;
        });
    }

    private editChildRegions() {
        var vm = this;
        RegionPickerDialog.open(vm.$modal, vm.childRegions)
            .result.then(function (result : Region[]) {
            vm.childRegions = result;
        });
    }

    private editSharingAgreements() {
        var vm = this;
        DsaPickerDialog.open(vm.$modal, vm.sharingAgreements)
            .result.then(function (result : Dsa[]) {
            vm.sharingAgreements = result;
        });
    }

    editOrganisation(item : Organisation) {
        this.$state.go('app.organisationManagerEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    editRegion(item : Organisation) {
        this.$state.go('app.regionEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    editSharingAgreement(item : Dsa) {
        this.$state.go('app.dsaEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }
}


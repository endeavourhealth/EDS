import {Component} from "@angular/core";
import {Organisation} from "../organisationManager/models/Organisation";
import {Address} from "../organisationManager/models/Address";
import {AdminService} from "../administration/admin.service";
import {LoggerService} from "../common/logger.service";
import {Transition, StateService} from "ui-router-ng2";
import {RegionPickerDialog} from "../region/regionPicker.dialog";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {OrganisationManagerService} from "./organisationManager.service";
import {Region} from "../region/models/Region";
import {OrganisationPickerDialog} from "../organisations/organisationPicker.dialog";
import {OrganisationManagerPickerDialog} from "./organisationManagerPicker.dialog";

@Component({
    template: require('./organisationManagerEditor.html')
})
export class OrganisationManagerEditorComponent {

    region : Region = <Region>{};
    organisation : Organisation = <Organisation>{};
    regions : Region[];
    childOrganisations : Organisation[];
    parentOrganisations : Organisation[];
    services : Organisation[];
    addresses : Address[];
    location : any;
    orgType : string = 'Organisation';

    constructor(private $modal: NgbModal,
                private state : StateService,
                private log:LoggerService,
                private adminService : AdminService,
                private organisationManagerService : OrganisationManagerService,
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
            case 'addService':
                this.createService(itemUuid);
                break;
            case 'edit':
                this.load(itemUuid);
                break;
        }
    }

    createService(uuid : string) {
        var vm = this;
        vm.orgType = 'Service';
        this.organisation = {
            name: '',
            isService: 1,
            bulkImported : 0
        } as Organisation;
    }

    createServiceFromOrg() {
        var parent : Organisation = (JSON.parse(JSON.stringify(this.organisation)));
        this.services = null;
        this.childOrganisations = null;
        this.regions = null;
        this.parentOrganisations = [];
        this.parentOrganisations.push(parent);
        this.organisation.uuid = null;
        this.organisation.isService = 1;
        this.orgType = 'Service';
    }

    create(uuid : string) {
        this.organisation = {
            name: '',
            isService: 0
        } as Organisation;

    }

    load(uuid : string) {
        var vm = this;
        vm.organisationManagerService.getOrganisation(uuid)
            .subscribe(result =>  {
                    vm.organisation = result;
                    vm.getOrganisationRegions();
                    vm.getOrganisationAddresses();
                    vm.getChildOrganisations();
                    vm.getParentOrganisations();
                    vm.getServices();
                    console.log(vm.organisation);
                    console.log(vm.organisation.isService);
                    if (vm.organisation.isService) {
                        vm.orgType = 'Service';
                    }
                    console.log(vm.orgType);
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;
        // Populate organisations regions before save
         vm.organisation.regions = {};
         for (var idx in this.regions) {
         var region : Region = this.regions[idx];
         this.organisation.regions[region.uuid] = region.name;
         }

        vm.organisation.childOrganisations = {};
        for (var idx in this.childOrganisations) {
            var org : Organisation = this.childOrganisations[idx];
            this.organisation.childOrganisations[org.uuid] = org.name;
        }

        vm.organisation.parentOrganisations = {};
        for (var idx in this.parentOrganisations) {
            var org : Organisation = this.parentOrganisations[idx];
            this.organisation.parentOrganisations[org.uuid] = org.name;
        }

        vm.organisation.services = {};
        for (var idx in this.services) {
            var org : Organisation = this.services[idx];
            this.organisation.services[org.uuid] = org.name;
        }

         //Populate Addresses before save
        vm.organisation.addresses = this.addresses;


        vm.organisationManagerService.saveOrganisation(vm.organisation)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Item saved', vm.organisation, 'Saved');
                    if (close) { vm.state.go(vm.transition.from()); }
                },
                error => vm.log.error('Error saving', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        this.state.go(this.transition.from());
    }

    addAddress() {
        var vm = this;

        console.log(vm.addresses);
        var address : Address = <Address>{};
        address.organisationUuid = vm.organisation.uuid;
        address.buildingName = '';
        address.numberAndStreet = '';
        address.locality = '';
        address.city = '';
        address.county = '';
        address.postcode = '';
        vm.addresses.push(address);
        console.log(vm.addresses);
    }

    private editRegions() {
        var vm = this;
        RegionPickerDialog.open(vm.$modal, vm.regions)
            .result.then(function (result : Region[]) {
            vm.regions = result;
        });
    }

    private editChildOrganisations() {
        var vm = this;
        OrganisationManagerPickerDialog.open(vm.$modal, vm.childOrganisations, 'organisation' )
            .result.then(function (result : Organisation[]) {
            vm.childOrganisations = result;
        });
    }

    private editParentOrganisations() {
        var vm = this;
        OrganisationManagerPickerDialog.open(vm.$modal, vm.parentOrganisations, 'organisation' )
            .result.then(function (result : Organisation[]) {
            vm.parentOrganisations = result;
        });
    }

    private editServices() {
        var vm = this;
        OrganisationManagerPickerDialog.open(vm.$modal, vm.services, 'services' )
            .result.then(function (result : Organisation[]) {
            vm.services = result;
        });
    }

    private getOrganisationRegions() {
        var vm = this;
        vm.organisationManagerService.getOrganisationRegions(vm.organisation.uuid)
            .subscribe(
                result => vm.regions = result,
                error => vm.log.error('Failed to load organisation regions', error, 'Load organisation regions')
            );
    }

    private getOrganisationAddresses() {
        var vm = this;
        vm.organisationManagerService.getOrganisationAddresses(vm.organisation.uuid)
            .subscribe(
                result => vm.addresses = result,
                error => vm.log.error('Failed to load organisation Addresses', error, 'Load organisation Addresses')
            );
    }

    private getChildOrganisations() {
        var vm = this;
        vm.organisationManagerService.getChildOrganisations(vm.organisation.uuid)
            .subscribe(
                result => vm.childOrganisations = result,
                error => vm.log.error('Failed to load child organisations', error, 'Load child organisation')
            );
    }

    private getParentOrganisations() {
        var vm = this;
        vm.organisationManagerService.getParentOrganisations(vm.organisation.uuid)
            .subscribe(
                result => vm.parentOrganisations = result,
                error => vm.log.error('Failed to load parent organisations', error, 'Load parent organisation')
            );
    }

    private getServices() {
        var vm = this;
        vm.organisationManagerService.getServices(vm.organisation.uuid)
            .subscribe(
                result => vm.services = result,
                error => vm.log.error('Failed to load services', error, 'Load services')
            );
    }

    editOrganisation(item : Organisation) {
        this.$state.go('app.organisationManagerEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    editRegion(item : Organisation) {
        this.$state.go('app.regionEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }
}

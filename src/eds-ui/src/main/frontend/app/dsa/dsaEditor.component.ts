import {Component} from "@angular/core";
import {DsaService} from "./dsa.service";
import {AdminService, LoggerService} from "eds-common-js";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Dsa} from "./models/Dsa";
import {DataFlowPickerDialog} from "../dataFlow/dataFlowPicker.dialog";
import {DataFlow} from "../dataFlow/models/DataFlow";
import {Region} from "../region/models/Region";
import {RegionPickerDialog} from "../region/regionPicker.dialog";
import {Organisation} from "../organisationManager/models/Organisation";
import {OrganisationManagerPickerDialog} from "../organisationManager/organisationManagerPicker.dialog";
import {DsaPurpose} from "./models/DsaPurpose";
import {PurposeAddDialog} from "./purposeAdd.dialog";

@Component({
    template: require('./dsaEditor.html')
})
export class DsaEditorComponent {
    public accordionClass: string = 'accordionClass';

    dsa : Dsa = <Dsa>{};
    dataFlows : DataFlow[];
    regions : Region[];
    publishers : Organisation[];
    subscribers : Organisation[];
    purposes : DsaPurpose[];

    status = [
        {num: 0, name : "Active"},
        {num: 1, name : "Inactive"}
    ];

    dataflowDetailsToShow = new DataFlow().getDisplayItems();
    regionDetailsToShow = new Region().getDisplayItems();
    orgDetailsToShow = new Organisation().getDisplayItems();
    purposeDetailsToShow = new DsaPurpose().getDisplayItems();

    constructor(private $modal: NgbModal,
                private state : StateService,
                private log:LoggerService,
                private adminService : AdminService,
                private dsaService : DsaService,
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
        this.dsa = {
            name : ''
        } as Dsa;
    }

    load(uuid : string) {
        var vm = this;
        vm.dsaService.getDsa(uuid)
            .subscribe(result =>  {
                    vm.dsa = result;
                    vm.getLinkedDataFlows();
                    vm.getLinkedRegions();
                    vm.getPublishers();
                    vm.getSubscribers();
                    vm.getPurposes();
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;
        // Populate data flows before save
        vm.dsa.dataFlows= {};
        for (var idx in this.dataFlows) {
            var dataflow : DataFlow = this.dataFlows[idx];
            this.dsa.dataFlows[dataflow.uuid] = dataflow.name;
        }

        // Populate regions before save
        vm.dsa.regions= {};
        for (var idx in this.regions) {
            var region : Region = this.regions[idx];
            this.dsa.regions[region.uuid] = region.name;
        }

        // Populate publishers before save
        vm.dsa.publishers = {};
        for (var idx in this.publishers) {
            var pub : Organisation = this.publishers[idx];
            this.dsa.publishers[pub.uuid] = pub.name;
        }

        // Populate subscribers before save
        vm.dsa.subscribers = {};
        for (var idx in this.subscribers) {
            var sub : Organisation = this.subscribers[idx];
            this.dsa.subscribers[sub.uuid] = sub.name;
        }

        // Populate purposes before save
        vm.dsa.purposes = [];
        vm.dsa.purposes = this.purposes;

        vm.dsaService.saveDsa(vm.dsa)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Data Sharing Agreement saved', vm.dsa, 'Saved');
                    if (close) { vm.close();}
                },
                error => vm.log.error('Error saving Data Sharing Agreement', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        window.history.back();
    }

    private editDataFlows() {
        var vm = this;
        DataFlowPickerDialog.open(vm.$modal, vm.dataFlows)
            .result.then(function
                (result : DataFlow[]) { vm.dataFlows = result;},
                () => vm.log.info('Edit Data Flows cancelled')
        );
    }

    private editRegion(item : DataFlow) {
        this.$state.go('app.regionEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    private editRegions() {
        var vm = this;
        RegionPickerDialog.open(vm.$modal, vm.regions)
            .result.then(function
                (result : Region[]) { vm.regions = result; },
                () => vm.log.info('Edit Regions cancelled')
        );
    }

    private editPublishers() {
        var vm = this;
        OrganisationManagerPickerDialog.open(vm.$modal, vm.publishers, 'organisation')
            .result.then(function
                (result : Organisation[]) { vm.publishers = result; },
            () => vm.log.info('Edit Publishers cancelled')
        );
    }

    private editSubscribers() {
        var vm = this;
        OrganisationManagerPickerDialog.open(vm.$modal, vm.subscribers, 'organisation')
            .result.then(function
                (result : Organisation[]) { vm.subscribers = result; },
            () => vm.log.info('Edit Subscribers cancelled')
        );
    }

    private editPurposes() {
        var vm = this;
        PurposeAddDialog.open(vm.$modal, vm.purposes)
            .result.then(function
                (result : DsaPurpose[]) { vm.purposes= result; },
            () => vm.log.info('Edit Purposes cancelled')
        );
    }

    private editDataFlow(item : DataFlow) {
        this.$state.go('app.dataFlowEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    private getLinkedDataFlows() {
        var vm = this;
        vm.dsaService.getLinkedDataFlows(vm.dsa.uuid)
            .subscribe(
                result => vm.dataFlows = result,
                error => vm.log.error('Failed to load linked Data Flows', error, 'Load Linked Data Flows')
            );
    }

    private getLinkedRegions() {
        var vm = this;
        vm.dsaService.getLinkedRegions(vm.dsa.uuid)
            .subscribe(
                result => vm.regions = result,
                error => vm.log.error('Failed to load linked Regions', error, 'Load Linked Regions')
            );
    }

    private getPublishers() {
        var vm = this;
        vm.dsaService.getPublishers(vm.dsa.uuid)
            .subscribe(
                result => vm.publishers = result,
                error => vm.log.error('Failed to load publishers', error, 'Load Publishers')
            );
    }

    private getSubscribers() {
        var vm = this;
        vm.dsaService.getSubscribers(vm.dsa.uuid)
            .subscribe(
                result => vm.subscribers = result,
                error => vm.log.error('Failed to load subscribers', error, 'Load Subscribers')
            );
    }

    private getPurposes() {
        var vm = this;
        vm.dsaService.getPurposes(vm.dsa.uuid)
            .subscribe(
                result => vm.purposes = result,
                error => vm.log.error('Failed to load purposes', error, 'Load Purposes')
            );
    }
}

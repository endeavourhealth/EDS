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

@Component({
    template: require('./dsaEditor.html')
})
export class DsaEditorComponent {
    dsa : Dsa = <Dsa>{};
    dataFlows : DataFlow[];
    regions : Region[];

    status = [
        {num: 0, name : "Active"},
        {num: 1, name : "Inactive"}
    ];

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
}

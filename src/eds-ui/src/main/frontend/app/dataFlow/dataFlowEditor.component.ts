import {Component} from "@angular/core";
import {DataFlowService} from "./dataFlow.service";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DataFlow} from "./models/DataFlow";
import {DsaPickerDialog} from "../dsa/dsaPicker.dialog";
import {DpaPickerDialog} from "../dpa/dpaPicker.dialog";
import {Dsa} from "../dsa/models/Dsa";
import {Dpa} from "../dpa/models/Dpa";
import {AdminService, LoggerService} from "eds-common-js";

@Component({
    template: require('./dataFlowEditor.html')
})
export class DataFlowEditorComponent {
    dataFlow : DataFlow = <DataFlow>{};
    dsas : Dsa[];
    dpas : Dpa[];

    flowDirections = [
        {num: 0, name : "Inbound"},
        {num: 1, name : "Outbound"}
    ];

    flowSchedules = [
        {num: 0, name : "Daily"},
        {num: 1, name : "On Demand"}
    ];

    exchangeMethod = [
        {num: 0, name : "Paper"},
        {num: 1, name : "Electronic"}
    ];

    flowStatus = [
        {num: 0, name : "In Development"},
        {num: 1, name : "Live"}
    ];

    constructor(private $modal: NgbModal,
                private state : StateService,
                private log:LoggerService,
                private adminService : AdminService,
                private dataFlowService : DataFlowService,
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
        this.dataFlow = {
            name : ''
        } as DataFlow;
    }

    load(uuid : string) {
        var vm = this;
        vm.dataFlowService.getDataFlow(uuid)
            .subscribe(result =>  {
                    vm.dataFlow = result;
                    vm.getLinkedDpas();
                    vm.getLinkedDsas();
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;
        // Populate Data Sharing Agreements before save
        vm.dataFlow.dsas= {};
        for (var idx in this.dsas) {
            var dsa : Dsa = this.dsas[idx];
            this.dataFlow.dsas[dsa.uuid] = dsa.name;
        }

        // Populate Data Processing Agreements before save
        vm.dataFlow.dpas= {};
        for (var idx in this.dpas) {
            var dpa : Dpa = this.dpas[idx];
            this.dataFlow.dpas[dpa.uuid] = dpa.name;
        }

        vm.dataFlowService.saveDataFlow(vm.dataFlow)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Data Flow saved', vm.dataFlow, 'Saved');
                    if (close) { vm.close(); }
                },
                error => vm.log.error('Error saving Data Flow', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        window.history.back();
    }


    toNumber(){
        this.dataFlow.directionId = +this.dataFlow.directionId;
        console.log(this.dataFlow.directionId);
    }

    private editDataSharingAgreements() {
        var vm = this;
        DsaPickerDialog.open(vm.$modal, vm.dsas)
            .result.then(function
                (result : Dsa[]) { vm.dsas = result; },
                () => vm.log.info('Edit Data Sharing Agreements cancelled')
        );
    }

    private editDataProcessingAgreements() {
        var vm = this;
        DpaPickerDialog.open(vm.$modal, vm.dpas)
            .result.then(function
                (result : Dpa[]) { vm.dpas = result; },
                () => vm.log.info('Edit Data Processing Agreements cancelled')
        );
    }

    private editDataSharingAgreement(item : Dsa) {
        this.$state.go('app.dsaEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    private editDataProcessingAgreement(item : Dpa) {
        this.$state.go('app.dpaEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    private getLinkedDpas() {
        var vm = this;
        vm.dataFlowService.getLinkedDpas(vm.dataFlow.uuid)
            .subscribe(
                result => vm.dpas = result,
                error => vm.log.error('Failed to load linked Data Processing Agreement', error, 'Load Linked Data Processing Agreement')
            );
    }

    private getLinkedDsas() {
        var vm = this;
        vm.dataFlowService.getLinkedDsas(vm.dataFlow.uuid)
            .subscribe(
                result => vm.dsas = result,
                error => vm.log.error('Failed to load linked Data Sharing Agreement', error, 'Load Linked Data Sharing Agreement')
            );
    }
}

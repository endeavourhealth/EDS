import {Component} from "@angular/core";
import {AdminService} from "../administration/admin.service";
import {DataFlowService} from "./dataFlow.service";
import {LoggerService} from "../common/logger.service";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DataFlow} from "./models/DataFlow";

@Component({
    template: require('./dataFlowEditor.html')
})
export class DataFlowEditorComponent {
    dataFlow : DataFlow = <DataFlow>{};

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
                    console.log(result);
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;

        vm.dataFlowService.saveDataFlow(vm.dataFlow)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Item saved', vm.dataFlow, 'Saved');
                    if (close) { vm.$state.go('app.dataSharingSummaryOverview'); }
                },
                error => vm.log.error('Error saving', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        this.$state.go('app.dataSharingSummaryOverview');
    }


    toNumber(){
        this.dataFlow.directionId = +this.dataFlow.directionId;
        console.log(this.dataFlow.directionId);
    }
}

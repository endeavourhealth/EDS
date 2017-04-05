import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {DataFlowService} from "./dataFlow.service";
import {DataFlow} from "./models/DataFlow";
import {LoggerService, MessageBoxDialog} from "eds-common-js";

@Component({
    template: require('./dataFlow.html')
})
export class DataFlowComponent {
    dataflows : DataFlow[] = [];

    constructor(private $modal: NgbModal,
                private dataFlowService : DataFlowService,
                private log : LoggerService,
                protected $state : StateService) {
        this.getCohorts();
    }

    getCohorts() {

        var vm = this;
        vm.dataFlowService.getAllDataFlows()
            .subscribe(
                result => vm.dataflows = result,
                error => vm.log.error('Failed to load data flows', error, 'Load data flows')
            );
    }

    add() {
        this.$state.go('app.dataFlowEditor', {itemUuid: null, itemAction: 'add'});
    }

    edit(item : DataFlow) {
        this.$state.go('app.dataFlowEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    delete(item : DataFlow) {
        var vm = this;
        MessageBoxDialog.open(vm.$modal, 'Delete Data Flow', 'Are you sure you want to delete the data flow?', 'Yes', 'No')
            .result.then(
            () => vm.doDelete(item),
            () => vm.log.info('Delete cancelled')
        );
    }

    doDelete(item : DataFlow) {
        var vm = this;
        vm.dataFlowService.deleteDataFlow(item.uuid)
            .subscribe(
                () => {
                    var index = vm.dataflows.indexOf(item);
                    vm.dataflows.splice(index, 1);
                    vm.log.success('Data flow deleted', item, 'Delete Data flow');
                },
                (error) => vm.log.error('Failed to delete Data flow', error, 'Delete Data flow')
            );
    }

    close() {
        this.$state.go('app.dataSharingSummaryOverview');
    }
}

import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {DataSetService} from "./dataSet.service";
import {DataSet} from "./models/Dataset";
import {LoggerService, MessageBoxDialog} from "eds-common-js";

@Component({
    template: require('./dataSet.html')
})
export class DataSetComponent {
    datasets : DataSet[] = [];

    constructor(private $modal: NgbModal,
                private dataSetService : DataSetService,
                private log : LoggerService,
                protected $state : StateService) {
        this.getDataSets();
    }

    getDataSets() {

        var vm = this;
        vm.dataSetService.getAllDataSets()
            .subscribe(
                result => vm.datasets = result,
                error => vm.log.error('Failed to load data flows', error, 'Load data flows')
            );
    }

    add() {
        this.$state.go('app.dataSetEditor', {itemUuid: null, itemAction: 'add'});
    }

    edit(item : DataSet) {
        this.$state.go('app.dataSetEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    delete(item : DataSet) {
        var vm = this;
        MessageBoxDialog.open(vm.$modal, 'Delete Data Set', 'Are you sure you want to delete the data set?', 'Yes', 'No')
            .result.then(
            () => vm.doDelete(item),
            () => vm.log.info('Delete cancelled')
        );
    }

    doDelete(item : DataSet) {
        var vm = this;
        vm.dataSetService.deleteDataSet(item.uuid)
            .subscribe(
                () => {
                    var index = vm.datasets.indexOf(item);
                    vm.datasets.splice(index, 1);
                    vm.log.success('Data set deleted', item, 'Delete Data set');
                },
                (error) => vm.log.error('Failed to delete Data set', error, 'Delete Data flow')
            );
    }

    close() {
        this.$state.go('app.dataSharingSummaryOverview');
    }
}

import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {LoggerService} from "../common/logger.service";
import {DataSharingSummaryService} from "./dataSharingSummary.service";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";
import {DataSharingSummary} from "./models/DataSharingSummary";

@Component({
    template: require('./dataSharingSummary.html')
})
export class DataSharingSummaryComponent {
    dataSharingSummaries : DataSharingSummary[] = [];

    constructor(private $modal: NgbModal,
                private dataSharingSummaryService : DataSharingSummaryService,
                private log : LoggerService,
                protected $state : StateService) {
        this.getDataSharingSummaries();
    }

    getDataSharingSummaries() {

        var vm = this;
        vm.dataSharingSummaryService.getAllDataSharingSummaries()
            .subscribe(
                result => vm.dataSharingSummaries = result,
                error => vm.log.error('Failed to load data sharing summaries', error, 'Load data sharing summaries')
            );
    }

    add() {
        this.$state.go('app.dataSharingSummaryEditor', {itemUuid: null, itemAction: 'add'});
    }

    edit(item : DataSharingSummary) {
        this.$state.go('app.dataSharingSummaryEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    delete(item : DataSharingSummary) {
        var vm = this;
        MessageBoxDialog.open(vm.$modal, 'Delete Data Sharing Summary', 'Are you sure you want to delete the Data Sharing Summary?', 'Yes', 'No')
            .result.then(
            () => vm.doDelete(item),
            () => vm.log.info('Delete cancelled')
        );
    }

    doDelete(item : DataSharingSummary) {
        var vm = this;
        vm.dataSharingSummaryService.deleteDataSharingSummary(item.uuid)
            .subscribe(
                () => {
                    var index = vm.dataSharingSummaries.indexOf(item);
                    vm.dataSharingSummaries.splice(index, 1);
                    vm.log.success('Data Sharing Summary deleted', item, 'Delete Data Sharing Summary');
                },
                (error) => vm.log.error('Failed to delete Data Sharing Summary', error, 'Delete Data Sharing Summary')
            );
    }
}

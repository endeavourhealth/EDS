import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {LoggerService, MessageBoxDialog} from "eds-common-js";
import {DpaService} from "./dpa.service";
import {Dpa} from "./models/Dpa";

@Component({
    template: require('./dpa.html')
})
export class DpaComponent {
    dpas : Dpa[] = [];

    constructor(private $modal: NgbModal,
                private dpaService : DpaService,
                private log : LoggerService,
                protected $state : StateService) {
        this.getDsas();
    }

    getDsas() {

        var vm = this;
        vm.dpaService.getAllDpas()
            .subscribe(
                result => vm.dpas = result,
                error => vm.log.error('Failed to load dpas', error, 'Load dpa')
            );
    }

    add() {
        this.$state.go('app.dpaEditor', {itemUuid: null, itemAction: 'add'});
    }

    edit(item : Dpa) {
        this.$state.go('app.dpaEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    delete(item : Dpa) {
        var vm = this;
        MessageBoxDialog.open(vm.$modal, 'Delete Data Processing Agreement', 'Are you sure you want to delete the Data Processing Agreement?', 'Yes', 'No')
            .result.then(
            () => vm.doDelete(item),
            () => vm.log.info('Delete cancelled')
        );
    }

    doDelete(item : Dpa) {
        var vm = this;
        vm.dpaService.deleteDpa(item.uuid)
            .subscribe(
                () => {
                    var index = vm.dpas.indexOf(item);
                    vm.dpas.splice(index, 1);
                    vm.log.success('Data Sharing Processing deleted', item, 'Delete Data Processing Agreement');
                },
                (error) => vm.log.error('Failed to delete Data Processing Agreement', error, 'Delete Data Processing Agreement')
            );
    }

    close() {
        this.$state.go('app.dataSharingSummaryOverview');
    }
}

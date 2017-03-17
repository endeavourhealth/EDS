import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {LoggerService} from "../common/logger.service";
import {DsaService} from "./dsa.service";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";
import {Dsa} from "./models/Dsa";

@Component({
    template: require('./dsa.html')
})
export class DsaComponent {
    dsas : Dsa[] = [];

    constructor(private $modal: NgbModal,
                private dsaService : DsaService,
                private log : LoggerService,
                protected $state : StateService) {
        this.getDsas();
    }

    getDsas() {

        var vm = this;
        vm.dsaService.getAllDsas()
            .subscribe(
                result => vm.dsas = result,
                error => vm.log.error('Failed to load dsas', error, 'Load dsa')
            );
    }

    add() {
        this.$state.go('app.dsaEditor', {itemUuid: null, itemAction: 'add'});
    }

    edit(item : Dsa) {
        this.$state.go('app.dsaEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    delete(item : Dsa) {
        var vm = this;
        MessageBoxDialog.open(vm.$modal, 'Delete Data Sharing Agreement', 'Are you sure you want to delete the Data Sharing Agreement?', 'Yes', 'No')
            .result.then(
            () => vm.doDelete(item),
            () => vm.log.info('Delete cancelled')
        );
    }

    doDelete(item : Dsa) {
        var vm = this;
        vm.dsaService.deleteDsa(item.uuid)
            .subscribe(
                () => {
                    var index = vm.dsas.indexOf(item);
                    vm.dsas.splice(index, 1);
                    vm.log.success('Data Sharing Agreement deleted', item, 'Delete Data Sharing Agreement');
                },
                (error) => vm.log.error('Failed to delete Data Sharing Agreement', error, 'Delete Data Sharing Agreement')
            );
    }
}

import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {CohortService} from "./cohort.service";
import {Cohort} from "./models/Cohort";
import {LoggerService, MessageBoxDialog} from "eds-common-js";

@Component({
    template: require('./cohort.html')
})
export class CohortComponent {
    cohorts : Cohort[] = [];

    constructor(private $modal: NgbModal,
                private cohortService : CohortService,
                private log : LoggerService,
                protected $state : StateService) {
        this.getCohorts();
    }

    getCohorts() {

        var vm = this;
        vm.cohortService.getAllCohorts()
            .subscribe(
                result => vm.cohorts = result,
                error => vm.log.error('Failed to load cohorts', error, 'Load cohorts')
            );
    }

    add() {
        this.$state.go('app.cohortEditor', {itemUuid: null, itemAction: 'add'});
    }

    edit(item : Cohort) {
        this.$state.go('app.cohortEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    delete(item : Cohort) {
        var vm = this;
        MessageBoxDialog.open(vm.$modal, 'Delete Cohort', 'Are you sure you want to delete the Cohort?', 'Yes', 'No')
            .result.then(
            () => vm.doDelete(item),
            () => vm.log.info('Delete cancelled')
        );
    }

    doDelete(item : Cohort) {
        var vm = this;
        vm.cohortService.deleteCohort(item.uuid)
            .subscribe(
                () => {
                    var index = vm.cohorts.indexOf(item);
                    vm.cohorts.splice(index, 1);
                    vm.log.success('Cohort deleted', item, 'Delete Cohort');
                },
                (error) => vm.log.error('Failed to delete Cohort', error, 'Delete Cohort')
            );
    }

    close() {
        this.$state.go('app.dataSharingSummaryOverview');
    }
}

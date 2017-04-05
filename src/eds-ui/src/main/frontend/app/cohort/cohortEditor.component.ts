import {Component} from "@angular/core";
import {CohortService} from "./cohort.service";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Cohort} from "./models/Cohort";
import {DpaPickerDialog} from "../dpa/dpaPicker.dialog";
import {Dpa} from "../dpa/models/Dpa";
import {AdminService, LoggerService} from "eds-common-js";

@Component({
    template: require('./cohortEditor.html')
})
export class CohortEditorComponent {
    cohort : Cohort = <Cohort>{};
    dpas : Dpa[];

    constructor(private $modal: NgbModal,
                private state : StateService,
                private log:LoggerService,
                private adminService : AdminService,
                private cohortService : CohortService,
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
        this.cohort = {
            name : ''
        } as Cohort;
    }

    load(uuid : string) {
        var vm = this;
        vm.cohortService.getCohort(uuid)
            .subscribe(result =>  {
                    vm.cohort = result;
                    vm.getLinkedDpas();
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;

        // Populate Data Processing Agreements before save
        vm.cohort.dpas= {};
        for (var idx in this.dpas) {
            var dpa : Dpa = this.dpas[idx];
            this.cohort.dpas[dpa.uuid] = dpa.name;
        }

        vm.cohortService.saveCohort(vm.cohort)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Item saved', vm.cohort, 'Saved');
                    if (close) { vm.$state.go('app.dataSharingSummaryOverview'); }
                },
                error => vm.log.error('Error saving', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        this.$state.go('app.dataSharingSummaryOverview');
    }

    private editDataProcessingAgreements() {
        var vm = this;
        DpaPickerDialog.open(vm.$modal, vm.dpas)
            .result.then(function (result : Dpa[]) {
            vm.dpas = result;
        });
    }

    private editDataProcessingAgreement(item : Dpa) {
        this.$state.go('app.dpaEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    private getLinkedDpas() {
        var vm = this;
        vm.cohortService.getLinkedDpas(vm.cohort.uuid)
            .subscribe(
                result => vm.dpas = result,
                error => vm.log.error('Failed to load linked Data Processing Agreement', error, 'Load Linked Data Processing Agreement')
            );
    }
}

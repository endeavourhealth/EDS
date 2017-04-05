import {Component} from "@angular/core";
import {DpaService} from "./dpa.service";
import {AdminService, LoggerService} from "eds-common-js";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Dpa} from "./models/Dpa";
import {DataFlow} from "../dataFlow/models/DataFlow";
import {Cohort} from "../cohort/models/Cohort";
import {DataFlowPickerDialog} from "../dataFlow/dataFlowPicker.dialog";
import {CohortPickerDialog} from "../cohort/cohortPicker.dialog";

@Component({
    template: require('./dpaEditor.html')
})
export class DpaEditorComponent {
    dpa : Dpa = <Dpa>{};
    dataFlows : DataFlow[];
    cohorts : Cohort[];

    status = [
        {num: 0, name : "Active"},
        {num: 1, name : "Inactive"}
    ];

    storageProtocols = [
        {num: 0, name : "Audit only"},
        {num: 1, name : "Temporary Store And Forward"},
        {num: 2, name : "Permanent Record Store"}
    ];

    constructor(private $modal: NgbModal,
                private state : StateService,
                private log:LoggerService,
                private adminService : AdminService,
                private dpaService : DpaService,
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
        this.dpa = {
            name : ''
        } as Dpa;
    }

    load(uuid : string) {
        var vm = this;
        vm.dpaService.getDpa(uuid)
            .subscribe(result =>  {
                    vm.dpa = result;
                    vm.getLinkedDataFlows();
                    vm.getLinkedCohorts();
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;

        // Populate Data Flows before save
        vm.dpa.dataFlows = {};
        for (var idx in this.dataFlows) {
            var dataflow : DataFlow = this.dataFlows[idx];
            this.dpa.dataFlows[dataflow.uuid] = dataflow.name;
        }

        // Populate Cohorts before save
        vm.dpa.cohorts= {};
        for (var idx in this.cohorts) {
            var cohort : Cohort = this.cohorts[idx];
            this.dpa.cohorts[cohort.uuid] = cohort.name;
        }

        vm.dpaService.saveDpa(vm.dpa)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Item saved', vm.dpa, 'Saved');
                    if (close) { vm.$state.go('app.dataSharingSummaryOverview'); }
                },
                error => vm.log.error('Error saving', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        this.$state.go('app.dataSharingSummaryOverview');
    }

    private editDataFlows() {
        var vm = this;
        DataFlowPickerDialog.open(vm.$modal, vm.dataFlows)
            .result.then(function (result : DataFlow[]) {
            vm.dataFlows = result;
        });
    }

    private editCohorts() {
        var vm = this;
        CohortPickerDialog.open(vm.$modal, vm.cohorts)
            .result.then(function (result : Cohort[]) {
            vm.cohorts = result;
        });
    }

    private editDataFlow(item : DataFlow) {
        this.$state.go('app.dataFlowEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    private editCohort(item : Cohort) {
        this.$state.go('app.cohortEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    private getLinkedCohorts() {
        var vm = this;
        vm.dpaService.getLinkedCohorts(vm.dpa.uuid)
            .subscribe(
                result => vm.cohorts = result,
                error => vm.log.error('Failed to load linked Cohorts', error, 'Load Linked Cohorts')
            );
    }

    private getLinkedDataFlows() {
        var vm = this;
        vm.dpaService.getLinkedDataFlows(vm.dpa.uuid)
            .subscribe(
                result => vm.dataFlows = result,
                error => vm.log.error('Failed to load linked Data Flows', error, 'Load Linked Data Flows')
            );
    }
}

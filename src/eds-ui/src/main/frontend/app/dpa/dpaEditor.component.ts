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
import {DataSet} from "../dataSet/models/Dataset";
import {DataSetPickerDialog} from "../dataSet/dataSetPicker.dialog";

@Component({
    template: require('./dpaEditor.html')
})
export class DpaEditorComponent {
    public accordionClass: string = 'accordionClass';

    dpa: Dpa = <Dpa>{};
    dataFlows: DataFlow[];
    cohorts: Cohort[];
    dataSets: DataSet[];
    editDisabled : boolean = false;
    processor : string = 'Discovery';

    status = [
        {num: 0, name: "Active"},
        {num: 1, name: "Inactive"}
    ];

    storageProtocols = [
        {num: 0, name: "Audit only"},
        {num: 1, name: "Temporary Store And Forward"},
        {num: 2, name: "Permanent Record Store"}
    ];

    dataFlowDetailsToShow = new DataFlow().getDisplayItems();
    datasetDetailsToShow = new DataSet().getDisplayItems();
    cohortDetailsToShow = new Cohort().getDisplayItems();

    constructor(private $modal: NgbModal,
                private log: LoggerService,
                private adminService: AdminService,
                private dpaService: DpaService,
                private transition: Transition,
                protected $state: StateService) {
        this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);
    }

    protected performAction(action: string, itemUuid: string) {
        switch (action) {
            case 'add':
                this.create(itemUuid);
                break;
            case 'edit':
                this.load(itemUuid);
                break;
        }
    }

    create(uuid: string) {
        this.dpa = {
            name: ''
        } as Dpa;
    }

    load(uuid: string) {
        var vm = this;
        vm.dpaService.getDpa(uuid)
            .subscribe(result => {
                    vm.dpa = result;
                    vm.getLinkedDataFlows();
                    vm.getLinkedCohorts();
                    vm.getLinkedDataSets();
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close: boolean) {
        var vm = this;

        // Populate Data Flows before save
        vm.dpa.dataFlows = {};
        for (var idx in this.dataFlows) {
            var dataflow: DataFlow = this.dataFlows[idx];
            this.dpa.dataFlows[dataflow.uuid] = dataflow.name;
        }

        // Populate Cohorts before save
        vm.dpa.cohorts = {};
        for (var idx in this.cohorts) {
            var cohort: Cohort = this.cohorts[idx];
            this.dpa.cohorts[cohort.uuid] = cohort.name;
        }

        //Populate DataSets before save
        vm.dpa.dataSets = {};
        for (var idx in this.dataSets) {
            var dataset: DataSet = this.dataSets[idx];
            this.dpa.dataSets[dataset.uuid] = dataset.name;
        }

        vm.dpaService.saveDpa(vm.dpa)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Data Processing Agreement saved', vm.dpa, 'Saved');
                    if (close) { vm.close(); }
                },
                error => vm.log.error('Error saving Data Processing Agreemen', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        window.history.back();
    }

    private editDataFlows() {
        var vm = this;
        DataFlowPickerDialog.open(vm.$modal, vm.dataFlows)
            .result.then(function
                (result: DataFlow[]) { vm.dataFlows = result; },
                () => vm.log.info('Edit Data Flows cancelled')
        );
    }

    private editCohorts() {
        var vm = this;
        CohortPickerDialog.open(vm.$modal, vm.cohorts)
            .result.then(function
                (result: Cohort[]) { vm.cohorts = result; },
                () => vm.log.info('Edit Cohorts cancelled')
        );
    }

    private editDataSets() {
        var vm = this;
        DataSetPickerDialog.open(vm.$modal, vm.dataSets)
            .result.then(function
                (result: DataSet[]) { vm.dataSets = result; },
                () => vm.log.info('Edit Data Sets cancelled')
        );
    }

    private editDataFlow(item: DataFlow) {
        this.$state.go('app.dataFlowEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    private editCohort(item: Cohort) {
        this.$state.go('app.cohortEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    private editDataSet(item: DataSet) {
        this.$state.go('app.dataSetEditor', {itemUuid: item.uuid, itemAction: 'edit'});
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

    private getLinkedDataSets() {
        var vm = this;
        vm.dpaService.getLinkedDataSets(vm.dpa.uuid)
            .subscribe(
                result => vm.dataSets = result,
                error => vm.log.error('Failed to load linked Data Sets', error, 'Load Linked Data Sets')
            );
    }
}
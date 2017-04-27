import {Component, Input} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {DataSharingSummary} from "./models/DataSharingSummary";
import {AdminService, LoggerService} from "eds-common-js";
import {DataSharingSummaryService} from "./dataSharingSummary.service";
import {Region} from "../region/models/Region";
import {OrganisationManagerStatistics} from "../organisationManager/models/OrganisationManagerStatistics";

@Component({
    template: require('./dataSharingSummaryOverview.html')
})
export class DataSharingSummaryOverviewComponent {
    private file : File;

    summaryStats : OrganisationManagerStatistics[];
    dpaStats : OrganisationManagerStatistics[];
    dsaStats : OrganisationManagerStatistics[];
    dataflowStats : OrganisationManagerStatistics[];
    cohortStats : OrganisationManagerStatistics[];
    datasetStats : OrganisationManagerStatistics[];

    constructor(private $modal: NgbModal,
                private dataSharingSummaryService: DataSharingSummaryService,
                private adminService : AdminService,
                private log: LoggerService,
                protected $state: StateService) {
        this.getOverview();
    }

    getOverview() {
        var vm = this;
        //vm.getSummaryStatistics();
        vm.getDpaStatistics();
        vm.getDsaStatistics();
        vm.getDataFlowStatistics();
        vm.getCohortStatistics();
        vm.getDataSetStatistics();
    }

    getSummaryStatistics() {
        var vm= this;
        vm.dataSharingSummaryService.getStatistics('summary')
            .subscribe(result => {
                    vm.summaryStats = result
                },
                error => vm.log.error('Failed to load Data Sharing Summary statistics', error, 'Load Data Sharing Summary statistics')
            );
    }

    getDpaStatistics() {
        var vm= this;
        vm.dataSharingSummaryService.getStatistics('dpa')
            .subscribe(result => {
                    vm.dpaStats = result
                },
                error => vm.log.error('Failed to load Data Processing Agreement statistics', error, 'Load Data Processing Agreement statistics')
            );
    }

    getDsaStatistics() {
        var vm= this;
        vm.dataSharingSummaryService.getStatistics('dsa')
            .subscribe(result => {
                    vm.dsaStats = result
                },
                error => vm.log.error('Failed to load Data Sharing Agreement statistics', error, 'Load Data Sharing Agreement statistics')
            );
    }

    getDataFlowStatistics() {
        var vm= this;
        vm.dataSharingSummaryService.getStatistics('dataflow')
            .subscribe(result => {
                    vm.dataflowStats = result
                },
                error => vm.log.error('Failed to load Data Flow statistics', error, 'Load Data Flow statistics')
            );
    }

    getCohortStatistics() {
        var vm= this;
        vm.dataSharingSummaryService.getStatistics('cohort')
            .subscribe(result => {
                    vm.cohortStats = result
                },
                error => vm.log.error('Failed to load Cohort statistics', error, 'Load Cohort statistics')
            );
    }

    getDataSetStatistics() {
        var vm= this;
        vm.dataSharingSummaryService.getStatistics('dataset')
            .subscribe(result => {
                    vm.datasetStats = result
                },
                error => vm.log.error('Failed to load Dataset statistics', error, 'Load Dataset statistics')
            );
    }

    fileChange(event) {
        let fileList: FileList = event.target.files;
        if(fileList.length > 0)
            this.file = fileList[0];
        else
            this.file = null;
    }

    goToSummary() {
        this.$state.go('app.dataSharingSummary');
    }

    goToDpa() {
        this.$state.go('app.dpa');
    }

    goToDsa() {
        this.$state.go('app.dsa');
    }

    goToDataFlow() {
        this.$state.go('app.dataFlow');
    }

    goToCohorts() {
        this.$state.go('app.cohort');
    }

    goToDataSets() {
        this.$state.go('app.dataSet');
    }
}
import {Component} from "@angular/core";
import {AdminService} from "../administration/admin.service";
import {DataSharingSummaryService} from "./dataSharingSummary.service";
import {LoggerService} from "../common/logger.service";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DataSharingSummary} from "./models/DataSharingSummary";

@Component({
    template: require('./dataSharingSummaryEditor.html')
})
export class DataSharingSummaryEditorComponent {
    dataSharingSummary : DataSharingSummary = <DataSharingSummary>{};

    NatureOfInformation = [
        {num: 0, name : "Personal"},
        {num: 1, name : "Personal Sensitive"},
        {num: 2, name : "Commercial"}
    ];

    FormatTypes = [
        {num: 0, name : "Removable Media"},
        {num: 1, name : "Electronic Structured Data"}
    ];

    DataSubjectTypes = [
        {num: 0, name : "Patient"}
    ];

    ReviewCycles = [
        {num: 0, name : "Annually"},
        {num: 1, name : "Monthly"},
        {num: 2, name : "Weekly"}
    ];

    constructor(private $modal: NgbModal,
                private state : StateService,
                private log:LoggerService,
                private adminService : AdminService,
                private dataSharingSummaryService : DataSharingSummaryService,
                private transition : Transition
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
        this.dataSharingSummary = {
            name : ''
        } as DataSharingSummary;
    }

    load(uuid : string) {
        var vm = this;
        vm.dataSharingSummaryService.getDataSharingSummary(uuid)
            .subscribe(result =>  {
                    vm.dataSharingSummary = result;
                    console.log(result);
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;
        console.log(vm.dataSharingSummary);

        vm.dataSharingSummaryService.saveDataSharingSummary(vm.dataSharingSummary)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Item saved', vm.dataSharingSummary, 'Saved');
                    if (close) { vm.state.go(vm.transition.from()); }
                },
                error => vm.log.error('Error saving', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        this.state.go(this.transition.from());
    }
}

import {Component} from "@angular/core";
import {Organisation} from "../organisationManager/models/Organisation";
import {AdminService} from "../administration/admin.service";
import {CohortService} from "./cohort.service";
import {LoggerService} from "../common/logger.service";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Cohort} from "./models/Cohort";

@Component({
    template: require('./cohortEditor.html')
})
export class CohortEditorComponent {
    cohort : Cohort = <Cohort>{};

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
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;

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
}

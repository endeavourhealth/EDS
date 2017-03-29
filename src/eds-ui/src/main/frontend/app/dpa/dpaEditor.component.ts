import {Component} from "@angular/core";
import {AdminService} from "../administration/admin.service";
import {DpaService} from "./dpa.service";
import {LoggerService} from "../common/logger.service";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Dpa} from "./models/Dpa";

@Component({
    template: require('./dpaEditor.html')
})
export class DpaEditorComponent {
    dpa : Dpa = <Dpa>{};

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
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;

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
}

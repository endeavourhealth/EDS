import {Component} from "@angular/core";
import {AdminService} from "../administration/admin.service";
import {DsaService} from "./dsa.service";
import {LoggerService} from "../common/logger.service";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Dsa} from "./models/Dsa";

@Component({
    template: require('./dsaEditor.html')
})
export class DsaEditorComponent {
    dsa : Dsa = <Dsa>{};

    status = [
        {num: 0, name : "Active"},
        {num: 1, name : "Inactive"}
    ];

    constructor(private $modal: NgbModal,
                private state : StateService,
                private log:LoggerService,
                private adminService : AdminService,
                private dsaService : DsaService,
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
        this.dsa = {
            name : ''
        } as Dsa;
    }

    load(uuid : string) {
        var vm = this;
        vm.dsaService.getDsa(uuid)
            .subscribe(result =>  {
                    vm.dsa = result;
                    console.log(result);
                },
                error => vm.log.error('Error loading', error, 'Error')
            );
    }

    save(close : boolean) {
        var vm = this;

        vm.dsaService.saveDsa(vm.dsa)
            .subscribe(saved => {
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Item saved', vm.dsa, 'Saved');
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

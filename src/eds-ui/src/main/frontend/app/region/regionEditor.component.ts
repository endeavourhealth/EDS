import {Component} from "@angular/core";
import {Organisation} from "../organisationManager/models/Organisation";
import {Service} from "../services/models/Service";
import {AdminService} from "../administration/admin.service";
import {RegionService} from "./region.service";
import {LoggerService} from "../common/logger.service";
import {Transition, StateService} from "ui-router-ng2";
import {ServicePickerDialog} from "../services/servicePicker.dialog";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Region} from "./models/Region";

@Component({
    template: require('./regionEditor.html')
})
export class RegionEditorComponent {

    region : Region = <Region>{};
    organisation : Organisation = <Organisation>{};
    services : Service[];
    organisations : Organisation[];

    constructor(private $modal: NgbModal,
                private state : StateService,
                private log:LoggerService,
                private adminService : AdminService,
                private regionService : RegionService,
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
        this.organisation = {
            uuid : uuid,
            name : ''
        } as Organisation;
    }

    load(uuid : string) {
        var vm = this;
        vm.regionService.getRegion(uuid)
            .subscribe(result =>  {
                    vm.region = result;
                    vm.getRegionOrganisations();
                },
                error => vm.log.error('Error loading', error, 'Error')
            );

        console.log(vm.region);
    }

    save(close : boolean) {
        var vm = this;

        // Populate service organisations before save
        /*
        vm.organisation.services = {};
        for (var idx in this.services) {
            var service : Service = this.services[idx];
            this.organisation.services[service.uuid] = service.name;
        }
        */

        vm.regionService.saveRegion(vm.region)
            .subscribe(saved => {
                    vm.region.uuid= saved.uuid;
                    vm.adminService.clearPendingChanges();
                    vm.log.success('Item saved', vm.organisation, 'Saved');
                    if (close) { vm.state.go(vm.transition.from()); }
                },
                error => vm.log.error('Error saving', error, 'Error')
            );
    }

    close() {
        this.adminService.clearPendingChanges();
        this.state.go(this.transition.from());
    }

    private getRegionOrganisations() {
        var vm = this;
        vm.regionService.getRegionOrganisations(vm.region.uuid)
            .subscribe(
                result => vm.organisations = result,
                error => vm.log.error('Failed to load region organisations', error, 'Load region organisation')
            );
    }

    private editOrganisations() {
        var vm = this;
        ServicePickerDialog.open(vm.$modal, vm.services)
            .result.then(function (result : Service[]) {
            vm.services = result;
        });
    }
}

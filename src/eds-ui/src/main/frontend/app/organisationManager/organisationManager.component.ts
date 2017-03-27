import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";
import {Organisation} from "./models/Organisation";
import {LoggerService} from "../common/logger.service";
import {OrganisationManagerService} from "./organisationManager.service";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";
import {Region} from "../region/models/Region";

@Component({
    template: require('./organisationManager.html')
})
export class OrganisationManagerComponent {
    organisations : Organisation[];
    modeType : string;

    constructor(private $modal: NgbModal,
                private organisationManagerService : OrganisationManagerService,
                private log : LoggerService,
                protected $state : StateService,
                private transition : Transition,
                private state : StateService) {
        this.performAction(transition.params()['mode']);
    }

    protected performAction(mode:string) {
        switch (mode) {
            case 'organisations':
                this.modeType = 'Organisation';
                this.getOrganisations();
                break;
            case 'services':
                this.modeType = 'Service';
                this.getAllServices();
                break;
        }
    }
    getOrganisations() {
        var vm = this;
        vm.organisationManagerService.getOrganisations()
            .subscribe(result => {
                    vm.organisations = result
                },
                error => vm.log.error('Failed to load organisations', error, 'Load organisations')
            );

    }

    private getAllServices() {
        var vm = this;
        vm.organisationManagerService.getAllServices()
            .subscribe(
                result => vm.organisations = result,
                error => vm.log.error('Failed to load Services', error, 'Load Services')
            );
    }

    add() {
        if (this.modeType === 'Organisation')
            this.$state.go('app.organisationManagerEditor', {itemUuid: null, itemAction: 'add'});
        else
            this.$state.go('app.organisationManagerEditor', {itemUuid: null, itemAction: 'addService'});
    }

    edit(item : Organisation) {
        this.$state.go('app.organisationManagerEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    save(original : Organisation, edited : Organisation) {
        var vm = this;
        vm.organisationManagerService.saveOrganisation(edited)
            .subscribe(
                saved =>  {
                    if (original.uuid)
                        jQuery.extend(true, original, saved);
                    else
                        vm.organisations.push(saved);

                    vm.log.success('Organisation saved', original, 'Save organisation');
                },
                error => vm.log.error('Failed to save organisation', error, 'Save organisation')
            );
    }

    delete(item : Organisation) {
        var vm = this;
        MessageBoxDialog.open(vm.$modal, 'Delete Organisation', 'Are you sure you want to delete the Organisation?', 'Yes', 'No')
            .result.then(
            () => vm.doDelete(item),
            () => vm.log.info('Delete cancelled')
        );
    }

    doDelete(item : Organisation) {
        var vm = this;
        vm.organisationManagerService.deleteOrganisation(item.uuid)
            .subscribe(
                () => {
                    var index = vm.organisations.indexOf(item);
                    vm.organisations.splice(index, 1);
                    vm.log.success('Organisation deleted', item, 'Delete Organisation');
                },
                (error) => vm.log.error('Failed to delete Organisation', error, 'Delete Organisation')
            );
    }

    close() {
        this.state.go(this.transition.from());
    }

}

import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {Organisation} from "../organisationManager/models/Organisation";
import {LoggerService} from "../common/logger.service";
import {RegionService} from "./region.service";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";
import {Region} from "../region/models/Region";

@Component({
    template: require('./region.html')
})
export class RegionComponent {
    organisations : Organisation[];
    regions : Region[] = [];

    constructor(private $modal: NgbModal,
                private regionService : RegionService,
                private log : LoggerService,
                protected $state : StateService) {
        this.getRegions();
    }

    getRegions() {

        console.log(JSON.stringify(this.regions));


         var vm = this;
         vm.regionService.getAllRegions()
         .subscribe(
         result => vm.regions = result,
         error => vm.log.error('Failed to load organisations', error, 'Load organisations')
         );
    }

    add() {
        this.$state.go('app.regionEditor', {itemUuid: null, itemAction: 'add'});
    }

    edit(item : Region) {
        this.$state.go('app.regionEditor', {itemUuid: item.uuid, itemAction: 'edit'});
    }

    save(original : Organisation, edited : Organisation) {
        var vm = this;
        vm.regionService.saveOrganisation(edited)
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
        vm.regionService.deleteOrganisation(item.uuid)
            .subscribe(
                () => {
                    var index = vm.organisations.indexOf(item);
                    vm.organisations.splice(index, 1);
                    vm.log.success('Organisation deleted', item, 'Delete Organisation');
                },
                (error) => vm.log.error('Failed to delete Organisation', error, 'Delete Organisation')
            );
    }
}

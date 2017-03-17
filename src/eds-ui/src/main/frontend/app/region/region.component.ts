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

    delete(item : Region) {
        var vm = this;
        MessageBoxDialog.open(vm.$modal, 'Delete Region', 'Are you sure you want to delete the Region?', 'Yes', 'No')
            .result.then(
            () => vm.doDelete(item),
            () => vm.log.info('Delete cancelled')
        );
    }

    doDelete(item : Region) {
        var vm = this;
        vm.regionService.deleteRegion(item.uuid)
            .subscribe(
                () => {
                    var index = vm.regions.indexOf(item);
                    vm.regions.splice(index, 1);
                    vm.log.success('Region deleted', item, 'Delete Region');
                },
                (error) => vm.log.error('Failed to delete Region', error, 'Delete Region')
            );
    }
}

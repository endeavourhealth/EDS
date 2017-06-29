import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";
import {Organisation} from "./models/Organisation";
import {LoggerService, MessageBoxDialog} from "eds-common-js";
import {OrganisationManagerService} from "./organisationManager.service";
import { PaginationService } from '../pagination/pagination.service';

@Component({
    template: require('./organisationManager.html')
})
export class OrganisationManagerComponent {
    organisations : Organisation[];
    modeType : string;
    searchData : string = '';
    searchType : string;
    totalItems : number = 5;
    pageNumber : number = 1;
    pageSize : number = 20;
    orderColumn : string = 'name';
    descending : boolean = false;
    orgDetailsToShow = new Organisation().getDisplayItems();


    constructor(private $modal: NgbModal,
                private organisationManagerService : OrganisationManagerService,
                private log : LoggerService,
                protected $state : StateService,
                private transition : Transition,
                private state : StateService,
                private paginationService : PaginationService) {
        this.performAction(transition.params()['mode']);
    }

    protected performAction(mode:string) {
        switch (mode) {
            case 'organisations':
                this.modeType = 'Organisation';
                this.searchType = 'organisation';
                this.search();
                this.getTotalOrganisationCount();
                break;
            case 'services':
                this.modeType = 'Service';
                this.searchType = 'services';
                this.search();
                this.getTotalOrganisationCount();
                break;
        }
    }

    getTotalOrganisationCount() {
        const vm = this;
        vm.organisationManagerService.getTotalCount(vm.searchData, vm.searchType)
            .subscribe(
                (result) => {
                    vm.totalItems = result;
                },
                (error) => console.log(error)
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
                    vm.search();
                    vm.log.success('Organisation deleted', item, 'Delete Organisation');
                },
                (error) => vm.log.error('Failed to delete Organisation', error, 'Delete Organisation')
            );
    }

    close() {
        this.$state.go('app.organisationManagerOverview');
    }

    onSearch($event) {
        var vm = this;
        vm.searchData = $event;
        vm.pageNumber = 1;
        vm.search();
        vm.getTotalOrganisationCount();
    }

    private search() {
        var vm = this;
        vm.organisationManagerService.search(vm.searchData, vm.searchType, vm.pageNumber, vm.pageSize, vm.orderColumn, vm.descending)
            .subscribe(result => {
                    vm.organisations = result;
                },
                error => vm.log.error(error)
            );
    }

    pageChange($event) {
        const vm = this;
        vm.pageNumber = $event;
        vm.search();
    }

    pageSizeChange($event) {
        const vm = this;
        vm.pageSize = $event;
        vm.search();
    }

    onOrderChange($event) {
        const vm = this;
        vm.orderColumn = $event.column;
        vm.descending = $event.descending;
        vm.search();
    }
}

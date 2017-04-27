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
    searchData : string;
    searchType : string;

    // array of all items to be paged
    private allItems: Organisation[];

    // pager object
    pager: any = {};

    // paged items
    pagedItems: any[];

    //page size
    pageSize : number = 15;

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
                this.getOrganisations();
                break;
            case 'services':
                this.modeType = 'Service';
                this.searchType = 'services';
                this.getAllServices();
                break;
        }
    }
    getOrganisations() {
        var vm = this;
        vm.organisationManagerService.getOrganisations()
            .subscribe(result => {
                    this.allItems = result;
                    this.organisations = result;
                    this.setPage(1);
                },
                error => vm.log.error('Failed to load organisations', error, 'Load organisations')
            );

    }

    setPage(page: number) {
        if (page < 1 || page > this.pager.totalPages) {
            return;
        }

        // get pager object from service
        this.pager = this.paginationService.getPager(this.allItems.length, page, this.pageSize);
        // get current page of items
        this.pagedItems = this.allItems.slice(this.pager.startIndex, this.pager.endIndex + 1);
    }

    private getAllServices() {
        var vm = this;
        vm.organisationManagerService.getAllServices()
            .subscribe(result => {
                    this.allItems = result;
                    this.setPage(1);
                },
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
                    var index = vm.allItems.indexOf(item);
                    vm.allItems.splice(index, 1);
                    vm.setPage(vm.pager.currentPage);
                    vm.log.success('Organisation deleted', item, 'Delete Organisation');
                },
                (error) => vm.log.error('Failed to delete Organisation', error, 'Delete Organisation')
            );
    }

    close() {
        this.$state.go('app.organisationManagerOverview');
    }

    private search() {
        var vm = this;
        if (vm.searchData.length < 3) {
            vm.allItems = vm.organisations;
            vm.setPage(1)
            return;
        }
        vm.organisationManagerService.search(vm.searchData, vm.searchType)
            .subscribe(result => {
                    vm.allItems = result;
                    vm.pager = {};
                    vm.setPage(1);
                },
                error => vm.log.error(error)
            );
    }

}

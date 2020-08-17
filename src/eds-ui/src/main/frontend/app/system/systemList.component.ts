import {Component, OnDestroy, OnInit} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {linq, LoggerService, MessageBoxDialog} from "eds-common-js";
import {Observable} from "rxjs";
import {Subscription} from 'rxjs/Subscription';
import {ServiceService} from "../services/service.service";
import {LibraryService} from "eds-common-js/dist/index";
import {SystemService} from "./system.service";
import {ItemSummaryList} from "eds-common-js/dist/library/models/ItemSummaryList";
import {FolderItem} from "eds-common-js/dist/folder/models/FolderItem";

@Component({
    template: require('./systemList.html')
})
export class SystemListComponent implements OnInit, OnDestroy{

    systems: FolderItem[];

    static $inject = ['$uibModal', 'ServiceService', 'LoggerService','$state'];

    constructor(private $modal : NgbModal,
                private serviceService : ServiceService,
                private libraryService: LibraryService,
                private systemService: SystemService,
                private log : LoggerService,
                protected $state : StateService) {


    }

    ngOnInit() {
        var vm = this;
        vm.refresh();
    }

    ngOnDestroy() {

    }

    addSystem() {
        this.$state.go('app.systemEdit', {itemUuid: null, itemAction: 'add'});

        //this.$state.go('app.systemEdit', {itemUuid: $event.uuid, itemAction: $event.action});
    }


    editSystem(system: FolderItem) {
        var vm = this;
        var uuid = system.uuid;
        this.$state.go('app.systemEdit', {itemUuid: uuid, itemAction: 'edit'});
    }

    refresh() {
        var vm = this;
        vm.systemService.getSystemList()
            .subscribe(
                (data) => {
                    vm.systems = data;
                },
                (error) => {
                    vm.log.error('Error getting systems');
                }
            );
    }

}

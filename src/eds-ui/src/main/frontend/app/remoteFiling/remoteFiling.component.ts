import {linq, LoggerService} from "eds-common-js";
import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {RemoteFilingService} from "./remoteFiling.service";
import {SubscriberZipFileUUID} from "./models/SubscriberZipFileUUID";
import {RemoteFilingStatistics} from "./models/RemoteFilingStatistics";

@Component({
    template : require('./remoteFiling.html')
})
export class RemoteFilingComponent {
    files : SubscriberZipFileUUID[];
    dayStats : RemoteFilingStatistics[];
    monthStats : RemoteFilingStatistics[];
    yearStats : RemoteFilingStatistics[];
    totalItems = 10;
    pageNumber = 1;
    pageSize = 50;

    constructor(private $modal : NgbModal,
                protected log : LoggerService,
                protected remoteFilingService : RemoteFilingService) {

    }


    ngOnInit() {
        this.refresh();
    }

    refresh() {
        const vm = this;
        vm.getPagedFiles();
        vm.getFileCount();
        vm.getDayStatistics();
        vm.getMonthStatistics();
        vm.getYearStatistics();
    }

    getPagedFiles() {
        var vm = this;
        vm.remoteFilingService.getPagedFiles(vm.pageNumber, vm.pageSize)
            .subscribe(
                (result) => {
                    vm.files= result;
                    console.log(result);
                },
                (error) => vm.log.error('Failed to load files', error, 'Load files')
            )
    }

    getFileCount() {
        const vm = this;
        vm.remoteFilingService.getRemoteFilingCount()
            .subscribe(
                (result) => {
                    vm.totalItems = result;
                },
                (error) => console.log(error)
            );
    }

    getDayStatistics() {
        var vm = this;
        vm.remoteFilingService.getStatistics('day')
            .subscribe(
                (result) => {
                    vm.dayStats= result;
                    console.log(result);
                },
                (error) => vm.log.error('Failed to load statistics', error, 'Load statistics')
            )
    }

    getMonthStatistics() {
        var vm = this;
        vm.remoteFilingService.getStatistics('month')
            .subscribe(
                (result) => {
                    vm.monthStats= result;
                    console.log(result);
                },
                (error) => vm.log.error('Failed to load statistics', error, 'Load statistics')
            )
    }

    getYearStatistics() {
        var vm = this;
        vm.remoteFilingService.getStatistics('year')
            .subscribe(
                (result) => {
                    vm.yearStats= result;
                    console.log(result);
                },
                (error) => vm.log.error('Failed to load statistics', error, 'Load statistics')
            )
    }

    pageChanged($event) {
        const vm = this;
        vm.pageNumber = $event;
        vm.getPagedFiles();
        vm.getFileCount();
    }

}

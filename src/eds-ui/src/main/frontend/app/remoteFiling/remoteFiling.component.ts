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

    constructor(private $modal : NgbModal,
                protected log : LoggerService,
                protected remoteFilingService : RemoteFilingService) {

    }


    ngOnInit() {
        this.refresh();
    }

    refresh() {
        const vm = this;
        vm.getAllFiles();
        vm.getDayStatistics();
        vm.getMonthStatistics();
        vm.getYearStatistics();
    }

    getAllFiles() {
        var vm = this;
        vm.remoteFilingService.getAllFiles()
            .subscribe(
                (result) => {
                    vm.files= result;
                    console.log(result);
                },
                (error) => vm.log.error('Failed to load files', error, 'Load files')
            )
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

}

import {linq, LoggerService} from "eds-common-js";
import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {RemoteFilingService} from "./remoteFiling.service";
import {SubscriberZipFileUUID} from "./models/SubscriberZipFileUUID";
import {RemoteFilingStatistics} from "./models/RemoteFilingStatistics";
import {RemoteFilingSubscribers} from "./models/RemoteFilingSubscribers";

@Component({
    template : require('./remoteFiling.html')
})
export class RemoteFilingComponent {
    files : SubscriberZipFileUUID[];
    //dayStats : RemoteFilingStatistics[];
    //monthStats : RemoteFilingStatistics[];
    //yearStats : RemoteFilingStatistics[];
    subscriberStats : RemoteFilingStatistics[];
    subscribers : RemoteFilingSubscribers[];
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
        vm.getSubscribers();
        vm.getSubscriberStatistics();

        //vm.getPagedFiles();
        //vm.getFileCount();
        //vm.getDayStatistics();
        //vm.getMonthStatistics();
        //vm.getYearStatistics();
    }

    viewHistory(subscriberId: string) {
        var vm = this;


    }

    getSubscriberStats(subscriberId: number) {
        return this.subscriberStats.filter((item) => item.subscriberId === subscriberId);
    }

    // getPagedFiles() {
    //     var vm = this;
    //     vm.remoteFilingService.getPagedFiles(vm.pageNumber, vm.pageSize)
    //         .subscribe(
    //             (result) => {
    //                 vm.files= result;
    //                 console.log(result);
    //             },
    //             (error) => vm.log.error('Failed to load files', error, 'Load files')
    //         )
    // }

    // getFileCount() {
    //     const vm = this;
    //     vm.remoteFilingService.getRemoteFilingCount()
    //         .subscribe(
    //             (result) => {
    //                 vm.totalItems = result;
    //             },
    //             (error) => console.log(error)
    //         );
    // }

    getSubscribers() {
        var vm = this;
        vm.remoteFilingService.getSubscribers()
            .subscribe(
                (result) => {
                    vm.subscribers = result;
                    console.log(result);
                },
                (error) => vm.log.error('Failed to load subscribers', error, 'Load subscribers')
            )
    }

    getSubscriberStatistics() {
        var vm = this;
        if (vm.subscribers == null) {
            console.log("No remote subscribers found");
            return;
        }

        //for each subscriber, get stats -> set timeFrame from a drop down
        for (var i=0; i<vm.subscribers.length; i++) {
            var subscriber = vm.subscribers[i];
            var id = subscriber.id;
            vm.remoteFilingService.getSubscriberStatistics(id, 'day')
                .subscribe(
                    (result) => {
                        vm.subscriberStats = result;
                        console.log(result);
                    },
                    (error) => vm.log.error('Failed to load subscriber statistics for Id: '+id, error, 'Load subscriber statistics')
                )
        }
    }

    // getDayStatistics() {
    //     var vm = this;
    //     vm.remoteFilingService.getStatistics('day')
    //         .subscribe(
    //             (result) => {
    //                 vm.dayStats= result;
    //                 console.log(result);
    //             },
    //             (error) => vm.log.error('Failed to load statistics', error, 'Load statistics')
    //         )
    // }

    // getMonthStatistics() {
    //     var vm = this;
    //     vm.remoteFilingService.getStatistics('month')
    //         .subscribe(
    //             (result) => {
    //                 vm.monthStats= result;
    //                 console.log(result);
    //             },
    //             (error) => vm.log.error('Failed to load statistics', error, 'Load statistics')
    //         )
    // }

    // getYearStatistics() {
    //     var vm = this;
    //     vm.remoteFilingService.getStatistics('year')
    //         .subscribe(
    //             (result) => {
    //                 vm.yearStats= result;
    //                 console.log(result);
    //             },
    //             (error) => vm.log.error('Failed to load statistics', error, 'Load statistics')
    //         )
    // }

    // pageChanged($event) {
    //     const vm = this;
    //     vm.pageNumber = $event;
    //     vm.getPagedFiles();
    //     vm.getFileCount();
    // }

}

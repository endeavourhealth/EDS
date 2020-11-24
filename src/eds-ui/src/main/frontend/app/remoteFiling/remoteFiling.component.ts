import {linq, LoggerService} from "eds-common-js";
import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {RemoteFilingService} from "./remoteFiling.service";
import {SubscriberZipFileUUID} from "./models/SubscriberZipFileUUID";
import {RemoteFilingStatistics} from "./models/RemoteFilingStatistics";
import {RemoteFilingSubscribers} from "./models/RemoteFilingSubscribers";
import {RemoteFilingFilesDialog} from "./remoteFilingFiles.dialog";

@Component({
    template : require('./remoteFiling.html')
})
export class RemoteFilingComponent {
    files : SubscriberZipFileUUID[];
    subscriberStats : RemoteFilingStatistics[];
    subscribers : RemoteFilingSubscribers[];
    totalItems = 10;
    pageNumber = 1;
    pageSize = 50;
    timeFrame = 'day';
    refreshingStatistics: boolean;

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
    }

    viewHistory(subscriberId: string) {
        var vm = this;

    }

    viewErrors(subscriberId: number) {
        var vm = this;

        vm.remoteFilingService.getFailedFiles(subscriberId, vm.timeFrame)
            .subscribe(
                (result) => {
                    vm.files = result;

                    console.log(result);

                    RemoteFilingFilesDialog.open(vm.$modal, vm.files, subscriberId);
                },
                (error) => vm.log.error('Failed to load files', error, 'Load files')
            )
    }

    isStatisticInError(subscriberStats : RemoteFilingStatistics) {

        var statisticText = subscriberStats.statisticsText;

        if (statisticText.endsWith('errors')) {

            var statisticValue = subscriberStats.statisticsValue;
            if (statisticValue != '0') {
                return true;
            }
        }
        return false;
    }

    getJsonDefinitionFormatted(jsonDefinition: string) {

        var obj = JSON.parse(jsonDefinition);
        return JSON.stringify(obj, null, 4);
    }

    getSubscriberStats(subscriberId: number) {
        var vm = this;
        return vm.subscriberStats.filter((item) => item.subscriberId === subscriberId);
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

                    vm.getSubscriberStatistics();
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
        vm.refreshingStatistics = true;
        for (var idx in vm.subscribers) {

            let id = vm.subscribers[idx].id;
            vm.remoteFilingService.getSubscriberStatistics(id, vm.timeFrame)
                .subscribe(
                    (result) => {

                        var remoteFilingSubscriber: RemoteFilingSubscribers[]
                            = $.grep(vm.subscribers, function (i) {
                                return i.id === result[0].subscriberId;
                        });

                        remoteFilingSubscriber[0].statistics = result;
                        console.log(result);
                        vm.refreshingStatistics = false;
                    },
                    (error) => {
                        vm.log.error('Failed to load subscriber statistics for Id: ' + id, error, 'Load subscriber statistics')
                        vm.refreshingStatistics = false;
                    }
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

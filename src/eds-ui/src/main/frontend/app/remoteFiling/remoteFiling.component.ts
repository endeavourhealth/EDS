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
    totalSubscriberFiles = 0;
    pageNumber = 1;
    pageSize = 50;
    timeFrame = 'day';
    refreshingStatus: boolean;

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

    viewHistory(subscriberId: number) {
        var vm = this;
        vm.getSubscriberFileCount(subscriberId);

        vm.remoteFilingService.getSubscriberPagedFiles(subscriberId, vm.pageNumber, vm.pageSize)
            .subscribe(
                (result) => {
                    vm.files = result;

                    //console.log(result);

                    RemoteFilingFilesDialog.open(
                        vm.$modal,
                        vm.files,
                        subscriberId,
                        ' - filing history (all)',
                        vm.totalSubscriberFiles);
                },
                (error) => vm.log.error('Failed to load files', error, 'Load files')
            )
    }

    viewErrors(subscriberId: number) {
        var vm = this;

        vm.remoteFilingService.getFailedFiles(subscriberId, vm.timeFrame)
            .subscribe(
                (result) => {
                    vm.files = result;

                    //console.log(result);

                    RemoteFilingFilesDialog.open(
                        vm.$modal,
                        vm.files,
                        subscriberId,
                        ' - filing errors ('+vm.timeFrame+')',
                        vm.totalSubscriberFiles);
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

    getSubscriberFileCount(subscriberId: number) {
        var vm = this;
        vm.remoteFilingService.getRemoteSubscriberFilingCount(subscriberId)
            .subscribe(
                (result) => {
                    vm.totalSubscriberFiles = result;
                },
                (error) => console.log(error)
            );
    }

    getSubscribers() {
        var vm = this;
        vm.remoteFilingService.getSubscribers()
            .subscribe(
                (result) => {
                    vm.subscribers = result;
                    //console.log(result);

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
        vm.refreshingStatus = true;
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
                        //console.log(result);
                        vm.refreshingStatus = false;
                    },
                    (error) => {
                        vm.log.error('Failed to load subscriber statistics for Id: ' + id, error, 'Load subscriber statistics')
                        vm.refreshingStatus = false;
                    }
                )
        }
    }
}
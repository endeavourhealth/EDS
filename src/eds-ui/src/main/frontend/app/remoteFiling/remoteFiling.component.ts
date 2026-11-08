import {linq, LoggerService} from "eds-common-js";
import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {RemoteFilingService} from "./remoteFiling.service";
import {SubscriberZipFileUUID} from "./models/SubscriberZipFileUUID";
import {RemoteFilingStatistics} from "./models/RemoteFilingStatistics";
import {RemoteFilingSubscribers} from "./models/RemoteFilingSubscribers";
import {RemoteFilingFilesDialog} from "./remoteFilingFiles.dialog";
import {RemoteFiler} from "./models/RemoteFiler";
import {RemoteFilerStatus} from "./models/RemoteFilerStatus";
import {DateTimeFormatter} from "../utility/DateTimeFormatter";
import {RemoteFilingTechnicalDetailsDialog} from "./remoteFilingTechnicalDetails.dialog";

@Component({
    template : require('./remoteFiling.html')
})
export class RemoteFilingComponent {

    formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM;
    getDateDiffSimple = DateTimeFormatter.getDateDiffSimple;
    getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow;

    remoteFilers: RemoteFiler[];
    statusLastRefreshed: Date;
    refreshingStatusMap: boolean[];
    statusMap: RemoteFilerStatus[];
    dayDuration: number;


    /*files : SubscriberZipFileUUID[];
    subscriberStats : RemoteFilingStatistics[];
    subscribers : RemoteFilingSubscribers[];
    totalSubscriberFiles = 0;
    pageNumber = 1;
    pageSize = 50;
    timeFrame = 'day';
    refreshingStatus: boolean;*/

    constructor(private $modal : NgbModal,
                protected log : LoggerService,
                protected remoteFilingService : RemoteFilingService) {

    }

    ngOnInit() {

        var vm = this;
        vm.dayDuration = 1000 * 60 * 60 * 24;
        vm.refresh();
    }

    refresh() {
        var vm = this;
        vm.getRemoteFilers();
        //vm.getSubscribers();
    }

    getRemoteFilers() {
        var vm = this;
        vm.remoteFilingService.getRemoteFilers()
            .subscribe(
                (result) => {
                    vm.remoteFilers = linq(result).OrderBy(s => s.name).ToArray();
                    //console.log('remote filers = ' + vm.remoteFilers.length);
                    vm.refreshStatuses();
                },
                (error) => {
                    vm.log.error('Failed to get remote filer details');
                }
            );
    }

    refreshStatuses() {
        var vm = this;
        vm.statusLastRefreshed = new Date();
        vm.refreshingStatusMap = [];
        vm.statusMap = [];

        for (var i = 0; i < vm.remoteFilers.length; i++) {
            var remoteFiler = vm.remoteFilers[i];
            vm.refreshStatus(remoteFiler);
        }
    }

    refreshStatus(remoteFiler: RemoteFiler) {
        var vm = this;

        var subscriberId = remoteFiler.subscriberId;

        vm.refreshingStatusMap[subscriberId] = true;

        vm.remoteFilingService.getRemoteFilerStatus(subscriberId).subscribe(
            (result) => {
                vm.calculateWarnings(result);
                vm.statusMap[subscriberId] = result;
                vm.refreshingStatusMap[subscriberId] = false;
            },
            (error) => {

                //vm.refreshingStatusMap[subscriberId] = false; //if it's broke, don't turn this off
                vm.log.error('Failed to get status for subscriber ' + subscriberId, error);
            }
        )
    }

    isRefreshing(remoteFiler: RemoteFiler): boolean {
        var vm = this;

        var subscriberId = remoteFiler.subscriberId;
        return vm.refreshingStatusMap[subscriberId];
    }


    /**
     * works out if there are any warnings about this status
     */
    calculateWarnings(result: RemoteFilerStatus) {
        var vm = this;

        if (result.oldestQueuedMessage) {
            var ms = vm.statusLastRefreshed.getTime() - result.oldestQueuedMessage;
            if (ms > vm.dayDuration) {
                var behindDesc = DateTimeFormatter.getDateDiffDescMs(result.oldestQueuedMessage, vm.statusLastRefreshed.getTime(), 2);
                result.queuedMessageWarning = 'Queued messages not written to SFTP for ' + behindDesc;
            }
        }

        if (result.oldestMessageWrittenToSftp) {
            var ms = vm.statusLastRefreshed.getTime() - result.oldestMessageWrittenToSftp;
            if (ms > vm.dayDuration * 2) {
                var behindDesc = DateTimeFormatter.getDateDiffDescMs(result.oldestMessageWrittenToSftp, vm.statusLastRefreshed.getTime(), 2);
                result.writtenToSftpWarning = 'Messages not collected or fed back for ' + behindDesc;
            }
        }

        if (result.countMessagesInError) {
            result.messagesInErrorWarning = '' + result.countMessagesInError + ' messages in error';
        }

    }

    /**
     * returns the status for the filer - put the status in an array so we can use a ngfor construct
     */
    getStatusForFiler(filer: RemoteFiler): RemoteFilerStatus[] {
        var vm = this;
        var subscriberId = filer.subscriberId;
        var status = vm.statusMap[subscriberId];
        var ret = [];
        ret.push(status);
        return ret;
    }

    viewErrors(filer: RemoteFiler, status: RemoteFilerStatus) {
        var vm = this;

        RemoteFilingFilesDialog.open(vm.$modal, filer, status.errors);
    }

    viewTechnicalDetails(filer: RemoteFiler) {
        var vm = this;

        //if a subscriber feed has been removed from the subscriber_file_sender table (which is something that is done, for some reason)
        //then we need to handle that we don't have these details available
        if (!filer.technicalDetails) {
            vm.log.error('No technical details found');
            return;
        }


        RemoteFilingTechnicalDetailsDialog.open(vm.$modal, filer);
    }

    getRemoteFilersToDisplay(): RemoteFiler[] {
        var vm = this;


        //if not got these back yet, we've got nothing to return
        if (!vm.remoteFilers) {
            return [];
        }

        var ret = [];

        for (var i=0; i<vm.remoteFilers.length; i++) {
            var filer = vm.remoteFilers[i] as RemoteFiler;
            var id = filer.subscriberId;
            var status = vm.statusMap[id];

            if (!vm.remoteFilingService.showWarningsOnly
                || !status
                || status.queuedMessageWarning
                || status.writtenToSftpWarning
                || status.messagesInErrorWarning) {

                ret.push(filer);
            }
        }

        if (vm.remoteFilingService.sortByFilter == 'name') {
            ret = linq(ret).OrderBy(s => s.name).ToArray();

        } else if (vm.remoteFilingService.sortByFilter == 'id') {
            ret = linq(ret).OrderBy(s => s.subscriberId).ToArray();

        } else {
            console.log('Unknown sorting mode [' + vm.remoteFilingService.sortByFilter + ']');
        }


        return ret;
    }

    /*viewHistory(subscriberId: number) {
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
    }*/

}
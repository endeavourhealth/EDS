import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {SftpReaderService} from "./sftpReader.service";
import {SftpReaderChannelStatus} from "./SftpReaderChannelStatus";
import {SftpReaderBatchContents} from "./SftpReaderBatchContents";
import {OdsSearchDialog} from "../services/odsSearch.dialog";
import {SftpReaderInstance} from "./SftpReaderInstance";
import {SftpReaderHistoryDialog} from "./sftpReaderHistory.dialog";

@Component({
    template : require('./sftpReader.html')
})
export class SftpReaderComponent {

    //resultStr: string;
    instanceNames: SftpReaderInstance[];
    filterInstanceName: string;
    statuses: SftpReaderChannelStatus[];
    statusesLastRefreshed: Date;
    resultStr: string;
    showRawJson: boolean;
    refreshingStatus: boolean;
    showWarningsOnly: boolean;

    constructor(private $modal: NgbModal,
                protected sftpReaderService: SftpReaderService,
                protected logger: LoggerService,
                protected $state: StateService) {


    }

    ngOnInit() {
        var vm = this;
        vm.filterInstanceName = 'active';
        vm.showWarningsOnly = true;
        vm.refreshInstances();
        vm.refreshStatus();
    }

    refreshInstances() {
        var vm = this;
        vm.sftpReaderService.getSftpReaderInstances().subscribe(
            (result) => {
                vm.instanceNames = result;
            },
            (error) => {
                vm.logger.error('Failed get SFTP Reader instances', error, 'SFTP Reader');
            }
        )
    }

    refreshStatus() {
        var vm = this;
        vm.refreshingStatus = true;
        //console.log('vm.refreshingStatus = ' + vm.refreshingStatus);

        vm.sftpReaderService.getSftpReaderStatus(vm.filterInstanceName).subscribe(
            (result) => {
                vm.refreshingStatus = false;
                //console.log('vm.refreshingStatus = ' + vm.refreshingStatus);

                vm.logger.success('Successfully got SFTP Reader status', 'SFTP Reader Status');
                vm.statuses = result;
                vm.statusesLastRefreshed = new Date();

                vm.resultStr = JSON.stringify(result, null, 2);

                //console.log('received SFTP Reader status');
                //console.log(result);
            },
            (error) => {
                vm.refreshingStatus = false;
                //console.log('vm.refreshingStatus = ' + vm.refreshingStatus);

                vm.logger.error('Failed get SFTP Reader status', error, 'SFTP Reader Status');
            }
        )
    }

    getStatusesToDisplay() {
        var vm = this;
        if (!vm.showWarningsOnly) {
            return vm.statuses;

        } else {
            var ret = [];
            var i;
            for (i=0; i<vm.statuses.length; i++) {
                var status = vm.statuses[i];

                //any of these count as a warning
                if (vm.isLastPollAttemptTooOld(status)
                    || !status.latestPollingStart
                    || status.latestPollingException
                    || vm.isLastExtractTooOld(status)
                    || !status.latestBatchId
                    || vm.filterOrgs(status.completeBatchContents, false).length > 0) {

                    ret.push(status);
                }
            }
            return ret;
        }

    }

    filterOrgs(arr: SftpReaderBatchContents[], wantOk: boolean): SftpReaderBatchContents[] {
        var ret = [];

        var i;
        for (i=0; i<arr.length; i++) {
            var c = arr[i];
            if (c.notified == wantOk) {
                ret.push(c);
            }
        }
        return ret;
    }

    getPanelClass(status: SftpReaderChannelStatus): string {
        if (status.instanceName) {
            return "panel panel-primary";
        } else {
            return "panel panel-info";
        }
    }

    odsSearch() {
        var vm = this;
        OdsSearchDialog.open(vm.$modal);

    }

    isLastPollAttemptTooOld(status: SftpReaderChannelStatus): boolean {
        var vm = this;

        var lastPolled = status.latestPollingStart;
        if (!lastPolled) {
            return true;
        }
        var pollingFrequencySec = status.pollFrequencySeconds;
        var pollingFrequencyMs = pollingFrequencySec * 1000;
        pollingFrequencyMs = (pollingFrequencyMs * 1.1); //add 10% to the poling frequency so it's less touchy

        //var now = new Date();
        var now = vm.statusesLastRefreshed; //use date of refresh rather than current date so it doesn't change if you don't refresh the page
        var lastPolledDiffMs = now.getTime() - lastPolled;

        return lastPolledDiffMs > pollingFrequencyMs;
    }

    isLastExtractTooOld(status: SftpReaderChannelStatus): boolean {
        var vm = this;

        var lastExtract = status.latestBatchReceived;
        if (!lastExtract) {
            return true;
        }

        var dataFrequencyDays = status.dataFrequencyDays;
        var dataFrequencyMs = dataFrequencyDays * 24 * 60 * 60 * 1000;
        dataFrequencyMs = (dataFrequencyMs * 1.5); //add 10% to the poling frequency so it's less touchy

        //var now = new Date();
        var now = vm.statusesLastRefreshed; //use date of refresh rather than current date so it doesn't change if you don't refresh the page
        var lastExtractDiffMs = now.getTime() - lastExtract;

        return lastExtractDiffMs > dataFrequencyMs;
    }

    viewHistory(status: SftpReaderChannelStatus) {
        var vm = this;
        SftpReaderHistoryDialog.open(vm.$modal, status);
    }


    /*ignoreBatchSplit(content: SftpReaderBatchContents, status: SftpReaderChannelStatus) {
        var vm = this;

        var reason = prompt('Enter reason');
        if (reason == null) {
            return;
        }

        vm.sftpReaderService.ignoreBatchSplit(status.latestBatchId, content.batchSplitId, status.id, reason).subscribe(
            (result) => {
                content.error = null;
                content.result = reason;
                content.notified = true;
            },
            (error) => {
                vm.logger.error('Failed to ignore batch split', error, 'SFTP Reader Error');
            }
        )
    }*/

    selectAllBatchSpitErrors(status: SftpReaderChannelStatus) {
        var vm = this;

        for (var i=0; i<status.completeBatchContents.length; i++) {
            var batchSplit = status.completeBatchContents[i] as SftpReaderBatchContents;
            if (!batchSplit.notified) {
                batchSplit.selected = true;
            }
        }
    }

    ignoreBatchSplits(status: SftpReaderChannelStatus) {
        var vm = this;

        var selected = [];
        for (var i=0; i<status.completeBatchContents.length; i++) {
            var batchSplit = status.completeBatchContents[i];
            if (batchSplit.selected) {
                selected.push(batchSplit);
            }
        }

        if (selected.length == 0) {
            vm.logger.warning("No errors selected");
            return;
        }

        var reason = prompt('Enter reason');
        if (reason == null) {
            return;
        }

        for (var j=0; j<selected.length; j++) {
            var batchSplit = selected[j] as SftpReaderBatchContents;
            vm.ignoreBatchSplit(status, batchSplit, reason);
        }
    }

    ignoreBatchSplit(status: SftpReaderChannelStatus, batchSplit: SftpReaderBatchContents, reason: string) {
        var vm = this;

        vm.sftpReaderService.ignoreBatchSplit(status.latestBatchId, batchSplit.batchSplitId, status.id, reason).subscribe(
            (result) => {
                batchSplit.error = null;
                batchSplit.result = reason;
                batchSplit.notified = true;
            },
            (error) => {
                vm.logger.error('Failed to ignore batch split', error, 'SFTP Reader Error');
            }
        )
    }
}
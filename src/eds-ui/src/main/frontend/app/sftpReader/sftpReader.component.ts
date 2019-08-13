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

@Component({
    template : require('./sftpReader.html')
})
export class SftpReaderComponent {

    //resultStr: string;
    instanceNames: SftpReaderInstance[];
    filterInstanceName: string;
    statuses: SftpReaderChannelStatus[];
    resultStr: string;
    showRawJson: boolean;
    refreshingStatus: boolean;

    constructor(private $modal : NgbModal,
                protected sftpReaderService:SftpReaderService,
                protected logger:LoggerService,
                protected $state:StateService) {


    }

    ngOnInit() {
        var vm = this;
        vm.filterInstanceName = 'active';
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
        console.log('vm.refreshingStatus = ' + vm.refreshingStatus);

        vm.sftpReaderService.getSftpReaderStatus(vm.filterInstanceName).subscribe(
            (result) => {
                vm.refreshingStatus = false;
                console.log('vm.refreshingStatus = ' + vm.refreshingStatus);

                vm.logger.success('Successfully got SFTP Reader status', 'SFTP Reader Status');
                vm.statuses = result;

                vm.resultStr = JSON.stringify(result, null, 2);

                console.log('received SFTP Reader status');
                console.log(result);
            },
            (error) => {
                vm.refreshingStatus = false;
                console.log('vm.refreshingStatus = ' + vm.refreshingStatus);

                vm.logger.error('Failed get SFTP Reader status', error, 'SFTP Reader Status');
            }
        )
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
        var lastPolled = status.latestPollingStart;
        if (!lastPolled) {
            return true;
        }
        var pollingFrequencySec = status.pollFrequencySeconds;
        var pollingFrequencyMs = pollingFrequencySec * 1000;
        pollingFrequencyMs = (pollingFrequencyMs * 1.1); //add 10% to the poling frequency so it's less touchy

        var now = new Date();
        var lastPolledDiffMs = now.getTime() - lastPolled;

        return lastPolledDiffMs > pollingFrequencyMs;
    }

    isLastExtractTooOld(status: SftpReaderChannelStatus): boolean {
        var lastExtract = status.latestBatchReceived;
        if (!lastExtract) {
            return true;
        }

        var dataFrequencyDays = status.dataFrequencyDays;
        var dataFrequencyMs = dataFrequencyDays * 24 * 60 * 60 * 1000;
        dataFrequencyMs = (dataFrequencyMs * 1.1); //add 10% to the poling frequency so it's less touchy

        var now = new Date();
        var lastExtractDiffMs = now.getTime() - lastExtract;

        return lastExtractDiffMs > dataFrequencyMs;
    }


}